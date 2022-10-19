package com.example.wetalkclient.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.wetalkclient.R;
import com.example.wetalkclient.bean.ChatData;
import com.example.wetalkclient.bean.Message;
import com.example.wetalkclient.constant.Constants;
import com.example.wetalkclient.constant.Signals;
import com.example.wetalkclient.db.CacheUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ChattingActivity extends AppCompatActivity {
    EditText editText;
    EditText editText2;
    TextView textView;
    TextView textView2;
    TextView textView4;

    Handler handler = new Handler();
    Handler handler2 = new Handler();
//    ObjectInputStream in = null ;
//    ObjectOutputStream out = null;

    BufferedReader i ;
    PrintWriter o;
    ArrayList<String> userList;
    Socket socket;
    private String delim1 = "/@";
    private String delim2 = "/&-";
    private String delim3 = "/#-";

    private Message message = new Message();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatting);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        editText =findViewById(R.id.editText);
        editText2 =findViewById(R.id.editText2);
        textView = findViewById(R.id.textView);
        textView2 = findViewById(R.id.textView2);
        textView4 = findViewById(R.id.textView4);
        Button button = findViewById(R.id.button);
        editText.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        new Thread(new Runnable() {
            @Override
            public void run() {
                ConnSocket(8888);

            }
        }).start();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = editText.getText().toString();
                String toId = editText2.getText().toString();
                send(msg, toId);
            }
        });
    }
    public void send(String msg,String toId){
//        ChatData.MSG message = new ChatData.MSG().setFromId("ss")
//                                                 .setMsg(msg)
//                                                 .setToId("dd")
//                                                .setNikName("나 ss야")
//                                                .setType(ChatData.Type.CHATTING_MSG);

//        ChatData.MSG message = new ChatData.MSG().setFromId("dd")
//                .setMsg(msg)
//                .setToId("ss")
//                .setNikName("나 dd야")
//                .setType(ChatData.Type.CHATTING_MSG);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String sendMsg = Signals.MSG.getSigal() + delim1
                        + CacheUtils.getUserCache().getUserId() + delim2
                + toId + delim2
                + msg + delim2
                + df.format(new Date()) + delim2
                + CacheUtils.getUserCache().getPhoto() + delim2
                + CacheUtils.getUserCache().getName();


//        printlnObj(message);
        o.println(sendMsg);
        o.flush();
        editText.setText("");
        editText2.setText("");
    }

    public synchronized void ConnSocket(int port){
        try{
            socket = new Socket(Constants.SERVER_IP, port);
            InputStream ins = socket.getInputStream();
            OutputStream os = socket.getOutputStream();
            i = new BufferedReader(new InputStreamReader(ins));
            o = new PrintWriter(new OutputStreamWriter(os),true);
            Log.d("*************","InputStream "+i.toString());
            Log.d("*************","OutputStream "+o.toString());
            o.println(Signals.CHECK_IN.getSigal()+"/@"+ CacheUtils.getUserCache().getUserId());
            o.flush();
            while (true){
            Log.d("*************","while 문 안");
                String line = i.readLine();
                println(line);
            }

//            OutputStream outputStream = socket.getOutputStream();
////            Log.d("*************",inputStream.toString());
////            Log.d("*************",outputStream.toString());
//            out = new ObjectOutputStream(outputStream);
//            ChatData.ID user = new ChatData.ID();
////            user.setUserId("ss");
//            user.setUserId("dd");
//            out.writeObject(user);
//            out.flush();
//            out.reset();
//            in = new ObjectInputStream(socket.getInputStream());

//            BufferedReader i = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//            while (true){
//                Log.d("7888888888888888888","while문 안");
////                Log.d("66666666666666666",in.toString());
////                Object obj = in.readObject();
//                Log.d("rrrrrrrrrrrrrrrrrrrrrrr",i.readLine()+"읽음");
////                if(obj instanceof ChatData.MSG){
////                    ChatData.MSG msg = (ChatData.MSG)obj;
////                    switch (msg.getType()){
////                        case CHATTING_MSG:
////                            println(msg.getFromId()+" -- " + msg.getMsg()+"\n");
////                            break;
////                    }
////                }
//            }
        }catch (IOException e){
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void printlnObj(ChatData.MSG data){
        handler2.post(new Runnable() {
            @Override
            public void run() {
                try{
//                    Log.d("^^^^^^^^^^^^^^^0",out.toString());
//                    out.writeObject(data);
//                    out.flush();
//                    out.reset();
                    editText.setText("");
                    textView.append(data.getFromId()+" - "+data.getMsg());
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    public void println(String data){
        handler.post(new Runnable() {
            @Override
            public void run() {
                message.setAllNull();
                String [] datas = data.split(delim1);
                String str = "";
                String str2;
                for(int i = 0 ; i < datas.length ; i++){
                    str = datas[i];
                    Log.d("//////======",str);
                    if(i == 0){
                        message.setSignal(str);
                    }else {
                        String [] datas2 = str.split(delim2);
                        for(int j = 0 ; j < datas2.length; j++){
                            str2 = datas2[j];
                            Log.d("//////********",str2);
                            if(j == 0){
                                message.setFromId(str2);
                            }else if(j == 1){
                                message.setToId(str2);
                            }else if(j == 2){
                                message.setMsg(str2);
                            }else if(j == 3){
                                message.setTime(str2);
                            }else if(j == 4){
                                message.setPhoto(str2);
                            }else if(j == 5){
                                message.setNikName(str2);
                            }
                        }
                    }
                }
                textView.append(message.getFromId()+"가 보낸 메세지 : "+message.getMsg()+" - "+message.getTime()+"\n");
            }
        });
    }
}