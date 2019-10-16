package com.socket.msg;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class StateMsg extends Message implements Serializable {
    boolean isActive;
    int nodeId;
    HashMap<Integer,ArrayList<ApplicationMsg>> channelStates;
    int[] vector;

    public StateMsg() {
        super.serialUID = 3;
    }
}
