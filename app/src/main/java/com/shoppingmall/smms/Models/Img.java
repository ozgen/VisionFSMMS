package com.shoppingmall.smms.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Img {
    @SerializedName("fileType")
    @Expose
    public String fileType;

    @SerializedName("base64Data")
    @Expose
    public String base64Data;
}
