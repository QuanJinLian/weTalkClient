package com.example.wetalkclient.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wetalkclient.R;
import com.example.wetalkclient.constant.Constants;
import com.example.wetalkclient.views.ViewUtils;
import com.example.wetalkclient.http.HttpCallBackListener;
import com.example.wetalkclient.http.HttpUtils;
import com.loopj.android.http.RequestParams;

import org.apache.http.conn.ConnectTimeoutException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Pattern;

public class Register extends AppCompatActivity implements HttpCallBackListener{
    private static final String TAG = Register.class.getSimpleName();
    private EditText idEdit;
    private EditText nikNameEdit;
    private EditText pwdEdit;
    private EditText pwd2Edit;
    private TextView idCheckTextView;
    private TextView nameCheckTextView;
    private TextView pwdCheckTextView;
    private TextView pwd2CheckTextView;
    private ImageView profile;
    private Button registerBnt;
    private Button loginBnt;
    private Button selectPhoto;
    private ScrollView layout;

    private String id;
    private String pwd;
    private String pwd2;
    private String nikName;
    private String photo;
    private int RESULT_LOAD_IMG = 1;
    private String imgPath;
    private boolean idCheck;
    private boolean nikNameCheck;
    private boolean pwdCheck;
    private boolean pwd2Check;
    private ProgressDialog prgDialog;
    private RequestParams params = new RequestParams();
    private Bitmap bitmap;
    SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");

    private static final String NO_EMPTY = "필수 입력 사항입니다.";
    private static final String EMAIL_EXP = "\\w+@\\w+\\.\\w+(\\.\\w+)?$";
    private static final String EMAIL_EXP_MSG = "example@example.com 같이 입력해 주세요.";
    private static final String EMAIL_CANT_MSG = "중복된 아이디 입니다";
    private static final String EMAIL_CAN_MSG = "멋진 아이디 입니다";
    private static final String NAME_EXP = "[A-z||0-9||가-힣][A-z||0-9||가-힣_\\-]{4,10}$";
    private static final String NAME_EXP_MSG = "5~10자의 영문 대/소문자, 한글, 숫자와 특수기호(_),(-)만 사용 가능합니다.";
    private static final String PWD_EXP = "[A-z0-9_\\-]{6,16}$";
    private static final String PWD_EXP_MSG = "6~16자의 영문 대/소문자, 숫자와 특수기호(_),(-)만 사용 가능합니다.";
    private static final String PWD_NOTSAME_MSG = "비밀번호가 동일하지 않습니다. 다시 확인해 주세요.";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setTitle("WeTalk 회원가입");
        findViews();

