package com.example.wetalkclient.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.wetalkclient.MainActivity;
import com.example.wetalkclient.R;
import com.example.wetalkclient.bean.User;
import com.example.wetalkclient.constant.Constants;
import com.example.wetalkclient.db.CacheUtils;
import com.example.wetalkclient.http.HttpCallBackListener;
import com.example.wetalkclient.http.HttpUtils;

import org.apache.http.conn.ConnectTimeoutException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.net.SocketTimeoutException;
import java.util.regex.Pattern;

public class NikNameChange extends AppCompatActivity implements HttpCallBackListener {
    private static final String TAG = NikNameChange.class.getSimpleName();
    private EditText editText;
    private Button button;
    private ProgressDialog prgDialog;
    private String nikName;
    private static final String NAME_EXP = "[A-z||0-9||가-힣][A-z||0-9||가-힣_\\-]{4,10}$";
    private static final String NAME_EXP_MSG = "5~10자의 영문 대/소문자, 한글, 숫자와 특수기호(_),(-)만 사용 가능합니다.";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nik_name_change);

        prgDialog= new ProgressDialog(this);
        prgDialog.setCancelable(false);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        findViwes();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nikName = editText.getText().toString();
                // 여기 공백체크 패턴체크
                if(nikName.equals("")){
                    makeToast("내용이 없습니다");
                }else if(!Pattern.matches(NAME_EXP,nikName)) {
                    makeToast(NAME_EXP_MSG);
                }else {
                    String params = "nikName="+nikName+"&id="+ CacheUtils.getUserCache().getUserId();
                    try {
                        HttpUtils.sendRequest(Constants.ID.MODIFY_USERINFO,params,NikNameChange.this,getApplicationContext());
                        prgDialog.setMessage("닉네임 수정 중...");
                    } catch (ConnectTimeoutException e) {
                        Log.d(TAG,"인터넷 연결시간이 초과되었습니다.");
                    }catch (SocketTimeoutException e){
                        Log.d(TAG,"System is Busy");
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        });
    }
    public void findViwes(){
        editText = findViewById(R.id.editText2);
        button = findViewById(R.id.button8);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("fragment",2);
                this.finish();
                startActivity(intent);
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void httpCallBack(int id, String response) {
        Document doc = Jsoup.parse(response);
        Elements resultChange= doc.select("p.result");
        if(id == Constants.ID.MODIFY_USERINFO){
            if(resultChange.get(0).text().equals("true")){
                prgDialog.hide();
                User user = new User().setUserId(CacheUtils.getUserCache().getUserId())
                        .setName(nikName)
                        .setPhoto(CacheUtils.getUserCache().getPhoto());
                CacheUtils.setUserCache(user);
                makeToast("닉네임 수정 성공!!");
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("fragment",2);
                this.finish();
                startActivity(intent);
            }
        }else {
            prgDialog.hide();
            makeToast("수정 실패!!");
        }

    }
    // 토스트 띄우기
    public void makeToast(String data ){
        Toast.makeText(this,data, Toast.LENGTH_LONG).show();
    }


    @Override
    protected void onDestroy() {
        if(prgDialog != null) {
            prgDialog.dismiss();
        }
        super.onDestroy();
    }
}