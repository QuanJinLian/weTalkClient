package com.example.wetalkclient.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wetalkclient.MainActivity;
import com.example.wetalkclient.R;
import com.example.wetalkclient.adapter.ChattingAdapter;
import com.example.wetalkclient.bean.ChattingMsg;
import com.example.wetalkclient.bean.Message;
import com.example.wetalkclient.bean.User;
import com.example.wetalkclient.constant.Signals;
import com.example.wetalkclient.db.CacheUtils;
import com.example.wetalkclient.db.DatabaseHelper;
import com.example.wetalkclient.tcpConn.MsgUtils;
import com.example.wetalkclient.tcpConn.ThreadUtils;

import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChattingRoom extends AppCompatActivity {
    private static final String TAG = ChattingRoom.class.getSimpleName();
    private User user;
    private Context context;
    private ListView listView;
    private int fragment;
    private String activity;
    private String roomId;
    private String toId;
    private String title;
    private String signal;
    private String editTextMsg;
    private TextView textView;
    private Button button;
    private EditText editText;
    private String delim1 = "/@";
    private String delim2 = "/&-";
    private String delim3 = "/#";
    private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private Handler chatConnHandler ;
    private Message message = new Message();
    private ChattingMsg chattingMsg = new ChattingMsg();
    private SQLiteDatabase database ;
    private DatabaseHelper dbHelper;
    private Cursor cursor;
    private ChattingAdapter adapter;
    private ArrayList<ChattingMsg> newChattingList;
    private boolean isGroupChatting;
    private ArrayList<String> teamMember;
    private String roomName;
    private SimpleDateFormat df2 = new SimpleDateFormat("yyyyMMddHHmmss");


    private ArrayList<ChattingMsg> chattingList;

    // 메세지 전송 받을 브로드캐스트
    private BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatting_room);
        roomName = null;

        dbHelper = new DatabaseHelper(this,CacheUtils.getUserCache().getUserId());
        database = dbHelper.getWritableDatabase();

        newChattingList = new ArrayList<ChattingMsg>();
        chattingList = new ArrayList<ChattingMsg>();

        findViews();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        chatConnHandler = ThreadUtils.GetMultiHandler(TAG);

        Intent intent = getIntent();
        // 메인 액티비티에서 넘어 왔을때
        if(intent.getStringExtra("activity").equals("MainActivity")){
            fragment = intent.getIntExtra("fragment",0);

            // 친구 리스트 에서 넘어 왔을때
            if(fragment ==1){
                isGroupChatting = false;
//                Toast.makeText(this, " 친구리스트 페이지 넘어옴", Toast.LENGTH_SHORT).show();
                user = (User) intent.getSerializableExtra("friend");
                roomId = user.getUserId();
                toId = user.getUserId();
                title = user.getName();
                signal = Signals.CHECK_IN.getSigal()+"";

            }
            // 채팅 리스트에서 넘어 왔을때
            else if(fragment ==0){
                isGroupChatting = false;
                ChattingMsg chatMsg = (ChattingMsg)intent.getSerializableExtra("chatting");
                roomId = chatMsg.getRoomId();
                // 룸이 단톡인지 갠톡인지 판단
                if(roomId.startsWith("GroupChatting:20")){  //단톡방일때
                    Cursor cursor2 = database.rawQuery("select * from  chattingRoomList WHERE roomId='"+roomId+"'",null);
                    if (cursor2.getCount() > 0) {
                        for (int i = 0; i < cursor2.getCount(); i++){
                            cursor2.moveToNext();
                            toId = cursor2.getString(3);
                            title = cursor2.getString(2);
                            roomName = cursor2.getString(2);
                        }
                    }
                    signal = Signals.CHECK_IN_GROUPROOM.getSigal()+"";

                }else {  // 갠톡일 때
                    user = CacheUtils.getFriend(roomId);
                    toId = user.getUserId();
                    title = user.getName();
                    signal = Signals.CHECK_IN.getSigal()+"";
                }
            }
            activity = "MainActivity";
        }
        // 다톡방 개설과 동시에 넘어왔을 때
        else if(intent.getStringExtra("activity").equals("TeamChat")){
            isGroupChatting = true;
            toId = "";
            title = "";
            fragment = intent.getIntExtra("fragment",0);
            activity = "MainActivity";
            roomName = intent.getStringExtra("roomName");
            teamMember = intent.getStringArrayListExtra("friends");
            for(int i = 0; i < teamMember.size(); i++){
                user = CacheUtils.getFriend(teamMember.get(i));

                if(toId.equals("")){
                    toId += teamMember.get(i);
//                    title += user.getName();
                }else {
                    toId += delim3 + teamMember.get(i);
//                    title += " ," + user.getName();
                }
            }
            title = roomName;
            roomId = "GroupChatting:"+df2.format(new Date())+ CacheUtils.getUserCache().getUserId();
            database.execSQL("insert into chattingRoomList (roomId, roomName, membersId) values "
                    +"('"+roomId+"', '"+roomName+"', '"+toId+"')");


            signal = Signals.CHECK_IN_GROUPROOM.getSigal()+"";
        }

        database.execSQL("update chatList set unread=0  WHERE room_id='"+roomId+"'");

        // 보로드캐스트 생성
        IntentFilter intentFilter = new IntentFilter();
        broadcastReceiver = new MyBroadcastReceiver();
        // action 명명 기준은 룸아이디
        intentFilter.addAction("com.example."+roomId);
        registerReceiver(broadcastReceiver, intentFilter);


        MsgUtils.setContext(this);
        MsgUtils.setCurrentRoom(roomId);
        setTitle(title);
        sendCheckInSignal();



        // 채팅 기록 있으면 뿌려주기 위해 SQLite에서 꺼냄
        if(database != null){
            String sql = "select * from chatList WHERE room_id='"+roomId+"' ORDER BY _no ASC";
            cursor = database.rawQuery(sql , null);
            for (int i = 0 ; i < cursor.getCount(); i++){
                cursor.moveToNext();
                chattingMsg = new ChattingMsg().setRoomId(cursor.getString(1))
                        .setFriendId(cursor.getString(2))
                        .setUnread(cursor.getInt(3))
                        .setMsg(cursor.getString(4))
                        .setTime(cursor.getString(5))
                        .setSend(cursor.getInt(6));
                if(cursor.getInt(6) ==1){
                    user = CacheUtils.getFriend(cursor.getString(2));
                    if(user.getUserId() == null){
                        chattingMsg.setPhoto(null)
                                .setNikName(cursor.getString(2));
                    }else {
                        chattingMsg.setPhoto(user.getPhoto())
                                .setNikName(user.getName());
                    }
                }
                chattingList.add(chattingMsg);
            }
        }
        adapter = new ChattingAdapter(this , chattingList);
        listView.setAdapter(adapter);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"룸아이디는요 요요요요요ㅛㅇ요 "+roomId);
                editTextMsg = editText.getText().toString();
                if(!editTextMsg.equals("")){
                    sendString(editTextMsg);
                    editText.setText("");
                }else {
                    Toast.makeText(getApplicationContext(), "내용을 입력해주세요", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void sendCheckInSignal(){
        message.setAllNull();
        message.setSignal(signal)
                .setRoomId(roomId)
                .setToId(toId)
                .setPhoto(CacheUtils.getUserCache().getPhoto())
                .setNikName(CacheUtils.getUserCache().getName());
        if(roomName != null){
            message.setRoomName(roomName);
        }

        MsgUtils.sendMsg(message);  //입장 신호
    }

    @Override
    protected void onPause() {
        super.onPause();
        String sql;
//        Toast.makeText(this, "온퍼즈 들어옴  "+newChattingList.size(), Toast.LENGTH_SHORT).show();
        for(int i = 0 ; i < newChattingList.size(); i++){
            chattingMsg = newChattingList.get(i);
            Log.d(TAG, i + " 번째 메세지 룸아이디->"+chattingMsg.getRoomId()+"/시간->"+chattingMsg.getTime()+"/샌드 ->"+chattingMsg.getSend());
            if(database != null){
                database.execSQL("insert into chatList (room_id, friend_id, unread, msg, time, send) values "
                        +"('"+chattingMsg.getRoomId()+"','"+chattingMsg.getFriendId()+"',"+chattingMsg.getUnread()+",'"
                        +chattingMsg.getMsg()+"', '"+ chattingMsg.getTime() +"', "+chattingMsg.getSend()+")");
            }
        }

        newChattingList.clear();

        message.setSignal(Signals.CHECK_OUT.getSigal()+"");

        MsgUtils.sendMsg(message);  //나가는 신호
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(broadcastReceiver!=null) {
            unregisterReceiver(broadcastReceiver);
        }
    }



    public synchronized void sendString(String msg){

        if(MsgUtils.isLogCheckOut() == false){
            // ChenkIN 신호 다시 보내기
            sendCheckInSignal();
        }
            // 소켓에 메세지 보내기
            message.setAllNull();

            // 현재 채팅룸이 단톡인지 갠톡인지 판단함
            if(roomId.startsWith("GroupChatting:20")){  // 단톡일 때
                message.setSignal(Signals.GROUP_CHATTING.getSigal()+"");

            }else {
                message.setSignal(Signals.MSG.getSigal()+"");
            }
            String sendmsg = "";
            Pattern p = Pattern.compile("\n");
            Matcher m = p.matcher(msg);
            sendmsg = m.replaceAll("/~=");

            message.setMsg(sendmsg);
            MsgUtils.sendMsg(message);

            ChattingMsg chatMsg = new ChattingMsg();

            // db에 메세지 저장을 위해 모아 놓기
            chatMsg.setUnread(0)
                    .setRoomId(roomId)
                    .setFriendId(CacheUtils.getUserCache().getUserId())
                    .setMsg(msg)
                    .setTime(df.format(new Date()))
                    .setSend(0);
            newChattingList.add(chatMsg);
            chattingList.add(chatMsg);


            adapter = new ChattingAdapter(this , chattingList);
            listView.setAdapter(adapter);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                if(activity.equals("MainActivity")){
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.putExtra("fragment",fragment);
                    this.finish();
                    startActivity(intent);
                }
                break;

        }
        return super.onOptionsItemSelected(item);
    }
    public void findViews(){
        button = findViewById(R.id.button5);
        editText = findViewById(R.id.editText);
        listView = findViewById(R.id.chatting_listView);
//        linearLayout = findViewById(R.id.containerMsg);

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        Toast.makeText(getApplicationContext(),"requestCode 받음"+requestCode+"+ 리절트 "+resultCode,Toast.LENGTH_SHORT).show();

        super.onActivityResult(requestCode, resultCode, data);
    }

    class MyBroadcastReceiver extends  BroadcastReceiver{

        @Override
        public synchronized void onReceive(Context context, Intent intent) {
            Message msg = (Message) intent.getSerializableExtra("msg");
            String error = intent.getStringExtra("error");
            if(error != null){
                if(error.equals("소켓이랑 연결 끊김")){
                    sendCheckInSignal();
                    if(!chattingList.get(chattingList.size()-1).getFriendId().equals(CacheUtils.getUserCache().getUserId())){
//                        Toast.makeText(getApplicationContext(), "소켓 끊김! 다시 연결중!", Toast.LENGTH_LONG).show();
                    }else {
                        if(message.getSignal().equals(Signals.CHECK_IN.getSigal()+"")){
                            message.setAllNull();
                            message.setSignal(Signals.MSG.getSigal()+"")
                                    .setMsg(newChattingList.get(newChattingList.size()-1).getMsg());
                        }else if(message.getSignal().equals(Signals.CHECK_IN_GROUPROOM.getSigal()+"")){
                            message.setAllNull();
                            message.setSignal(Signals.GROUP_CHATTING.getSigal()+"")
                                    .setMsg(newChattingList.get(newChattingList.size()-1).getMsg());
                        }
                        MsgUtils.sendMsg(message);
//                        chattingList.remove(chattingList.size()-1);
//                        newChattingList.remove(newChattingList.size()-1);
//                        Toast.makeText(getApplicationContext(), "소켓 끊김! 메세지 발송 실패!", Toast.LENGTH_LONG).show();
                    }
                }else if(error.equals("핸들러 재생성")){
                    chattingList.remove(chattingList.size()-1);
                    newChattingList.remove(newChattingList.size()-1);
                    sendCheckInSignal();
                }
            }else {
                ChattingMsg chattingMsg = new ChattingMsg();
                User friend = CacheUtils.getFriend(msg.getFromId());
                if(friend.getUserId() == null){
                    chattingMsg.setFriendId(msg.getFromId())
                            .setNikName(msg.getFromId())
                            .setRoomId(roomId)
                            .setPhoto(null)
                            .setMsg(msg.getMsg())
                            .setSend(1)
                            .setTime(df.format(new Date()));


                }else{
                    chattingMsg.setFriendId(friend.getUserId())
                            .setNikName(friend.getName())
                            .setRoomId(roomId)
                            .setPhoto(friend.getPhoto())
                            .setMsg(msg.getMsg())
                            .setSend(1)
                            .setTime(df.format(new Date()));

                }

                newChattingList.add(chattingMsg);
                chattingList.add(chattingMsg);
            }

            adapter = new ChattingAdapter(getApplicationContext() , chattingList);
            listView.setAdapter(adapter);
        }
    }

}