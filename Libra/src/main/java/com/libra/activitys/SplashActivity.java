package com.libra.activitys;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.libra.BuildConfig;
import com.libra.R;
import com.libra.jobs.JobAt_7_40;
import com.libra.services.GeofenceIntentService;
import com.libra.support.PrefHelper;
import com.libra.ui.home.CafeActivity;

import java.security.MessageDigest;

public class SplashActivity extends BaseActivity {

    private static final long TIME_DELAY_SPLASH = 2000;
    private Handler mHandler = new Handler();
    private FirebaseAuth mAuth;

    public static Intent launchIntent(Context context) {
        return new Intent(context, SplashActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this,R.color.app_white));
        setContentView(R.layout.activity_splash);
        GeofenceIntentService.cancelNotification(this);

        mAuth = FirebaseAuth.getInstance();
        mAuth.signInAnonymously().addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    FirebaseUser user = mAuth.getCurrentUser();
                } else {
                    // If sign in fails, display a message to the user.
                    Log.e("TAG", "signInAnonymously:failure", task.getException());


                }
            }
        });

        // create one time request that will run on 7:40 PM everyday
        JobAt_7_40.set7_40Job(getApplicationContext());
        mHandler.postDelayed(mRunNextActivity, TIME_DELAY_SPLASH);
        if (BuildConfig.DEBUG) printHashKey();
    }

    private Runnable mRunNextActivity = () -> {
        if (PrefHelper.isLoginUser(SplashActivity.this)) {
            startActivity(ChatActivity.launchIntent(SplashActivity.this));
        } else {
            startActivity(CafeActivity.launchIntent(SplashActivity.this));
        }
    };


    private void printHashKey() {
        try {
            @SuppressLint("PackageManagerGetSignatures")
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String hashKey = new String(Base64.encode(md.digest(), 0));
                Log.i("HASH_KEY", "printHashKey() Hash Key: " + hashKey);
            }
        } catch (Exception e) {
            Log.e("HASH_KEY", "printHashKey()", e);
        }
    }

    @Override
    protected void onDestroy() {
        mHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
