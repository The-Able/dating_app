package com.libra.support;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;

public class TypefaceCache {

    public static final String GEO_BLACK = "Geomanist-Black.otf";
    public static final String GEO_BOLD = "Geomanist-Bold.otf";
    public static final String GEO_BOOK = "Geomanist-Book.otf";
    public static final String GEO_EXTRA_LIGHT = "Geomanist-ExtraLight.otf";
    public static final String GEO_LIGHT = "Geomanist-Light.otf";
    public static final String GEO_MEDIUM = "Geomanist-Medium.otf";
    public static final String GEO_REGULAR = "Geomanist-Regular.otf";
    public static final String GEO_THIN = "Geomanist-Thin.otf";
    public static final String GEO_ULTRA = "Geomanist-Ultra.otf";

    public static final String KEEP_CALM_MEDIUM = "KeepCalm-Medium.ttf";

    private static Map<String, Typeface> sCache = new HashMap<>();

    public static Typeface getTypeface(Context context, String typeName) {
        if (TextUtils.isEmpty(typeName)) {
            return null;
        }

        typeName = "fonts/" + typeName;
        if (sCache.containsKey(typeName)) {
            return sCache.get(typeName);
        }

        Typeface typeface = Typeface.createFromAsset(context.getAssets(), typeName);
        if (typeface != null) {
            sCache.put(typeName, typeface);
        }
        return typeface;
    }
}
