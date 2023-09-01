package com.libra.notifications.entities;

import com.google.gson.annotations.SerializedName;

/**
 * @author Ilya on 02.02.2018.
 */

public class NotificationModel {
  @SerializedName("body") public String body = "";
  @SerializedName("title") public String title = "";
  @SerializedName("icon") public String icon = "myicon";
}
