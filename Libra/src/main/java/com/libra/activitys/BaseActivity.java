package com.libra.activitys;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.libra.R;
import com.libra.support.Logger;
import com.libra.tools.ExceptionHandler;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public abstract class BaseActivity extends AppCompatActivity {

    protected final Logger LOG = Logger.getLogger(getClass().getSimpleName(), isLogged());

    private ProgressDialog mProgressDialog;

    protected boolean isLogged() {
        return false;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getString(R.string.wait));
        mProgressDialog.setCancelable(false);
        mProgressDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    protected void showPd() {
        try {
            mProgressDialog.show();
        } catch (Exception e){e.printStackTrace();}
    }

    protected void dismissPd() {
        try{
        mProgressDialog.dismiss();
    } catch (Exception e){e.printStackTrace();}
    }
}
