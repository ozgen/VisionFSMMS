package com.shoppingmall.smms.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UserCard {
    @SerializedName("_id")
    @Expose
    public String userID;

    @SerializedName("qrcode")
    @Expose
    public String QRCode;

    @SerializedName("img")
    @Expose
    public Img Img;
}
