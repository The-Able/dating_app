package com.libra.entity;

import java.io.Serializable;

public class StateUser implements Serializable {
    private String imageUrl;
    private String name;
    private String address;

    User user;

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getPlacesType() {
        return placesType;
    }

    public void setPlacesType(String placesType) {
        this.placesType = placesType;
    }

    private String placesType;
}
