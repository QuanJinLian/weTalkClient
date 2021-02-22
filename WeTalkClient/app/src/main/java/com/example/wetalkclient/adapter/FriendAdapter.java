package com.example.wetalkclient.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wetalkclient.R;
import com.example.wetalkclient.bean.User;
import com.example.wetalkclient.constant.Constants;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class FriendAdapter extends  RecyclerView.Adapter<FriendAdapter.ViewHolder>  {
    ArrayList<User> items = new ArrayList<>();
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
        View itemView = inflater.inflate(R.layout.friend_item, parent, false);


        return new ViewHolder(itemView);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User item = items.get(position);
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
    public void addItem(User item){
        items.add(item);
    }
    public void clearItems(){
        items.clear();
    }



    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView textView;
        ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.friendName);
            imageView = itemView.findViewById(R.id.friendimageView);
        }
        @RequiresApi(api = Build.VERSION_CODES.O)
        public void setItem(User user){
            textView.setText(user.getName());
            String photo = Constants.HTTP_SERVER_IP + "/webServerProject/upload/"+user.getPhoto()+".jpg";
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
            imageView.setImageBitmap(bitmap);

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
