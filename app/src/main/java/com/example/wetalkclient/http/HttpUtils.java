package com.example.wetalkclient.http;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.example.wetalkclient.constant.Constants;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;

public class HttpUtils {
    private static String TAG = HttpUtils.class.getSimpleName();
    private static RequestQueue requestQueue;
    public static void sendRequest(int id, String data, HttpCallBackListener listener, Context context) throws SocketTimeoutException, Exception{
        String urlStr = null;
        StringBuilder output = new StringBuilder();
        switch (id){
            case Constants.ID.CHECK_CODE:
                urlStr = Constants.Register.IDCHECK_CODE_URL;
                break;
            case Constants.ID.REGISTER:
                urlStr = Constants.Register.REGISTER_URL;
                break;
            case Constants.ID.UPLOADIMAGE:
                urlStr = Constants.Register.UPLOADIMAGE_URL;
                break;
            case Constants.ID.LOGIN:
                urlStr = Constants.Login.LOGIN_URL;
                break;
            case Constants.ID.MODIFY_USERINFO:
                urlStr = Constants.UserInfo.MODIFY_USERINFO_URL;
                break;
            case Constants.ID.MODIFY_PWD:
                urlStr = Constants.UserInfo.MODIFY_PWD_URL;
                break;
            case Constants.ID.GET_FRIEND_LIST:
                urlStr = Constants.GetFriendList.MODIFY_USERINFO_URL;
                break;
            case Constants.ID.SEARCH_USER:
                urlStr = Constants.AddFriend.SEARCH_USER_URL;
                break;
            default:
                Log.e(TAG, "해당 아이디는 존재하지 않습니다.");
                return;
        }
        Log.d(TAG,"url = " + urlStr);
        try{
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            if(conn != null){
                conn.setConnectTimeout(10000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                OutputStream outputStream = conn.getOutputStream();
                outputStream.write(data.getBytes("UTF-8"));
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line = null;
                while (true){
                    line = reader.readLine();
                    if(line == null){
                        break;
                    }
                    output.append(line+"\n");
                }
                reader.close();
                conn.disconnect();
                String response = output.toString();
                listener.httpCallBack(id, response);
            }else {
                Log.d(TAG, "연결실패");
            }
        }catch (SocketTimeoutException e){
            Toast.makeText(context, "서버가 바쁘거나 꺼져 있음", Toast.LENGTH_SHORT).show();
        }catch (ConnectException e){
            Toast.makeText(context, "인터넷 연결시간이 초과되었습니다.", Toast.LENGTH_SHORT).show();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}