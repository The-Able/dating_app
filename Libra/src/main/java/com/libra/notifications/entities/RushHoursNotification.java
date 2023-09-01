package com.libra.notifications.entities;

import com.google.gson.annotations.SerializedName;

/**
 * @author Ilya on 30.01.2018.
 */

public class RushHoursNotification {
  @SerializedName("msg_type") String action = "rushHours";
  @SerializedName("cafeId") String cafeId = "";
  @SerializedName("cafe_name") String cafeName = "";
  @SerializedName("user_count") int usersCount = -1;

  public RushHoursNotification(String cafeId, String cafeName, int usersCount) {
    this.cafeId = cafeId;
    this.cafeName = cafeName;
    this.usersCount = usersCount;
  }
}
