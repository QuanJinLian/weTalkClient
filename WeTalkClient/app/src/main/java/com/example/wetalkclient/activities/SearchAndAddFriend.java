package com.example.wetalkclient.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.wetalkclient.MainActivity;
import com.example.wetalkclient.R;
import com.example.wetalkclient.adapter.FriendAdapter;
import com.example.wetalkclient.bean.Message;
import com.example.wetalkclient.bean.User;
import com.example.wetalkclient.constant.Constants;
import com.example.wetalkclient.constant.Signals;
import com.example.wetalkclient.db.CacheUtils;
import com.example.wetalkclient.http.HttpCallBackListener;
import com.example.wetalkclient.http.HttpUtils;
import com.example.wetalkclient.tcpConn.ChatConnThread;
import com.example.wetalkclient.tcpConn.MsgUtils;
import com.example.wetalkclient.tcpConn.ThreadUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class SearchAndAddFriend extends AppCompatActivity implements HttpCallBackListener {
    private static final String TAG = SearchAndAddFriend.class.getSimpleName();
    private EditText editText;
    private Button button;
    private RecyclerView recyclerView;
    private FriendAdapter adapter;
    private boolean canSearch;
    private AlertDialog.Builder builder;
    private Handler handler ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_and_add_friend);

        findViews();
        MsgUtils.setContext(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = findViewById(R.id.recycleView1);
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new FriendAdapter();
        recyclerView.setAdapter(adapter);

        builder = new AlertDialog.Builder(this);

        handler = ThreadUtils.GetMultiHandler(TAG);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                canSearch = true;
                adapter.clearItems();
                adapter.notifyDataSetChanged();
                String id = editText.getText().toString();
                if (id.equals("")) {
                    makeToast("아이디 입력후 검색해주세요.");
                    canSearch = false;
                }else if(id.equals(CacheUtils.getUserCache().getUserId())){
                    makeToast("본인을 왜 검색하시는겁니까?");
                    canSearch = false;
                }else{
                    for(int i = 0 ; i < CacheUtils.getFriendList().size();i++){
                        if(id.equals(CacheUtils.getFriendList().get(i).getUserId())){
                            makeToast("이미 친구인데 왜 검색하시는겁니까?");
                            canSearch = false;
                        }
                    }
                }
                if(canSearch == true){
                    String data = "id="+id;
                    try{
                        HttpUtils.sendRequest(Constants.ID.SEARCH_USER,data,SearchAndAddFriend.this,getApplicationContext());
                    }catch (Exception e){
                        Log.d(TAG,e.toString());
                    }
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

    // 토스트 띄우기
    public void makeToast(String data ){
        Toast.makeText(this,data, Toast.LENGTH_LONG).show();
    }

    public void findViews(){
        editText = findViewById(R.id.editText12);
        button = findViewById(R.id.button4);
    }

    @Override
    public void httpCallBack(int id, String response) {
        Log.d(TAG,"id---"+id+"+++++response----"+response);
        Document doc = Jsoup.parse(response);
        Elements result = doc.select("p.result");
        Elements userId = doc.select("ol > li.id");
        Elements nikName = doc.select("ol > li.nikName");
        Elements photo = doc.select("ol > li.photo");
        if (result.get(0).text().equals("true")){
            User user =  new User().setUserId(userId.get(0).text())
                                    .setName(nikName.get(0).text())
                                    .setPhoto(photo.get(0).text());
            adapter.addItem(user);
            adapter.setOnItemClickLIstener(new FriendAdapter.OnItemClickListener() {
                @Override
                public void onClick(int position) {
                    builder.setMessage(nikName.get(0).text()+"님한테 친구 추가 요청을 하시겠습니까?");
                    builder.setIcon(R.drawable.ic_baseline_person_add_alt_1_24);
                    builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Message msg = new Message().setSignal(Signals.ADD_FRIEND.getSigal()+"")
                                            .setFromId(CacheUtils.getUserCache().getUserId())
                                            .setToId(userId.get(0).text())
                                            .setPhoto(CacheUtils.getUserCache().getPhoto())
                                            .setNikName(CacheUtils.getUserCache().getName());
                                    MsgUtils.sendMsg(msg);
                                }
                            });
                            makeToast("친구 추가 요청을 보냈습니다");
                        }
                    });
                    builder.setNegativeButton("아니요", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            makeToast("친구 추가 요청을 하지 않았습니다.");
                        }
                    });
                    builder.create().show();
                }
            });
            adapter.notifyDataSetChanged();

        }else if(result.get(0).text().equals("false")){
            makeToast("찾으시는 친구가 존재하지 않습니다.");
        }
    }
}