package com.libra.entity;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.database.Exclude;
import com.libra.R;

import java.util.Locale;

public class User implements Parcelable {

    public static final int GENDER_TYPE_MALE = 0;
    public static final int GENDER_TYPE_FEMALE = 1;
    public static final String PUSH_TOKEN = "pushToken";

    private String name;
    private int gender;
    private String pushToken;
    private String imageUrl;
    private long loginedTime;

    @Exclude
    private String mId;
    @Exclude
    private String mCafeId;
    @Exclude
    private boolean isLike;

    public User() {

    }

    protected User(Parcel in) {
        name = in.readString();
        gender = in.readInt();
        pushToken = in.readString();
        mId = in.readString();
        mCafeId = in.readString();
        imageUrl = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

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

    @Exclude
    public boolean isFemale() {
        return getGender() == GENDER_TYPE_FEMALE;
    }

    @Exclude
    public boolean isEmpty() {
        return getName() == null;
    }

    @Exclude
    public String getId() {
        return mId;
    }

    @Exclude
    public void setId(String id) {
        mId = id;
    }

    public String getPushToken() {
        return pushToken;
    }

    public void setPushToken(String pushToken) {
        this.pushToken = pushToken;
    }

    @Exclude
    public String getCafeId() {
        return mCafeId;
    }

    @Exclude
    public void setCafeId(String cafeId) {
        mCafeId = cafeId;
    }

    @Exclude
    public boolean isLike() {
        return isLike;
    }

    @Exclude
    public void setLike(boolean like) {
        isLike = like;
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

    public String getLastSeenString(Context context){
        long secondsAgo = System.currentTimeMillis()/1000-getLoginedTime();
        Log.w("time",secondsAgo+"/"+getLoginedTime());
        long days = secondsAgo/60/60/24;
        if(days>0){
            return String.format(Locale.getDefault(),context.getString(R.string.days_ago_), days);
        }else{
            long hours = secondsAgo/60/60;
            if(hours>0){
                return String.format(Locale.getDefault(),context.getString(R.string.hours_ago_), hours);
            } else{
                long minutes = secondsAgo/60;
                return String.format(Locale.getDefault(),context.getString(R.string.minute_ago), minutes);
            }
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(getName());
        dest.writeInt(getGender());
        dest.writeString(getPushToken());
        dest.writeString(getId());
        dest.writeString(getCafeId());
        dest.writeString(getImageUrl());
    }
}
