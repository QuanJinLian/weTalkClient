package com.example.wetalkclient.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wetalkclient.R;
import com.example.wetalkclient.bean.ChattingMsg;
import com.example.wetalkclient.bean.Message;
import com.example.wetalkclient.bean.User;
import com.example.wetalkclient.constant.Constants;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class ChattingListAdapter extends RecyclerView.Adapter<ChattingListAdapter.ViewHolder> {
    ArrayList<ChattingMsg> items = new ArrayList<ChattingMsg>();
    public interface OnItemClickListener{
        void onClick(int position);
    }
    private ChattingListAdapter.OnItemClickListener listener;

    public void setOnItemClickLIstener(ChattingListAdapter.OnItemClickListener listener){
        this.listener = listener;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.chatting_item, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChattingMsg item = items.get(position);
        holder.setItem(item);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener != null){
                    listener.onClick(position);
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        return items.size();
    }
    public void addItem(ChattingMsg item){
        items.add(item);
    }
    public void clearItems(){
        items.clear();
    }


    static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView imageView;
        TextView nikName;
        TextView time;
        TextView message;
        TextView unread;
        private SimpleDateFormat df = new SimpleDateFormat("yy-M-d H:m");

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.chattingRoomImg);
            nikName = itemView.findViewById(R.id.roomNikName);
            time = itemView.findViewById(R.id.chattingLastTime);
            message = itemView.findViewById(R.id.chattingLastmsg);
            unread = itemView.findViewById(R.id.unread);
        }
        public void setItem(ChattingMsg msg ){
            if(msg.getPhoto() == null){
                if (msg.getRoomId().startsWith("GroupChatting:20")){
                    imageView.setImageResource(R.drawable.teamphoto);
                    nikName.setTextSize(14);
                }else {
                    imageView.setImageResource(R.drawable.ic_baseline_person_24);
                    nikName.setTextSize(14);
                }
            }else {
                String photo = Constants.HTTP_SERVER_IP + "/webServerProject/upload/"+msg.getPhoto()+".jpg";  URL url = null;
                try {
                    url = new URL(photo);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                Bitmap bitmap = getRemoteImage(url);
                imageView.setImageBitmap(bitmap);
                nikName.setTextSize(20);
            }

            nikName.setText(msg.getNikName());
            Timestamp timestamp = Timestamp.valueOf(msg.getTime());
            time.setText(df.format(timestamp));
            message.setText(msg.getMsg());

            if(msg.getUnread()>10){
                unread.setText("...");
            }else {
                if(msg.getUnread()>0){
                    unread.setText(msg.getUnread()+"");
                }else {
                    unread.setText("");
                    unread.setBackgroundColor(Color.WHITE);
                }
            }
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
}
