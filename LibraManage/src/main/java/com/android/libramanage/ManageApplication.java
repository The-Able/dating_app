package com.android.libramanage;

import android.app.Application;

import com.firebase.client.Firebase;
import com.firebase.client.Logger;
import com.orhanobut.logger.AndroidLogAdapter;

public class ManageApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Firebase.setAndroidContext(this);
        Firebase.getDefaultConfig().setLogLevel(Logger.Level.INFO);
        com.orhanobut.logger.Logger.addLogAdapter(new AndroidLogAdapter());
    }
}
