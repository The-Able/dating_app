package com.libra.support;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.libra.entity.User;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public final class PrefHelper {

  private static final String ARRAY_CAFE_RUSH_HOURS = "listCafeRushHours";
  private static final String USER = "user";
  private static final String APP_ID = "appId";
  private static final String IS_SEND_NOTIFY_NEW_USER = "isSendNotifyNewUser";
  private static final String IS_18_YEARS_OLD = "is18YearsOld";

  private PrefHelper() {

  }

  private static SharedPreferences getSharedPref(Context context) {
    return PreferenceManager.getDefaultSharedPreferences(context);
  }

  public static void logoutUser(Context context) {
    getSharedPref(context).edit().remove(USER).apply();
  }

  public static boolean isLoginUser(Context context) {
    String user = getSharedPref(context).getString(USER, "");
    return !TextUtils.isEmpty(user);
  }

  public static void setUser(Context context, User user) {
    String userStr = new Gson().toJson(user);
    getSharedPref(context).edit().putString(USER, userStr).apply();
  }

  public static User getUser(Context context) {
    String userStr = getSharedPref(context).getString(USER, "");
    if (TextUtils.isEmpty(userStr)) {
      return null;
    }
    return new Gson().fromJson(userStr, User.class);
  }

  public static void setAppId(Context context, String appId) {
    getSharedPref(context).edit().putString(APP_ID, appId).apply();
  }

  public static String getAppId(Context context) {
    return getSharedPref(context).getString(APP_ID, "");
  }

  public static void removeCafeIdForRushHours(Context context, String cafeId) {
    List<String> list = getArrayCafeIdRushHours(context);
    if (list == null) {
      return;
    }
    list.remove(cafeId);
    getSharedPref(context).edit().putString(ARRAY_CAFE_RUSH_HOURS, new Gson().toJson(list)).apply();
  }

  public static void addCafeIdForRushHours(Context context, String cafeId) {
    List<String> list = getArrayCafeIdRushHours(context);
    if (list == null) {
      list = new ArrayList<>();
    }
    list.add(cafeId);
    getSharedPref(context).edit().putString(ARRAY_CAFE_RUSH_HOURS, new Gson().toJson(list)).apply();
  }

  public static List<String> getArrayCafeIdRushHours(Context context) {
    String list = getSharedPref(context).getString(ARRAY_CAFE_RUSH_HOURS, "");
    if (TextUtils.isEmpty(list)) {
      return null;
    }
    return new Gson().fromJson(list, new TypeToken<List<String>>() {
    }.getType());
  }

  public static void setLastTimeRushHoursNotifyBuCafeId(Context context, String cafeId, long time) {
    getSharedPref(context).edit().putLong(cafeId, time).apply();
  }

  public static long getLastTimeRushHoursNotify(Context context, String cafeId) {
    return getSharedPref(context).getLong(cafeId, 0);
  }

  public static void setNotifyNewUser(Context context, boolean isNotify) {
    getSharedPref(context).edit().putBoolean(IS_SEND_NOTIFY_NEW_USER, isNotify).apply();
  }

  public static boolean isNotifyNewUser(Context context) {
    return getSharedPref(context).getBoolean(IS_SEND_NOTIFY_NEW_USER, false);
  }

  public static boolean is18YearsOld(Context context) {
    return getSharedPref(context).getBoolean(IS_18_YEARS_OLD, false);
  }

  public static void markAs18YearsOld(Context context,boolean value) {
    getSharedPref(context).edit().putBoolean(IS_18_YEARS_OLD, value).apply();
  }
}
