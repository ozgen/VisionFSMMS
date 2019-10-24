package com.shoppingmall.smms.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class FCMToken {
    @SerializedName("user")
    @Expose
    public String userID;

    @SerializedName("subscription")
    @Expose
    public FCMTokenSubscription subscription;

    public FCMToken(String userID, String deviceToken, String oldDeviceToken) {
        this.userID = userID;
        FCMTokenSubscription _subscribtion = new FCMTokenSubscription();
        _subscribtion.deviceToken = deviceToken;
        _subscribtion.oldDeviceToken = oldDeviceToken;
        this.subscription = _subscribtion;
    }
}

class FCMTokenSubscription {
    @SerializedName("endpoint")
    @Expose
    public String deviceToken;

    @SerializedName("target")
    @Expose
    public String target = "Android";

    @SerializedName("oldDeviceToken")
    @Expose
    public String oldDeviceToken;
}