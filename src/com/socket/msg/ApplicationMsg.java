package com.socket.msg;

import java.io.Serializable;

//  Application Message Consists of a String and a Vector Timestamp
public class ApplicationMsg extends Message implements Serializable {
    private String msg = "Aloha!";

    public int nodeId;
    // int[] vector;

    public ApplicationMsg() {

    }

    public ApplicationMsg(String m) {
        this();
        msg = m;
    }

    public String getMsg() {
        return this.msg;
    }
}
