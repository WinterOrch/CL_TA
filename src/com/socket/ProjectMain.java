package com.socket;

import com.SocketConstant;
import com.service.Cache;
import com.service.ReceiveMessageService;
import com.service.SendMessageService;
import com.socket.msg.ApplicationMsg;
import com.socket.msg.Message;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

enum State { SNAPPING,WORKING }

public class ProjectMain {
    private static Cache cache;

    private String nextIP;
    private int nextPort;

    private int currentNode;
    private int[] neighbors;

    private State currentState = State.WORKING;
    private Boolean active = false;

    ArrayList<Node> nodesInSystem = new ArrayList<>();
    HashMap<Integer,Node> nodesMapInSystem = new HashMap<>();
    HashMap<Integer,Socket> channels = new HashMap<>();

    private HashMap<Integer,ObjectOutputStream> outputStreamHashMap = new HashMap<>();

    // Message Buffer Waiting to be Sent While Process is Snapping
    private HashMap<Integer,ConcurrentLinkedQueue<ApplicationMsg>> waitingBuffer = new HashMap<>();

    private ProjectMain() {

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

    public boolean rollDice(int n) {
        Random r = new Random();
        int temp = r.nextInt(SocketConstant.FACE_NUM_OF_DICE) + 1;

        return temp <= n;
    }

    public static String output() {
        Map<String,Integer> temp = cache.transfer();
        return temp.get("A").toString() + " " + temp.get("B").toString()
                + " " + temp.get("C").toString();
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
                            m.nodeId = this.currentNode;

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
                            m.nodeId = this.currentNode;

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
                            m.nodeId = this.currentNode;

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

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ApplicationMsg getApplicationMsg() {
        //TODO 生成业务信息并放入ApplicationMsg，参考SendMessageService

        return new ApplicationMsg();
    }

    private Boolean judge() {
        synchronized (this) {
            //TODO 通过随机判断是否需要产生新的ApplicationMsg
        }
        return this.active;
    }

    public static void main(String[] args) {
        //TODO 线程调用策略
        ProjectMain me = new ProjectMain();
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
    }
}
