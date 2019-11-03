package com.socket.msg;

import com.SocketConstant;

import java.io.Serializable;

/**
 * Marker消息，可以根据设计需要添加时间戳等
 */
public class MarkerMsg extends Message implements Serializable {
    String msg;


    public MarkerMsg(int nodeId) {
        this.msg = SocketConstant.MARKER_STRING;
        this.nodeId = nodeId;
    }
}
