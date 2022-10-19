package com.example.wetalkclient.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.wetalkclient.MainActivity;
import com.example.wetalkclient.R;
import com.example.wetalkclient.adapter.AddFriendAdapter;
import com.example.wetalkclient.adapter.FriendAdapter;
import com.example.wetalkclient.bean.AddFriend;
import com.example.wetalkclient.bean.Message;
import com.example.wetalkclient.bean.User;
import com.example.wetalkclient.constant.Constants;
import com.example.wetalkclient.constant.Signals;
import com.example.wetalkclient.db.CacheUtils;
import com.example.wetalkclient.db.DatabaseHelper;
import com.example.wetalkclient.tcpConn.MsgUtils;
import com.example.wetalkclient.tcpConn.ThreadUtils;

import java.util.ArrayList;

public class NewFriend extends AppCompatActivity {
    private AlertDialog.Builder builder;
    private static final String TAG = NewFriend.class.getSimpleName();
    private Handler handler ;
    private AddFriend user;
    private AddFriendAdapter adapter;
    private SQLiteDatabase database ;
    private DatabaseHelper dbHelper;
    private Cursor cursor;
    private ArrayList<AddFriend> requests = new ArrayList<>();
    private boolean isSameId ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_friend);
        RecyclerView recyclerView = findViewById(R.id.recycleView2);
        MsgUtils.setContext(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        builder = new AlertDialog.Builder(this);
        handler = ThreadUtils.GetMultiHandler(TAG);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new AddFriendAdapter();
        ArrayList<AddFriend> friendRequests = CacheUtils.getFriendRequests();

        Intent intent = getIntent();
        int fragment = intent.getIntExtra("fragment",0);

        dbHelper = new DatabaseHelper(this,CacheUtils.getUserCache().getUserId());
        database = dbHelper.getWritableDatabase();
        if(database != null){
            cursor = database.rawQuery("select * from friendRequest ORDER BY time DESC ",null);
        }
        Log.d(TAG , "+++++++++++++++++++cursor++++"+cursor.getCount());
        ArrayList<User> friends = CacheUtils.getFriendList();
        for (int i = 0; i < cursor.getCount(); i++){
            cursor.moveToNext();
            isSameId = true;
            user = new AddFriend().setFromId(cursor.getString(1))
                    .setToId(cursor.getString(2))
                    .setFromId_photo(cursor.getString(4))
                    .setFromId_nikName(cursor.getString(5))
                    .setDate(cursor.getString(6));

            
            // 이미 친구인지 한번 더 판단
            for(int k = 0 ; k < friends.size() ; k++){
                if(cursor.getString(1).equals(friends.get(k).getUserId())){
                    isSameId = false;
                }
            }
            
            // 이미 친구이면 디비의 값 바꿈 & 이미수락으로 보여줌
            if(isSameId == true){
                user.setAgree(cursor.getString(3));
            }else if(isSameId == false) {
                user.setAgree("true");
                String sql1 = "UPDATE friendRequest SET agree = 'true' WHERE fromId='"+user.getFromId()+"'";
                database.execSQL(sql1);
            }

            adapter.addItem(user);
            requests.add(user);
            Log.d(TAG , "+++++++++user.getAgree()+++"+user.getFromId_nikName() + "//////////"+user.getAgree());


        }
        Log.d(TAG , "+++++++++++++++++++friendRequests++++"+friendRequests.size());

        adapter.setOnItemClickLIstener(new AddFriendAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position) {

                if(requests.get(position).getAgree().equals("false")){

                    builder.setMessage(requests.get(position).getFromId_nikName()+"님의 친구 추가 요청을 동의 하시겠습니까?");
                    builder.setIcon(R.drawable.ic_baseline_person_add_alt_1_24);
                    builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Message msg = new Message().setSignal(Signals.AGREE_FRIEND.getSigal()+"")
                                            .setFromId(CacheUtils.getUserCache().getUserId())
                                            .setToId(requests.get(position).getFromId())
                                            .setPhoto(CacheUtils.getUserCache().getPhoto())
                                            .setNikName(CacheUtils.getUserCache().getName());
                                    MsgUtils.sendMsg(msg);
                                    String sql = "UPDATE friendRequest SET agree = 'true' WHERE fromId='"+requests.get(position).getFromId()+"'";
                                    database.execSQL(sql);
                                    refresh();
                                }
                            });
                            makeToast(requests.get(position).getFromId_nikName()+"와 친구가 되었습니다.");
                        }
                    });
                    builder.setNegativeButton("아니요", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            makeToast("친구 추가 요청을 수락하지 않았습니다.");
                        }
                    });
                    builder.create().show();
                }
            }
        });
        recyclerView.setAdapter(adapter);
    }
    public void refresh(){
        Intent intent = new Intent(this, NewFriend.class);
        this.finish();
        startActivity(intent);

    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.putExtra("fragment",1);
                    this.finish();
                    startActivity(intent);
                break;

        }
        return super.onOptionsItemSelected(item);
    }
    public void makeToast(String data ){
        Toast.makeText(this,data, Toast.LENGTH_LONG).show();
    }
}