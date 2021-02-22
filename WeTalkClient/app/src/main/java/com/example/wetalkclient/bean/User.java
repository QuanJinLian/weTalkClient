package com.example.wetalkclient.bean;

import java.io.Serializable;

public class User implements Serializable {
    private static final long serialVersionUID = 4577541314244455442L;
    private String userId ;
    private String name ;
    private String photo;

    public String getUserId() {
        return userId;
    }

    public User setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public String getName() {
        return name;
    }

    public User setName(String name) {
        this.name = name;
        return this;
    }

    public String getPhoto() {
        return photo;
    }

    public User setPhoto(String photo) {
        this.photo = photo;
        return this;
    }
}
