package com.libra;

public class Const {

    public static final String PLAYUSERNAMEMALE = "DemoMale";
    public static final String PLAYUSERNAMEFEMALE = "DemoFemale";
    public static final int USERNAMELIMIT = 10;

    private Const() {
    }

//    public static final float RADIUS_AROUND_CAFE_ROOMS_IN_KM = 1f;
//    public static final float RADIUS_AROUND_CAFE_ROOMS_IN_KM = 1000f;
    public static final float RADIUS_AROUND_CAFE_ROOMS_IN_KM = 20f;
//    public static final float RADIUS_NEARBY_CAFE_IN_KM = 0.5f;//TODO Return when test
//    public static final float RADIUS_NEARBY_CAFE_IN_KM = 1000f;
//    public static final float RADIUS_NEARBY_CAFE_IN_KM = 70f;
    public static final float RADIUS_NEARBY_CAFE_IN_KM = 8587f;
    public static final float RADIUS_FOR_GEOFENCE_IN_METER = 550;

    public static final float ACCURACY_LOCATION = RADIUS_NEARBY_CAFE_IN_KM * 1000 / 2.5f;

    public static final int TIMEOUT_GEOFENCE_DWELL = 3 * 60 * 60 * 1000;
    public static final int TIMEOUT_GEOFENCE_NOTIFY_RESPONSIVENESS = 10 * 1000;

    public static final long TIMEOUT_NOTIFY_RUSH_HOURS_IN_MS = 60 * 60 * 1000;

    public static final int MAX_SIDE_AVATAR_IN_PX = 1024;

    public static final String LOGINTIME_ALARM = "login time alarm";
}
