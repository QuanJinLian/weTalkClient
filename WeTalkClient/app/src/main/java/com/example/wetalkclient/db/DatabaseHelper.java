package com.example.wetalkclient.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.example.wetalkclient.fragment.FriendListFragment;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static int VERSION = 1;

    public DatabaseHelper(@Nullable Context context,@Nullable String name) {
        super(context, name, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table if not exists myInfo ("
                +" _id text PRIMARY KEY, "
                +" nikName text, "
                +" photo text)");
        db.execSQL("create table if not exists friendList ("
                +" friend_id text PRIMARY KEY, "
                +" nikName text, "
                +" photo text)");
        db.execSQL("create table if not exists chatList ("
                +" _no integer PRIMARY KEY autoincrement, "
                +" room_id text, "
                +" friend_id text, "
                +" unread integer, "
                +" msg text, "
                +" time text, "
                +" send integer)");
        db.execSQL("create table if not exists friendRequest ("
                + " _no integer PRIMARY KEY autoincrement, "
                + " fromId text,"
                + " toId text,"
                + " agree text,"
                + " fromId_photo text,"
                + " fromId_nikName text,"
                + " time text )");
        db.execSQL("create table if not exists chattingRoomList ("
                + " _no integer PRIMARY KEY autoincrement, "
                + " roomId text,"
                + " roomName text,"
                + " membersId text )");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
