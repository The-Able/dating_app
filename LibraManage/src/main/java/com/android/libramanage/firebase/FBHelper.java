package com.android.libramanage.firebase;

import com.android.libramanage.BuildConfig;
import com.firebase.client.Firebase;
import com.firebase.geofire.GeoFire;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FBHelper {

    public static final String CAFE = "cafe";
    public static final String LOCATIONS = "locations";
    public static final String CAFE_ROOMS = "cafe_rooms";
    public static final String CAFE_ROOMS_STAT = "cafe_rooms_stats";
    public static final String AVATARS = "avatars";
    public static final String CAFE_ICONS = "cafe_icons";
    public static final String CONFIG = "config";
    public static final String HOME_SCREEN_RADIUS = "main_screen_radius";

    private static String getBaseUrl() {
        return "";/*BuildConfig.FIRE_HOST;*/
    }

    private static String getCafeUrl() {
        return getBaseUrl() + CAFE;
    }

    private static String getLocationsUrl() {
        return getBaseUrl() + LOCATIONS;
    }

    private static String getCafeRoomUrl() {
        return getBaseUrl() + CAFE_ROOMS;
    }

    private static String getAvatarsUrl() {
        return getBaseUrl() + AVATARS;
    }

    private static String getCafeIconsUrl() {
        return getBaseUrl() + CAFE_ICONS;
    }

    private static String getConfigUrl() { return getBaseUrl() + CONFIG; }

    public static Firebase getCafeIcons() {
        return new Firebase(getCafeIconsUrl());
    }

    public static DatabaseReference getCafe() {
        return FirebaseDatabase.getInstance().getReference().child(getCafeUrl());
    }

    public static DatabaseReference getLocations() {
        return FirebaseDatabase.getInstance().getReference().child(getLocationsUrl());
    }
    public static DatabaseReference getBase() {
        return FirebaseDatabase.getInstance().getReference();
    }

    public static GeoFire getLocationsCafe() {
        return new GeoFire(FirebaseDatabase.getInstance().getReference().child(getLocationsUrl()));
    }

    public static DatabaseReference getCafeRooms() {
        return FirebaseDatabase.getInstance().getReference().child(getCafeRoomUrl());
    }

    public static DatabaseReference getCafeRoomByCafeId(String cafeId) {
        return getCafeRooms().child(cafeId);
    }

    public static DatabaseReference getUserById(String userId, String cafeId) {
        return getCafeRoomByCafeId(cafeId).child(userId);
    }

    public static DatabaseReference getAvatars() {
        return FirebaseDatabase.getInstance().getReference().child(getAvatarsUrl());
    }

    public static DatabaseReference getAvatarById(String userId) {
        return getAvatars().child(userId);
    }

    public static DatabaseReference getConfig() {
        return FirebaseDatabase.getInstance().getReference().child(getConfigUrl());
    }

    public static DatabaseReference getHomeScreenRadius() {
        return getConfig().child(HOME_SCREEN_RADIUS);
    }

}
