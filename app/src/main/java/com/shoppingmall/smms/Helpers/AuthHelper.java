package com.shoppingmall.smms.Helpers;

import android.util.Log;

import com.shoppingmall.smms.ApiClient;
import com.shoppingmall.smms.Models.FCMToken;
import com.shoppingmall.smms.Models.ResponseMessage;
import com.shoppingmall.smms.Models.UserCard;
import com.shoppingmall.smms.Models.UserLogin;
import com.shoppingmall.smms.Models.UserLoginResult;
import com.shoppingmall.smms.Models.UserMacAddress;
import com.shoppingmall.smms.VisionfService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthHelper {
    private static String userID;
    private static String userEmail;
    private static String password;
    private static String apiToken;
    private static String fcmToken;
    private static UserCard userCard;

    private static boolean _isLoggedIn = false;

    public static boolean isLoggedIn() {
        return _isLoggedIn;
    }

    public static void setUserID(String userID) {
        AuthHelper.userID = userID;
    }

    public static String getUserID() { return userID; }

    public static void setUserEmail(String userEmail) {
        AuthHelper.userEmail = userEmail;
    }

    public static void setPassword(String password) {
        AuthHelper.password = password;
    }

    public static void setApiToken(String token) {
        AuthHelper.apiToken = token;
    }

    public static String getFcmToken() {
        return fcmToken;
    }

    public static void setFcmToken(String fcmToken) {
        AuthHelper.fcmToken = fcmToken;
    }

    public static UserCard getUserCard() { return userCard; }

    public static boolean login() {
        VisionfService visionfService = ApiClient.getClient();

        if (userEmail != null && password != null) {
            Call<UserLoginResult> callSync = visionfService.login(new UserLogin(userEmail, password));

            try {
                Response<UserLoginResult> response = callSync.execute();
                UserLoginResult apiResponse = response.body();

                if (response.code() == 200 && apiResponse != null) {
                    _isLoggedIn = true;
                    AuthHelper.apiToken = apiResponse.token;
                    ApiClient.setAccessToken(AuthHelper.apiToken);
                    AuthHelper.sendFCMTokenToServer();
                    AuthHelper.getStaffCardFromServer();
                    return true;
                }
            } catch (Exception ex) {
                Log.e("AuthHelper", ex.getMessage() != null ? ex.getMessage() : "Null");
            }
        }

        _isLoggedIn = false;
        return false;
    }

    public static boolean sendFCMTokenToServer() {
        if (fcmToken != null && _isLoggedIn) {
            VisionfService visionfService = ApiClient.getClient();

            Call<ResponseMessage> callSync = visionfService.sendFCMTokenToServer(new FCMToken( userID, fcmToken, null));

            try {
                Response<ResponseMessage> response = callSync.execute();
                ResponseMessage apiResponse = response.body();

                if (response.code() == 200 && apiResponse != null) {
                    return true;
                }
            } catch (Exception ex) {
                Log.e("AuthHelper", ex.getMessage() != null ? ex.getMessage() : "Null");
            }
        }

        return false;
    }

    private static void getStaffCardFromServer() {
        VisionfService _visionfService = ApiClient.getClient();
        Call<UserCard> call = _visionfService.getUserPassCard(new UserMacAddress(userID, NetworkHelper.getMacAddr()));
        call.enqueue(new Callback<UserCard>() {
            @Override
            public void onResponse(Call<UserCard> call, Response<UserCard> response) {
                if (response.code() == 200) {
                    userCard = response.body();
                }
            }

            @Override
            public void onFailure(Call<UserCard> call, Throwable t) {

            }
        });
    }
}