        prgDialog= new ProgressDialog(this);
        prgDialog.setCancelable(false);

        
        // 사진 선택 버튼 클릭 이벤트
        selectPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String [] permissions= {
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                };
                checkPermissions(permissions);
            }
        });
        // 로그인 버튼 클릭 이벤트( 로그인 페이지로 이동)
        loginBnt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginIntent = new Intent(getApplicationContext(),Login.class);
                ((Activity)getApplicationContext()).finish();
                startActivity(loginIntent);
            }
        });

        // 회원가입 버튼 이벤트 ( 회원가입 진행)
        registerBnt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(idCheck == true && nikNameCheck == true && pwdCheck == true && pwd2Check == true && imgPath != null){
                    uploadImage();
                }else if(idCheck == false){
                    makeToast("아이디를 다시 확인해주세요.");
                }else if(nikNameCheck == false){
                    makeToast("닉네임을 다시 확인해주세요.");
                }else if(pwdCheck == false || pwd2Check == false ){
                    makeToast("비밀번호를 다시 확인해주세요.");
                }else if(imgPath == null){
                    makeToast("사진을 선택해 주세요");
                }
            }
        });

        // 정규식 체크를 위한 레이아웃 터치 이벤트
        layout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    EditTextCheck();
                    return true;
                }
                return false;
            }
        });

    }

    // EditText 정규식 체크
    public void EditTextCheck(){
//        makeToast("여기 들어오나?");
        id = idEdit.getText().toString();
        nikName = nikNameEdit.getText().toString();
        pwd = pwdEdit.getText().toString();
        pwd2 = pwd2Edit.getText().toString();
        if(id.equals("")){
            idCheckTextView.setText(NO_EMPTY);
            idCheck = false;
        }else if(!Pattern.matches(EMAIL_EXP,id)){
            idCheckTextView.setText(EMAIL_EXP_MSG);
            idCheck = false;
        }else {
            idCheck(id);
//            idCheckTextView.setText("");
//            idCheck = true;
        }
        if(nikName.equals("")){
            nameCheckTextView.setText(NO_EMPTY);
            nikNameCheck = false;
        } else if (!Pattern.matches(NAME_EXP,nikName)) {
            nameCheckTextView.setText(NAME_EXP_MSG);
            nikNameCheck = false;
        }else {
            nameCheckTextView.setText("");
            nikNameCheck = true;
        }
        if(pwd.equals("")){
            pwdCheckTextView.setText(NO_EMPTY);
            pwdCheck = false;
        }else if(!Pattern.matches(PWD_EXP,pwd)){
            pwdCheckTextView.setText(PWD_EXP_MSG);
            pwdCheck = false;
        }else{
            pwdCheckTextView.setText("");
            pwdCheck = true;
        }
        if(pwd2.equals("")){
            pwd2CheckTextView.setText(NO_EMPTY);
            pwd2Check = false;
        }else if(!pwd.equals(pwd2)){
            pwd2CheckTextView.setText(PWD_NOTSAME_MSG);
            pwd2Check = false;
        }else{
            pwd2CheckTextView.setText("");
            pwd2Check = true;
        }
    }

    public void idCheck(String id){
        String data = "id="+id;
        try {
            HttpUtils.sendRequest(Constants.ID.CHECK_CODE,data, Register.this,this);
        } catch (ConnectTimeoutException e) {
            Log.d(TAG,"인터넷 연결시간이 초과되었습니다.");
        }catch (SocketTimeoutException e){
            Log.d(TAG,"System is Busy");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void register(){
        String data = "id="+id+"&pwd="+pwd+"&nikName="+nikName+"&photo="+photo;
        try {
            HttpUtils.sendRequest(Constants.ID.REGISTER,data, Register.this,this);
        } catch (ConnectTimeoutException e) {
            Log.d(TAG,"인터넷 연결시간이 초과되었습니다.");
        }catch (SocketTimeoutException e){
            Log.d(TAG,"System is Busy");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void httpCallBack(int id, String response) {
//        Log.d(TAG,"id---"+id+"+++++response----"+response);
        Document doc = Jsoup.parse(response);
        Elements result = doc.select("ol > li.result");
        if(id == Constants.ID.CHECK_CODE){
            for(int i = 0; i < result.size(); i++){
                Log.d(TAG,"Constants.ID.CHECK_CODE---result="+result.get(i).text());
                if(result.get(i).text().equals("true")){
                    idCheckTextView.setText(EMAIL_CANT_MSG);
                    idCheck = false;
                }else if(result.get(i).text().equals("false")){
                    idCheckTextView.setTextColor(Color.GREEN);
                    idCheckTextView.setText(EMAIL_CAN_MSG);
                    idCheck = true;
                    idEdit.setEnabled(false);
                }
            }
        }else if(id == Constants.ID.REGISTER){
            for(int i = 0 ; i < result.size(); i++){
                int resultCnt = Integer.parseInt(result.get(i).text());
                Log.d(TAG,"Constants.ID.REGISTER---result="+resultCnt);
                if(resultCnt > 0){
                    new AlertDialog.Builder(this)
                            .setMessage(nikName+"님 가입 성공 하였습니다\n 로그인 페이지로 넘어 갑니다.")
                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(getApplicationContext(),Login.class);
                                    ((Activity)getApplicationContext()).finish();
                                    startActivity(intent);
                                }
                            }).show();
                }else {
                    makeToast("무슨 원인인지 모르겠지만 가입 실패");
                }
            }
        }else if(id == Constants.ID.UPLOADIMAGE){
            for(int i = 0 ; i < result.size(); i++){
                int resultCnt = Integer.parseInt(result.get(i).text());
                Log.d(TAG,"Constants.ID.UPLOADIMAGE---result="+resultCnt);
                if(resultCnt == 1000){
                    prgDialog.hide();
                    makeToast("이미지 업로드 성공");
                    register();
                }else if(resultCnt == 1001){
                    prgDialog.hide();
                    makeToast("이미지 없음 imgEncodeStr null값");
                }else if(resultCnt == 1002){
                    prgDialog.hide();
                    makeToast("이미지 업로드 경로를 못찾습니다 (서버의 주소 문제)");
                }else if(resultCnt == 1003){
                    prgDialog.hide();
                    makeToast("IOException 났음 에러는 서버 콘솔에서 봐");
                }
            }
        }
    }

    // 권한 체크, 권한 있으면 갤러리로 이동
    public void checkPermissions(String [] permissions){
        ArrayList<String> targetList = new ArrayList<>();
        for(int i = 0 ; i < permissions.length; i++){
            String curPermission = permissions[i];
            Log.d(TAG,"curPermission---"+curPermission);
            int permissionCheck = ContextCompat.checkSelfPermission(this,curPermission);
            if(permissionCheck == PackageManager.PERMISSION_GRANTED){
//              curPermission 권한 있음
                if(curPermission.equals(permissions[0])){
                    loadImage();
                }
            }else {
                makeToast(curPermission+"권한 없음");
                if(ActivityCompat.shouldShowRequestPermissionRationale(this,curPermission)){
                    makeToast("권한 설명 필요함");
                }else {
                    targetList.add(curPermission);
                }
            }
        }
        String [] targets = new String[targetList.size()];
        Log.d(TAG,"타겟 사이즈---"+targetList.size());
        targetList.toArray(targets);
        if(targetList.size() >0){
            ActivityCompat.requestPermissions(this,targets,101);
        }
        
    }

// 갤러리 접근해서 사진 선택하기
    public void loadImage() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
    }

