package com.shoppingmall.smms.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UserLoginResult {
    @SerializedName("success")
    @Expose
    public Boolean success;

    @SerializedName("message")
    @Expose
    public String message;

    @SerializedName("id")
    @Expose
    public String userID;

    @SerializedName("token")
    @Expose
    public String token;
}
