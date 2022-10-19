package com.example.wetalkclient.tcpConn;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.example.wetalkclient.MainActivity;
import com.example.wetalkclient.R;
import com.example.wetalkclient.activities.ChattingRoom;
import com.example.wetalkclient.activities.NewFriend;
import com.example.wetalkclient.bean.AddFriend;
import com.example.wetalkclient.bean.ChattingMsg;
import com.example.wetalkclient.bean.Message;
import com.example.wetalkclient.constant.Constants;
import com.example.wetalkclient.constant.Signals;
import com.example.wetalkclient.db.CacheUtils;
import com.example.wetalkclient.db.DatabaseHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatConnThread extends Thread {
    private static final String TAG = ChatConnThread.class.getSimpleName();
    private Context context;
    private Socket socket;
    private String delim1 = "/@";
    private String delim2 = "/&-";
    private String delim3 = "/#";
    private String delim4 = "/~=";
    private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private Message message;
    private AddFriend friendRequest = new AddFriend();

    private SQLiteDatabase database ;
    private DatabaseHelper dbHelper;

    private String currentroomId;
    private String roomId;
    private String toId;
    private String sql;
    private boolean isLogout;
    private String checkInMsg;
    private ArrayList<String> roomMembers;
    private String photoName;
    private String nikName;
    private String userId;


    // 입출력 장치 서버랑 동일 해야 함
    private BufferedReader i;
    private PrintWriter o ;

    private volatile boolean flag = true;

    // 생성자
    public ChatConnThread(Context context){
        this.context = context;
        iniView();
        SharedPreferences share = context.getSharedPreferences("Login", Context.MODE_PRIVATE);
        photoName = share.getString("photo","");
        nikName = share.getString("name","");
        userId = share.getString("id","");
        dbHelper = new DatabaseHelper(context,userId);
        database = dbHelper.getWritableDatabase();
    }

    public void setContext(Context context){
        this.context = context;
    }
    public void setRoomId(String currentroomId){
        this.currentroomId = currentroomId;
    }
    public Boolean isLogCheckOut(){return isLogout;}



    //  메세지 전송
    public synchronized void sendMsg(Message msg){
        String sendMsg = "";
        int signal = Integer.parseInt(msg.getSignal());
        Log.d(TAG,"+++++++++++++++++++++++"+signal);

        if(flag == false || isOnLine() == false){
            checkConnSocket();
            if(signal == Signals.MSG.getSigal() || signal == Signals.GROUP_CHATTING.getSigal()){
                try{
                    o.println(sendMsg);
                    o.flush();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        switch (Signals.getSignals(signal)){
            case LOGOUT:
                sendMsg = Signals.LOGOUT.getSigal() + delim1
                        + msg.getFromId();
                isLogout = false;
                break;
            case CHECK_IN:
                sendMsg = Signals.CHECK_IN.getSigal() + delim1
                        + msg.getRoomId() + delim2
                        + msg.getToId() + delim2
                        + msg.getPhoto() + delim2
                        + msg.getNikName() ;
                roomId = msg.getRoomId();
                toId = msg.getToId();
                isLogout = true;
                checkInMsg = sendMsg;
                break;
            case CHECK_OUT:
                sendMsg = Signals.CHECK_OUT.getSigal() + delim1;
                roomId = null;
                isLogout = false;
                checkInMsg = null;
                break;
            case MSG:
                sendMsg = Signals.MSG.getSigal() + delim1
                        + msg.getMsg() ;

                // unread =>  읽으면 0 안 읽었으면 1 // send => 내가 보냈으면 0 상대가 보냈으면 1
//                sql = "insert into chatList (room_id, friend_id, unread, msg, time, send) values "
//                        +"('"+toId+"','"+toId+"',0,'"+msg.getMsg()+"', CURRENT_TIMESTAMP, 0)";
//                if(database != null){
//                    database.execSQL(sql);
//                }
                break;
            case GROUP_CHATTING:
                sendMsg = Signals.GROUP_CHATTING.getSigal() + delim1
                        + msg.getMsg() ;
                break;
            case CHECK_IN_GROUPROOM:
                sendMsg = Signals.CHECK_IN_GROUPROOM.getSigal() + delim1
                        + msg.getRoomId() + delim2
                        + msg.getToId() + delim2
                        + msg.getPhoto() + delim2
                        + msg.getNikName() + delim2
                        +msg.getRoomName();
                roomId = msg.getRoomId();
                toId = msg.getToId();
                isLogout = true;
                checkInMsg = sendMsg;
                break;
            case CHECK_OUT_GROUPROOM:
                sendMsg = Signals.CHECK_OUT_GROUPROOM.getSigal() + delim1;
                roomId = null;
                isLogout = false;
                checkInMsg = null;
                break;
            case ADD_FRIEND:
                Log.d(TAG,"------------친추 들어옴");
                sendMsg = Signals.ADD_FRIEND.getSigal() + delim1
                        + msg.getFromId() + delim2
                        + msg.getToId() + delim2
                        + msg.getPhoto() + delim2
                        + msg.getNikName() ;
                break;
            case AGREE_FRIEND:
                Log.d(TAG,"------------친추 수락 들어옴");
                sendMsg = Signals.AGREE_FRIEND.getSigal() + delim1
                        + msg.getFromId() + delim2
                        + msg.getToId() + delim2
                        + msg.getPhoto() + delim2
                        + msg.getNikName() ;
                break;



        }


//        sendMsg = Signals.MSG.getSigal() + delim1  //  프로토콜
//                + CacheUtils.getUserCache().getUserId() + delim2  // fromId
//                + toId + delim2                                   // toId
//                + msg + delim2                                    // 메세지
//                + df.format(new Date()) + delim2                  //  현재 시간
//                + CacheUtils.getUserCache().getPhoto() + delim2   // fromId 프로필 사진 명
//                + CacheUtils.getUserCache().getName();        // fromId 닉네임
        try{
            checkConnSocket();
            Log.d(TAG,"**************************"+sendMsg);
            o.println(sendMsg);
            o.flush();
            if(signal == Signals.LOGOUT.getSigal()){
                closeConn();
                closeSocket();
                roomId = null;
                isLogout = false;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void run() {

        // read 해 올때
        while (flag){
            try{
                checkConnSocket();
                Log.d(TAG,"readLine while 문 안");
                String line = i.readLine();
                String [] splitData = StringSpilt1(line);
                int signal = Integer.parseInt(splitData[0]);
                line = splitData[1];
                switch (Signals.getSignals(signal)){
                    case ErrorMsg:
//                        makeToast("메세지 안감 ! 룸 나갔다가 다시 들어와야 함!!");
                        Log.d(TAG,"----------------------투 아이디 없어서 에러남");
                        Intent intent1 = new Intent("com.example."+roomId);
                        intent1.putExtra("error","핸들러 재생성");
                        context.sendBroadcast(intent1);
                    break;
                    case MSG:
                        // unread =>  읽으면 0 안 읽었으면 1 // send => 내가 보냈으면 0 상대가 보냈으면 1
                        message = StringSpilt(line,"msg");
                        if(message.getRoomId().startsWith("GroupChatting:20")){
                            roomId = message.getRoomId();
                        }else {
                            roomId = message.getFromId();
                        }
                        if(roomId.equals(currentroomId)){  // 현재 액티비티가 채팅 보낸 친구의 채팅룸인지 판단 맞을때
                            Log.d(TAG,"콘텍스트 : "+context+"  // 룸아이디 : "+roomId+" +++" + message.getMsg());
                            Intent intent = new Intent("com.example."+roomId);
                            intent.putExtra("msg",message);
                            context.sendBroadcast(intent);
                        }else {  // 현재 액티비티가 채팅보낸 친구의 채팅룸이 아닐때

                            // sqlite에 정보 넣기
                            sql = "insert into chatList (room_id, friend_id, unread, msg, time, send) values "
                                    +"('"+roomId+"','"+message.getFromId()+"',1,'"+message.getMsg()+"', '"+df.format(new Date())+"', 1)";
                            if(database != null){
                                database.execSQL(sql);
                            }

                            // 브로드캐스트
                            Intent intent = new Intent("com.example.main");
                            context.sendBroadcast(intent);

                            // 노티피케이션
                            ChattingMsg chatting = new ChattingMsg().setRoomId(roomId);
                            sendChatMsg("메세지",message.getNikName()+"에게서 메세지 옴--" + message.getMsg(),chatting,"chat");
                        }
                        Log.d(TAG,"갠톡 -- 콘텍스-->"+context+message.getNikName()+"에게서 메세지 옴--" + message.getMsg());
                        break;
                    case MSG_IMG:

                        break;
                    case GROUP_CHATTING:
                        message = StringSpilt(line,"msg");
                        roomId = message.getRoomId();
                        Cursor cursor = database.rawQuery("select * from  chattingRoomList WHERE roomId='"+roomId+"'",null);

                        if(cursor.getCount() == 0){
                            roomMembers = getRoomMembers(message.getToId());
                            toId = message.getFromId();
//                            String title = message.getFromId();
                            String title = message.getRoomName();
                            for(String id : roomMembers){
                                toId += delim3 + id;
//                                title += " ," + id;
                            }
                            Log.d(TAG,"단톡--------------------------- ---->"+toId);
                            database.execSQL("insert into chattingRoomList (roomId, roomName, membersId) values "
                                    +"('"+roomId+"', '"+title+"', '"+toId+"')");
                        }
                        if(roomId.equals(currentroomId)){  // 현재 액티비티가 채팅 보낸 친구의 채팅룸인지 판단
                            Log.d(TAG,"----------같은 룸: "+roomId+" = "+currentroomId);
                            Log.d(TAG,"콘텍스트 : "+context+"  // 룸아이디 : "+roomId+" +++" + message.getMsg());
                            Intent intent = new Intent("com.example."+roomId);
                            intent.putExtra("msg",message);
                            context.sendBroadcast(intent);
                        }else {  // 현재 액티비티가 채팅보낸 친구의
                            Log.d(TAG,"----------다른 룸: "+roomId+" != "+currentroomId);

                            // sqlite에 정보 넣기
                            sql = "insert into chatList (room_id, friend_id, unread, msg, time, send) values "
                                    +"('"+roomId+"','"+message.getFromId()+"',1,'"+message.getMsg()+"', '"+df.format(new Date())+"', 1)";
                            if(database != null){
                                database.execSQL(sql);
                            }

                            // 브로드캐스트
                            Intent intent = new Intent("com.example.main");
                            context.sendBroadcast(intent);

                            // 노티피케이션
                            ChattingMsg chatting = new ChattingMsg().setRoomId(roomId);
                            sendChatMsg("메세지",message.getNikName()+"에게서 메세지 옴--" + message.getMsg(), chatting,"chat");
                        }
                        Log.d(TAG,"단톡 -- 콘텍스-->"+context+message.getNikName()+"에게서 메세지 옴--" + message.getMsg());

                        break;
                    case ADD_FRIEND:
                        message = StringSpilt(line,"addFriend");
                        if(database != null){
                            database.execSQL("insert into friendRequest"
                                    +" (fromId, toId, agree, fromId_photo, fromId_nikName, time) values "
                                    +"('"+message.getFromId()+"','"+message.getToId()+"','false','"+message.getPhoto()+"','"+message.getNikName()+"','"+df.format(new Date())+"')");
                        }

                        Intent intent = new Intent("com.example.addFriend");
                        context.sendBroadcast(intent);

                        makeToast(message.getFromId()+"에게서 친구추가 요청 옴--" + message.getNikName());

                        ChattingMsg noChatting  = new ChattingMsg().setFriendId(message.getFromId());
                        sendChatMsg("친구 요청", message.getFromId()+"님이 친구 요청을 하였습니다.", noChatting,"addFriend");

                        Log.d(TAG,"토스트"+context+message.getFromId()+"에게서 친구추가 요청 옴--" + message.getNikName());
                        break;
                    case AGREE_FRIEND:

                        break;
                    case REMOVE_FRIEND:

                        break;

                }

            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    private void iniView() {
        //判断当前版本是不是>=8.0
        /**
         * 由于现在所有的app应用都希望通过推送消息来进行自己app的宣传造成用户的通知栏非常的臃肿，有时候一晚上不看手机，第二天通知栏已经爆满了
         * google已经根据这种情况做出了相应的对策
         * 开发者进行8.0(O)进行通知栏的适配 如果你的targetSdkVersion>=26  那么恭喜你的  你需要进行8.0通知栏适配，不然用户是收不到你的通知信息
         * 8.0通知栏适配效果：用户可以根据自己想要收到的通知，在设置里面找到应用来进行通知的关闭和打开
         * 注意:这里必须区分8.0以后和8.0之前的区别  不然在低版本的手机上会出现崩溃的现象
         *  这里需要创建一个通知渠道其中必须包括
         *  渠道id  这个可以随便定义，但是要保证全局的唯一性
         *  渠道名称 这个是最直接最接近可以的 用来让用户知道渠道的用途
         *  重要等级
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "chat";
            String channelName = "톡";

            int importanceHigh = NotificationManager.IMPORTANCE_HIGH;
            createNotificationChannel(channelId, channelName, importanceHigh);
            channelId = "addFriend";
            channelName = "친추";
            importanceHigh = NotificationManager.IMPORTANCE_DEFAULT;
            createNotificationChannel(channelId, channelName, importanceHigh);

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel(String channelId, String channelName, int importanceHigh) {
        NotificationChannel channel = new NotificationChannel(channelId, channelName, importanceHigh);
        channel.setShowBadge(true);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);
    }

    private Notification getNotification_25(String title, String content) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        return builder.setContentTitle(title)
                .setContentText(content)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                .setAutoCancel(true)
                .build();
    }

    @TargetApi(Build.VERSION_CODES.O)
    private Notification getChannelNotification(String title, String content, String channelId, ChattingMsg chatting, String msgType) {
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (msgType.equals("chat")){
            intent.setClass(context, ChattingRoom.class);
            intent.putExtra("chatting",chatting);
            intent.putExtra("fragment",0);
            intent.putExtra("activity", "MainActivity");
        }else if (msgType.equals("addFriend")){
            intent.setClass(context, NewFriend.class);
            intent.putExtra("fragment",1);
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(context,105,intent,PendingIntent.FLAG_CANCEL_CURRENT);
        Notification.Builder builder = new Notification.Builder(context, channelId);
        return builder
                .setContentIntent(pendingIntent)
                .setContentTitle(title)
                .setContentText(content)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setNumber(10)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                .setAutoCancel(true)
                .setVibrate(new long[]{1000, 500, 2000})
                .setLights(Color.BLUE, 2000, 1000)
                .build();

    }

    public void sendChatMsg(String title, String ContentText, ChattingMsg chatting, String msgType ) {
        NotificationManager manager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Notification notification = getChannelNotification(title, ContentText, "chat", chatting, msgType);
            manager.notify(1, notification);
        } else {
            Notification notification_25 = getNotification_25(title, ContentText);
            manager.notify(1, notification_25);
        }

    }

    public void addFriendMsg(String title, String ContentText, ChattingMsg chatting, String msgType ) {
        NotificationManager manager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Notification notification = getChannelNotification(title, ContentText, "addFriend",chatting, msgType);
            manager.notify(2, notification);
        } else {
            Notification notification_25 = getNotification_25(title, ContentText);
            manager.notify(2, notification_25);
        }

    }



    // 토스트 띄우기
    public void makeToast(String data ){
        Toast.makeText(context,data, Toast.LENGTH_LONG).show();
    }

    public String[] StringSpilt1(String data){
        String[] result = new String[2];
//        StringBuilder sb = new StringBuilder(data);
//        sb.replace(0,1,"");
//        data = sb.toString();
        Log.d(TAG,"★★★★★★★★★★★★★★★★"+data);
        String [] datas = data.split("\\,");
        data ="";
        if(datas.length>2){
            data = datas[1];
            for(int i = 2 ; i < datas.length ; i++){
                data += "," + datas[i];
            }
        }else if(datas.length == 2) {
            data = datas[1];
        }else if(datas.length == 1){
            data = datas[0];
        }
        Log.d(TAG,"▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲"+data);
        datas = data.split(delim1);
        String str = "";
        for(int i = 0 ; i < datas.length ; i++){
            str = datas[i];
            Log.d("//////StringSpilt1===",str);
            if(i == 0){
                result[0] = str;
            }else {
                result[1] = str;
            }
        }
        return result;
    }
    public ArrayList<String> getRoomMembers(String toId){
        ArrayList<String> members = new ArrayList<>();
        String [] ids = toId.split(delim3);
        for(String id : ids){
            if(!id.equals(userId)){
                members.add(id);
            }
        }
        return members;
    }
    public Message StringSpilt(String data,String type){
        message = new Message();
        String [] datas2 = data.split(delim2);
        String str = "";
        if(type.equals("msg")){
            for(int j = 0 ; j < datas2.length; j++){
                str = datas2[j];
                Log.d("//////msg********",str);
                if(j == 1){
                    message.setFromId(str);
                }else if(j == 2){
                    message.setToId(str);
                }else if(j == 3){
                    message.setPhoto(str);
                }else if(j == 4){
                    message.setNikName(str);
                }else if(j == 5){
                    String [] datas3 = str.split(delim4);
                    str ="";
                    for (int k = 0; k < datas3.length; k++){
                        if(k == 0){
                            str += datas3[k];
                        }else {
                            str += "\n" + datas3[k];
                        }
                    }
                    message.setMsg(str);
                }else if(j == 0){
                    message.setRoomId(str);
                }else if(j == 6){
                    message.setRoomName(str);
                }
            }
        }else if(type.equals("addFriend")){
            for(int j = 0 ; j < datas2.length; j++){
                str = datas2[j];
                Log.d("//////addFriend********",str);
                if(j == 0){
                    message.setFromId(str);
                }else if(j == 1){
                    message.setToId(str);
                }else if(j == 2){
                    message.setPhoto(str);
                }else if(j == 3){
                    message.setNikName(str);
                }
            }
        }
        return message;
    }

    public synchronized void checkConnSocket(){
        while (flag && !isOnLine()){
            try{
                Log.d(TAG, userId+" : 소켓이랑 연결 시도");
                socket = new Socket(Constants.SERVER_IP, Constants.SERVER_PORT);
                InputStream ins = socket.getInputStream();
                OutputStream os = socket.getOutputStream();
                i = new BufferedReader(new InputStreamReader(ins));
                o = new PrintWriter(new OutputStreamWriter(os),true);
                Log.d(TAG,"InputStream "+i.toString());
                Log.d(TAG,"OutputStream "+o.toString());
                o.println(Signals.LOGIN.getSigal()+"/@"+ userId);
                o.flush();
                Thread.sleep(100);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    //  소켓 연결 끊기
    public void closeConn()
    {
        flag = false;
    }


    // 소켓 닫기
    private void closeSocket()
    {
        try {
            if(socket != null)
            {
                o.close();
                i.close();
                socket.close();

                socket = null;
                Log.d(TAG,  "Socket 닫음");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 서버 소켓에 연결 되었는지 판단
    public boolean isOnLine()
    {
        if(socket==null)
            return false;

        boolean ret = true;
        try{

            socket.sendUrgentData(0xFF);
        }catch(Exception e){
            Log.d(TAG, userId+" : 소켓이랑 연결 끊김");
            Intent intent = new Intent("com.example."+roomId);
            intent.putExtra("error","소켓이랑 연결 끊김");
            context.sendBroadcast(intent);
            ret = false;
//            closeSocket();
        }

        return ret;
    }

}
