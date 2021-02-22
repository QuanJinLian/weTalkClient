package com.example.wetalkclient.bean;

import java.sql.Date;

public class AddFriend {
    private String fromId;
    private String toId;
    private String agree;
    private String fromId_photo;
    private String fromId_nikName;
    private String date;

    public void setAllNull(){
        this.fromId = "";
        this.toId = "";
        this.agree = "";
        this.fromId_photo = "";
        this.fromId_nikName = "";
    }

    public String getDate() {
        return date;
    }

    public AddFriend setDate(String date) {
        this.date = date;
        return this;
    }

    public String getFromId() {
        return fromId;
    }

    public AddFriend setFromId(String fromId) {
        this.fromId = fromId;
        return this;
    }

    public String getToId() {
        return toId;
    }

    public AddFriend setToId(String toId) {
        this.toId = toId;
        return this;
    }

    public String getAgree() {
        return agree;
    }

    public AddFriend setAgree(String agree) {
        this.agree = agree;
        return this;
    }

    public String getFromId_photo() {
        return fromId_photo;
    }

    public AddFriend setFromId_photo(String fromId_photo) {
        this.fromId_photo = fromId_photo;
        return this;
    }

    public String getFromId_nikName() {
        return fromId_nikName;
    }

    public AddFriend setFromId_nikName(String fromId_nikName) {
        this.fromId_nikName = fromId_nikName;
        return this;
    }
}
