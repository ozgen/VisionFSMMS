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

    public static String getUserID() {
        return userID;
    }

    public static void setUserID(String userID) {
        AuthHelper.userID = userID;
    }

    public static void setUserEmail(String userEmail) {
        AuthHelper.userEmail = userEmail;
    }

    public static void setPassword(String password) {
        AuthHelper.password = password;
    }

    public static String getApiToken() {
        return AuthHelper.apiToken;
    }

    public static String getFcmToken() {
        return fcmToken;
    }

    public static void setFcmToken(String fcmToken) {
        AuthHelper.fcmToken = fcmToken;
    }

    public static User getUserInfo() {
        return userInfo;
    }

    public static String getSessionInjectScript() {
        return sessionInjectScript;
    }

    public static void setSessionInjectScript(String sessionInjectScript) {
        AuthHelper.sessionInjectScript = sessionInjectScript;
    }

    public static void login(final RunnableArg<ResponseMessage<UserLoginResult>> runnableArg) {
        VisionfService visionfService = ApiClient.getClient();
        Call<UserLoginResult> call = visionfService.login(new UserLogin(userEmail, password));

        call.enqueue(new Callback<UserLoginResult>() {
            @Override
            public void onResponse(Call<UserLoginResult> call, retrofit2.Response<UserLoginResult> response) {
                ResponseMessage<UserLoginResult> runnableRes = new ResponseMessage<>();
                int responseCode = response.code();
                if (responseCode == 200) {
                    UserLoginResult apiResponse = response.body();
                    runnableRes.success = false;
                    if (apiResponse != null && apiResponse.success) {
                        _isLoggedIn = true;
                        AuthHelper.apiToken = apiResponse.token;
                        ApiClient.setAccessToken(AuthHelper.apiToken);
                        AuthHelper.sendFCMTokenToServer();
                        AuthHelper.getUserDataFromServer();
                        runnableRes.success = true;
                        runnableRes.message = apiResponse;
                        runnableArg.run(runnableRes);
                        return;
                    }
                } else {
                    runnableArg.run(runnableRes);
                }

                _isLoggedIn = false;
            }

            @Override
            public void onFailure(Call<UserLoginResult> call, Throwable t) {
                ResponseMessage<UserLoginResult> runnableRes = new ResponseMessage<>();
                runnableRes.success = false;
                runnableArg.run(runnableRes);
            }
        });
    }

    public static void logOut() {
        userID = null;
        userEmail = null;
        password = null;
        apiToken = null;
        fcmToken = null;
        userInfo = null;
        sessionInjectScript = null;
        _isLoggedIn = false;
    }

    public static void sendFCMTokenToServer() {
        if (fcmToken != null && _isLoggedIn) {
            VisionfService visionfService = ApiClient.getClient();
            Call<ResponseMessage<String>> call = visionfService.sendFCMTokenToServer(new FCMToken(userID, fcmToken, null));

            call.enqueue(new Callback<ResponseMessage<String>>() {
                @Override
                public void onResponse(Call<ResponseMessage<String>> call, Response<ResponseMessage<String>> response) {

                }

                @Override
                public void onFailure(Call<ResponseMessage<String>> call, Throwable t) {

                }
            });
        }
    }

    public static void getUserDataFromServer() {
        if (!userID.isEmpty() && _isLoggedIn) {
            VisionfService _visionfService = ApiClient.getClient();
            Call<ResponseMessage<User>> call = _visionfService.getUserData();

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
