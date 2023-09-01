package com.libra.services;

import android.app.PendingIntent;
import android.content.Intent;
import android.media.RingtoneManager;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.libra.R;
import com.libra.activitys.SplashActivity;
import com.libra.firebase.FBHelper;
import com.libra.support.Logger;
import com.libra.support.NotificationMsgHelper;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Dzmity Lukashanets on 27-Apr-16.
 */
public class AppGcmListenerService extends FirebaseMessagingService {

    private final Logger LOG = Logger.getLogger(getClass().getSimpleName(), true);

    private static final String ACTION = "msg_type";
    public static final String NEW_MESSAGE = "chat";
    public static final String NEW_USER = "new_user";
    private static final String RUSH_HOURS = "rushHours";
    private static final String HEART = "heart";
    private static final String DATA = "data";
    private static final String PRIORITY = "priority";
    private static final String PARAMS = "params";

    private static final String NOTIFICATION = "notification";
    public static final String USER_ID = "userId";
    private static final String USER_NAME = "userName";
    private static final String MESSAGE = "message";
    private static final String CAFE_NAME = "cafe_name";
    private static final String USER_COUNT = "user_count";
    private static final String CAFE_ID = "cafeId";
    private static final String PUSH_TOKEN = "pushToken";
    private static final String CAFE_NAMES = "cafeName";
    private static final String GENDER = "gender";

    @Override public void onMessageReceived(RemoteMessage message) {

        //String notification = data.getString(DATA);
        try {
            //String params = new JSONObject(message.getData()).getString(PARAMS);
            JSONObject jsonNotify = new JSONObject(message.getData());

            String action = jsonNotify.optString(ACTION);
            LOG.d("notification - " + jsonNotify.toString());

            switch (action) {
                case NEW_MESSAGE:
                    createMsgNotification(jsonNotify);
                    break;

                case NEW_USER:
                    createNewUserNotification(jsonNotify);
                    break;

                case RUSH_HOURS:
                    //TODO uncomment to return rush hours functionality
                    //String cafeId = jsonNotify.optString(CAFE_ID);
                    //
                    //long lasTimeByCafeId = PrefHelper.getLastTimeRushHoursNotify(this, cafeId);
                    //
                    //if (System.currentTimeMillis() - lasTimeByCafeId
                    //    > Const.TIMEOUT_NOTIFY_RUSH_HOURS_IN_MS) {
                    //  createRushHoursNotify(cafeId, jsonNotify);
                    //  PrefHelper.setLastTimeRushHoursNotifyBuCafeId(this, cafeId, System.currentTimeMillis());
                    //}
                    break;

                //TODO uncomment to return heart functionality
                case HEART:
                    //String userId = jsonNotify.getString(USER_ID);
                    //createHeartNotify(userId, jsonNotify);
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            LOG.e(e);
        }
    }

    @Override public void onNewToken(String s) {
        startService(RushHoursUpdateService.launchIntent(this));
    }

    private void createMsgNotification(JSONObject json) throws JSONException {
        String userId = json.getString(USER_ID);
        String userName = json.getString(USER_NAME);
        String msg = json.getString(MESSAGE);

        NotificationMsgHelper.addNewMessage(this, userId, userName, msg);
        LocalBroadcastManager.getInstance(this)
              .sendBroadcast(new Intent(NEW_MESSAGE).putExtra(USER_ID, userId));
    }

    private void createNewUserNotification(JSONObject json) throws JSONException {
        String pushToken = json.getString(PUSH_TOKEN);
        int gender = json.getInt(GENDER);
        String cafeName = json.getString(CAFE_NAMES);

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (!task.isSuccessful()) {
                    return;
                }

                // Get new Instance ID token
                if (task.getResult() != null) {
                    String token = task.getResult();
                    if (!token.equals(pushToken)) {
                        String message =
                                (gender == 0) ? getString(R.string.notification_someone_logged_in_man, cafeName)
                                        : getString(R.string.notification_someone_logged_in_woman, cafeName);
                        String title = getString(R.string.app_name);
                        showNotification(1785, title, message);
                    }
                }
            }
        });

//        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(instanceIdResult -> {
//            String token = instanceIdResult.getToken();
//            if (!token.equals(pushToken)) {
//                String message =
//                      (gender == 0) ? getString(R.string.notification_someone_logged_in_man, cafeName)
//                            : getString(R.string.notification_someone_logged_in_woman, cafeName);
//                String title = getString(R.string.app_name);
//                showNotification(1785, title, message);
//            }
//        });
    }

    private void createRushHoursNotify(String cafeId, JSONObject json) throws JSONException {
        String cafeName = json.getString(CAFE_NAME);
        String visitorCount = json.getString(USER_COUNT);
        String message = getString(R.string.count_visitors, visitorCount);

        showNotification(cafeId.hashCode(), cafeName, message);
    }

    private void createHeartNotify(String userId, JSONObject json) throws JSONException {
        String title = " ";
        String message = json.getString(MESSAGE);

        showNotification(userId.hashCode(), title, message);
    }

    private void showNotification(int notificationID, String title, String message) {
        NotificationManagerCompat nm = NotificationManagerCompat.from(this);

        String channelId = getString(R.string.notification_channel_id);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId);
        builder.setSmallIcon(R.mipmap.ic_launcher)
              .setColor(ContextCompat.getColor(this, R.color.color_accent))
              .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
              .setContentTitle(title)
              .setAutoCancel(true)
              .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
              .setContentText(message);
        Intent activityIntent = SplashActivity.launchIntent(this);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        builder.setContentIntent(pIntent);
        nm.notify(notificationID, builder.build());
    }
}
