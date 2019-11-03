package com.socket;

import java.io.Serializable;
import java.util.ArrayList;

public class Map implements Serializable{
    private int numOfNodes;

    // 需要传递给ProjectMain的参数
    private ArrayList<Node> nodesInSystem = new ArrayList<>();
    private int currentNode;
    private int[][] adjMatrix;

    Map() {
        //TODO 3.0 拓扑策略
        this.numOfNodes = 3;

        Node node_1 = new Node(0,"192.168.43.43",8899);
        this.nodesInSystem.add(node_1);
    }

    /**
     * 传递拓扑，生成ProjectMain实例
     */
    public ProjectMain getMain() {
        ProjectMain main = new ProjectMain();

        main.nodesInSystem = this.nodesInSystem;
        main.currentNode = currentNode;
        main.adjMatrix = new int[numOfNodes][numOfNodes];

        for(int i = 0; i < numOfNodes; i++){
            System.arraycopy(this.adjMatrix[i], 0, main.adjMatrix[i], 0, numOfNodes);
        }

        return main;
    }

}
