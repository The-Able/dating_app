package com.libra.firebase;

import com.firebase.geofire.GeoFire;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;

public class FBHelper {

    public static final String CAFE = "cafe";
    public static final String LOCATIONS = "locations";
    public static final String CAFE_ROOMS = "cafe_rooms";
    public static final String CAFE_ROOMS_STATS = "cafe_rooms_stats";
    public static final String CHATS = "chats";
    public static final String USERS = "users";
    public static final String RUSH_HOURS = "cafe_rush_hours";
    public static final String NAME = "name";
    public static final String HEART = "heart";
    public static final String MESSAGES = "messages";
    public static final String USER_TOKENS = "userTokens";

    private static String getBaseUrl() {
        return ""/*BuildConfig.FIRE_HOST*/;
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

    private static String getChatsUrl() {
        return getBaseUrl() + CHATS;
    }

    private static String getCafeRushHoursUrl() {
        return getBaseUrl() + RUSH_HOURS;
    }

    public static DatabaseReference getCafe() {
        return FirebaseDatabase.getInstance().getReference().child(getCafeUrl());
    }

    public static DatabaseReference getCafeById(String cafeId) {
        return getCafe().child(cafeId);
    }

    public static GeoFire getLocationsCafe() {
        return new GeoFire(FirebaseDatabase.getInstance().getReference().child(getLocationsUrl()));
    }

    public static DatabaseReference getCafeRooms() {
        return FirebaseDatabase.getInstance().getReference().child(getCafeRoomUrl());
    }

    public static DatabaseReference getNameCafeRoom(String id) {
        return getCafeRooms().child(id).child(NAME);
    }

    public static DatabaseReference getCafeRoomById(String cafeId) {
        return getCafeRooms().child(cafeId).child(cafeId);
    }

    public static DatabaseReference getUsersFromCafeRoom(String cafeId) {
        return getCafeRoomById(cafeId).child(USERS);
    }

    public static DatabaseReference getUsersFromCafeRoomStat(String cafeId) {
        return FirebaseDatabase.getInstance().getReference().child(getBaseUrl() + CAFE_ROOMS_STATS).child(cafeId).child(new SimpleDateFormat("yyyy-MM-dd").format(new Date())).child(USERS);
    }

    public static DatabaseReference getUserById(String cafeId, String userId) {
        return getUsersFromCafeRoom(cafeId).child(userId);
    }

    public static DatabaseReference getChats() {
        return FirebaseDatabase.getInstance().getReference().child(getChatsUrl());
    }

    public static DatabaseReference getChatById(String chatId) {
        return getChats().child(chatId);
    }

    public static DatabaseReference getChatMessagesById(String id) {
        return getChatById(id).child(MESSAGES);
    }

    public static DatabaseReference getHeartChatById(String id) {
        return getChatById(id).child(HEART);
    }

    public static DatabaseReference getCafeRushHours(String cafeId) {
        return FirebaseDatabase.getInstance().getReference().child(getCafeRushHoursUrl()).child(cafeId);
    }

    public static DatabaseReference getCafeRushHoursByAppId(String cafeId, String appId) {
        return getCafeRushHours(cafeId).child(appId);
    }

    public static DatabaseReference getUserTokenRef(String userId) {
        return FirebaseDatabase.getInstance().getReference().child(USER_TOKENS).child(userId);
    }
}
