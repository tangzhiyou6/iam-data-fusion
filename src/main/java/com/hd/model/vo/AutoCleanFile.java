package com.hd.model.vo;

import java.io.IOException;
import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>当实例被垃圾回收时，自动删除关联的磁盘文件。
 * 也可通过 {@link #close()} 手动提前删除。
 *
 * <p>适用于：
 * <ul>
 *   <li>大文件下载的临时文件</li>
 *   <li>HTTP 接口返回的临时文件</li>
 *   <li>测试生成的临时资源</li>
 * </ul>
 *
 * <p><b>注意：</b>不要缓存此类实例，否则文件不会被清理。
 */
public final class AutoCleanFile implements AutoCloseable {

    private static final Logger LOGGER = Logger.getLogger(AutoCleanFile.class.getName());

    /** 全局引用队列（所有实例共享） */
    private static final ReferenceQueue<AutoCleanFile> REFERENCE_QUEUE =
            new ReferenceQueue<>();

    /** 后台清理线程 */
    private static final Thread CLEANER_THREAD;

    static {
        CLEANER_THREAD = new Thread(new CleanerTask(), "auto-clean-file-thread");
        CLEANER_THREAD.setDaemon(true);
        CLEANER_THREAD.start();
    }

    /** 文件路径 */
    private final Path filePath;

    /** 是否已清理 */
    private final AtomicBoolean cleaned = new AtomicBoolean(false);

    /** 虚引用（GC 后触发清理） */
    private final FileCleanupReference cleanupReference;

    /* ========================= 构造方法 ========================= */

    /**
     * 包装已有文件
     */
    public AutoCleanFile(Path path) {
        this.filePath = Objects.requireNonNull(path, "filePath must not be null");
        ensureFileExists();
        this.cleanupReference = new FileCleanupReference(this, path, REFERENCE_QUEUE);
    }

    /**
     * 通过字符串路径创建
     */
    public AutoCleanFile(String filePath) {
        this(Paths.get(filePath));
    }

    /**
     * 创建临时文件（推荐）
     */
    public static AutoCleanFile createTempFile(String prefix, String suffix) throws IOException {
        Path tempFile = Files.createTempFile(prefix, suffix);
        return new AutoCleanFile(tempFile);
    }

    /**
     * 在指定目录创建临时文件
     */
    public static AutoCleanFile createTempFile(Path dir, String prefix, String suffix)
            throws IOException {
        Path tempFile = Files.createTempFile(dir, prefix, suffix);
        return new AutoCleanFile(tempFile);
    }

    /* ========================= 公共方法 ========================= */

    /**
     * 获取文件路径
     */
    public Path getPath() {
        return filePath;
    }

    /**
     * 文件是否存在
     */
    public boolean exists() {
        return Files.exists(filePath);
    }

    /**
     * 获取文件大小
     */
    public long size() throws IOException {
        return Files.size(filePath);
    }

    /**
     * 手动清理（推荐在 finally / try-with-resources 中使用）
     */
    @Override
    public void close() {
        if (cleaned.compareAndSet(false, true)) {
            deleteFile();
            cleanupReference.clear(); // 取消引用，避免重复清理
        }
    }

    /**
     * 是否已清理
     */
    public boolean isClosed() {
        return cleaned.get();
    }

    /* ========================= 私有方法 ========================= */

    private void ensureFileExists() {
        if (!Files.exists(filePath)) {
            try {
                Files.createDirectories(filePath.getParent());
                Files.createFile(filePath);
            } catch (IOException e) {
                throw new RuntimeException("Failed to create file: " + filePath, e);
            }
        }
    }

    private void deleteFile() {
        try {
            boolean deleted = Files.deleteIfExists(filePath);
            if (deleted) {
                LOGGER.fine("Deleted file: " + filePath);
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to delete file: " + filePath, e);
        }
    }

    /* ========================= 内部类 ========================= */

    /**
     * 虚引用包装类（不阻止 GC）
     */
    private static final class FileCleanupReference
            extends PhantomReference<AutoCleanFile> {

        private final Path pathToDelete;

        FileCleanupReference(AutoCleanFile referent,
                             Path path,
                             ReferenceQueue<? super AutoCleanFile> queue) {
            super(referent, queue);
            this.pathToDelete = path;
        }

        void cleanup() {
            try {
                Files.deleteIfExists(pathToDelete);
                LOGGER.fine("Auto-deleted file via GC: " + pathToDelete);
            } catch (IOException e) {
                LOGGER.log(Level.WARNING,
                        "Failed to auto-delete file via GC: " + pathToDelete, e);
            }
        }
    }

    /**
     * 后台清理任务（守护线程）
     */
    private static final class CleanerTask implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    Reference<?> ref = REFERENCE_QUEUE.remove();
                    if (ref instanceof FileCleanupReference) {
                        ((FileCleanupReference) ref).cleanup();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Throwable t) {
                    LOGGER.log(Level.SEVERE, "Unexpected error in cleaner thread", t);
                }
            }
        }
    }
}