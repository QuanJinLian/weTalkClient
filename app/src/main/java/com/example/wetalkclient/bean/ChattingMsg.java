package com.example.wetalkclient.bean;

import java.io.Serializable;

public class ChattingMsg  implements Serializable {
    private static final long serialVersionUID = 1544754715415724562L;
    private String roomId;
    private String friendId;
    private int unread;   // 읽었으면 0 안읽었으면 1
    private String msg ;
    private String time;
    private int send;   // 내가 보냈으면 0 상대가 보냈으면 1
    private String photo;
    private String nikName;
    private String roomName;

    public String getRoomName() {
        return roomName;
    }

    public ChattingMsg setRoomName(String roomName) {
        this.roomName = roomName;
        return this;
    }

    public String getPhoto() {
        return photo;
    }

    public ChattingMsg setPhoto(String photo) {
        this.photo = photo;
        return this;
    }

    public String getNikName() {
        return nikName;
    }

    public ChattingMsg setNikName(String nikName) {
        this.nikName = nikName;
        return this;
    }

    public String getRoomId() {
        return roomId;
    }

    public ChattingMsg setRoomId(String roomId) {
        this.roomId = roomId;
        return this;
    }

    public String getFriendId() {
        return friendId;
    }

    public ChattingMsg setFriendId(String friendId) {
        this.friendId = friendId;
        return this;
    }

    public int getUnread() {
        return unread;
    }

    public ChattingMsg setUnread(int unread) {
        this.unread = unread;
        return this;
    }

    public String getMsg() {
        return msg;
    }

    public ChattingMsg setMsg(String msg) {
        this.msg = msg;
        return this;
    }

    public String getTime() {
        return time;
    }

    public ChattingMsg setTime(String time) {
        this.time = time;
        return this;
    }

    public int getSend() {
        return send;
    }

    public ChattingMsg setSend(int send) {
        this.send = send;
        return this;
    }
}
