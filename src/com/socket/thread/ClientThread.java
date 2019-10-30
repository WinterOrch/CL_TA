package com.socket.thread;

import com.socket.ProjectMain;
import com.socket.msg.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

//Server reading objects sent by other clients in the system in a thread
public class ClientThread extends Thread {
    Socket cSocket;
    ProjectMain mainObj;

    public ClientThread(Socket socket, ProjectMain mainObj) {
        this.cSocket = socket;
        this.mainObj = mainObj;
    }

    public void run() {
        ObjectInputStream ois = null;

        try {
            ois = new ObjectInputStream(cSocket.getInputStream());
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        while(true){
            try {
                Message msg;
                msg = (Message) ois.readObject();
                // Synchronizing mainObj so that multiple threads access mainObj in a synchronized way
                synchronized(mainObj){

                    //If message is a marker message then process has to turn red if its blue and send messages along all its
                    //channels
                    if(msg instanceof MarkerMsg){
                        int channelNo = ((MarkerMsg) msg).nodeId;
                        //TODO Chandy-Lamport协议策略
                    }

                    else if(msg instanceof ApplicationMsg){
                        //TODO 接收消息策略
                    }

                    //If message is a state message then if this node id is 0 then process it
                    // otherwise forward it to the parent on converge cast tree towards Node 0
                    else if(msg instanceof StateMsg){
                        //TODO 启动快照的策略
                    }

                }
            }
            catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}