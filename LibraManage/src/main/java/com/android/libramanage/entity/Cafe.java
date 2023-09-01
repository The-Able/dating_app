package com.android.libramanage.entity;


import android.text.TextUtils;
import android.view.ActionMode;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.firebase.geofire.GeoLocation;

public class Cafe {

    public static final String NAME = "name";
    public static final String ADDRESS = "address";
    public static final String IMAGE_URL = "imageUrl";
    public static final String placeType = "placesType";
    public static final String PLACES_ID = "placesId";

    private String name;
    private String address;
    private String imageUrl;
    private String placesId;

    public String getPlacesType() {
        return placesType;
    }

    public void setPlacesType(String placesType) {
        this.placesType = placesType;
    }

    private String placesType;

    public Cafe() {
    }

    public Cafe(String name, String address, String imageUrl, String placesId,String placesType) {
        this.name = name;
        this.address = address;
        this.imageUrl = imageUrl;
        this.placesId = placesId;
        this.placesType = placesType;
    }

    @JsonIgnore
    private String mId;
    @JsonIgnore
    private GeoLocation mGeoLoc;
    @JsonIgnore
    private ActionMode mMode;

    public String getName() {
        return name;
    }

    public void setName(String mName) {
        this.name = mName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String mAddress) {
        this.address = mAddress;
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

    @JsonIgnore
    public String getId() {
        return mId;
    }

    @JsonIgnore
    public void setId(String id) {
        mId = id;
    }

    @JsonIgnore
    public GeoLocation getGeoLoc() {
        return mGeoLoc;
    }

    @JsonIgnore
    public void setGeoLoc(GeoLocation geoLoc) {
        mGeoLoc = geoLoc;
    }

    @JsonIgnore
    public ActionMode getMode() {
        return mMode;
    }

    @JsonIgnore
    public void setMode(ActionMode mode) {
        mMode = mode;
    }

    public String getPlacesId() {
        return placesId;
    }

    public void setPlacesId(String placesId) {
        this.placesId = placesId;
    }
}
