package com.gionee.framework.utils;

public class StringUtils {
    public static final String ENCODING_UTF8 = "utf-8";

    private StringUtils() {
    }

    public static boolean isNull(String content) {
        return content == null || "".equals(content);
    }

    public static boolean isNotNull(String content) {
        return (content == null || "".equals(content)) ? false : true;
    }

    public static boolean isBlank(String content) {
        if (content == null) {
            return true;
        }
        int strLen = content.length();
        if (strLen == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(content.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isNotBlank(String content) {
        return !isBlank(content);
    }

    public static String trim(String content) {
        if (content != null) {
            return content.trim();
        }
        return content;
    }

    public static String toUpperCaseFirstChar(String content) {
        if (isNull(content)) {
            return content;
        }
        return Character.toUpperCase(content.charAt(0)) + content.substring(1, content.length());
    }

    public static String toLowerCaseFirstChar(String content) {
        if (isNull(content)) {
            return content;
        }
        return Character.toLowerCase(content.charAt(0)) + content.substring(1, content.length());
    }
}
