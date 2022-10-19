package com.example.wetalkclient.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.wetalkclient.MainActivity;
import com.example.wetalkclient.R;
import com.example.wetalkclient.bean.User;
import com.example.wetalkclient.db.CacheUtils;
import com.example.wetalkclient.tcpConn.ThreadUtils;

import java.util.ArrayList;
import java.util.List;

public class ChoiceTeamMember extends AppCompatActivity {

    private static final String TAG = ChoiceTeamMember.class.getSimpleName();
    private ArrayAdapter<String> adapter;
    private ArrayList<String> friendNikNames;
    private ArrayList<User> friendList;
    private ArrayList<String> teamMember;
    private Handler handler ;
    private Button button;
    private ListView listView;
    private SparseBooleanArray sba;
    private AlertDialog.Builder builder;
    private Context context;
    private String roomName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choice_team_member);
        listView = findViewById(R.id.list);
        button = findViewById(R.id.startTeamTalk);
        builder = new AlertDialog.Builder(this);
        friendNikNames = new ArrayList<>();
        friendList = CacheUtils.getFriendList();
        handler = ThreadUtils.GetMultiHandler(TAG);
        context = getApplicationContext();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Log.d(TAG, "!!!!!!!!!friendList.size()!!!!!!!!!!"+friendList.size());

        for(User user : friendList){
            friendNikNames.add(user.getName());
        }

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice,friendNikNames);
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int count = 0 ;
                sba = listView.getCheckedItemPositions();
                if(sba.size() != 0){
                    for(int i = listView.getCount()-1; i >=0; i--){
                        if (sba.get(i) == true){
                            count++;
                        }
                    }
                }
                button.setText("단체 채팅 ["+count+"]");
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int count = 0 ;
                sba = listView.getCheckedItemPositions();
                teamMember = new ArrayList<>();
                String friends = "";
                roomName = CacheUtils.getUserCache().getName();
                if(sba.size() != 0){
                    for(int i = listView.getCount()-1; i >=0; i--){
                        if (sba.get(i) == true){
                            if (friends.equals("")){
                                friends += friendNikNames.get(i);
                            }else {
                                friends += ","+ friendNikNames.get(i);
                            }
                            roomName += ","+ friendNikNames.get(i);
                            teamMember.add(friendList.get(i).getUserId());
                            count++;
                        }
                    }
                }
                if(count <= 1){
                    Toast.makeText(getApplicationContext(), "한명 이상의 친구를 체크 해 주시오.", Toast.LENGTH_SHORT).show();
                }else {

                    builder.setMessage(friends+"님들과 단체 채팅을 하시겠습니까?");
                    builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    moveToRoom();
                                }
                            });
                        }
                    });
                    builder.setNegativeButton("아니요", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(getApplicationContext(), "취소하였습니다.", Toast.LENGTH_SHORT).show();
                        }
                    });
                    builder.create().show();
                }

            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("fragment",0);
                this.finish();
                startActivity(intent);
                break;

        }
        return super.onOptionsItemSelected(item);
    }
    public void moveToRoom(){
        Intent intent = new Intent(getApplicationContext(),ChattingRoom.class);
        intent.putExtra("roomName",roomName);
        intent.putExtra("friends",teamMember);
        intent.putExtra("fragment", 0);
        intent.putExtra("activity", "TeamChat");
        this.finish();
        startActivityForResult(intent, 102);
    }

}