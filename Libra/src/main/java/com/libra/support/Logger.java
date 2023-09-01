package com.libra.support;

import androidx.annotation.NonNull;
import android.util.Log;

import com.libra.BuildConfig;
//import com.google.firebase.crash.FirebaseCrash;

public class Logger {

    private static final int MAX_LENGTH_MSG = 4000;
    private final String CLASS_NAME;
    private final boolean IS_ENABLED;

    private Logger(String name, boolean isEnabled) {
        this.CLASS_NAME = name;
        this.IS_ENABLED = isEnabled;
    }

    public static Logger getLogger(String tag, boolean isEnabled) {
        return new Logger(tag, isEnabled);
    }

    public void d(String msg, Throwable tr) {
        if (IS_ENABLED && BuildConfig.DEBUG) {
            if (msg.length() > MAX_LENGTH_MSG) {
                Log.d(getThread() + ": " + CLASS_NAME, "[" + msg.substring(0, MAX_LENGTH_MSG) + "]", tr);
                d(msg.substring(MAX_LENGTH_MSG), tr);
            } else {
                Log.d(getThread() + ": " + CLASS_NAME, "[" + msg + "]", tr);
            }
        }
    }

    public void d(String msg) {
        d(msg, null);
    }

    public void w(String msg, Throwable tr) {
        if (IS_ENABLED && BuildConfig.DEBUG) {
            if (msg.length() > MAX_LENGTH_MSG) {
                Log.w(getThread() + ": " + CLASS_NAME, "[" + msg.substring(0, MAX_LENGTH_MSG) + "]", tr);
                w(msg.substring(MAX_LENGTH_MSG), tr);
            } else {
                Log.w(getThread() + ": " + CLASS_NAME, "[" + msg + "]", tr);
            }
        }
    }

    public void w(String msg) {
        w(msg, null);
    }

    public void i(String msg, Throwable tr) {
        if (IS_ENABLED && BuildConfig.DEBUG) {
            if (msg.length() > MAX_LENGTH_MSG) {
                Log.i(getThread() + ": " + CLASS_NAME, "[" + msg.substring(0, MAX_LENGTH_MSG) + "]", tr);
                i(msg.substring(MAX_LENGTH_MSG), tr);
            } else {
                Log.i(getThread() + ": " + CLASS_NAME, "[" + msg + "]", tr);
            }
        }
    }

    public void i(String msg) {
        i(msg, null);
    }

    public void e(@NonNull String msg, Throwable tr) {
        if (IS_ENABLED && BuildConfig.DEBUG) {
            if (msg.length() > MAX_LENGTH_MSG) {
                Log.e(getThread() + ": " + CLASS_NAME, "[" + msg.substring(0, MAX_LENGTH_MSG) + "]", tr);
                e(msg.substring(MAX_LENGTH_MSG), tr);
            } else {
                Log.e(getThread() + ": " + CLASS_NAME, "[" + msg + "]", tr);
            }
        } else if (!BuildConfig.DEBUG) {
            //FirebaseCrash.log(msg);
            //FirebaseCrash.report(tr);
        }
    }

    public void e(@NonNull String msg) {
        e(msg, null);
    }

    public void e(Throwable e) {
        e(e.getMessage(), e);
    }

    private String getThread() {
        return Thread.currentThread().getName();
    }
}
