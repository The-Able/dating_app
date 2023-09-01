package com.libra.entity;

import java.util.ArrayList;
import java.util.List;


public class UserMsg {

    private String mUserId;
    private long mTimeLastMsg;
    private String mUserName;
    private List<String> mMessages = new ArrayList<>();

    public long getTimeLastMsg() {
        return mTimeLastMsg;
    }

    public void setTimeLastMsg(long timeLastMsg) {
        mTimeLastMsg = timeLastMsg;
    }

    public String getUserName() {
        return mUserName;
    }

    public void setUserName(String userName) {
        mUserName = userName;
    }

    public List<String> getMessages() {
        return mMessages;
    }

    public void setMessages(List<String> messages) {
        mMessages = messages;
    }

    public void addMessage(String msg) {
        mMessages.add(msg);
    }

    public String getUserId() {
        return mUserId;
    }

    public void setUserId(String userId) {
        mUserId = userId;
    }
}
