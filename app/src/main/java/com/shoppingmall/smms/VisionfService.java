package com.shoppingmall.smms;

import com.shoppingmall.smms.Models.FCMToken;
import com.shoppingmall.smms.Models.InviteResponse;
import com.shoppingmall.smms.Models.ResponseMessage;
import com.shoppingmall.smms.Models.StaffCard;
import com.shoppingmall.smms.Models.User;
import com.shoppingmall.smms.Models.UserLogin;
import com.shoppingmall.smms.Models.UserLoginResult;
import com.shoppingmall.smms.Models.UserMacAddress;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

public interface VisionfService {
    @POST("api/login")
    Call<UserLoginResult> login(@Body UserLogin userLoginCredential);

    @POST("api/security/getUserPassCard")
    Call<StaffCard> getUserPassCard(@Body UserMacAddress user);

    @GET("api/getUserData/{userId}")
    Call<ResponseMessage<User>> getUserData(@Path("userId") String userID);

    @POST("api/security/macaddress")
    Call<ResponseMessage<String>> createMacAddress(@Body UserMacAddress user);

    @POST("api/push/subscribe")
    Call<ResponseMessage<String>> sendFCMTokenToServer(@Body FCMToken fcmToken);

    @GET("api/security/checkQrCode/{userID}/{qrCodeText}")
    Call<ResponseMessage<String>> checkQrCode(@Path("userID") String userID, @Path("qrCodeText") String qrCodeText);

    @GET
    Call<ResponseBody> downloadFileWithDynamicUrlSync(@Url String fileUrl);

    @POST("api/inviteResponse")
    Call<ResponseMessage<String>> sendInviteResponse(@Body InviteResponse inviteResponse);
}
