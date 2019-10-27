package com.socket.thread;

import com.SocketConstant;
import com.socket.ProjectMain;

class EmitMessageThread extends Thread{

    private ProjectMain mainObj;

    public EmitMessageThread(ProjectMain mainObj){
        this.mainObj = mainObj;
    }
    public void run(){
        boolean isRunning = true;   // TODO 窗口开放策略

        try {
            while(isRunning) {
                Thread.sleep(SocketConstant.WINDOW_DELAY);

                mainObj.emitMessages();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}