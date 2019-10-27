package com.socket.msg;

import com.socket.Map;

import java.io.Serializable;

public class Message implements Serializable{
    Map m = new Map();
    int n = m.numNodes;
}
