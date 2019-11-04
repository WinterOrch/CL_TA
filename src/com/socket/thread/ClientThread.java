package com.socket.thread;

import com.socket.ProjectMain;
import com.socket.msg.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.Objects;

/**
 * 接收套接字并根据收到对象类型进行处理
 */
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
                msg = (Message) Objects.requireNonNull(ois).readObject();
                // Synchronizing mainObj so that multiple threads access mainObj in a synchronized way
                synchronized(mainObj){

                    //If message is a marker message then process has to turn red if its blue and send messages along all its
                    //channels
                    if(msg instanceof MarkerMsg){
                        mainObj.setStateSnapping();
                        //  接收到来自channelNo节点的Marker
                        int channelNo = ((MarkerMsg) msg).nodeId;
                        //TODO 1.0 Chandy-Lamport协议策略
                    }

                    else if(msg instanceof ApplicationMsg){
                        if(mainObj.isSnapping()) {
                            mainObj.hangMessage((ApplicationMsg)msg);
                        }else {
                            mainObj.processApplicationMessage((ApplicationMsg)msg);
                        }
                    }
                }
            }
            catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}