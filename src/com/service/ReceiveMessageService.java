package com.service;

import com.socket.Node;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ReceiveMessageService implements Runnable{
    private int port;

    public ReceiveMessageService(int listenPort){
        this.port = listenPort;
    }

    @Override
    public void run() {
        try {
            boolean isRunning = true;

            Map<String,Integer> in;

            ServerSocket server = new ServerSocket(port);
            // 监听客户端是否有消息
            Socket socket = server.accept();

            // 创建输出流
            InputStream is = socket.getInputStream();
            byte[] bytes = new byte[1024];
            int len;

            while ((len = is.read(bytes)) != -1) { // 阻塞
                String s = new String(bytes, 0, len);
                String[] num = s.split(" ");

                in = new HashMap<>();
                in.put("A",Integer.parseInt(num[0]));
                in.put("B",Integer.parseInt(num[1]));
                in.put("C",Integer.parseInt(num[2]));

                Node.receive(in);

                System.out.print("AR:");
                Node.test();
            }

        }catch (IOException e){
            System.err.println("IO error occurs when trying to receive message.");
            System.exit(1);
        }
    }
}
