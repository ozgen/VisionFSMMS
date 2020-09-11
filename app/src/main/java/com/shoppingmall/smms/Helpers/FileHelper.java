package com.shoppingmall.smms.Helpers;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Base64;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.shoppingmall.smms.ApiClient;
import com.shoppingmall.smms.R;
import com.shoppingmall.smms.VisionfService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FileHelper {

    private static Context _mainContext;

    public static void setMainContext(Context _c) {
        _mainContext = _c;
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

    public static void convertBase64StringToFileAndStoreIt(Context _context, String base64PDf, String fileName, String mimeType) throws IOException {
        final File dwldsPath = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS) + "/" + fileName);
        byte[] pdfAsBytes = Base64.decode(base64PDf.replaceFirst("^data:" + mimeType + ";base64,", ""), 0);
        FileOutputStream os;
        os = new FileOutputStream(dwldsPath, false);
        os.write(pdfAsBytes);
        os.flush();

        if (dwldsPath.exists()) {
            showDownloadedFileNotification(_mainContext, dwldsPath, fileName);
        }
    }

    public static void saveFileFromUrl(String fileUrl, final String fileName) {
        VisionfService visionfService = ApiClient.getClient();

        Call<ResponseBody> call = visionfService.downloadFileWithDynamicUrlSync(fileUrl);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, final Response<ResponseBody> response) {
                if (response.isSuccessful()) {

                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... voids) {
                            boolean writtenToDisk = writeResponseBodyToDisk(response.body(), fileName);
                            return null;
                        }
                    }.execute();
                }
                else {
                    Toast.makeText(_mainContext, Resources.getSystem().getString(R.string.downloadFail), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(_mainContext, Resources.getSystem().getString(R.string.downloadFail), Toast.LENGTH_LONG).show();
            }
        });
    }

    private static String getMimeType(Uri uri) {
        String mimeType = null;
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            ContentResolver cr = _mainContext.getContentResolver();
            mimeType = cr.getType(uri);
        } else {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri
                    .toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    fileExtension.toLowerCase());
        }
        return mimeType;
    }

    private static boolean writeResponseBodyToDisk(ResponseBody body, String fileName) {
        try {
            final File dwldsPath = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS) + "/" + fileName);

            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                byte[] fileReader = new byte[4096];

                long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;

                inputStream = body.byteStream();
                outputStream = new FileOutputStream(dwldsPath);

                while (true) {
                    int read = inputStream.read(fileReader);

                    if (read == -1) {
                        break;
                    }

                    outputStream.write(fileReader, 0, read);
                    fileSizeDownloaded += read;

                    //Log.d("sekanbas", "file download: " + fileSizeDownloaded + " of " + fileSize);
                }

                outputStream.flush();

                return true;
            } catch (IOException e) {
                return false;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }

                if (dwldsPath.exists()) {
                    showDownloadedFileNotification(_mainContext, dwldsPath, fileName);
                }
            }
        } catch (IOException e) {
            return false;
        }
    }

    private static void showDownloadedFileNotification(Context _c, File path, String fileName) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = Uri.fromFile(path);

        intent.setDataAndType(uri, getMimeType(uri));
        final PendingIntent contentIntent = PendingIntent.getActivity(_c, 0, intent, 0);

        NotificationCompat.Builder b = new NotificationCompat.Builder(_c, "MY_DL")
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Downloaded File")
                .setContentText(fileName)
                .setContentIntent(contentIntent)
                .setAutoCancel(true);
        NotificationManager nm = (NotificationManager) _mainContext.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null)
            nm.notify(NotificationHelper.getNotificationCount(), b.build());
    }
}
