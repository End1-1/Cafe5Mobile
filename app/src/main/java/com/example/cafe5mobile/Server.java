package com.example.cafe5mobile;

public class Server {
    public static final int PORT = 3390;
    public static final String LOCAL_MESSAGE_ACTION = "local_message_action";
    public static final String DATA_TYPE = "data_type";
    public static final int BROADCAST_SOCKET_DATA = 1;
    public static final int BROADCAST_SOCKET_ERROR = 2;
    public static final String SOCKET_REPLY = "socket_reply";

    public static final int WHAT_GETSERVER = 1;
    public static final int WHAT_GETDOCS = 2;
    public static final int WHAT_PARSE_STORE_STRING = 3;
    public static final int WHAT_STORE_APPEND_ITEM = 4;
    public static final int WHAT_PARSE_STORE_QTY = 5;
    public static final int WHAT_PARSE_STORE_PRICE = 6;
    public static final int WHAT_PARSE_STORE_AMOUNT = 7;
    public static final int WHAT_SET_ACTIVE_WINDOW = 8;
}
