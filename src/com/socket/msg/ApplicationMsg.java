package com.socket.msg;

import java.io.Serializable;

/**
 * 业务消息，用于传输转账信息
 */
public class ApplicationMsg extends Message implements Serializable {
    private String msg = "Aloha!";

    // int[] vector;

    private ApplicationMsg() {

    }

    public ApplicationMsg(String m) {
        this();
        msg = m;
    }

    public String getMsg() {
        return this.msg;
    }
}
