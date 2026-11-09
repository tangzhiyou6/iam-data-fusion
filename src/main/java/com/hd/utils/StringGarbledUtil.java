package com.hd.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 中文乱码检测工具。
 * @author SuBLin
 * @since 2026/7/28
 */
public final class StringGarbledUtil {

    /** Unicode 替换字符（解码失败时常见） */
    private static final char REPLACEMENT_CHAR = '\uFFFD';

    /** 连续问号簇，如 ??? */
    private static final Pattern QUESTION_CLUSTER = Pattern.compile("\\?{2,}");

    /**
     * 典型乱码混入的异常脚本字符：希伯来、西里尔、阿拉伯、泰文等
     * （正常中文业务文件名极少出现）。
     */
    private static final Pattern ABNORMAL_SCRIPT = Pattern.compile(
            "[\\u0590-\\u05FF\\u0400-\\u04FF\\u0600-\\u06FF\\u0E00-\\u0E7F"
                    + "\\u0370-\\u03FF\\u0530-\\u058F]"
    );

    /**
     * Latin-1 补充区连续乱码（GBK/UTF-8 被按 ISO-8859-1 误读后常见）。
     */
    private static final Pattern LATIN1_MOJIBAKE_CLUSTER = Pattern.compile(
            "[\\u00C0-\\u00FF]{2,}"
    );

    /** 默认判定阈值：得分达到即视为乱码 */
    private static final int DEFAULT_THRESHOLD = 40;

    private StringGarbledUtil() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    /**
     * 是否乱码文件名（使用默认阈值）。
     */
    public static boolean isGarbled(String content) {
        return detect(content);
    }

    /**
     * 是否乱码文件名（自定义阈值，0-100，越大越严格）。
     */
    public static boolean isGarbled(String content, int threshold) {
        return detect(content, threshold);
    }

    /**
     * 详细检测，返回得分与命中原因。
     */
    public static Boolean detect(String fileName) {
        return detect(fileName, DEFAULT_THRESHOLD);
    }

    /**
     * 详细检测。
     *
     * @param fileName  文件名或相对路径
     * @param threshold 判定阈值，建议 30-60；默认 40
     */
    public static Boolean detect(String fileName, int threshold) {
        if (fileName == null) {
            return false;
        }
        if (fileName.isEmpty()) {
            return false;
        }

        int score = 0;
        List<String> reasons = new ArrayList<>();

        int questionCount = countChar(fileName, '?');
        int replacementCount = countChar(fileName, REPLACEMENT_CHAR);
        int questionClusters = countMatches(QUESTION_CLUSTER, fileName);
        int abnormalScriptCount = countMatches(ABNORMAL_SCRIPT, fileName);
        int latin1Clusters = countMatches(LATIN1_MOJIBAKE_CLUSTER, fileName);
        int controlCount = countControlChars(fileName);

        int effectiveLen = Math.max(1, stripExtension(fileName).replace("/", "").replace("\\", "").length());
        double questionRatio = (double) questionCount / effectiveLen;

        // 1) Unicode 替换符：强信号
        if (replacementCount > 0) {
            score += Math.min(50, 30 + replacementCount * 10);
            reasons.add("contains Unicode replacement char U+FFFD x" + replacementCount);
        }

        // 2) 连续 ???：强信号（示例主特征）
        if (questionClusters > 0) {
            score += Math.min(45, 25 + questionClusters * 8);
            reasons.add("contains consecutive '?' clusters x" + questionClusters);
        }

        // 3) 问号占比过高
        if (questionRatio >= 0.25d && questionCount >= 3) {
            int add = questionRatio >= 0.4d ? 30 : 20;
            score += add;
            reasons.add(String.format(Locale.ROOT,
                    "high '?' ratio=%.2f (count=%d)", questionRatio, questionCount));
        } else if (questionCount >= 6) {
            score += 15;
            reasons.add("many '?' chars, count=" + questionCount);
        }

        // 4) 异常文字脚本混入（希伯来/西里尔等）
        if (abnormalScriptCount > 0) {
            score += Math.min(35, 15 + abnormalScriptCount * 8);
            reasons.add("abnormal script chars x" + abnormalScriptCount);
        }

        // 5) Latin-1 mojibake 连续簇
        if (latin1Clusters > 0) {
            score += Math.min(25, 10 + latin1Clusters * 8);
            reasons.add("Latin-1 mojibake clusters x" + latin1Clusters);
        }

        // 6) 控制字符
        if (controlCount > 0) {
            score += Math.min(30, 20 + controlCount * 5);
            reasons.add("control chars x" + controlCount);
        }

        // 7) 「可读中文/字母数字」过少，但非 ASCII 杂符偏多
        int cjkCount = countCjk(fileName);
        int weirdNonAscii = countWeirdNonAscii(fileName);
        if (weirdNonAscii >= 2 && cjkCount == 0 && questionCount >= 2) {
            score += 15;
            reasons.add("few readable CJK but many weird non-ASCII chars");
        }

        // 组合加成：问号簇 + 异常脚本，典型跨编码乱码
        if (questionClusters > 0 && abnormalScriptCount > 0) {
            score += 15;
            reasons.add("combined '?' clusters with abnormal scripts");
        }

        score = Math.min(100, score);
        return score >= Math.max(0, threshold);
    }

    /**
     * 批量过滤乱码文件名。
     */
    public static List<String> filterGarbled(Iterable<String> fileNames) {
        Objects.requireNonNull(fileNames, "fileNames must not be null");
        List<String> result = new ArrayList<>();
        for (String name : fileNames) {
            if (isGarbled(name)) {
                result.add(name);
            }
        }
        return result;
    }

    private static String stripExtension(String fileName) {
        String name = fileName;
        int slash = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
        if (slash >= 0 && slash < name.length() - 1) {
            name = name.substring(slash + 1);
        }
        int dot = name.lastIndexOf('.');
        if (dot > 0) {
            return name.substring(0, dot);
        }
        return name;
    }

    private static int countChar(String text, char target) {
        int count = 0;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == target) {
                count++;
            }
        }
        return count;
    }

    private static int countMatches(Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    private static int countControlChars(String text) {
        int count = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c < 0x20 && c != '\t') {
                count++;
            } else if (c == 0x7F) {
                count++;
            }
        }
        return count;
    }

    private static int countCjk(String text) {
        int count = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (isCjk(c)) {
                count++;
            }
        }
        return count;
    }

    private static int countWeirdNonAscii(String text) {
        int count = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c <= 0x7F || isCjk(c) || isCommonPunctuation(c)) {
                continue;
            }
            // 全角标点、常见兼容字符不算 weird
            if (c >= 0xFF00 && c <= 0xFFEF) {
                continue;
            }
            count++;
        }
        return count;
    }

    private static boolean isCjk(char c) {
        Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
        return block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || block == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || block == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || block == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS;
    }

    private static boolean isCommonPunctuation(char c) {
        return c == '_' || c == '-' || c == '.' || c == '(' || c == ')'
                || c == '[' || c == ']' || c == ' ' || c == '、' || c == '，'
                || c == '（' || c == '）' || c == '【' || c == '】';
    }
}
