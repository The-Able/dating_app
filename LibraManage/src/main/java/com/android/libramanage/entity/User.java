package com.android.libramanage.entity;

import android.text.TextUtils;

import java.io.Serializable;

public class User implements Serializable {
    private String name;
    private int gender;
    private String pushToken;
    private String imageUrl;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    private String userId;
    private long loginedTime;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public String getPushToken() {
        return pushToken;
    }

    public void setPushToken(String pushToken) {
        this.pushToken = pushToken;
    }

    public String getImageUrl() {
        if (!TextUtils.isEmpty(imageUrl)) {
            imageUrl = imageUrl.replace("http://", "https://");
        }
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public long getLoginedTime() {
        return this.loginedTime;
    }

    public void setLoginedTime(long loginedTime) {
        this.loginedTime = loginedTime;
    }
}
