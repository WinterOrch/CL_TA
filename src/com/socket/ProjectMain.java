package com.socket;

import com.SocketConstant;
import com.service.Cache;
import com.socket.msg.ApplicationMsg;
import com.socket.msg.MarkerMsg;
import com.socket.msg.Message;
import com.socket.thread.*;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

enum State { SNAPPING,WORKING }

public class ProjectMain {
    private Cache cache;

    private int currentId;
    private int numOfNodes;
    private int[] neighbors;

    private HashMap<Integer,Node> nodesMapInSystem = new HashMap<>();
    private HashMap<Integer,Socket> channels = new HashMap<>();

    private State currentState = State.WORKING;
    private Boolean active = false;

    //  Map Protocol's Concern
    int currentNode;
    ArrayList<Node> nodesInSystem = new ArrayList<>();
    int[][] adjMatrix;

    private HashMap<Integer,ObjectOutputStream> outputStreamHashMap = new HashMap<>();

    // Message Buffer Waiting to be Sent While Process is Snapping
    private HashMap<Integer,ConcurrentLinkedQueue<ApplicationMsg>> waitingBuffer = new HashMap<>();

    // Message Buffer Waiting to be Received While Process is Snapping
    private ConcurrentLinkedQueue<ApplicationMsg> hangingBuffer = new ConcurrentLinkedQueue<>();

    ProjectMain() {
        this.initialize();
    }

    private void initialize() {
        //  Initialize Cache
        cache = new Cache();

        this.active = true;
    }

    /**
     * 生成新的转账信息并生成String
     */
    public String map2Token() {
        Map<String,Integer> temp = cache.transfer();
        return temp.get("A").toString() + " " + temp.get("B").toString()
                + " " + temp.get("C").toString();
    }

    /**
     * 将String形式的转账信息转换为Map形式
     */
    private static Map<String, Integer> token2Map(String s) {
        Map<String, Integer> in;
        String[] num = s.split(" ");

        in = new HashMap<>();
        in.put("A",Integer.parseInt(num[0]));
        in.put("B",Integer.parseInt(num[1]));
        in.put("C",Integer.parseInt(num[2]));

        return in;
    }

    public void receive(Map<String,Integer> in) {
        cache.receive(in);
    }

    public void test() {
        cache.print();
    }

    // Function to generate random number in a given range
    private int getRandomNumber(int min, int max){
       if(min == max) {
           return min;
       }else {
           Random rand = new Random();

           return rand.nextInt((max - min) + 1) + min;
       }
    }

    public void emitMessages() {
       // get a random number between minPerActive to maxPerActive to emit that many messages
        int numMsgs;

        synchronized(this){
            numMsgs = this.getRandomNumber(SocketConstant.MIN_PER_ACTIVE,SocketConstant.MAX_PER_ACTIVE);
        }

        for(int i = 0; i < numMsgs; i++){
            if(this.currentState == State.WORKING) {
                if(this.waitingBuffer.isEmpty()) {
                    synchronized(this){
                        int neighborIndex = this.getRandomNumber(0, this.neighbors.length-1);
                        int curNeighbor = this.neighbors[neighborIndex];

                        if(judge()){
                            //send application message
                            ApplicationMsg m = getApplicationMsg();
                            m.nodeId = this.currentId;

                            // Write the message in the channel connecting to neighbor
                            emitSingleMessage(curNeighbor,m);
                        }
                    }
                    // Wait for minimum sending delay before sending another message
                    try {
                        Thread.sleep(SocketConstant.MIN_SEND_DELAY);
                    } catch (InterruptedException e) {
                        System.out.println("Error in EmitMessages");
                    }
                }else {
                    synchronized (this){
                        int j;
                        int curNeighbor = 0;

                        for(j = 0; j < this.neighbors.length-1; j++) {
                            if(waitingBuffer.containsKey(j)) {
                                curNeighbor = this.neighbors[j];
                                break;
                            }
                        }

                        if(judge()) {
                            ConcurrentLinkedQueue<ApplicationMsg> msgBuffer = waitingBuffer.get(j);
                            ApplicationMsg m = msgBuffer.poll();
                            m.nodeId = this.currentId;

                            // Write the message in the channel connecting to neighbor
                            emitSingleMessage(curNeighbor,m);

                            if(msgBuffer.isEmpty())
                                waitingBuffer.remove(j);
                            else
                                waitingBuffer.replace(j,msgBuffer);
                        }
                    }
                    // Wait for minimum sending delay before sending another message
                    try {
                        Thread.sleep(SocketConstant.MIN_SEND_DELAY);
                    } catch (InterruptedException e) {
                        System.out.println("Error in EmitMessages");
                    }
                }
            } else if(currentState == State.SNAPPING){
                synchronized (this){
                    if(judge()) {
                        int neighborIndex = this.getRandomNumber(0, this.neighbors.length-1);
                        int curNeighbor = this.neighbors[neighborIndex];

                        if(judge()){
                            //send application message
                            ApplicationMsg m = getApplicationMsg();
                            m.nodeId = this.currentId;

                            // Write the message in the waitingBuffer
                            if(waitingBuffer.containsKey(curNeighbor)) {
                                ConcurrentLinkedQueue<ApplicationMsg> q = waitingBuffer.get(curNeighbor);
                                q.add(m);

                                waitingBuffer.replace(curNeighbor,q);
                            }else {
                                ConcurrentLinkedQueue<ApplicationMsg> q = new ConcurrentLinkedQueue<>();
                                q.add(m);

                                waitingBuffer.put(curNeighbor,q);
                            }
                        }
                    }
                }
                // Wait for minimum sending delay before sending another message
                try {
                    Thread.sleep(SocketConstant.MIN_SEND_DELAY);
                } catch (InterruptedException e) {
                    System.out.println("Error in EmitMessages");
                }
            }
        }
    }

