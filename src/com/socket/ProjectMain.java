package com.socket;

import com.SocketConstant;
import com.service.Cache;
import com.service.ReceiveMessageService;
import com.service.SendMessageService;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.util.Map;
import java.util.Random;

public class ProjectMain {
    private static Cache cache;

    private String nextIP;
    private int nextPort;

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

    public static void sayHello(Socket socket) {
        try {
            Writer writer = new OutputStreamWriter(socket.getOutputStream(), "GBK");
            writer.write("Aloha!");
            writer.write(" eof\n");
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
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
