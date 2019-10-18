package com.shoppingmall.smms;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

public class MainActivity extends AppCompatActivity {

    protected static WebView webViewSMMS;
    protected static String userFCMToken = "";
    protected static String urlStr = "";
    protected static String webSiteURL = "http://192.168.1.55:3000";
    protected static SwipeRefreshLayout swipeRefreshLayout;
    private ViewTreeObserver.OnScrollChangedListener myOnScrollChangedListener;


    protected static final String ELEMENTID_EMAIL = "email";

    protected static final String ELEMENTID_PASSWORD = "password";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermission();

        checkUsageAgreement();

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) return;

                        String token = task.getResult().getToken();
                        MainActivity.userFCMToken = token;
                    }
                });

        processIntent(getIntent());
        initialSwipeRefreshLayout();
        initialWebView();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (intent.getBooleanExtra("reload", false)) {
            MainActivity.gotoURL(MainActivity.webSiteURL);
        } else {
            processIntent(intent);
        }
        super.onNewIntent(intent);
    }

    @Override
    public void onBackPressed() {
        MainActivity.webViewSMMS.goBack();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        webViewSMMS.saveState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        webViewSMMS.restoreState(savedInstanceState);
    }

    private void processIntent(Intent intent) {
        String urlValue = intent.getStringExtra("url");
        String messageTypes = intent.getStringExtra("type");

        if (messageTypes != null && messageTypes.equals("chat")) {
            // New Chat Message
        } else if (urlValue != null && !urlValue.isEmpty()) {
            if (MainActivity.webViewSMMS == null) {
                MainActivity.urlStr = urlValue;
            } else {
                MainActivity.gotoURL(urlValue);
            }
        }
    }

    public static void saveToken() {
        MainActivity.evaluateJavascriptStr("window.actions.saveToken({deviceToken: \'" + MainActivity.userFCMToken + "\', oldToken: null});");
    }

    public static void gotoURL(String url) {
        if (url != null && !url.isEmpty()) {
            MainActivity.evaluateJavascriptStr("document.location = \"" + url + "\";");
        }
    }

    public static void evaluateJavascriptStr(String evaluateString) {
        if ((evaluateString != null) && !evaluateString.isEmpty() && webViewSMMS != null) {
            final String finalEvaluateString = evaluateString;
            webViewSMMS.post(new Runnable() {
                @Override
                public void run() {
                    webViewSMMS.evaluateJavascript(finalEvaluateString, null);
                }
            });
        }
    }

    @SuppressLint("JavascriptInterface")
    private void initialWebView() {
        webViewSMMS = findViewById(R.id.webViewSMMS);

        webViewSMMS.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        if (Build.VERSION.SDK_INT >= 19) {
            webViewSMMS.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            webViewSMMS.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            webViewSMMS.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        }

        webViewSMMS.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String urlNewString) {
                view.loadUrl(urlNewString);
                return false;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap facIcon) {
                swipeRefreshLayout.setRefreshing(true);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                swipeRefreshLayout.setRefreshing(false);

                SharedPreferences prefs = getSharedPreferences("com.shoppingmall.smms", Activity.MODE_PRIVATE);
                String email = prefs.getString(ELEMENTID_EMAIL, "");
                String password = prefs.getString(ELEMENTID_PASSWORD, "");
                String signinURL = webSiteURL + "/#/login/signin";

                if (email.length() > 0 && password.length() > 0 && url.equals(signinURL)) {
                    view.evaluateJavascript("javascript:window.actions.initLoginForAndroid('" + email+ "' , '"+ password +"') ; ", null);
                }

            }
        });

        webViewSMMS.setWebChromeClient(new WebChromeClient() {
        });
        // TODO: remove setWebContentsDebuggingEnabled
        WebView.setWebContentsDebuggingEnabled(true);

        webViewSMMS.getSettings().setAppCachePath(this.getApplicationContext().getCacheDir().getAbsolutePath() + "/cache");
        webViewSMMS.getSettings().setDatabaseEnabled(true);
        webViewSMMS.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        webViewSMMS.getSettings().setDomStorageEnabled(true);

        webViewSMMS.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimeType, long contentLength) {
                String fileName = URLUtil.guessFileName(url, contentDisposition, mimeType);
                webViewSMMS.loadUrl(WebAppInterface.getBase64StringFromBlobUrl(url, fileName, mimeType));
            }
        });

        webViewSMMS.getSettings().setJavaScriptEnabled(true);
        webViewSMMS.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webViewSMMS.addJavascriptInterface(new WebAppInterface(this), "Android");
        webViewSMMS.getSettings().setAllowFileAccess(true);
        webViewSMMS.getSettings().setAllowFileAccessFromFileURLs(true);
        webViewSMMS.loadUrl(MainActivity.webSiteURL);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    if (webViewSMMS.canGoBack()) {
                        webViewSMMS.goBack();
                    } else {
                        finish();
                    }
                    return true;
            }

        }
        return super.onKeyDown(keyCode, event);
    }

    private void initialSwipeRefreshLayout() {
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                MainActivity.webViewSMMS.reload();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    /// Permission Stage
    private static final int MY_PERMISSION_REQUEST_CODE = 123;

    protected void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    // show an alert dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("Write external storage permission is required.");
                    builder.setTitle("Please grant permission");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(
                                    MainActivity.this,
                                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    MY_PERMISSION_REQUEST_CODE
                            );
                        }
                    });
                    builder.setNeutralButton("Cancel", null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
                    // Request permission
                    ActivityCompat.requestPermissions(
                            this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            MY_PERMISSION_REQUEST_CODE
                    );
                }
            } else {
                // Permission already granted
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                } else {
                    // Permission denied
                }
            }
        }
    }

    private void checkUsageAgreement() {
        final SharedPreferences userDetails = this.getSharedPreferences("userdetails", MODE_PRIVATE);
        boolean usageAgreement = userDetails.getBoolean("usageagreement", false);

        if (usageAgreement)
            return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.alert_dialog_usage_agreement, null);
        builder.setPositiveButton(R.string.allow, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                SharedPreferences.Editor edit = userDetails.edit();
                edit.putBoolean("usageagreement", true);
                edit.apply();
            }
        });
        builder.setNegativeButton(R.string.denny, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                MainActivity.this.finish();
                System.exit(0);
            }
        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                MainActivity.this.finish();
                System.exit(0);
            }
        });

        TextView textView = (TextView) dialogLayout.findViewById(R.id.usageAgreementLink);
        textView.setMovementMethod(LinkMovementMethod.getInstance());

        builder.setView(dialogLayout);
        builder.show();
    }


}
