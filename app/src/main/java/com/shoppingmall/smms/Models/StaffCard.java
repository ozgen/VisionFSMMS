package com.shoppingmall.smms.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class StaffCard extends User {
    @SerializedName("qrcode")
    @Expose
    public String QRCode;

    @SerializedName("latestEnrtyDate")
    @Expose
    public String latestEnrtyDate;

    @SerializedName("acceptableSSIDs")
    @Expose
    public List<String> acceptableSSIDs;
}
