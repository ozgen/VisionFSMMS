package com.shoppingmall.smms.Helpers;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Environment;
import android.util.Base64;

import androidx.core.app.NotificationCompat;

import com.shoppingmall.smms.MyFirebaseMessagingService;
import com.shoppingmall.smms.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileHelper {
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

    public static void convertBase64StringToFileAndStoreIt(Context _context, String base64PDf, String fileName, String mimeType) throws IOException {
        final File dwldsPath = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS) + "/" + fileName);
        byte[] pdfAsBytes = Base64.decode(base64PDf.replaceFirst("^data:" + mimeType + ";base64,", ""), 0);
        FileOutputStream os;
        os = new FileOutputStream(dwldsPath, false);
        os.write(pdfAsBytes);
        os.flush();

        if (dwldsPath.exists()) {
            NotificationCompat.Builder b = new NotificationCompat.Builder(_context, "MY_DL")
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("Downloaded File")
                    .setContentText(fileName)
                    .setAutoCancel(true);
            NotificationManager nm = (NotificationManager) _context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null)
                nm.notify(NotificationHelper.getNotificationCount(), b.build());
        }
    }
}
