package com.service;

import com.socket.ProjectMain;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;

public class SendMessageService implements Runnable {
    private Socket socket;

    public SendMessageService(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try{
            boolean isRunning = true;

            //建立连接后就可以往服务端写数据了
            Writer writer = new OutputStreamWriter(socket.getOutputStream(), "GBK");

            while(isRunning){
                int time = 1000 * (int)(1 + Math.random() * 5);
                Thread.sleep(30000);

                writer.write(ProjectMain.output());
                writer.write(" eof\n");
                writer.flush();

                System.out.print("AS:");
                ProjectMain.test();
            }

            writer.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
