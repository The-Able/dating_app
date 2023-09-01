package com.android.libramanage.support;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import androidx.annotation.WorkerThread;

import com.android.libramanage.R;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class CloudHelper {

    private static final String CLOUD_NAME = "cloud_name";
    private static final String API_KEY = "api_key";
    private static final String API_SECRET = "api_secret";
    private static final String TAG_URL = "url";
    private static final String TAG_SECURE_URL = "secure_url";
    private static final String RESULT = "result";

    public static final String RESULT_OK = "ok";

    private static Map getConfig(Context context) {
        Map<String, String> config = new HashMap<>();
        config.put(CLOUD_NAME, context.getString(R.string.cloud_name));
        config.put(API_KEY, context.getString(R.string.cloud_api_key));
        config.put(API_SECRET, context.getString(R.string.cloud_api_secret));
        return config;
    }

    @WorkerThread
    public static String uploadImage(Context context, Bitmap bitmap) throws IOException {
        Cloudinary cloudinary = new Cloudinary(getConfig(context));

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        byte[] imageInByte = bos.toByteArray();
        ByteArrayInputStream bis = new ByteArrayInputStream(imageInByte);

        Map uploadResult = cloudinary.uploader().upload(bis, ObjectUtils.asMap());
        Object resultUrl = uploadResult.get(TAG_SECURE_URL);
        return resultUrl == null ? null : resultUrl.toString();
    }

    @WorkerThread
    public static String destroyImage(Context context, String url) throws IOException {
        String publicKey = Uri.parse(url).getLastPathSegment().split(".jpg")[0];
        Map resultMap = new Cloudinary(getConfig(context)).uploader().destroy(publicKey, ObjectUtils.emptyMap());
        Object result = resultMap.get(RESULT);
        return result == null ? null : result.toString();
    }
}
