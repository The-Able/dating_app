/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Backend with Google Cloud Messaging" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/GcmEndpoints
*/

package com.backend.libra;

import com.google.android.gcm.server.Constants;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.appengine.repackaged.com.google.gson.Gson;
import com.google.appengine.repackaged.com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.inject.Named;

@Api(
        name = "messaging",
        version = "v1",
        namespace = @ApiNamespace(
                ownerDomain = "libra.backend.com",
                ownerName = "libra.backend.com",
                packagePath = ""
        )
)
public class MessagingEndpoint {

    private static final String ACTION = "action";
    private static final String NEW_MESSAGE = "newMessage";
    private static final String RUSH_HOURS = "rushHours";
    private static final String NOTIFICATION = "notification";
    private static final String DATA = "data";
    private static final String PRIORITY = "priority";
    private static final String PARAMS = "params";
    private static final String BODY = "body";
    private static final String SOUND = "sound";

    private static final String PUSH_TOKEN = "pushToken";
    private static final String USER_ID = "userId";
    private static final String USER_NAME = "userName";
    private static final String MESSAGE = "message";
    private static final String CAFE_ID = "cafeId";
    private static final String USERS_COUNT = "user_count";
    private static final String CAFE_NAME = "cafe_name";
    private static final String LIST_PUSH_TOKENS = "list_push_tokens";

    private static final Logger LOG = Logger.getLogger(MessagingEndpoint.class.getName());
    private static final String API_KEY = System.getProperty("gcm.api.key");

    private final Gson GSON = new Gson();
    private Sender mSender = new Sender(API_KEY);

    public void notifySendMessage(
            @Named(PUSH_TOKEN) String pushToken,
            @Named(USER_ID) String userId,
            @Named(USER_NAME) String userName,
            @Named(MESSAGE) String message) throws IOException {

        LOG.info(PUSH_TOKEN + " - " + pushToken + ", "
                + USER_ID + " - " + userId + ", "
                + USER_NAME + " - " + userName + ", "
                + MESSAGE + " - " + message + ", ");

        if (pushToken == null
                || pushToken.length() == 0
                || userId == null
                || userId.length() == 0
                || userName == null
                || userName.length() == 0) {
            return;
        }

        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put(ACTION, NEW_MESSAGE);
        paramsMap.put(USER_ID, userId);
        paramsMap.put(USER_NAME, userName);
        paramsMap.put(MESSAGE, message);

        Map<String, String> dataMap = new HashMap<>();
        dataMap.put(PARAMS, GSON.toJson(paramsMap));

        Map<String, String> notificationMap = new HashMap<>();
        notificationMap.put(BODY, userName + " - " + message);
        notificationMap.put(SOUND, "default");

        Message msg = new Message.Builder()
                .addData(DATA, GSON.toJson(dataMap))
                .addData(NOTIFICATION, GSON.toJson(notificationMap))
                .addData(PRIORITY, "high")
                .build();

        Result result = mSender.send(msg, pushToken, 5);

        if (result.getMessageId() != null) {
            LOG.info("Message sent to " + pushToken);
        } else {
            String error = result.getErrorCodeName();
            if (error.equals(Constants.ERROR_NOT_REGISTERED)) {
                LOG.warning("Registration Id " + pushToken + " no longer registered with GCM, removing from datastore");
            } else {
                LOG.warning("Error when sending message : " + error);
            }
        }
    }

    public void notifyNewUser(@Named(LIST_PUSH_TOKENS) String jsonListPushTokens,
                              @Named(CAFE_ID) String cafeId,
                              @Named(CAFE_NAME) String cafeName,
                              @Named(USERS_COUNT) int userCount) throws IOException {

        LOG.info("listPushTokens - " + jsonListPushTokens + ", cafeId - " + cafeId + ", cafeName - " + cafeName + ", userCount - " + userCount);

        ArrayList<String> listPushTokens = GSON.fromJson(jsonListPushTokens, new TypeToken<ArrayList<String>>() {
        }.getType());


        if (listPushTokens.isEmpty()
                || cafeId == null
                || cafeName == null
                || userCount == 0) {
            return;
        }

        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put(ACTION, RUSH_HOURS);
        paramsMap.put(CAFE_ID, cafeId);
        paramsMap.put(CAFE_NAME, cafeName);
        paramsMap.put(USERS_COUNT, String.valueOf(userCount));

        Map<String, String> dataMap = new HashMap<>();
        dataMap.put(PARAMS, GSON.toJson(paramsMap));

        Map<String, String> notificationMap = new HashMap<>();
        notificationMap.put(BODY, cafeName + " " + userCount);
        notificationMap.put(SOUND, "default");


        Message msg = new Message.Builder()
                .addData(DATA, GSON.toJson(dataMap))
                .addData(NOTIFICATION, GSON.toJson(notificationMap))
                .addData(PRIORITY, "high")
                .build();

        MulticastResult results = mSender.send(msg, listPushTokens, 5);

        List<Result> resultList = results.getResults();
        for (int i = 0; i < resultList.size(); i++) {
            Result result = resultList.get(i);
            String pushToken = listPushTokens.get(i);
            if (result.getMessageId() != null) {
                LOG.info("Message sent to " + pushToken);
            } else {
                String error = result.getErrorCodeName();
                if (error.equals(Constants.ERROR_NOT_REGISTERED)) {
                    LOG.warning("Registration Id " + pushToken + " no longer registered with GCM, removing from datastore");
                } else {
                    LOG.warning("Error when sending message : " + error);
                }
            }
        }

    }
}
