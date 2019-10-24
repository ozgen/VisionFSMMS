package com.shoppingmall.smms;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.webkit.JavascriptInterface;

import androidx.core.app.NotificationCompat;

import com.shoppingmall.smms.Helpers.AuthHelper;
import com.shoppingmall.smms.Helpers.FileHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class WebAppInterface {
    Context mContext;

    WebAppInterface(Context c) {
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

            AuthHelper.login(); // TODO: Login başarısız olursa Splash Screen Eklenmeli
            AuthHelper.sendFCMTokenToServer();

            new android.os.Handler().postDelayed(
                    new Runnable() {
                        public void run() {
                            MainActivity.gotoURL(MainActivity.urlStr);
                        }
                    },
                    500);
        } else {
            // TODO: Hatalı süreç, Yönetilmesi Gerekiyor
        }
    }

    @JavascriptInterface
    public void userLogoutHandler() {

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

    // For Download
    @JavascriptInterface
    public void getBase64FromBlobData(String base64Data, String fileName, String mimeType) throws IOException {
        FileHelper.convertBase64StringToFileAndStoreIt(mContext, base64Data, fileName, mimeType);
    }

    public void storeElement(String id, String element) {
        SharedPreferences.Editor edit = ((MainActivity) mContext).getSharedPreferences("com.shoppingmall.smms",Activity.MODE_PRIVATE).edit();
        edit.putString(id, element);
        edit.commit();

    }
}