package com.gionee.feedback.config;

public final class NetConfig {
    public static final int CONNECTION_TIME_OUT = 20000;
    public static final String LOGIN_URI = "/insight-api/fb/login.do";
    public static final String MARK_READ_URI = "/insight-api/fb/markRead.do";
    public static final String NORMAL_HOST = "http://insapi.gionee.com";
    public static final String QUERY_ALL_URI = "/insight-api/fb/allReplies.do";
    public static final String QUERY_UNREAD_URI = "/insight-api/fb/unreadReplies.do";
    public static final String SEND_URI = "/insight-api/fb/save.do";
    public static final int SOCKET_BUFFER_SIZE = 2048;
    public static final int SO_TIME_OUT = 120000;
    public static final String TEST_HOST = "http://t-insapi.gionee.com";

    public static class NetType {
        public static final String NET_TYPE_2G = "2G";
        public static final String NET_TYPE_3G = "3G";
        public static final String NET_TYPE_4G = "4G";
        public static final String NET_TYPE_UNKNOW = "unknow";
        public static final String NET_TYPE_WAP = "wap";
        public static final String NET_TYPE_WIFI = "wifi";
    }
}
