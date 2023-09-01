package com.libra.tools;

import com.libra.notifications.entities.NewMessageNotification;
import com.libra.notifications.entities.PushNotificationRequest;
import com.libra.notifications.entities.PushNotificationsService;
import com.libra.notifications.entities.RushHoursNotification;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.logging.Logger;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

//Sorry for this please. I know that this is very bad way to send pushes, but I needed very fast solution.
public class MessagingEndpoint {

  private static final Logger LOG = Logger.getLogger(MessagingEndpoint.class.getName());
  private static final String SERVER_KEY = "key=AIzaSyCwmLbIiTQn4XC7IICREgd8oX1dNPUOklA";
  private static final String BASE_URL = "https://fcm.googleapis.com/";

  private static final String NEW_MESSAGE_TEMPLATE = "%s: %s";
  private static final String HEART_MESSAGE_TEMPLATE = "%s sent a heart!";
  private static final String HEART_TYPE = "heart";

  private PushNotificationsService service;

  public MessagingEndpoint() {
    Retrofit retrofit = new Retrofit.Builder().baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build();

    service = retrofit.create(PushNotificationsService.class);
  }

  public void notifySendMessage(final String pushToken, String userId, String userName, String message) {

    if (pushToken == null
        || pushToken.length() == 0
        || userId == null
        || userId.length() == 0
        || userName == null
        || userName.length() == 0) {
      return;
    }

    String notificationMessage = String.format(NEW_MESSAGE_TEMPLATE, userName, message);
    NewMessageNotification notification = new NewMessageNotification(userId, userName, message);
    PushNotificationRequest<NewMessageNotification> request = new PushNotificationRequest<>(pushToken, notification, notificationMessage);
    Call<JsonObject> result = service.sendNewMessagePush(SERVER_KEY, request);
    result.enqueue(new Callback<JsonObject>() {
      @Override public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
        LOG.info("Success response: " + response.toString());
      }

      @Override public void onFailure(Call<JsonObject> call, Throwable t) {
        LOG.warning("Failure response: " + t.getMessage());
        t.printStackTrace();
      }
    });
  }

  //TODO Uncomment to return heart functionality
  public void notifyHeartMessage(final String pushToken, String userId, String userName) {
    //String message = String.format(HEART_MESSAGE_TEMPLATE, userName);
    //NewMessageNotification notification = new NewMessageNotification(userId, userName, message);
    //notification.action = HEART_TYPE;
    //PushNotificationRequest<NewMessageNotification> request = new PushNotificationRequest<>(pushToken, notification, message);
    //Call<JsonObject> result = service.sendNewMessagePush(SERVER_KEY, request);
    //result.enqueue(new Callback<JsonObject>() {
    //  @Override public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
    //    LOG.info("Success response: " + response.toString());
    //  }
    //
    //  @Override public void onFailure(Call<JsonObject> call, Throwable t) {
    //    LOG.warning("Failure response: " + t.getMessage());
    //    t.printStackTrace();
    //  }
    //});
  }

  public void notifyNewUser(List<String> pushTokens, String cafeId, String cafeName, int userCount) {

    RushHoursNotification notification = new RushHoursNotification(cafeId, cafeName, userCount);
    PushNotificationRequest<RushHoursNotification> request = new PushNotificationRequest<>("", notification, "");

    for (int i = 0; i < pushTokens.size(); i++) {
      request.pushToken = pushTokens.get(i);
      Call<JsonObject> result = service.sendRushHoursPush(SERVER_KEY, request);
      result.enqueue(new Callback<JsonObject>() {
        @Override public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
          LOG.info("Success response: " + response.toString());
        }

        @Override public void onFailure(Call<JsonObject> call, Throwable t) {
          LOG.warning("Failure response: " + t.getMessage());
          t.printStackTrace();
        }
      });
    }
  }
}
