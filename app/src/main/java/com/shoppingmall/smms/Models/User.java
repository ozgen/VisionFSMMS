package com.shoppingmall.smms.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("_id")
    @Expose
    public String userID;

    @SerializedName("email")
    @Expose
    public String email;

    @SerializedName("name")
    @Expose
    public String name;

    @SerializedName("surname")
    @Expose
    public String surname;

    @SerializedName("phone")
    @Expose
    public String phone;

    @SerializedName("img")
    @Expose
    public Img Img;

    @SerializedName("role")
    @Expose
    public String role;
}
