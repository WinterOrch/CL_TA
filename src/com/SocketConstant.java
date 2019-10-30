package com;

public class SocketConstant {
    public static final int FACE_NUM_OF_DICE = 10;

    // Constants for ProjectMain
    public static final int MIN_PER_ACTIVE = 200;
    public static final int MAX_PER_ACTIVE = 200;
    public static final int MIN_SEND_DELAY = 200;
    public static final int SNAPSHOT_DELAY = 200;

    //  Time to Wait before Client Starts
    public static final int WAITING_TIME_OUT = 7000;

    //  Time to Wait between Communication Windows
    public static final int WINDOW_DELAY = 1000;

    public static final int CACHE_INITIALIZED_SIZE = 2000;

    //  Constants for Chandy-Lamport Protocol
    public static final String MARKER_STRING = "A MARKER THIS IS";
    public static final String TERMINATE_STRING = "HALT";

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
