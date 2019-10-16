package com.socket;

import com.socket.msg.ApplicationMsg;
import com.socket.msg.StateMsg;

import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

enum State { SNAPPING,WORKING }

public class Map implements Serializable{
    int id;
    public int numNodes;

    static String outputFileName;

    int numTotalMsgSent = 0;

    boolean isActive = false;

    State state = State.WORKING;

    //  Nodes Map of the Distributed System
    ArrayList<Node> nodesList = new ArrayList<>();
    //
    HashMap<Integer,Socket> channelsMap = new HashMap<>();
    //  Message Buffer Waiting to be Sent While Process is Snapping
    HashMap<Integer,ArrayList<ApplicationMsg>> waitingBuffer = new HashMap<>();
    //  HashMap which Records all Incoming Channels Sending Marker
    HashMap<Integer,Boolean> markerReceived = new HashMap<>();
    //
    StateMsg myState;

}
