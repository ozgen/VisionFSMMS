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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class WebAppInterface {
    Context mContext;

    WebAppInterface(Context c) {
        mContext = c;
    }

    @JavascriptInterface
    public void userLoginHandler(String userID) {
        try {
            if (userID != null && !userID.isEmpty()) {

                MainActivity.saveToken();
                new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                MainActivity.gotoURL(MainActivity.urlStr);
                            }
                        },
                        500);
            }
        } catch (Exception e) {
            Log.e("token", e.toString());
        }
    }

    @JavascriptInterface
    public void userLogInInfoForAndroidWebView(String email, String password) {
        storeElement(MainActivity.ELEMENTID_EMAIL, email);
        storeElement(MainActivity.ELEMENTID_PASSWORD, password);
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
            Log.i("errorsk", e.toString());
        }
    }

    // For Download
    @JavascriptInterface
    public void getBase64FromBlobData(String base64Data, String fileName, String mimeType) throws IOException {
        convertBase64StringToFileAndStoreIt(base64Data, fileName, mimeType);
    }

    public static String getBase64StringFromBlobUrl(String blobUrl, String fileName, String mimeType) {
        if (blobUrl.startsWith("blob")) {
            return "javascript: var xhr = new XMLHttpRequest();" +
                    "xhr.open('GET', '" + blobUrl + "', true);" +
                    "xhr.setRequestHeader('Content-type','" + mimeType + "');" +
                    "xhr.responseType = 'blob';" +
                    "xhr.onload = function(e) {" +
                    "    if (this.status == 200) {" +
                    "        var blobFile = this.response;" +
                    "        var reader = new FileReader();" +
                    "        reader.readAsDataURL(blobFile);" +
                    "        reader.onloadend = function() {" +
                    "            base64data = reader.result;" +
                    "            Android.getBase64FromBlobData(base64data, '" + fileName + "','" + mimeType + "');" +
                    "        }" +
                    "    }" +
                    "};" +
                    "xhr.send();";
        }
        return "javascript: (function () {})();"; // No operation
    }

    private void convertBase64StringToFileAndStoreIt(String base64PDf, String fileName, String mimeType) throws IOException {
        final File dwldsPath = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS) + "/" + fileName);
        byte[] pdfAsBytes = Base64.decode(base64PDf.replaceFirst("^data:" + mimeType + ";base64,", ""), 0);
        FileOutputStream os;
        os = new FileOutputStream(dwldsPath, false);
        os.write(pdfAsBytes);
        os.flush();

        if (dwldsPath.exists()) {
            NotificationCompat.Builder b = new NotificationCompat.Builder(mContext, "MY_DL")
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("Downloaded File")
                    .setContentText(fileName)
                    .setAutoCancel(true);
            NotificationManager nm = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null)
                nm.notify(++MyFirebaseMessagingService.notifyCounter, b.build());
        }
    }

    public void storeElement(String id, String element) {
        SharedPreferences.Editor edit = ((MainActivity) mContext).getSharedPreferences("com.shoppingmall.smms",Activity.MODE_PRIVATE).edit();
        edit.putString(id, element);
        edit.commit();

    }
}