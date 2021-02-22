package com.example.wetalkclient.tcpConn;

import android.content.Context;
import android.util.Log;

import com.example.wetalkclient.bean.Message;

public class MsgUtils {
    private static ChatConnThread connThread;

    public static void setConnThread(ChatConnThread thread){
        connThread = thread;
    }

    public static void sendMsg(Message msg){
        connThread.sendMsg(msg);
    }
    public static void setCurrentRoom(String roomId){
        connThread.setRoomId(roomId);
    }
    public static void setContext(Context context){
        connThread.setContext(context);
        Log.d("+++++++++++++++++++",context.toString()+"로 바뀜");
    }
    public static boolean isLogCheckOut(){
        return connThread.isLogCheckOut();
    }

}
