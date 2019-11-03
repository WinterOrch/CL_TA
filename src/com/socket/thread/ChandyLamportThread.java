package com.socket.thread;

import com.socket.ProjectMain;

/**
 * 快照协议进程，通过调用ChandyLamport类中方法实现快照
 */
public class ChandyLamportThread extends Thread{

    ProjectMain mainObj;

    public ChandyLamportThread(ProjectMain mainObj){
        this.mainObj = mainObj;
    }
    public void run(){
        //TODO 1.1 Chandy-Lamport协议线程
    }
}