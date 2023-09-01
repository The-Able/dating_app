package com.libra.support;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.libra.BuildConfig;

import java.io.File;
import java.io.IOException;

public class Tools {
    private Tools() {

    }

    private static final String FILE_NAME = "avatar.jpg";

    public static Drawable getDrawableWithColor(Context context, @DrawableRes int drawable, @ColorRes int color) {
        Drawable d = ContextCompat.getDrawable(context, drawable);
        if (color != 0) {
            d.setColorFilter(ContextCompat.getColor(context, color), PorterDuff.Mode.MULTIPLY);
        }
        return d;
    }

    public static boolean isExternalStorageWritable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    public static boolean hasCamera(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    public static boolean isDev() {
        return TextUtils.equals(BuildConfig.FLAVOR, "dev");
    }

    public static double convertKmToMiles(double km) {
        return (km * 0.621371f);
    }

    public static Uri getUriAvatar(Context context, boolean isWrite) {
        File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) + File.separator + FILE_NAME);

        if (isWrite) {
            if (file.exists() && !file.delete()) {
                return null;
            }
            try {
                if (!file.createNewFile()) {
                    return null;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }

        }

        return FileProvider.getUriForFile(context,
                context.getApplicationContext().getPackageName() + ".provider",
                file);
    }

    public static Drawable getDrawableFromRes(Context context, @DrawableRes int dRes, @ColorRes int colorRes) {
        Drawable d = ContextCompat.getDrawable(context, dRes);
        d.setColorFilter(ContextCompat.getColor(context, colorRes), PorterDuff.Mode.MULTIPLY);
        return d;
    }

    public static int convertStrInSumAscii(String str) {
        int sum = 0;
        for (int i = 0; i < str.length(); i++) {
            sum += (int) str.charAt(i);
        }
        return sum;
    }

    public static String getDeviceID(Context context) {
        String id = null;

        try {
            id = Settings.Secure.getString(context.getContentResolver(),
                    Settings.Secure.ANDROID_ID);

            if (id != null)
                return id;
        } catch (Exception ex) {

        }

        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (tm != null) {
            id = tm.getDeviceId();
        }

        if (id == null) {
            String phoneNumber = tm.getLine1Number();
            if (phoneNumber != null) {
                id = phoneNumber;
            }

        }

        if (id == null)
            id = Build.BOARD + Build.BRAND + Build.DEVICE + Build.DISPLAY + Build.PRODUCT
                    + Build.USER + Build.MANUFACTURER + Build.USER;

        return id;
    }
}
