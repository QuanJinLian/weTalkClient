package com.example.wetalkclient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.wetalkclient.activities.ChattingRoom;
import com.example.wetalkclient.activities.ChoiceTeamMember;
import com.example.wetalkclient.activities.Login;
import com.example.wetalkclient.activities.SearchAndAddFriend;
import com.example.wetalkclient.bean.User;
import com.example.wetalkclient.constant.Constants;
import com.example.wetalkclient.db.CacheUtils;
import com.example.wetalkclient.fragment.ChattingFragment;
import com.example.wetalkclient.fragment.FriendListFragment;
import com.example.wetalkclient.fragment.MyPageFragment;
import com.example.wetalkclient.http.HttpCallBackListener;
import com.example.wetalkclient.http.HttpUtils;
import com.example.wetalkclient.tcpConn.ChatConnThread;
import com.example.wetalkclient.tcpConn.MsgUtils;
import com.example.wetalkclient.tcpConn.ThreadUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.apache.http.conn.ConnectTimeoutException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements HttpCallBackListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private ChattingFragment chattingFragment;
    private FriendListFragment friendListFragment;
    private MyPageFragment myPageFragment;
    private SQLiteDatabase database;

    private ChatConnThread chatConnThread ;
    private Handler chatConnHandler ;
    private ActionBar abar;
    private String userId;
    private String photoName;
    private String nikName;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("WeTalk");
        chattingFragment = new ChattingFragment();
        friendListFragment = new FriendListFragment();
        myPageFragment = new MyPageFragment();

        // 9버전 이상 부터 netWorkException이 날 수 있으니 설정 해줌
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        SharedPreferences share = getSharedPreferences("Login", Context.MODE_PRIVATE);
        photoName = share.getString("photo","");
        nikName = share.getString("name","");
        userId = share.getString("id","");
        Log.d(TAG,"%%%%%%%%%%%%%%%%%%%%%%%%%"+photoName);
        String photo = Constants.HTTP_SERVER_IP + "/webServerProject/upload/"+photoName+".jpg";
//            photo.replace(" ","+");
//            Bitmap bitmap = ViewUtils.StringToBitmap(photo);
//            imageView.setImageBitmap(bitmap);
//            Drawable drawable = ViewUtils.StringToDrawable(photo);
//            imageView.setImageDrawable(drawable);
        URL url = null;
        try {
            url = new URL(photo);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        Bitmap bitmap = getRemoteImage(url);
        Drawable drawable = new BitmapDrawable(bitmap);

        abar = getSupportActionBar();
        abar.setLogo(drawable);
        abar.setTitle(nikName);
        abar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME|ActionBar.DISPLAY_USE_LOGO|ActionBar.DISPLAY_SHOW_TITLE);

        // 핸들러 생성과 동시에 실행
        chatConnHandler = ThreadUtils.GetMultiHandler(TAG+"_Chat");
        

        // 소켓 연결
        chatConnThread = new ChatConnThread(this);
        chatConnHandler.post(chatConnThread);
        MsgUtils.setConnThread(chatConnThread);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.tab1:
                        getSupportFragmentManager().beginTransaction().replace(R.id.container, chattingFragment).commit();
                        return true;
                    case R.id.tab2:
                        getSupportFragmentManager().beginTransaction().replace(R.id.container, friendListFragment).commit();
                        return true;
                    case R.id.tab3:
                        getSupportFragmentManager().beginTransaction().replace(R.id.container, myPageFragment).commit();
                        return true;
                }

                return false;
            }
        });

        Intent intent = getIntent();

        switch (intent.getIntExtra("fragment",0)){
            case 0:
                getSupportFragmentManager().beginTransaction().replace(R.id.container, chattingFragment).commit();
                bottomNavigationView.setSelectedItemId(R.id.tab1);
                break;
            case 1:
                getSupportFragmentManager().beginTransaction().replace(R.id.container, friendListFragment).commit();
                bottomNavigationView.setSelectedItemId(R.id.tab2);
                break;
            case 2:
                getSupportFragmentManager().beginTransaction().replace(R.id.container, myPageFragment).commit();
                bottomNavigationView.setSelectedItemId(R.id.tab3);
                break;

        }
//        Toast.makeText(this, "fragment="+intent.getIntExtra("fragment",0), Toast.LENGTH_SHORT).show();


        database = openOrCreateDatabase(userId, MODE_PRIVATE, null);

        getFriendList(userId);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int curId = item.getItemId();
        switch (curId){
            case R.id.addFriend:
                Intent intent = new Intent(this, SearchAndAddFriend.class);
                this.finish();
                startActivity(intent);
                break;
            case R.id.teamTalk:
                Intent intent1 = new Intent(this, ChoiceTeamMember.class);
                this.finish();
                startActivity(intent1);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    // 초기 로딩 시 친구 리스트 재 로딩
    public void getFriendList(String id){
        String data = "id="+id;
        try {
            HttpUtils.sendRequest(Constants.ID.GET_FRIEND_LIST,data, MainActivity.this,this);
        } catch (ConnectTimeoutException e) {
            Log.d(TAG,"인터넷 연결시간이 초과되었습니다.");
        }catch (SocketTimeoutException e){
            Log.d(TAG,"System is Busy");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void httpCallBack(int id, String response) {
        Log.d(TAG,"id---"+id+"+++++response----"+response);
        CacheUtils.setFriendList(response);
        ArrayList<User> friendList = CacheUtils.getFriendList();
        Log.d(TAG,"+++++friendList----"+friendList.size());
        Cursor cursor = database.rawQuery("select friend_id from friendList",null);
        if(cursor.getCount() > 0){
            database.execSQL("delete from friendList");
        }
        for(User user : friendList){
            database.execSQL("insert into "+"friendList"
                    +" (friend_id, nikName, photo) "
                    + " values "
                    + "('"+user.getUserId()+"','"+user.getName()+"','"+user.getPhoto()+"')");
        }
        Log.d(TAG,"-------+++++----db insert 완료");
    }


    public Bitmap getRemoteImage(URL aURL) {
        try {
            URLConnection conn = aURL.openConnection();
            conn.connect();
            BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());
            Bitmap bm = BitmapFactory.decodeStream(bis);
            bis.close();
            return bm;
        } catch (IOException e) {}
        return null;
    }
}