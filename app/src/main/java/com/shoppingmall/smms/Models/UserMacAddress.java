package com.shoppingmall.smms.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UserMacAddress {
    @SerializedName("userID")
    @Expose
    public String userID;

    @SerializedName("macaddress")
    @Expose
    public String macAddress;

    public UserMacAddress(String userID, String macAddress) {
        this.userID = userID;
        this.macAddress = macAddress;
    }
}
