package com.hd.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArchiveExtractorVo {

    /**
     * 文件二进制内容
     */
    private byte[] content;

    /**
     * 文件名（不含路径）
     */
    private String fileName;

    /**
     * 文件在压缩包内的完整路径（含目录层级）
     */
    private String path;

    /**
     * 是否为嵌套压缩包（递归提取后仍失败的归档，content 保留原始二进制）
     */
    @Builder.Default
    private boolean archive = false;

    /**
     * 提取失败时的错误码
     * <ul>
     *   <li>RAR_V5_UNSUPPORTED — RAR v5 格式，junrar 暂不支持</li>
     *   <li>ZIP_MALFORMED      — ZIP 内容损坏或编码非法</li>
     *   <li>ARCHIVE_CORRUPT    — 归档内部数据损坏</li>
     *   <li>RECURSION_EXCEEDED — 嵌套层级超限（最大 8 层）</li>
     * </ul>
     */
    private String extractError;

    /**
     * 快速判断是否提取成功
     */
    public boolean isExtractFailed() {
        return extractError != null;
    }

    @Override
    public String toString() {
        return "\nArchiveExtractorVo{" +
                "path='" + path + '\'' +
                '}';
    }
}
