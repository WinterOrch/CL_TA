package com.protocol;

import com.socket.ProjectMain;

public class ChandyLamport {

    public static void operateSnapping(ProjectMain projectMain, int neighbour) {
        //TODO 2.1 快照操作
    }

    public static void sendMarkerMessage(ProjectMain projectMain, int channelNo) {
        //TODO 2.0 发送MARKER的策略
        projectMain.sendMarkerMessage(channelNo);
    }

    /**
     * 完成快照操作并发送完MARKER后令状态归位
     */
    public static void returnToNormalState(ProjectMain projectMain) {
        projectMain.processHangedMessage();
        projectMain.setStateWorking();
    }
}
