package com.libra.notifications.entities;

import com.google.gson.annotations.SerializedName;

/**
 * @author Ilya on 30.01.2018.
 */

public class PushNotificationRequest<T> {
  @SerializedName("content_available") public boolean contentAvailable = true;
  @SerializedName("to") public String pushToken = "";
  @SerializedName("notification") public NotificationModel notification = new NotificationModel();
  @SerializedName("data") T data;

  public PushNotificationRequest(String pushToken, T data, String notificationBody) {
    this.pushToken = pushToken;
    this.data = data;
    this.notification.body = notificationBody;
  }
}
