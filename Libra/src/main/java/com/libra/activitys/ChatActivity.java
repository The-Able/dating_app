package com.libra.activitys;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.libra.R;
import com.libra.entity.User;
import com.libra.firebase.FBHelper;
import com.libra.fragments.chatfragments.ChatFragment;
import com.libra.fragments.chatfragments.ListChatFragment;
import com.libra.fragments.chatfragments.RushHoursFragment;
import com.libra.fragments.chatfragments.UserFragment;
import com.libra.services.GeofenceIntentService;
import com.libra.support.PrefHelper;
import com.libra.support.Tools;
import com.libra.ui.home.CafeActivity;

import java.util.List;

public class ChatActivity extends ToolbarHostActivity {

    public static final String FRIEND_ID = "friendId";

    private FrameLayout.LayoutParams mLayoutParamsToolbar;
    private String mUserId;
    private String mCafeId;
    private String mCurTag;

    public static Intent launchIntent(Context context) {
        return new Intent(context, ChatActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        User user = PrefHelper.getUser(this);
        if (user == null) {
            startActivity(CafeActivity.launchIntent(this));
            finish();
            return;
        }

        // get the token of user
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (!task.isSuccessful()) {
                    return;
                }

                // Get new Instance ID token
                if (task.getResult() != null) {
                    String token = task.getResult();
                    System.out.println(">>>>> TOKEN >>>>>" + token);
                    // add this token to Firebase
                    FBHelper.getUserTokenRef(mUserId).setValue(token);
                }
            }
        });
//        FirebaseInstanceId.getInstance().getInstanceId()
//                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
//
//                        if (!task.isSuccessful()) {
//                            return;
//                        }
//
//                        // Get new Instance ID token
//                        if (task.getResult() != null) {
//                            String token = task.getResult().getToken();
//                            System.out.println(">>>>> TOKEN >>>>>" + token);
//                            // add this token to Firebase
//                            FBHelper.getUserTokenRef(mUserId).setValue(token);
//                        }
//                    }
//                });

        mUserId = user.getId();
        mCafeId = user.getCafeId();
        String friendId = getIntent().getStringExtra(FRIEND_ID);
        if (TextUtils.isEmpty(friendId)) {
            changeFragment(ListChatFragment.newInstance(mUserId, mCafeId), false);
        } else {
            changeFragment(ChatFragment.newInstance(user, friendId), false);
        }
        mImageButtonLeft.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_reply_24dp));
        mLayoutParamsToolbar = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getResources()
                .getDimensionPixelSize(R.dimen.height_default_toolbar));

        IntentFilter filter = new IntentFilter();
        filter.addAction(GeofenceIntentService.ACTION_DEFAULT_LOGOUT);
        filter.addAction(GeofenceIntentService.ACTION_MANUAL_LOGOUT);
        LocalBroadcastManager.getInstance(this).registerReceiver(mLogoutReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mLogoutReceiver);
        super.onDestroy();
    }

    private BroadcastReceiver mLogoutReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case GeofenceIntentService.ACTION_DEFAULT_LOGOUT:
                case GeofenceIntentService.ACTION_MANUAL_LOGOUT:
                    startActivity(SplashActivity.launchIntent(ChatActivity.this));
                    break;
            }
        }
    };

    @Override
    public void onBackPressed() {
        if (TextUtils.equals(mCurTag, ChatFragment.class.getSimpleName())
                && !(getSupportFragmentManager().findFragmentByTag(ListChatFragment.class.getSimpleName()) instanceof ListChatFragment)) {
            changeFragment(ListChatFragment.newInstance(mUserId, mCafeId), false);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void changeToolbar(String nameFg) {
        mCurTag = nameFg;
        mLayoutParamsToolbar.height = getResources().getDimensionPixelSize(R.dimen.height_default_toolbar);
        mToolbar.setLayoutParams(mLayoutParamsToolbar);
        //mTitle.setTypeface(TypefaceCache.getTypeface(this, TypefaceCache.GEO_LIGHT));
//        mTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.text_size_extra_large));
        mTitle.setText(R.string.app_names);
        mImageButtonLeft.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_action_back));
        if (TextUtils.equals(nameFg, ListChatFragment.class.getSimpleName())) {
            //mImageButtonRight.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_toolbar_rush_hours));
            //mImageButtonRight.setVisibility(isNeedToShowRushIcon() ? View.VISIBLE : View.INVISIBLE);
            mImageButtonRight.setVisibility(View.INVISIBLE);
            mImageButtonLeft.setImageResource(R.drawable.ic_action_login_black);
            mImageButtonLeft.getDrawable().setTint(ContextCompat.getColor(this,R.color.app_white));
        } else if (TextUtils.equals(nameFg, UserFragment.class.getSimpleName())) {
            mImageButtonRight.setVisibility(View.INVISIBLE);
        } else if (TextUtils.equals(nameFg, ChatFragment.class.getSimpleName())) {
            mImageButtonRight.setImageDrawable(Tools.getDrawableWithColor(this, R.drawable.ic_favorite_24dp, R.color.color_accent));
            mImageButtonRight.setVisibility(View.INVISIBLE);
        } else if (TextUtils.equals(nameFg, RushHoursFragment.class.getSimpleName())) {
            mImageButtonRight.setVisibility(View.INVISIBLE);
            //mLayoutParamsToolbar.height = getResources().getDimensionPixelSize(R.dimen.height_favorites_toolbar);
            //mToolbar.setLayoutParams(mLayoutParamsToolbar);

            //mTitle.setTypeface(TypefaceCache.getTypeface(this, TypefaceCache.KEEP_CALM_MEDIUM));
            //mTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.text_size_extra_giant));
            //mTitle.setText(R.string.app_names);
            //tvRush.setVisibility(View.VISIBLE);
        }
    }

    private boolean isNeedToShowRushIcon() {
        List<String> cafeIds = PrefHelper.getArrayCafeIdRushHours(this);
        return cafeIds == null || cafeIds.isEmpty() || !cafeIds.contains(mCafeId);
    }
}
