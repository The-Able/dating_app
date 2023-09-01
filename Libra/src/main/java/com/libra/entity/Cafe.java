package com.libra.entity;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.util.GeoUtils;
import com.libra.R;
import com.libra.support.Tools;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Cafe implements Serializable {

    public static final String NAME = "name";

    private String imageUrl;
    private String name;
    private String address;
    private String id;
    private String openHours;
    private GeoLocation location;
    private double distance = -1;
    private List<User> users = new ArrayList<>();
    private int maleCount = 0;
    private int femaleCount = 0;

    public String getPlacesType() {
        return placesType;
    }

    public void setPlacesType(String placesType) {
        this.placesType = placesType;
    }

    private String placesType;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public GeoLocation getLocation() {
        return location;
    }

    public void setLocation(GeoLocation location) {
        this.location = location;
    }

    public String getOpenHours() {
        return openHours;
    }

    public void setOpenHours(String openHours) {
        this.openHours = openHours;
    }

    public void setDataFromEntry(Map.Entry<String, GeoLocation> entry, GeoLocation curGeoLoc) {
        this.setId(entry.getKey());
        this.setLocation(entry.getValue());
        this.setDistance(GeoUtils.distance(curGeoLoc, entry.getValue()));
    }

    public String getImageUrl() {
        if (!TextUtils.isEmpty(imageUrl)) {
            imageUrl = imageUrl.replace("http://", "https://");
        }
        return imageUrl;
    }

    public void setImageUrl(String mUrlImage) {
        this.imageUrl = mUrlImage;
    }

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

    public int getUsersCount() {
        return users.size();
    }

    public int getMaleCount() {
        return maleCount;
    }

    public void setMaleCount(int maleCount) {
        this.maleCount = maleCount;
    }

    public int getFemaleCount() {
        return femaleCount;
    }

    public void setFemaleCount(int femaleCount) {
        this.femaleCount = femaleCount;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public boolean isEmpty() {
        return TextUtils.isEmpty(id);
    }

    public boolean canLogin() {
        return distance <= 500.0 && distance >= 0.0;
    }

    public String getDistanceLabel(Context context) {
        DecimalFormat df = new DecimalFormat("##.##");
        StringBuilder sb = new StringBuilder();
        int meters = (int) distance;
        double kilometers = distance / 1000;
        double miles = Tools.convertKmToMiles(distance / 1000);

        //if (kilometers < 0.1) {
        //  sb.append(context.getString(R.string.distance_meters, meters + ""));
        //} else {
        sb.append(context.getString(R.string.distance_km, df.format(kilometers)));
        //}

//        if (miles > 0.1) {
//            sb.append(" | ")
//                    .append(context.getString(R.string.distance_miles, df.format(miles)));
//        }

        return sb.toString();
    }

    @Override
    public String toString() {
        return "Cafe{" +
                "imageUrl='" + imageUrl + '\'' +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", id='" + id + '\'' +
                ", openHours='" + openHours + '\'' +
                ", location=" + location +
                ", distance=" + distance +
                ", usersCount=" + getUsersCount() +
                ", maleCount=" + maleCount +
                ", femaleCount=" + femaleCount +
                ", placesType='" + placesType + '\'' +
                '}';
    }
}
