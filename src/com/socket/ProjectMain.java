package com.socket;

import com.SocketConstant;
import com.service.Cache;
import com.socket.msg.ApplicationMsg;
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
    private static Cache cache;

    private String nextIP;
    private int nextPort;

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

    ProjectMain() {
        this.initialize();
    }

    private void initialize() {
        //  Initialize Cache
        cache = new Cache();

        /*
        //  Read Address List from Config.txt
        FileReader fr;
        StreamTokenizer st = null;

        try{
            fr = new FileReader("./config.txt" );
            st = new StreamTokenizer( fr );
        }catch( FileNotFoundException e ){
            System.err.println("File not found.");
            System.exit(1);
        }

        try{
            st.nextToken();
            int num = (int)st.nval;
            System.out.println(num);
            this.addressList = new String[num];
            this.portList = new int[num];

            for(int i = 0; i < num; i++) {
                st.nextToken();
                if( (int)st.nval == i ){
                    st.nextToken();
                    addressList[i] = st.sval;
                    System.out.println(addressList[i]);
                }
                st.nextToken();
                portList[i] = (int)st.nval;
                System.out.println(portList[i]);
            }

            st.nextToken();
            this.numNode = (int)st.nval;

            System.out.println(numNode);

            if( numNode >= num || numNode < 0 ) {
                System.err.println("Wrong number for this node..");
                System.exit(1);
            }else {
                this.nextIP = addressList[(numNode + 1) % num];
                this.nextPort = portList[(numNode + 1) % num];
            }

        } catch (IOException e) {
            System.err.println("Cannot read the config file.");
            System.exit(1);
        } catch( NoSuchElementException e ) {
            System.err.println("There are mistakes in the config file.");
            System.exit(1);
        }*/

        this.nextIP = "192.168.43.43";
        this.nextPort = 8899;

        this.active = true;
    }

    public static String map2Token() {
        Map<String,Integer> temp = cache.transfer();
        return temp.get("A").toString() + " " + temp.get("B").toString()
                + " " + temp.get("C").toString();
    }

    private static Map<String, Integer> token2Map(String s) {
        Map<String, Integer> in;
        String[] num = s.split(" ");

        in = new HashMap<>();
        in.put("A",Integer.parseInt(num[0]));
        in.put("B",Integer.parseInt(num[1]));
        in.put("C",Integer.parseInt(num[2]));

        return in;
    }

    public static void receive(Map<String,Integer> in) {
        cache.receive(in);
    }

    public static void test() {
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

    private void emitSingleMessage(int curNeighbor, Message m) {
        try {
            ObjectOutputStream oos = this.outputStreamHashMap.get(curNeighbor);
            oos.writeObject(m);
            oos.flush();

            if(m instanceof ApplicationMsg) {
                ProjectMain.cache.transfer(ProjectMain.token2Map(((ApplicationMsg)m).getMsg()));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ApplicationMsg getApplicationMsg() {
        //TODO 2.1  生成业务信息并放入ApplicationMsg，参考SendMessageService

        return new ApplicationMsg();
    }

    private Boolean judge() {
        synchronized (this) {
            //TODO 2.2  通过随机判断是否需要产生新的ApplicationMsg
        }
        return this.active;
    }

    public static void main(String[] args) throws IOException, InterruptedException{
        ProjectMain mainObj = com.socket.Map.getMain();
        int curNode = mainObj.currentNode;
        int serverPort = mainObj.nodesInSystem.get(mainObj.currentNode).port;
        mainObj.numOfNodes = mainObj.nodesInSystem.size();

        mainObj.currentId = mainObj.nodesInSystem.get(mainObj.currentNode).nodeId;

        for(int i = 0; i < mainObj.nodesInSystem.size(); i++){
            mainObj.nodesMapInSystem.put(mainObj.nodesMapInSystem.get(i).nodeId, mainObj.nodesInSystem.get(i));
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

        //Initially node 0 is active therefore if this node is 0 then it should be active
        if(mainObj.currentId == 0){
            ////System.out.println("Emitted Messages");
            //Call Chandy Lamport protocol if it is node 0
            new ChandyLamportThread(mainObj).start();

        }

        /*
         * 原来的主函数
         *
         * ProjectMain me = new ProjectMain();
        me.initialize();

        try {
            new Thread(new ReceiveMessageService(me.nextPort)).start();

            Thread.sleep(SocketConstant.WAITING_TIME_OUT);

            // ProjectMain.sayHello(socket);
            Socket socket = new Socket(me.nextIP, me.nextPort);

            new Thread(new SendMessageService(socket)).start();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
         *
         * **/

    }
}
