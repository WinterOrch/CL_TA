package com.socket.msg;

import com.SocketConstant;

import java.io.Serializable;

public class MarkerMsg extends Message implements Serializable {
    String msg;

    public int nodeId;

    public MarkerMsg() {
        this.msg = SocketConstant.MARKER_STRING;
    }
}
