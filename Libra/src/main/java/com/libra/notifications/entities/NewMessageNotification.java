package com.libra.notifications.entities;

import com.google.gson.annotations.SerializedName;

/**
 * @author Ilya on 30.01.2018.
 */

public class NewMessageNotification {
  @SerializedName("msg_type") public String action = "chat";
  @SerializedName("userId") String userId = "";
  @SerializedName("userName") String userName = "";
  @SerializedName("message") String message = "";

  public NewMessageNotification(String userId, String userName, String message) {
    this.userId = userId;
    this.userName = userName;
    this.message = message;
  }
}
