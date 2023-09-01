package com.libra.services;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.core.app.NotificationCompat;
import android.text.TextUtils;

import com.libra.R;
import com.libra.activitys.SplashActivity;
import com.libra.helpers.LogoutHelper;
import com.libra.support.Logger;
import com.libra.support.PrefHelper;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

public class GeofenceIntentService extends IntentService {

    private Logger LOG = Logger.getLogger(getClass().getSimpleName(), true);

    public static final String ACTION_DEFAULT_LOGOUT = "localActionDefaultLogout";
    public static final String ACTION_MANUAL_LOGOUT = "localActionManualLogout";

    private static final int NOTIFICATION_LOGOUT_ID = GeofenceIntentService.class.getName().hashCode();

    public static Intent getDefaultIntent(Context context) {
        return new Intent(context, GeofenceIntentService.class);
    }

    public static Intent getLogoutIntent(Context context) {
        return getDefaultIntent(context).setAction(ACTION_DEFAULT_LOGOUT);
    }

    public static Intent getManualLogoutIntent(Context context) {
        return getDefaultIntent(context).setAction(ACTION_MANUAL_LOGOUT);
    }

    public static void cancelNotification(Context context) {
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_LOGOUT_ID);
    }

    public GeofenceIntentService() {
        super(GeofenceIntentService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        LOG.d("onHandleIntent");

        if (!PrefHelper.isLoginUser(this)) {
            LOG.e("Logout app completed");
            return;
        }

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            int errorCode = geofencingEvent.getErrorCode();
            LOG.e(new Exception("Geofence error with code - " + errorCode));
            return;
        }

        int geofenceTransition = geofencingEvent.getGeofenceTransition();
        String action = intent.getAction();
        if (TextUtils.isEmpty(action)) {
            action = ACTION_DEFAULT_LOGOUT;
        }

        LOG.i("geofenceTransition - " + geofenceTransition + ", action - " + action);

        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL ||
                TextUtils.equals(action, ACTION_DEFAULT_LOGOUT) ||
                TextUtils.equals(action, ACTION_MANUAL_LOGOUT)) {

            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(action));
            //logoutApp();
            LogoutHelper.logoutApp(this);
            String msg;
            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
                msg = getString(R.string.have_left_cafe_area);
            } else {
                msg = getString(R.string.time_over_in_cafe);
            }
            if (!TextUtils.equals(action, ACTION_MANUAL_LOGOUT)) {
                sendNotificationLogout(msg);
            }

            LOG.i("Geofence onHandleIntent is ended");
        } else {
            LOG.e("Geofence transition invalid type - " + geofenceTransition);
        }
    }

    private void sendNotificationLogout(String msg) {
        NotificationManagerCompat nm = NotificationManagerCompat.from(this);
        String channelId = getString(R.string.notification_channel_id);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId);
        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setColor(ContextCompat.getColor(this, R.color.color_accent))
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentTitle(getString(R.string.logout))
                .setAutoCancel(true)
                .setContentText(msg);
        Intent activityIntent = SplashActivity.launchIntent(this);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        builder.setContentIntent(pIntent);
        nm.notify(NOTIFICATION_LOGOUT_ID, builder.build());
    }

/*    private void logoutApp() {
        final User user = PrefHelper.getUser(this);
        PrefHelper.logoutUser(this);
        NotificationMsgHelper.cancelAll(this);
        PrefHelper.setNotifyNewUser(this, false);

        if (user != null) {
            String destroyResult = "";
            try {
                destroyResult = CloudHelper.destroyImage(this, user.getImageUrl());
                if (!TextUtils.equals(destroyResult, CloudHelper.RESULT_OK)) {
                    LOG.e(new Exception("LOGOUT, not removed image from cloud, destroy result - " + destroyResult));
                }
            } catch (IOException e) {
                e.printStackTrace();
                LOG.e(e);
                LOG.e(new Exception("LOGOUT, not removed image from cloud, destroy result - " + destroyResult));
            }
        }

        final CountDownLatch countDownLatch = new CountDownLatch(1);
        if (user != null) {
            FBHelper.getUserById(user.getCafeId(), user.getId()).removeValue();
            FBHelper.getChats().addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        if (data.getKey().contains(user.getId())) {
                            FBHelper.getChats().child(data.getKey()).removeValue();
                        }
                    }
                    LOG.i("data logout is success");
                    countDownLatch.countDown();
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                    LOG.e(new Exception(firebaseError.getMessage()));
                    countDownLatch.countDown();
                }
            });
        }

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }*/
}
