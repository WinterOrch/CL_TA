package com.socket.msg;

import com.SocketConstant;

import java.io.Serializable;

public class TerminateMsg extends Message implements Serializable {
    String msg = SocketConstant.TERMINATE_STRING;

    public TerminateMsg() {
        super.serialUID = 4;
    }
}
