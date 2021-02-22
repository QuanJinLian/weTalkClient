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
import com.example.wetalkclient.bean.AddFriend;
import com.example.wetalkclient.constant.Constants;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class AddFriendAdapter extends RecyclerView.Adapter<AddFriendAdapter.ViewHolder> {
    ArrayList<AddFriend> items = new ArrayList<>();
    public interface OnItemClickListener{
        void onClick(int position);
    }
    private OnItemClickListener listener;

    public void setOnItemClickLIstener(OnItemClickListener listener){
        this.listener = listener;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.addfriend_item, parent, false);


        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AddFriend item = items.get(position);
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
    public void addItem(AddFriend item){
        items.add(item);
    }
    public void clearItems(){
        items.clear();
    }


    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView textView1;
        TextView textView2;
        TextView textView3;
        ImageView imageView;
        private SimpleDateFormat df = new SimpleDateFormat("yy-M-d H:m");

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView1 = itemView.findViewById(R.id.friendName1);
            imageView = itemView.findViewById(R.id.friendimageView1);
            textView2 = itemView.findViewById(R.id.agreeOrNot);
            textView3 = itemView.findViewById(R.id.textView12);
        }
        public void setItem(AddFriend user){
            textView1.setText(user.getFromId_nikName());
            String photo = Constants.HTTP_SERVER_IP+ "/webServerProject/upload/"+user.getFromId_photo()+".jpg";
            URL url = null;
            try{
                url = new URL(photo);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            Bitmap bitmap = getRemoteImage(url);
            imageView.setImageBitmap(bitmap);
            if(user.getAgree().equals("true")){
                textView2.setText("이미 수락");
                textView2.setTextColor(Color.LTGRAY);
            }else {
                textView2.setText("수락 하기");
                textView2.setTextColor(Color.GREEN);
            }
            Timestamp timestamp = Timestamp.valueOf(user.getDate());
            textView3.setText(df.format(timestamp));

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
