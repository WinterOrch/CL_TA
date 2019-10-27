package com.socket;

import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;



public class Map implements Serializable{
    public int numNodes;

    // Nodes in this system
    private ArrayList<Node> nodesInSystem = new ArrayList<>();
    //
    HashMap<Integer,Socket> channelsMap = new HashMap<>();

    //  HashMap which Records all Incoming Channels Sending Marker
    HashMap<Integer,Boolean> markerReceived = new HashMap<>();

    public Map() {
        //TODO 拓扑策略
        Node node_1 = new Node(0,"192.168.43.43",8899);
        nodesInSystem.add(node_1);
    }

    public ArrayList<Node> getMap() {
        return nodesInSystem;
    }

}
