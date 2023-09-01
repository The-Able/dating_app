package com.libra.notifications.entities;

import com.google.gson.JsonObject;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * @author Ilya on 30.01.2018.
 */

public interface PushNotificationsService {

  @Headers("Content-Type:application/json") @POST("fcm/send")
  public Call<JsonObject> sendNewMessagePush(
      @Header("Authorization") String authorization,
      @Body PushNotificationRequest<NewMessageNotification> request
  );

  @Headers("Content-Type:application/json") @POST("fcm/send")
  public Call<JsonObject> sendRushHoursPush(
      @Header("Authorization") String authorization,
      @Body PushNotificationRequest<RushHoursNotification> request
  );
}
