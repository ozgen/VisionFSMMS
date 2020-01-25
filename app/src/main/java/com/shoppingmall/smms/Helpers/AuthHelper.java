package com.shoppingmall.smms.Helpers;

import android.content.Context;
import android.util.Log;

import com.shoppingmall.smms.ApiClient;
import com.shoppingmall.smms.Models.FCMToken;
import com.shoppingmall.smms.Models.InviteResponse;
import com.shoppingmall.smms.Models.ResponseMessage;
import com.shoppingmall.smms.Models.StaffCard;
import com.shoppingmall.smms.Models.User;
import com.shoppingmall.smms.Models.UserLogin;
import com.shoppingmall.smms.Models.UserLoginResult;
import com.shoppingmall.smms.Models.UserMacAddress;
import com.shoppingmall.smms.RunnableArg;
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
    private static User userInfo;
    private static String sessionInjectScript;

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

    public static User getUserInfo() { return userInfo; }

    public static String getSessionInjectScript() {
        return sessionInjectScript;
    }

    public static void setSessionInjectScript(String sessionInjectScript) {
        AuthHelper.sessionInjectScript = sessionInjectScript;
    }

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
                    AuthHelper.getUserDataFromServer();
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

            Call<ResponseMessage<String>> callSync = visionfService.sendFCMTokenToServer(new FCMToken( userID, fcmToken, null));

            try {
                Response<ResponseMessage<String>> response = callSync.execute();
                ResponseMessage<String> apiResponse = response.body();

                if (response.code() == 200 && apiResponse != null) {
                    return true;
                }
            } catch (Exception ex) {
                Log.e("AuthHelper", ex.getMessage() != null ? ex.getMessage() : "Null");
            }
        }

        return false;
    }

    public static void getUserDataFromServer() {
        if(!userID.isEmpty() && _isLoggedIn) {
            VisionfService _visionfService = ApiClient.getClient();
            Call<ResponseMessage<User>> call = _visionfService.getUserData(userID);

            call.enqueue(new Callback<ResponseMessage<User>>() {
                @Override
                public void onResponse(Call<ResponseMessage<User>> call, Response<ResponseMessage<User>> response) {
                    if (response.code() == 200) {
                        ResponseMessage<User> res = response.body();
                        if (res != null && res.success) {
                            AuthHelper.userInfo = res.message;
                        }
                    }
                }

                @Override
                public void onFailure(Call<ResponseMessage<User>> call, Throwable t) {

                }
            });
        }
    }

    public static StaffCard getStaffCardFromServer() {
        VisionfService _visionfService = ApiClient.getClient();
        Call<StaffCard> callSync = _visionfService.getUserPassCard(new UserMacAddress(userID, NetworkHelper.getMacAddr()));

        try {
            Response<StaffCard> response = callSync.execute();
            StaffCard apiResponse = response.body();

            if (response.code() == 200) {
                return apiResponse;
            }
        } catch (Exception ex) {
            Log.e("AuthHelper", ex.getMessage() != null ? ex.getMessage() : "Null");
        }

        return null;
    }

    public static ResponseMessage<String> sendMacAddressToServer() {
        String macAddr = NetworkHelper.getMacAddr();
        if (_isLoggedIn && macAddr != null && !macAddr.isEmpty()) {
            VisionfService _visionfService = ApiClient.getClient();
            Call<ResponseMessage<String>> callSync = _visionfService.createMacAddress(new UserMacAddress(userID, macAddr));

            try {
                Response<ResponseMessage<String>> response = callSync.execute();
                ResponseMessage<String> apiResponse = response.body();

                if (apiResponse != null) {
                    apiResponse.responseCode = response.code();
                    return apiResponse;
                }

                if (response.code() == 404) {

                }
            } catch (Exception ex) {
                Log.e("AuthHelper", ex.getMessage() != null ? ex.getMessage() : "Null");
            }
        }

        return null;
    }

    public static void sendInviteResponse(final Context context, final int notificationID, InviteResponse inviteReqObj, final RunnableArg<ResponseMessage<String>> runnableArg) {
        VisionfService _visionfService = ApiClient.getClient();
        Call<ResponseMessage<String>> call = _visionfService.sendInviteResponse(inviteReqObj);

        call.enqueue(new Callback<ResponseMessage<String>>() {
            @Override
            public void onResponse(Call<ResponseMessage<String>> call, retrofit2.Response<ResponseMessage<String>> response) {
                int responseCode = response.code();
                if (responseCode == 200) {
                    ResponseMessage<String> res = response.body();
                    if (res != null && res.success) {
                        runnableArg.run(res);
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseMessage<String>> call, Throwable t) {
                ResponseMessage<String> responseMessage = new ResponseMessage<String>();
                responseMessage.success = false;
                responseMessage.message = t.getMessage();
                runnableArg.run(responseMessage);
            }
        });
    }
}
