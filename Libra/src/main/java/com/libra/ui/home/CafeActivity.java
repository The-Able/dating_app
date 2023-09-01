package com.libra.ui.home;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.core.widget.TextViewCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.libra.Const;
import com.libra.R;
import com.libra.activitys.GeoLocationActivity;
import com.libra.entity.User;
import com.libra.firebase.FBHelper;
import com.libra.fragments.GeoLocationFragment;
import com.libra.support.PrefHelper;
import com.libra.ui.home.cafeslist.CafeFragment;
import com.libra.ui.home.userslist.UsersListFragment;

import java.util.List;

import static com.libra.firebase.FBHelper.USERS;

public class CafeActivity extends GeoLocationActivity {
    private static final int APP_UPDATE_REQUEST_CODE = 351;
    private AppUpdateManager appUpdateManager;

    private Handler mHandler = new Handler();

    public static Intent launchIntent(Context context) {
        return new Intent(context, CafeActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
    }

    @Override
    protected GeoLocationFragment getGeoLocationFragment() {
        Fragment fg = getSupportFragmentManager().findFragmentById(R.id.container);
        if (fg instanceof GeoLocationFragment) {
            return ((GeoLocationFragment) fg);
        } else {
            return null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setShouldRequestPermissionsAndGps(true);
        changeFragment(CafeFragment.Companion.newInstance(), false);
        clearOldUsers();

        // Creates instance of the manager.
        appUpdateManager = AppUpdateManagerFactory.create(this);

        // Returns an intent object that you use to check for an update.
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

        // Checks that the platform will allow the specified type of update.
        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    // For a flexible update, use AppUpdateType.FLEXIBLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                // Request the update.
                requestAppUpdate(appUpdateInfo);
            }
        });
    }

    @Override
    protected void onDestroy() {
        mHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    @Override
    public void changeToolbar(String nameFg) {
        int s4 = (int) getResources().getDimension(R.dimen.space_4);
        int s6 = (int) getResources().getDimension(R.dimen.space_6);
        int s8 = (int) getResources().getDimension(R.dimen.space_8);
        int s10 = (int) getResources().getDimension(R.dimen.space_10);
        int s12 = (int) getResources().getDimension(R.dimen.space_12);
        int s14 = (int) getResources().getDimension(R.dimen.space_14);
//        TextViewCompat.setAutoSizeTextTypeWithDefaults(mTitle, TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM);
        if (TextUtils.equals(nameFg, CafeFragment.class.getSimpleName())) {
            mImageButtonLeft.setVisibility(/*isNeedToShowRushIcon() ? */View.INVISIBLE/* : View.INVISIBLE*/);
            //mImageButtonLeft.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_toolbar_notification));
            mImageButtonRight.setVisibility(View.VISIBLE);
            //mImageButtonRight.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_toolbar_watch_around));
            //mTitle.setTypeface(TypefaceCache.getTypeface(this, TypefaceCache.KEEP_CALM_MEDIUM));
//            mTitle.setPadding(s8, s14, s8, s14);
        } else if(TextUtils.equals(nameFg, UsersListFragment.class.getSimpleName())){
            mImageButtonLeft.setVisibility(View.VISIBLE);
            mImageButtonLeft.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_action_back));
        }

        /*else {
      mImageButtonRight.setVisibility(View.INVISIBLE);
      mImageButtonLeft.setVisibility(View.VISIBLE);
      mImageButtonLeft.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_reply_24dp));
      mTitle.setTypeface(TypefaceCache.getTypeface(this, TypefaceCache.GEO_LIGHT));
      mTitle.setPadding(s10, s14, s10, s12);
    }*/
    }

    private boolean isNeedToShowRushIcon() {
        List<String> listIdCafe = PrefHelper.getArrayCafeIdRushHours(this);
        return listIdCafe != null && !listIdCafe.isEmpty();
    }

    //This bad and dumb methods used to clear old users from cafe's (if user delete the app without logout and etc).
    private void clearOldUsers() {
        FBHelper.getCafeRooms().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    DataSnapshot users = snapshot.child(snapshot.getKey()).child(USERS);
                    for (DataSnapshot userSnapshot : users.getChildren()) {
                        User user = userSnapshot.getValue(User.class);
                        if (isNeedToRemoveUser(user)) {
                            userSnapshot.getRef().removeValue();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {

            }
        });
    }

    private boolean isNeedToRemoveUser(User user) {
        long loginedTime = user.getLoginedTime() * 1000;
        return loginedTime < 0
                || System.currentTimeMillis() - loginedTime > Const.TIMEOUT_GEOFENCE_DWELL;
    }

    @Override
    public void setTitleToolbar(String str) {
        if (str == null) {
            return;
        }
        mTitle.setText(str);
    }

    @Override
    public void setTitleToolbar(int res) {
        if (res == 0) {
            return;
        }
        String text = getString(res);
        mTitle.setText(text);
    }

    @Override
    protected void onResume() {
        super.onResume();

        appUpdateManager
                .getAppUpdateInfo()
                .addOnSuccessListener(
                        appUpdateInfo -> {
                            if (appUpdateInfo.updateAvailability()
                                    == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                                // If an in-app update is already running, resume the update.
                                requestAppUpdate(appUpdateInfo);
                            }
                        });
    }

    private void requestAppUpdate(AppUpdateInfo appUpdateInfo) {
        try {
            appUpdateManager.startUpdateFlowForResult(
                    // Pass the intent that is returned by 'getAppUpdateInfo()'.
                    appUpdateInfo,
                    // Or 'AppUpdateType.FLEXIBLE' for flexible updates.
                    AppUpdateType.IMMEDIATE,
                    // The current activity making the update request.
                    this,
                    // Include a request code to later monitor this update request.
                    APP_UPDATE_REQUEST_CODE);
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == APP_UPDATE_REQUEST_CODE) {
            if (resultCode != RESULT_OK) {
                System.out.println("Update flow failed! Result code: " + resultCode);
                // If the update is cancelled or fails,
                // you can request to start the update again.
            }
        }
    }
}
