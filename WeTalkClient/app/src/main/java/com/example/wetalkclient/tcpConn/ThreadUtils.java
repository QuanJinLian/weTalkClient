package com.example.wetalkclient.tcpConn;

import android.os.Handler;
import android.os.HandlerThread;

public class ThreadUtils {


    public static Handler GetMultiHandler(String name) {
        HandlerThread thread = new HandlerThread(name + "_MultiThread");
        thread.start();
        return new Handler(thread.getLooper());
    }
}
