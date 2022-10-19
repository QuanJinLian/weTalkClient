package com.example.wetalkclient.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.wetalkclient.R;
import com.example.wetalkclient.activities.ImageChange;
import com.example.wetalkclient.activities.Login;
import com.example.wetalkclient.activities.NikNameChange;
import com.example.wetalkclient.bean.Message;
import com.example.wetalkclient.constant.Constants;
import com.example.wetalkclient.constant.Signals;
import com.example.wetalkclient.db.CacheUtils;
import com.example.wetalkclient.tcpConn.MsgUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MyPageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MyPageFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public MyPageFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MyPageFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MyPageFragment newInstance(String param1, String param2) {
        MyPageFragment fragment = new MyPageFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }
    private TextView nikName;
    private ImageView imageView;
    private TextView changeImage;
    private TextView changeNikName;
    private Button button;
    private URL url;
    private Bitmap bitmap;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View viewRoot  = inflater.inflate(R.layout.fragment_my_page, container, false);

        findViews(viewRoot);

        String photoUrl = Constants.HTTP_sERVER_IP+ "/webServerProject/upload/"+ CacheUtils.getUserCache().getPhoto()+".jpg";
        try{
            url = new URL(photoUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        bitmap = getRemoteImage(url);
        imageView.setImageBitmap(bitmap);
        nikName.setText(CacheUtils.getUserCache().getName());
        changeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), ImageChange.class);
                getActivity().finish();
                startActivity(intent);
            }
        });

        changeNikName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), NikNameChange.class);
                getActivity().finish();
                startActivity(intent);
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Message msg  = new Message().setSignal(Signals.LOGOUT.getSigal()+"")
                                            .setFromId(CacheUtils.getUserCache().getUserId());
                MsgUtils.sendMsg(msg);

                SharedPreferences share = getActivity().getSharedPreferences("Login", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = share.edit();
                editor.putBoolean("autoLogin",false);
                editor.commit();

                Intent intent = new Intent(getContext(), Login.class);
                getActivity().finish();
                startActivity(intent);
            }
        });


        return viewRoot;
    }

    public void findViews(View viewRoot){
        nikName = viewRoot.findViewById(R.id.textView18);
        imageView = viewRoot.findViewById(R.id.imageView3);
        changeImage = viewRoot.findViewById(R.id.textView16);
        changeNikName = viewRoot.findViewById(R.id.textView19);
        button = viewRoot.findViewById(R.id.button6);
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