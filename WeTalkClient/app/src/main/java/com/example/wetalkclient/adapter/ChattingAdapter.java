package com.example.wetalkclient.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.wetalkclient.R;
import com.example.wetalkclient.bean.ChattingMsg;
import com.example.wetalkclient.bean.Message;
import com.example.wetalkclient.constant.Constants;
import com.example.wetalkclient.db.CacheUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class ChattingAdapter extends BaseAdapter {
    private ArrayList<ChattingMsg> chattingMsgs = null;
    private Context context;
    private String photoUrl;
    private URL url;
    private Bitmap bitmap;
    private SimpleDateFormat df = new SimpleDateFormat("yy-M-d H:m");

    public ChattingAdapter (Context context, ArrayList<ChattingMsg> chattingMsgs){
        this.context = context;
        this.chattingMsgs = chattingMsgs;
    }


    @Override
    public int getCount() {
        return chattingMsgs.size();
    }

    @Override
    public Object getItem(int position) {
        return chattingMsgs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder viewHolder = null;
        ChattingMsg message = chattingMsgs.get(position);
//        if(view == null){
        viewHolder = new ViewHolder();
        if(message.getFriendId().equals(CacheUtils.getUserCache().getUserId())){
            view = LayoutInflater.from(context).inflate(R.layout.listview_item_chatting_send,null);
            viewHolder.imageView = view.findViewById(R.id.chatting_send_photo);
            viewHolder.tvTime = view.findViewById(R.id.chatting_send_time);
            viewHolder.tvMsg = view.findViewById(R.id.chatting_send_msg);
            viewHolder.tvName = null;

        }else {
            view = LayoutInflater.from(context).inflate(R.layout.listview_item_chatting_receive,null);
            viewHolder.imageView = view.findViewById(R.id.chatting_receive_photo);
            viewHolder.tvTime = view.findViewById(R.id.chatting_receive_time);
            viewHolder.tvMsg = view.findViewById(R.id.chatting_receive_msg);
            viewHolder.tvName = view.findViewById(R.id.chatting_receive_nikname);
        }
        view.setTag(viewHolder);
//        }else {
//            viewHolder = (ViewHolder) view.getTag();
//        }
        if(message.getFriendId().equals(CacheUtils.getUserCache().getUserId())){
            if(CacheUtils.getUserCache().getPhoto() == null){
                viewHolder.imageView.setImageResource(R.drawable.ic_baseline_person_24);
            }else {
                photoUrl = Constants.HTTP_SERVER_IP+ "/webServerProject/upload/"+CacheUtils.getUserCache().getPhoto()+".jpg";
                try{
                    url = new URL(photoUrl);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                bitmap = getRemoteImage(url);
                viewHolder.imageView.setImageBitmap(bitmap);
            }
        }else {
            if(message.getPhoto() == null){
                viewHolder.imageView.setImageResource(R.drawable.ic_baseline_person_24);
            }else {
                photoUrl = Constants.HTTP_SERVER_IP+ "/webServerProject/upload/"+message.getPhoto()+".jpg";

                try{
                    url = new URL(photoUrl);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                bitmap = getRemoteImage(url);
                if(bitmap != null){
                    viewHolder.imageView.setImageBitmap(bitmap);
                }else {
                    viewHolder.imageView.setImageResource(R.drawable.ic_baseline_person_24);
                }
            }
        }
        viewHolder.tvMsg.setText(message.getMsg());
        Timestamp timestamp = Timestamp.valueOf(message.getTime());
        viewHolder.tvTime.setText(df.format(timestamp));
        if(viewHolder.tvName != null){
            if(message.getPhoto()==null){
                viewHolder.tvName.setText(message.getNikName()+" (친구아님)");
            }else {
                viewHolder.tvName.setText(message.getNikName());
            }
        }

        return view;
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
    static class ViewHolder{
        ImageView imageView;
        TextView tvTime;
        TextView tvMsg;
        TextView tvName;
    }

}
