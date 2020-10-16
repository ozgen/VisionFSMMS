package com.shoppingmall.smms;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.webkit.JavascriptInterface;

import androidx.appcompat.app.AppCompatActivity;

import com.shoppingmall.smms.Helpers.AuthHelper;
import com.shoppingmall.smms.Helpers.BiometricHelperBase;
import com.shoppingmall.smms.Helpers.FileHelper;
import com.shoppingmall.smms.Models.ResponseMessage;
import com.shoppingmall.smms.Models.UserLoginResult;

import java.io.IOException;
import java.util.Objects;

import static com.shoppingmall.smms.MainActivity.ELEMENTID_DONTASKBIOMETRIC;
import static com.shoppingmall.smms.MainActivity.ELEMENTID_USEBIOMETRIC;

public class WebAppInterface {
    Context mContext;
    SharedPreferences sharedPreferences;
    Boolean dontaskbiometric = false;
    Boolean usebiometric = false;

    WebAppInterface(Context c) {
        sharedPreferences = c.getSharedPreferences(MainActivity.packageId, Activity.MODE_PRIVATE);
        mContext = c;
    }

    @JavascriptInterface
    public void userLogInInfoForAndroidWebView(String userID, String email, String password) {
        if (!userID.isEmpty() && !email.isEmpty() && !password.isEmpty()) {
            AuthHelper.setUserID(userID);
            AuthHelper.setUserEmail(email);
            AuthHelper.setPassword(password);
            storeElement(MainActivity.ELEMENTID_USERID, userID);
            storeElement(MainActivity.ELEMENTID_EMAIL, email);
            storeElement(MainActivity.ELEMENTID_PASSWORD, password);
            final BiometricHelperBase biometricHelper = BiometricHelperBase.getInstance((AppCompatActivity) mContext);

            if (!AuthHelper.isLoggedIn()) {
                final ProgressDialog progressDialog = new ProgressDialog(mContext,
                        R.style.AppTheme_Dark_Dialog);
                progressDialog.setIndeterminate(true);
                progressDialog.setMessage(mContext.getResources().getString(R.string.loginRedirect));
                progressDialog.show();

                refreshVariables();

                AuthHelper.login(new RunnableArg<ResponseMessage<UserLoginResult>>() {
                    @Override
                    public void run() {
                        ResponseMessage<UserLoginResult> responseMessage = this.getArg();
                        if (responseMessage.success) {
                            AuthHelper.sendFCMTokenToServer();
                            final Boolean canAbleBiometricHw = biometricHelper.checkBiometricHardware();

                            if (!dontaskbiometric) {
                                if (canAbleBiometricHw == null || canAbleBiometricHw == true) {
                                    MainActivity.showAlertDialog("Biyometrik Kimlik Doğrulama", "Otomatik giriş işlemini daha güvenli bir hale getirmek için biyometrik doğrulama kullanmak istermisiniz ? ", "Evet", "Bir Daha Sorma", new RunnableArg<Boolean>() {
                                        @Override
                                        public void run() {
                                            if (this.getArg()) {
                                                if (canAbleBiometricHw == null) {
                                                    biometricHelper.openFingerprintSetting();
                                                } else {
                                                    storeElement(ELEMENTID_DONTASKBIOMETRIC, "true");
                                                    storeElement(ELEMENTID_USEBIOMETRIC, "true");
                                                }
                                            } else {
                                                storeElement(ELEMENTID_DONTASKBIOMETRIC, "true");
                                                storeElement(ELEMENTID_USEBIOMETRIC, "false");
                                            }
                                        }
                                    }, mContext);
                                }
                            }
                        }

                        MainActivity.gotoURL(MainActivity.urlStr);
                        progressDialog.dismiss();
                    }
                });
            }
        }
    }

    @JavascriptInterface
    public void userLogoutMessageHandler() {
        clearStoredUserData();
        AuthHelper.logOut();
    }

    @JavascriptInterface
    public void setSidebarState(String state) {
        try {
            if (state != null && state.equals("true")) {
                MainActivity.swipeRefreshLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        MainActivity.swipeRefreshLayout.setEnabled(true);
                    }
                });
            } else {
                MainActivity.swipeRefreshLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        MainActivity.swipeRefreshLayout.setEnabled(false);
                    }
                });
            }
        } catch (Exception e) {
            Log.e("WebAppInterface", e.toString());
        }
    }

    @JavascriptInterface
    public void downloadFile(String url, String fileName, String isBase64File) {
        if (isBase64File.equals("false")) {
            if (!url.isEmpty() && !fileName.isEmpty()) {
                FileHelper.saveFileFromUrl(url, fileName);
            }
        } else {
            if (!url.isEmpty() && !fileName.isEmpty()) {
                try {
                    FileHelper.convertBase64StringToFileAndStoreIt(mContext, url, fileName, "");
                } catch (IOException e) {
                    // e.printStackTrace();
                }
            }
        }
    }

    // For Download
    @JavascriptInterface
    public void getBase64FromBlobData(String base64Data, String fileName, String mimeType) throws IOException {
        FileHelper.convertBase64StringToFileAndStoreIt(mContext, base64Data, fileName, mimeType);
    }

    private void refreshVariables() {
        dontaskbiometric = Objects.equals(sharedPreferences.getString(ELEMENTID_DONTASKBIOMETRIC, "false"), "true");
        usebiometric = Objects.equals(sharedPreferences.getString(ELEMENTID_USEBIOMETRIC, "false"), "true");
    }

    private void storeElement(String id, String element) {
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putString(id, element);
        edit.apply();
    }

    private void clearStoredUserData() {
        refreshVariables();
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.clear();
        if (dontaskbiometric && !usebiometric) {
            edit.putString(ELEMENTID_DONTASKBIOMETRIC, "true");
        }
        edit.apply();
    }
}