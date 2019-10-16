package com.socket;

import java.io.Serializable;

public class Map implements Serializable{
    int id;
    public int numNodes;

    static String outputFileName;

    int numTotalMsgSent = 0;

    boolean isActive = false;

}
