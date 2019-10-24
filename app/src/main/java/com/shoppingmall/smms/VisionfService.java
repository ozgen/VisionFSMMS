package com.shoppingmall.smms;

import com.shoppingmall.smms.Models.FCMToken;
import com.shoppingmall.smms.Models.ResponseMessage;
import com.shoppingmall.smms.Models.UserCard;
import com.shoppingmall.smms.Models.UserLogin;
import com.shoppingmall.smms.Models.UserLoginResult;
import com.shoppingmall.smms.Models.UserMacAddress;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface VisionfService {
    @POST("api/login")
    Call<UserLoginResult> login(@Body UserLogin userLoginCredential);

    @POST("api/security/getUserPassCard")
    Call<UserCard> getUserPassCard(@Body UserMacAddress user);

    @POST("api/security/macaddress")
    Call<ResponseMessage> createMacAddress(@Body UserMacAddress user);

    @POST("api/push/subscribe")
    Call<ResponseMessage> sendFCMTokenToServer(@Body FCMToken fcmToken);

}
