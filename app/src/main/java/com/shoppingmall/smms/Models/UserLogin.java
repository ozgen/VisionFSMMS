package com.shoppingmall.smms.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UserLogin {
    @SerializedName("email")
    @Expose
    public String email;

    @SerializedName("password")
    @Expose
    public String password;

    public UserLogin() {

    }

    public UserLogin(String userEmail, String userPassword) {
        this.email = userEmail;
        this.password =userPassword;
    }
}
