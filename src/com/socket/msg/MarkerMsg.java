package com.socket.msg;

import com.SocketConstant;

import java.io.Serializable;

public class MarkerMsg extends Message implements Serializable {
    String msg;

    int nodeId;

    public MarkerMsg() {
        super.serialUID = 2;
        this.msg = SocketConstant.MARKER_STRING;
    }
}
