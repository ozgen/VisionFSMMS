package com.shoppingmall.smms.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ResponseMessage<T> {
    @SerializedName("success")
    @Expose
    public Boolean success;

    @SerializedName("message")
    @Expose
    public T message;

    public int responseCode = 0;
}
