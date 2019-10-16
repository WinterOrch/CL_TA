package com.socket.msg;

import java.io.Serializable;

//  Application Message Consists of a String and a Vector Timestamp
public class ApplicationMsg extends Message implements Serializable {
    String msg = "Aloha!";

    int nodeId;
    int[] vector;

    public ApplicationMsg() {
        super.serialUID = 1;
    }

    public ApplicationMsg(String m) {
        this();
        msg = m;
    }
}
