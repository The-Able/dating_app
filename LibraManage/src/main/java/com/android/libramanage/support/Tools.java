package com.android.libramanage.support;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;

import java.io.File;
import java.io.IOException;

public class Tools {
    private Tools() {

    }

    private static final String FILE_NAME = "cafeImage";

    public static boolean isExternalStorageWritable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    public static boolean hasCamera(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
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
        return Uri.fromFile(file);
    }
}