    /**
     * 发送Marker给ID为neighbour的邻节点
     */
    public void sendMarkerMessage(int neighbour) {
        MarkerMsg m = new MarkerMsg(this.currentId);
        this.emitSingleMessage(neighbour,m);
    }

    /**
     * 用于供其它线程查看状态
     */
    public Boolean isSnapping() {
        return this.currentState == State.SNAPPING;
    }

    /**
     * 发送信息m给ID为neighbour的邻节点
     */
    private void emitSingleMessage(int curNeighbor, Message m) {
        try {
            ObjectOutputStream oos = this.outputStreamHashMap.get(curNeighbor);
            oos.writeObject(m);
            oos.flush();

            if(m instanceof ApplicationMsg) {
                this.cache.transfer(ProjectMain.token2Map(((ApplicationMsg)m).getMsg()));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 快照期间（收到Marker后）收到的转账信息暂时不处理
     */
    public void hangMessage(ApplicationMsg m) {
        this.hangingBuffer.add(m);
    }

    /**
     * 快照结束后处理所有信息
     */
    public void processHangedMessage() {
        if(!this.hangingBuffer.isEmpty()) {
            for(int i = 0; i < this.hangingBuffer.size(); i++)
                this.processApplicationMessage(this.hangingBuffer.poll());
        }
    }

    /**
     * 处理单条信息
     */
    public void processApplicationMessage(ApplicationMsg m) {
        this.receive(ProjectMain.token2Map(m.getMsg()));
    }

    public void setStateWorking() {
        this.currentState = State.WORKING;
    }

    public void setStateSnapping() {
        this.currentState = State.SNAPPING;
    }

    private ApplicationMsg getApplicationMsg() {
        return new ApplicationMsg(map2Token());
    }

    /**
     * 产生转账信息频率，可通过更改常量改变
     */
    private Boolean judge() {
        synchronized (this) {
            int randomNumber=(int)(Math.random()*100)+1;
            this.active = randomNumber < 40;

        }
        return this.active;
    }

    public static void main(String[] args) throws IOException, InterruptedException{
        //  从Map获得拓扑
        ProjectMain mainObj = new com.socket.Map().getMain();
        int curNode = mainObj.currentNode;
        int serverPort = mainObj.nodesInSystem.get(mainObj.currentNode).port;
        mainObj.numOfNodes = mainObj.nodesInSystem.size();

        mainObj.currentId = mainObj.nodesInSystem.get(mainObj.currentNode).nodeId;

        for(int i = 0; i < mainObj.nodesInSystem.size(); i++){
            mainObj.nodesMapInSystem.put(mainObj.nodesInSystem.get(i).nodeId, mainObj.nodesInSystem.get(i));
        }

        //  Start Server on Current Node
        ServerSocket listener = new ServerSocket(serverPort);
        Thread.sleep(SocketConstant.WAITING_TIME_OUT);

        //  Open Channels
        for(int i = 0; i < mainObj.numOfNodes; i++) {
            if(0 != mainObj.adjMatrix[curNode][i]) {
                String hostName = mainObj.nodesInSystem.get(i).host;
                int port = mainObj.nodesInSystem.get(i).port;

                Socket client;

                if(SocketConstant.isIPAddressByRegex(hostName)) {
                    InetAddress address = InetAddress.getByName(hostName);
                    client = new Socket(address, port);
                }else {
                    client = new Socket(hostName, port);
                }

                mainObj.channels.put(i,client);

                ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream());
                mainObj.outputStreamHashMap.put(i,oos);
            }

            //  Populate Neighbors Array
            int index = 0;
            Set<Integer> keys = mainObj.channels.keySet();
            mainObj.neighbors = new int[keys.size()];
            for(Integer element : keys) mainObj.neighbors[index++] = element;
        }

        mainObj.active = true;

        // This node listens as a Server for the clients requests
        Socket socket = listener.accept();
        // For every client request start a new thread
        new ClientThread(socket, mainObj).start();
        new EmitMessageThread(mainObj).start();

        // TODO 4.1 0号节点触发快照协议
    }
}
