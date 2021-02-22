package com.example.wetalkclient.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.example.wetalkclient.MainActivity;
import com.example.wetalkclient.R;
import com.example.wetalkclient.bean.User;
import com.example.wetalkclient.constant.Constants;
import com.example.wetalkclient.db.CacheUtils;
import com.example.wetalkclient.db.DatabaseHelper;
import com.example.wetalkclient.http.HttpCallBackListener;
import com.example.wetalkclient.http.HttpUtils;

import org.apache.http.conn.ConnectTimeoutException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.net.SocketTimeoutException;

public class Login extends AppCompatActivity implements HttpCallBackListener {
    private static final String TAG = Login.class.getSimpleName();
    EditText idEdit;
    EditText pwdEdit;
    Button loginBnt;
    Button registerBnt;
    CheckBox autoLoginCheckBox;
    CheckBox rememberPwdCheckBox;

    private String strId = "";
    private String strPwd = "";
    private boolean autoLoginChecked=false;
    private boolean rememberPwd =false;
    private String id ;
    private String pwd ;
    private boolean IsAutoLogin;
    private boolean IsRememberPwd;
    private SQLiteDatabase database;
    private User user;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitle("WeTalk 로그인");
        // 9버전 이상 부터 netWorkException이 날 수 있으니 설정 해줌
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        // 뷰들을 찾아줌;
        findViews();
        
        // 자동 로그인 체크 이벤트
        autoLoginCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    IsAutoLogin = true;
                    IsRememberPwd = true;
                    rememberPwdCheckBox.setChecked(true);
                }else {
                    IsAutoLogin = false;
                }
            }
        });
        // 비번 저장 체크 이벤트
        rememberPwdCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    IsRememberPwd = true;
                }else {
                    IsRememberPwd = false;
                }
            }
        });
        // 전에 저장했던 데이터가 있는지 꺼내본다
        SharedPreferences share = getSharedPreferences("Login", Context.MODE_PRIVATE);
        autoLoginChecked = share.getBoolean("autoLogin",false);
        rememberPwd = share.getBoolean("rememberPwd",false);

        if(rememberPwd == true){
            strId = share.getString("id","");
            strPwd = share.getString("pwd","");
        }else {
            strId = "";
            strPwd = "";
        }

        if(share == null){  //데이터 저장한적 없음;
            makeToast("share이 null인간");
        }else{  //데이터 있음
            // 있는 데이터들로 셋팅해줌
            idEdit.setText(strId);
            pwdEdit.setText(strPwd);
            autoLoginCheckBox.setChecked(autoLoginChecked);
            rememberPwdCheckBox.setChecked(rememberPwd);
            if(autoLoginChecked == true){  // 예전에 자동 로그인이 체크되어 있었다면 로그인 작업 실시
                login(strId, strPwd);
            }
        }
        // 로그인 버튼 이벤트
        loginBnt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                init();
            }
        });
        
        // 회원가입 페이지로 이동
        registerBnt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Register.class);
                startActivity(intent);
            }
        });

    }
    public void init(){
        id = idEdit.getText().toString();
        pwd = pwdEdit.getText().toString();
        if(id.equals("") || pwd.equals("")){
            makeToast("아이디와 비번을 모두 입력해주세요!!");
        }else {
            SharedPreferences share = getSharedPreferences("Login",Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = share.edit();
            editor.putString("id",id);
            editor.putString("pwd",pwd);
            if(IsRememberPwd == true){
                editor.putBoolean("rememberPwd",true);
                if(IsAutoLogin == true){
                    editor.putBoolean("autoLogin",true);
                }else {
                    editor.putBoolean("autoLogin",false);
                }
            }else {
                editor.putBoolean("rememberPwd",false);
                editor.putBoolean("autoLogin",false);
            }
            editor.commit();
            login(id,pwd);
        }
    }

    // 로그인 작업
    public void login(String id, String pwd){
        String data = "id="+id+"&pwd="+pwd;
        try {
            HttpUtils.sendRequest(Constants.ID.LOGIN,data,Login.this,this);
        } catch (ConnectTimeoutException e) {
            Log.d(TAG,"인터넷 연결시간이 초과되었습니다.");
        }catch (SocketTimeoutException e){
            Log.d(TAG,"System is Busy");
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    
    // 토스트 띄우기
    public void makeToast(String data ){
        Toast.makeText(this,data, Toast.LENGTH_LONG).show();
    }


    private void findViews() {
        idEdit = findViewById(R.id.editText);
        pwdEdit = findViewById(R.id.editText2);
        loginBnt = findViewById(R.id.button);
        registerBnt = findViewById(R.id.button2);
        autoLoginCheckBox = findViewById(R.id.checkBox);
        rememberPwdCheckBox = findViewById(R.id.checkBox2);

    }

    @Override
    public void httpCallBack(int id, String response) {
        Log.d(TAG,"id---"+id+"+++++response----"+response);
        Document doc = Jsoup.parse(response);
        Elements result = doc.select("ol > li.result");
        Elements userId = doc.select("ol > li.id");
        Elements pwd = doc.select("ol > li.pwd");
        Elements nikName = doc.select("ol > li.nikName");
        Elements photo = doc.select("ol > li.photo");
        for(int i = 0; i < result.size(); i++){
            if(result.get(0).text().equals("성공")){
                makeToast("로그인 성공");
                Log.d(TAG,"id="+userId.get(i).text()+",,,pwd="+pwd.get(i).text()+",,,nikName="+nikName.get(i).text()+",,,photo="+photo.get(i).text());
                user = new User().setUserId(userId.get(i).text())
                                        .setName(nikName.get(i).text())
                                        .setPhoto(photo.get(i).text());
                CacheUtils.setUserCache(user);

                SharedPreferences share = getSharedPreferences("Login",Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = share.edit();
                editor.putString("name",nikName.get(i).text());
                editor.putString("photo",photo.get(i).text());
                editor.commit();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                createDatabase(user.getUserId());
//                Intent intent = new Intent(getApplicationContext(), ChattingActivity.class);
                this.finish();
                startActivity(intent);
            }else {
                makeToast("로그인 실패");
            }
        }
    }
    public void createDatabase(String dbName){
        dbHelper = new DatabaseHelper(this, dbName);
        database = dbHelper.getWritableDatabase();
        Log.d(TAG,database+"데이터 베이스 생성됨");
        createTable();
    }
    public void createTable(){
        if(database == null){
            Log.d(TAG,"데이터베이스를 먼저 생성하세요 !!");
            return;
        }
        Cursor cursor = database.rawQuery("select _id from myInfo",null);
        int recordCount = cursor.getCount();
        if(recordCount < 1){
            database.execSQL("insert into "+"myInfo"
                    +" (_id, nikName, photo) "
                    + " values "
                    + "('"+user.getUserId()+"','"+user.getName()+"','"+user.getPhoto()+"')");
        }
        cursor.close();
    }

}