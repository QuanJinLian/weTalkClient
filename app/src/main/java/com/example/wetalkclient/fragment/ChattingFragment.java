package com.example.wetalkclient.fragment;

import android.app.ActionBar;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.view.accessibility.AccessibilityViewCommand;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.wetalkclient.R;
import com.example.wetalkclient.activities.ChattingRoom;
import com.example.wetalkclient.adapter.ChattingAdapter;
import com.example.wetalkclient.adapter.ChattingListAdapter;
import com.example.wetalkclient.bean.ChattingMsg;
import com.example.wetalkclient.bean.Message;
import com.example.wetalkclient.bean.User;
import com.example.wetalkclient.db.CacheUtils;
import com.example.wetalkclient.db.DatabaseHelper;

import java.util.ArrayList;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChattingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChattingFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ChattingFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChattingFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChattingFragment newInstance(String param1, String param2) {
        ChattingFragment fragment = new ChattingFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    private SQLiteDatabase database ;
    private DatabaseHelper dbHelper;
    private ArrayList<ChattingMsg> chattingList ;
    private ArrayList<String> roomIds;
    private ChattingListAdapter adapter;
    private RecyclerView recyclerView;
    // 메세지 전송 받을 브로드캐스트
    private BroadcastReceiver broadcastReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        setHasOptionsMenu(true);
        SharedPreferences share = getActivity().getSharedPreferences("Login", Context.MODE_PRIVATE);
        String userId = share.getString("id","");

        dbHelper = new DatabaseHelper(getActivity(), userId);
        database = dbHelper.getWritableDatabase();
        chattingList = new ArrayList<>();

        // 보로드캐스트 생성
        IntentFilter intentFilter = new IntentFilter();
        broadcastReceiver = new MyBroadcastReceiver();
        // action 명명 기준은 룸아이디
        intentFilter.addAction("com.example.main");
        getActivity().registerReceiver(broadcastReceiver, intentFilter);
    }

    ActionBar abar;

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_top,menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup)inflater.inflate(R.layout.fragment_chatting,container,false);
        recyclerView = rootView.findViewById(R.id.recycleView2);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new ChattingListAdapter();
        setRecyclerView();

        return rootView;
    }

    public void setRecyclerView(){
        Cursor cursor = database.rawQuery("SELECT DISTINCT room_id from chatList ORDER BY _no DESC",null);
        roomIds = new ArrayList<String>();
        if(cursor.getCount() > 0){
            for (int i = 0 ; i < cursor.getCount(); i++){
                cursor.moveToNext();
                roomIds.add(cursor.getString(0));
            }
        }
        if(roomIds.size() > 0){
            for (int i = 0 ; i < roomIds.size(); i++){
                cursor = database.rawQuery("SELECT *, MAX(_no) From chatList WHERE room_id='"+roomIds.get(i)+"'",null);
                Cursor cursor1 = database.rawQuery("SELECT count(*) FROM chatList WHERE room_id='"+roomIds.get(i)+"' and unread=1",null);
                if(cursor.getCount() > 0){
                    for(int j = 0 ; j < cursor.getCount(); j++ ){
                        cursor.moveToNext();
                        cursor1.moveToNext();
                        ChattingMsg chatMsg = new ChattingMsg().setRoomId(cursor.getString(1))
                                .setUnread(cursor1.getInt(0))
                                .setMsg(cursor.getString(4))
                                .setTime(cursor.getString(5));

                        // 여기서 단톡인지 갠톡인지 판단해야 함

                        if(cursor.getString(1).startsWith("GroupChatting:20")){
                            Cursor cursor2 = database.rawQuery("select * from  chattingRoomList WHERE roomId='"+cursor.getString(1)+"'",null);
                            if (cursor2.getCount() > 0) {
                                for (int k = 0; k < cursor2.getCount(); k++){
                                    cursor2.moveToNext();
                                    chatMsg.setNikName(cursor2.getString(2));
                                }
                            }
                        }else {
                            User user = CacheUtils.getFriend(cursor.getString(1));
                            chatMsg.setPhoto(user.getPhoto())
                                    .setNikName(user.getName());
                        }
                        chattingList.add(chatMsg);
                        adapter.addItem(chatMsg);
                    }
                }
            }
        }
        cursor.close();
        adapter.setOnItemClickLIstener(new ChattingListAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position) {
                ChattingMsg chatting  = chattingList.get(position);
                Intent intent = new Intent(getContext(), ChattingRoom.class);
                intent.putExtra("chatting",chatting);
                intent.putExtra("fragment",0);
                intent.putExtra("activity", "MainActivity");
                getActivity().finish();
                startActivityForResult(intent, 103);
//                Toast.makeText(getContext(), "페이지 넘어가야 함++"+chattingList.get(position).getRoomId(), Toast.LENGTH_SHORT).show();
            }
        });
        recyclerView.setAdapter(adapter);
    }
    class MyBroadcastReceiver extends  BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            chattingList.clear();
            adapter.clearItems();
            setRecyclerView();
        }
    }

}