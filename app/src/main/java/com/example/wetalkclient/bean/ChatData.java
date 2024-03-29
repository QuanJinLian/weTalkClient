package com.example.wetalkclient.bean;

import java.io.Serializable;

public class ChatData implements Serializable {
    private static final long serialVersionUID = -5598765559893512679L;

    public enum Type{
        // 채팅 메세지
        CHATTING_MSG,
        // 채팅 이미지
        CHATTING_IMG,
        // 오프라인 메세지
        OFFLINE_MSG,
        // 친구 추가
        ADD_FRIEND,
        // 친구 추가 동의
        ADD_AGREE,
        // 로그아웃
        LOGOUT
    }


    public static class ID implements Serializable{
        private static final long serialVersionUID = 2788053974101111966L;
        private String userId;

        public String getUserId() {
            return userId;
        }
        public ID setUserId(String userId) {
            this.userId = userId;
            return this;
        }
    }



    public static class MSG implements Serializable{
        private static final long serialVersionUID = 7266838079022652922L;
        private Type type;
        private String fromId;
        private String toId;
        private String msg;
        private String time;

        // 친구 추가 시 fromId의 인포 필요
        private String photo;
        private String nikName;
        public Type getType() {
            return type;
        }
        public MSG setType(Type type) {
            this.type = type;
            return this;
        }
        public String getFromId() {
            return fromId;
        }
        public MSG setFromId(String fromId) {
            this.fromId = fromId;
            return this;
        }
        public String getToId() {
            return toId;
        }
        public MSG setToId(String toId) {
            this.toId = toId;
            return this;
        }
        public String getMsg() {
            return msg;
        }
        public MSG setMsg(String msg) {
            this.msg = msg;
            return this;
        }
        public String getTime() {
            return time;
        }
        public MSG setTime(String time) {
            this.time = time;
            return this;
        }
        public String getPhoto() {
            return photo;
        }
        public MSG setPhoto(String photo) {
            this.photo = photo;
            return this;
        }
        public String getNikName() {
            return nikName;
        }
        public MSG setNikName(String nikName) {
            this.nikName = nikName;
            return this;
        }
    }

}
