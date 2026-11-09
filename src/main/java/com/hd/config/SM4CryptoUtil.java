package com.hd.config;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Security;
import java.util.Base64;

import javax.annotation.PostConstruct;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * SM4 国密对称加密/解密工具类
 * <p>
 * 使用 SM4/CBC/PKCS5Padding 模式，秘钥和IV从 application.yml 中读取。
 * SM4 是对称算法，加密和解密使用同一个秘钥。
 * </p>
 *
 * @author auto
 */
@Component
public class SM4CryptoUtil {

    private static final Logger log = LoggerFactory.getLogger(SM4CryptoUtil.class);

    private static final String ALGORITHM = "SM4";
    private static final String TRANSFORMATION = "SM4/CBC/PKCS5Padding";

    /** 流式读写缓冲区大小 8KB */
    private static final int BUFFER_SIZE = 8192;

    @Value("${sk-sm4.key}")
    private String keyBase64;

    @Value("${sk-sm4.iv}")
    private String ivBase64;

    private byte[] keyBytes;
    private byte[] ivBytes;

    @PostConstruct
    public void init() {
        // 注册 BouncyCastle 安全提供者
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }

        this.keyBytes = Base64.getDecoder().decode(keyBase64);
        this.ivBytes = Base64.getDecoder().decode(ivBase64);

        if (keyBytes.length != 16) {
            throw new IllegalArgumentException("SM4 秘钥必须为 16 字节（128位），当前长度: " + keyBytes.length);
        }
        if (ivBytes.length != 16) {
            throw new IllegalArgumentException("SM4 IV 必须为 16 字节，当前长度: " + ivBytes.length);
        }

        log.info("SM4 加密工具初始化完成，模式: CBC/PKCS5Padding");
    }

    // ==================== 字节数组操作 ====================

    /**
     * SM4 加密字节数组
     *
     * @param data 明文数据
     * @return 密文
     */
    public byte[] encrypt(byte[] data) {
        try {
            Cipher cipher = createCipher(Cipher.ENCRYPT_MODE);
            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new RuntimeException("SM4 加密失败", e);
        }
    }

    /**
     * SM4 解密字节数组
     *
     * @param data 密文数据
     * @return 明文
     */
    public byte[] decrypt(byte[] data) {
        try {
            Cipher cipher = createCipher(Cipher.DECRYPT_MODE);
            return cipher.doFinal(data);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } catch (Exception e) {
            log.error("{} - {}","SM4 流解密失败", e.getMessage(), e);
        }
        return null;
    }

    // ==================== 文件操作 ====================

    /**
     * 加密文件（流式读写，支持大文件）
     *
     * @param inputPath  源文件路径
     * @param outputPath 加密后输出文件路径
     * @throws IOException 文件读写异常
     */
    public void encryptFile(String inputPath, String outputPath) throws IOException {
        try (InputStream in = new BufferedInputStream(new FileInputStream(inputPath));
             OutputStream out = new BufferedOutputStream(new FileOutputStream(outputPath))) {
            encryptStream(in, out);
        }
        log.debug("文件加密完成: {} -> {}", inputPath, outputPath);
    }

    /**
     * 解密文件（流式读写，支持大文件）
     *
     * @param inputPath  加密文件路径
     * @param outputPath 解密后输出文件路径
     * @throws IOException 文件读写异常
     */
    public void decryptFile(String inputPath, String outputPath) throws IOException {
        try (InputStream in = new BufferedInputStream(new FileInputStream(inputPath));
             OutputStream out = new BufferedOutputStream(new FileOutputStream(outputPath))) {
            decryptStream(in, out);
        }
        log.debug("文件解密完成: {} -> {}", inputPath, outputPath);
    }

    // ==================== 流操作 ====================

    /**
     * 加密流：从 InputStream 读取明文，加密后写入 OutputStream。
     * <p>使用 CipherOutputStream 处理分块填充对齐问题。</p>
     *
     * @param in  明文输入流
     * @param out 密文输出流
     * @throws IOException 读写异常
     */
    public void encryptStream(InputStream in, OutputStream out) throws IOException {
        try {
            Cipher cipher = createCipher(Cipher.ENCRYPT_MODE);
            try (CipherOutputStream cipherOut = new CipherOutputStream(out, cipher)) {
                byte[] buffer = new byte[BUFFER_SIZE];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    cipherOut.write(buffer, 0, bytesRead);
                }
            }
            out.flush();
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("SM4 流加密失败", e);
        }
    }

    /**
     * 解密流：从 InputStream 读取密文，解密后写入 OutputStream。
     * <p>使用 CipherInputStream 处理分块填充对齐问题。</p>
     *
     * @param in  密文输入流
     * @param out 明文输出流
     * @throws IOException 读写异常
     */
    public void decryptStream(InputStream in, OutputStream out) throws IOException {
        try {
            Cipher cipher = createCipher(Cipher.DECRYPT_MODE);
            try (CipherInputStream cipherIn = new CipherInputStream(in, cipher)) {
                byte[] buffer = new byte[BUFFER_SIZE];
                int bytesRead;
                while ((bytesRead = cipherIn.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
            out.flush();
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("SM4 流解密失败", e);
        }
    }

    // ==================== 内部方法 ====================

    /**
     * 创建 SM4 Cipher 实例
     *
     * @param mode Cipher.ENCRYPT_MODE 或 Cipher.DECRYPT_MODE
     * @return 配置好的 Cipher 对象
     */
    private Cipher createCipher(int mode) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION, BouncyCastleProvider.PROVIDER_NAME);
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, ALGORITHM);
        IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
        cipher.init(mode, keySpec, ivSpec);
        return cipher;
    }

}
