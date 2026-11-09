package com.hd.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class DateUtils {
    public static final String STANDARD_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final String YYYYMMDDHHMMSS_FORMAT_PATTERN = "yyyyMMddHHmmss";

    public static  String getStandardDateTime(Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(STANDARD_FORMAT_PATTERN);
        return simpleDateFormat.format(Objects.isNull(date) ? new Date() : date);
    }

    public static  String getFormatDateTime(Date date, String pattern) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        return simpleDateFormat.format(Objects.isNull(date) ? new Date() : date);
    }

    public static String formatHMS(long etaSec) {
        if (etaSec < 0) {
            etaSec = Math.abs(etaSec);
        }
        long hours = etaSec / 3600;
        long minutes = (etaSec % 3600) / 60;
        long seconds = etaSec % 60;

        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%d:%02d", minutes, seconds);
        } else {
            return String.format("%d", seconds);
        }
    }
}
