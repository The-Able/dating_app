package com.libra.support;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.app.NotificationCompat;
import android.text.TextUtils;

import com.libra.R;
import com.libra.activitys.ChatActivity;
import com.libra.entity.UserMsg;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public final class NotificationMsgHelper {

    private static final String NEW_MESSAGES = "NewMessages";

    private static final int NOTIFICATION_ID = NotificationMsgHelper.class.getName().hashCode();
    private static final Gson GSON = new Gson();
    private static String mUserIdCurrentChat;

    private NotificationMsgHelper() {
    }

    public static void addNewMessage(Context context, String userId, String userName, String msg) {
        if (TextUtils.equals(userId, mUserIdCurrentChat)) {
            return;
        }
        String userStr = getUserMsgById(context, userId);
        UserMsg userMsg;
        if (TextUtils.isEmpty(userStr)) {
            userMsg = new UserMsg();
            userMsg.setUserId(userId);
            userMsg.setUserName(userName);
        } else {
            userMsg = GSON.fromJson(userStr, UserMsg.class);
        }
        userMsg.addMessage(msg);
        userMsg.setTimeLastMsg(System.currentTimeMillis());

        getSharedPrefMsg(context)
                .edit()
                .putString(userId, GSON.toJson(userMsg))
                .apply();

        buildNotification(context, false);
    }

    public static void setUserIdCurrentChat(String userId) {
        mUserIdCurrentChat = userId;
    }

    public static void cancelByUserId(Context context, String userId) {
        getSharedPrefMsg(context).edit().remove(userId).commit();
        buildNotification(context, true);
    }

    public static void cancelAll(Context context) {
        getSharedPrefMsg(context).edit().clear().apply();
        buildNotification(context, true);
    }

    public static int getCountMessageByUserId(Context context, String id) {
        String userStr = getUserMsgById(context, id);
        if (TextUtils.isEmpty(userStr)) {
            return 0;
        } else {
            UserMsg userMsg = GSON.fromJson(userStr, UserMsg.class);
            return userMsg.getMessages().size();
        }
    }

    private static int getCountAllMessage(Context context) {
        List<UserMsg> list = getUserMsgForNotification(context);
        int count = 0;
        for (UserMsg userMsg : list) {
            count += userMsg.getMessages().size();
        }
        return count;
    }

    private static List<UserMsg> getUserMsgForNotification(Context context) {
        List<UserMsg> list = new ArrayList<>();
        Map<String, String> userMap = (Map<String, String>) getSharedPrefMsg(context).getAll();
        for (String userStr : userMap.values()) {
            if (!TextUtils.isEmpty(userStr)) {
                list.add(GSON.fromJson(userStr, UserMsg.class));
            }
        }
        return list;
    }

    private static void buildNotification(Context context, boolean isCancel) {

        List<UserMsg> list = getUserMsgForNotification(context);
        NotificationManagerCompat nm = NotificationManagerCompat.from(context);

        if (list.isEmpty()) {
            nm.cancel(NOTIFICATION_ID);
            return;
        }
        int countAllMsg = getCountAllMessage(context);

        String channelId = context.getString(R.string.notification_channel_id);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId);
        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setColor(ContextCompat.getColor(context, R.color.color_accent))
                .setAutoCancel(false);

        if (countAllMsg != 1) {
            builder.setNumber(countAllMsg);
        }
        if (!isCancel) {
            builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        }

        Intent activityIntent = ChatActivity.launchIntent(context)
                .setAction(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_LAUNCHER);

        if (list.size() == 1) {
            UserMsg userMsg = list.get(0);

            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle()
                    .setSummaryText(context.getString(R.string.app_name))
                    .setBigContentTitle(userMsg.getUserName());

            for (String msg : userMsg.getMessages()) {
                inboxStyle.addLine(msg);
            }
            builder.setStyle(inboxStyle)
                    .setContentTitle(userMsg.getUserName())
                    .setContentText(userMsg.getMessages().get(userMsg.getMessages().size() - 1))
                    .setGroupSummary(true)
                    .setGroup(NEW_MESSAGES);
            activityIntent.putExtra(ChatActivity.FRIEND_ID, userMsg.getUserId());
        } else {
            String msgName = context.getString(R.string.from);
            for (int i = 0; i < list.size(); i++) {
                if (i == 0) {
                    msgName += " " + list.get(i).getUserName();
                } else {
                    msgName += ", " + list.get(i).getUserName();
                }
            }
            builder.setContentTitle(String.valueOf(countAllMsg) + " " + context.getString(R.string.new_message))
                    .setContentText(msgName);
        }
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        builder.setContentIntent(pIntent);
        nm.notify(NOTIFICATION_ID, builder.build());
    }

    private static String getUserMsgById(Context context, String userId) {
        return getSharedPrefMsg(context).getString(userId, "");
    }

    private static SharedPreferences getSharedPrefMsg(Context context) {
        return context.getSharedPreferences(NEW_MESSAGES, Context.MODE_PRIVATE);
    }
}
