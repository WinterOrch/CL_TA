package com;

public class SocketConstant {
    // 有消息传输机会时有PERCENTAGE_OF_APPLICATION%概率产生转账信息
    public static final int PERCENTAGE_OF_APPLICATION = 40;

    // 每个通信窗口中消息数量的上下限
    public static final int MIN_PER_ACTIVE = 2;
    public static final int MAX_PER_ACTIVE = 8;
    public static final int MIN_SEND_DELAY = 50;

    //  Time to Wait before Client Starts
    public static final int WAITING_TIME_OUT = 7000;

    //  Time to Wait between Communication Windows
    public static final int WINDOW_DELAY = 300;

    public static final int CACHE_INITIALIZED_SIZE = 2000;
    public static final int TRANSFER_MAX_SIZE = 20;

    //  Constants for Chandy-Lamport Protocol
    public static final String MARKER_STRING = "A MARKER THIS IS";

    public static boolean isIPAddressByRegex(String str) {
        String regex = "\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}";
        if (str.matches(regex)) {
            String[] arr = str.split("\\.");
            for (int i = 0; i < 4; i++) {
                int temp = Integer.parseInt(arr[i]);
                if (temp < 0 || temp > 255) return false;
            }
            return true;
        } else return false;
    }
}
