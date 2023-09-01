package com.android.libramanage.entity;

import java.io.Serializable;
import java.util.List;

public class dateModel implements Serializable {

    String date;
    List<User> userList;

    public List<User> getUserList() {
        return userList;
    }

    public void setUserList(List<User> userList) {
        this.userList = userList;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
