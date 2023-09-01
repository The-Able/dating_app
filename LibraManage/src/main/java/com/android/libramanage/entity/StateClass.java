package com.android.libramanage.entity;

import java.util.List;

public class StateClass {
    Cafe cafe;
    String cafeId;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    String date;

    public List<dateModel> getDateModelList() {
        return dateModelList;
    }

    public void setDateModelList(List<dateModel> dateModelList) {
        this.dateModelList = dateModelList;
    }

    List<dateModel> dateModelList;


    public String getCafeId() {
        return cafeId;
    }

    public void setCafeId(String cafeId) {
        this.cafeId = cafeId;
    }

    public Cafe getCafe() {
        return cafe;
    }

    public void setCafe(Cafe cafe) {
        this.cafe = cafe;
    }
}
