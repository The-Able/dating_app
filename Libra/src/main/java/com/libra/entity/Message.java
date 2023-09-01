package com.libra.entity;

public class Message {

    public static final String USER_ID = "userId";
    public static final String MSG = "msg";
    public static final String USER_NAME = "userName";

    private String userId;
    private String msg;
    private String userName;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
