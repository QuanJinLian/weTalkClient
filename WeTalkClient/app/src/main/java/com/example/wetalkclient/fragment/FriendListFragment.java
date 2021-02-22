package com.example.wetalkclient.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wetalkclient.R;
import com.example.wetalkclient.activities.ChattingRoom;
import com.example.wetalkclient.activities.NewFriend;
import com.example.wetalkclient.adapter.FriendAdapter;
import com.example.wetalkclient.bean.ChattingMsg;
import com.example.wetalkclient.bean.User;
import com.example.wetalkclient.db.CacheUtils;
import com.example.wetalkclient.db.DatabaseHelper;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FriendListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FriendListFragment extends Fragment {
    ArrayList<User> friendList ;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public FriendListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FriendListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FriendListFragment newInstance(String param1, String param2) {
        FriendListFragment fragment = new FriendListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    private SQLiteDatabase database ;
    private DatabaseHelper dbHelper;
    private LinearLayout linearLayout;
    private TextView unreadRequest;
    // 메세지 전송 받을 브로드캐스트
    private BroadcastReceiver broadcastReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        dbHelper = new DatabaseHelper(getActivity(),CacheUtils.getUserCache().getUserId());
        database = dbHelper.getWritableDatabase();
        friendList = new ArrayList<>();

        // 보로드캐스트 생성
        IntentFilter intentFilter = new IntentFilter();
        broadcastReceiver = new MyBroadcastReceiver();
        // action 명명 기준은 룸아이디
        intentFilter.addAction("com.example.addFriend");
        getActivity().registerReceiver(broadcastReceiver, intentFilter);

//        Toast.makeText(getContext(), "onCreate 호출", Toast.LENGTH_SHORT).show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View viewRoot  = inflater.inflate(R.layout.fragment_friend_list, container, false);
        linearLayout = viewRoot.findViewById(R.id.layout_newFriend);
        unreadRequest = viewRoot.findViewById(R.id.unreadRequest);
        RecyclerView recyclerView = viewRoot.findViewById(R.id.recycleView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(layoutManager);
        FriendAdapter adapter = new FriendAdapter();
        Cursor cursor = database.rawQuery("select friend_id, nikName, photo from friendList",null);
        friendList.clear();
        if(cursor.getCount() > 0 ){
            for(int i = 0; i < cursor.getCount(); i++){
                cursor.moveToNext();
                User user = new User().setUserId(cursor.getString(0))
                                      .setName(cursor.getString(1))
                                      .setPhoto(cursor.getString(2));
                adapter.addItem(user);
                friendList.add(user);
            }
        }
        cursor.close();
//        if(CacheUtils.friendList.size() >0){
//            for(User user : CacheUtils.friendList){
//                adapter.addItem(user);
//            }
//        }
        
        adapter.setOnItemClickLIstener(new FriendAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position) {
                Intent intent = new Intent(getContext(), ChattingRoom.class);
                intent.putExtra("friend",  friendList.get(position));
                intent.putExtra("fragment", 1);
                intent.putExtra("activity", "MainActivity");
                getActivity().finish();
                startActivityForResult(intent, 102);
            }
        });
        recyclerView.setAdapter(adapter);

        setUnreadRequest();


        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), NewFriend.class);
                getActivity().finish();
                startActivity(intent);
            }
        });

//        Toast.makeText(getContext(), "onCreateView 호출", Toast.LENGTH_SHORT).show();
        return viewRoot;
    }
    public void setUnreadRequest(){
        Cursor cursor1 = database.rawQuery("SELECT count(*) FROM friendRequest WHERE agree='false'",null);
        if(cursor1.getCount()>0){
            for(int i = 0; i < cursor1.getCount(); i++){
                cursor1.moveToNext();
                if(cursor1.getInt(0)>0){
                    unreadRequest.setText(cursor1.getInt(0)+"");
                }else{
                    unreadRequest.setBackgroundColor(Color.WHITE);
                    unreadRequest.setText("");
                }
            }
        }
        cursor1.close();
    }

    class MyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
//            Toast.makeText(getContext(), "여기 들어옴??", Toast.LENGTH_SHORT).show();
            setUnreadRequest();
        }
    }
}