// 사진 선택 후 이미지뷰에 보여주기
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try{
            if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK && null != data) {
                Log.d(TAG,"-------------requestCode->"+requestCode+",  resultCode->"+resultCode+", data->"+data.toString());
                Uri selectedImage = data.getData();
                String[] filePathColumn = { MediaStore.Images.Media.DATA };

                // 获取游标
                Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                cursor.moveToFirst();
                // 안드로이드   Q버전 부터 설정 해줘야 함
                if (Build.VERSION.SDK_INT >= 29) {
                    try (ParcelFileDescriptor pfd = this.getContentResolver().openFileDescriptor(selectedImage, "r")) {
                        if (pfd != null) {
                            bitmap = BitmapFactory.decodeFileDescriptor(pfd.getFileDescriptor());
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                imgPath = cursor.getString(columnIndex);
                cursor.close();
//                Bitmap bitmap = BitmapFactory.decodeFile(imgPath);
                profile.setImageBitmap(bitmap);
//                photo = ViewUtils.BitmapToString(bitmap);
//                Drawable drawable = Drawable.createFromPath(imgPath);
//                profile.setImageDrawable(drawable);
//                photo = ViewUtils.DrawableToString(profile.getDrawable());
//                Log.d(TAG,"--->"+photo);
                Log.d("ddddddddddddddd","imgView set완료"+imgPath);
            }else{
                makeToast("사진을 선택해 주세요!");
            }
        }catch (Exception e){
            e.printStackTrace();
            makeToast("뭔가 잘못 되었음, 다시 시도해 보세요.");
        }
    }

    // 권한 선택후 토스트로 보여주기
    @Override
    public void onRequestPermissionsResult(int requestCode, String permission[],int[] grantResults){
        switch (requestCode){
            case 101:
                if(grantResults.length > 0&& grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    makeToast("권한을 승인함");
                }else{
                    makeToast("권한을 거부함");
                }
                return;
        }
    }
    // 토스트 띄우기
    public void makeToast(String data ){
        Toast.makeText(this,data, Toast.LENGTH_LONG).show();
    }

    // 뷰들 찾아주기
    private void findViews(){
        layout = findViewById(R.id.layout);
        idEdit = findViewById(R.id.editText);
        nikNameEdit = findViewById(R.id.editText4);
        pwdEdit = findViewById(R.id.editText2);
        pwd2Edit = findViewById(R.id.editText3);
        profile = findViewById(R.id.imageView);
        registerBnt = findViewById(R.id.button);
        loginBnt = findViewById(R.id.button2);
        selectPhoto = findViewById(R.id.button3);
        idCheckTextView = findViewById(R.id.textView4);
        nameCheckTextView = findViewById(R.id.textView8);
        pwdCheckTextView = findViewById(R.id.textView5);
        pwd2CheckTextView = findViewById(R.id.textView6);
    }


    //开始上传图片
    private void uploadImage() {
        if (imgPath != null && !imgPath.isEmpty()) {
            prgDialog.setMessage("사진 인코딩 중...");
            prgDialog.show();
            String encodedString = null;
//            encodeImagetoString();
            try{
                encodedString = ViewUtils.encodeImagetoString(imgPath);
            }catch (UnsupportedEncodingException e){
                makeToast(e.toString());
            }
            photo = nikName+df.format(new Date());
            String params = "image="+encodedString+"&filename="+photo;
//            Log.d(TAG,"image="+encodedString+"&filename="+photo);
            try {
                HttpUtils.sendRequest(Constants.ID.UPLOADIMAGE,params,Register.this,this);
                prgDialog.setMessage("업로드 중 입니다.");
            } catch (ConnectTimeoutException e) {
                Log.d(TAG,"인터넷 연결시간이 초과되었습니다.");
            }catch (SocketTimeoutException e){
                Log.d(TAG,"System is Busy");
            }catch (Exception e){
                e.printStackTrace();
            }
        } else {
            makeToast("사진을 선택해 주세요!");
        }
    }


//    public void encodeImagetoString() {
//        new AsyncTask<Void, Void, String>() {
//
//            protected void onPreExecute() {
//
//            };
//
//            @Override
//            protected String doInBackground(Void... params) {
//                BitmapFactory.Options options = null;
//                options = new BitmapFactory.Options();
//                options.inSampleSize = 3;
//                bitmap = BitmapFactory.decodeFile(imgPath,
//                        options);
//                ByteArrayOutputStream stream = new ByteArrayOutputStream();
//                // 压缩图片
//                bitmap.compress(Bitmap.CompressFormat.PNG, 50, stream);
//                byte[] byte_arr = stream.toByteArray();
//                // Base64图片转码为String
//                encodedString = Base64.encodeToString(byte_arr, 0);
//                return "";
//
//            }
//
//            @Override
//            protected void onPostExecute(String msg) {
//                prgDialog.setMessage("Calling Upload");
//
//                SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
//                photo = nikName+df.format(new Date());
//                // 将转换后的图片添加到上传的参数中
//                params.put("image", encodedString);
//                params.put("filename",photo);
////                Log.d("ddddddddddddddd","imgView set완료"+photo+"dddencodedString: "+encodedString);
////                params.put("filename", editTextName.getText().toString());
//                // 上传图片
//                imageUpload();
//            }
//        }.execute(null, null, null);
//    }
//    public void imageUpload() {
//        prgDialog.setMessage("업로드 중 입니다.");
//        String url = Constants.Register.UPLOADIMAGE_URL;
//        AsyncHttpClient client = new AsyncHttpClient();
//        client.post(url, params, new AsyncHttpResponseHandler() {
//            @Override
//            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
//                prgDialog.hide();
//                Toast.makeText(getApplicationContext(), "upload success", Toast.LENGTH_LONG).show();
//                register();
//            }
//
//
//            @Override
//            public void onFailure(int statusCode, Header[] headers, byte[] bytes, Throwable throwable) {
//                prgDialog.hide();
//                if (statusCode == 404) {
//                    Toast.makeText(getApplicationContext(),
//                            "Requested resource not found", Toast.LENGTH_LONG).show();
//                }
//                // 当 Http 响应码'500'
//                else if (statusCode == 500) {
//                    Toast.makeText(getApplicationContext(),
//                            "Something went wrong at server end", Toast.LENGTH_LONG).show();
//                }
//                // 当 Http 响应码 404, 500
//                else {
//                    Toast.makeText(
//                            getApplicationContext(), "Error Occured n Most Common Error: n1. Device " +
//                                    "not connected to Internetn2. Web App is not deployed in App servern3." +
//                                    " App server is not runningn HTTP Status code : "
//                                    + statusCode, Toast.LENGTH_LONG).show();
//                }
//
//            }
//        });
//    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (prgDialog != null) {
            prgDialog .dismiss();
        }
    }
}