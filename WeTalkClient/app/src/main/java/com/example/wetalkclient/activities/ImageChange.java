package com.example.wetalkclient.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.wetalkclient.MainActivity;
import com.example.wetalkclient.R;
import com.example.wetalkclient.bean.User;
import com.example.wetalkclient.constant.Constants;
import com.example.wetalkclient.db.CacheUtils;
import com.example.wetalkclient.http.HttpCallBackListener;
import com.example.wetalkclient.http.HttpUtils;
import com.example.wetalkclient.views.ViewUtils;

import org.apache.http.conn.ConnectTimeoutException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ImageChange extends AppCompatActivity implements HttpCallBackListener {
    private static final String TAG = ImageChange.class.getSimpleName();
    private int RESULT_LOAD_IMG = 1;
    private ImageView imageView;
    private Button button;
    private URL url;
    private Bitmap bitmap;
    private String imgPath;
    private ProgressDialog prgDialog;
    private SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
    private String photo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_change);

        prgDialog= new ProgressDialog(this);
        prgDialog.setCancelable(false);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        findViwes();
        String photoUrl = Constants.HTTP_SERVER_IP+ "/webServerProject/upload/"+ CacheUtils.getUserCache().getPhoto()+".jpg";
        try{
            url = new URL(photoUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        bitmap = getRemoteImage(url);
        imageView.setImageBitmap(bitmap);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String [] permissions= {
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                };
                checkPermissions(permissions);
            }
        });
    }

    public void findViwes(){
        imageView = findViewById(R.id.imageView4);
        button = findViewById(R.id.button7);
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
                imageView.setImageBitmap(bitmap);
                uploadImage(imgPath);
            }else{
                makeToast("사진을 선택해 주세요!");
            }
        }catch (Exception e){
            e.printStackTrace();
            makeToast("뭔가 잘못 되었음, 다시 시도해 보세요.");
        }
    }

    //开始上传图片
    private void uploadImage(String imgPath) {
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
            photo = CacheUtils.getUserCache().getName()+df.format(new Date());
            String params = "image="+encodedString+"&filename="+photo;
//            Log.d(TAG,"image="+encodedString+"&filename="+photo);
            try {
                HttpUtils.sendRequest(Constants.ID.UPLOADIMAGE,params,ImageChange.this,this);
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


    // 토스트 띄우기
    public void makeToast(String data ){
        Toast.makeText(this,data, Toast.LENGTH_LONG).show();
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



    @Override
    public void httpCallBack(int id, String response) {
        Log.d(TAG,response);
        Document doc = Jsoup.parse(response);
        Elements result = doc.select("ol > li.result");
        Elements resultChange= doc.select("p.result");
        String params2 = "id="+CacheUtils.getUserCache().getUserId()+"&photo="+photo;
        if(id == Constants.ID.UPLOADIMAGE){
            for(int i = 0 ; i < result.size(); i++){
                int resultCnt = Integer.parseInt(result.get(i).text());
                Log.d(TAG,"Constants.ID.UPLOADIMAGE---result="+resultCnt);
                if(resultCnt == 1000){
                    prgDialog.hide();
                    try {
                        HttpUtils.sendRequest(Constants.ID.MODIFY_USERINFO,params2, ImageChange.this,this);
                        prgDialog.setMessage("사진정보 수정 중");
                    } catch (ConnectTimeoutException e) {
                        Log.d(TAG,"인터넷 연결시간이 초과되었습니다.");
                    }catch (SocketTimeoutException e){
                        Log.d(TAG,"System is Busy");
                    }catch (Exception e){
                        e.printStackTrace();
                    }
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
        }else if(id == Constants.ID.MODIFY_USERINFO){
            if(resultChange.get(0).text().equals("true")){
                prgDialog.hide();
                User user = new User().setUserId(CacheUtils.getUserCache().getUserId())
                                        .setName(CacheUtils.getUserCache().getName())
                                        .setPhoto(photo);
                CacheUtils.setUserCache(user);
                SharedPreferences share = getSharedPreferences("Login", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = share.edit();
                editor.putString("photo",photo);
                editor.commit();
                makeToast("이미지 업로드 성공");
            }else {
                prgDialog.hide();
                makeToast("이미지 업로드 실패! 다시 시도해주세요!");
            }
        }
    }

    @Override
    protected void onDestroy() {
        if(prgDialog != null) {
            prgDialog.dismiss();
        }
        super.onDestroy();
    }
}