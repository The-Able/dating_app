package com.backend.libra;

public class User {

    public static final int GENDER_TYPE_MALE = 0;
    public static final int GENDER_TYPE_FEMALE = 1;

    private String name;
    private int gender;
    private String pushToken;

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

    public boolean isFemale() {
        return getGender() == GENDER_TYPE_FEMALE;
    }

    public boolean isEmpty() {
        return getName() == null;
    }

    public String getPushToken() {
        return pushToken;
    }

    public void setPushToken(String pushToken) {
        this.pushToken = pushToken;
    }
}
