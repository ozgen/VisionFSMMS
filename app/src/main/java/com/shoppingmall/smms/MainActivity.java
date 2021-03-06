package com.shoppingmall.smms;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.shoppingmall.smms.Helpers.AuthHelper;
import com.shoppingmall.smms.Helpers.BiometricHelperBase;
import com.shoppingmall.smms.Helpers.FileHelper;
import com.shoppingmall.smms.Helpers.NetworkHelper;
import com.shoppingmall.smms.Helpers.NotificationHelper;
import com.shoppingmall.smms.Helpers.QRCodeHelper;
import com.shoppingmall.smms.Models.ConnectionStatus;
import com.shoppingmall.smms.Models.ResponseMessage;
import com.shoppingmall.smms.Models.StaffCard;
import com.shoppingmall.smms.Models.User;
import com.shoppingmall.smms.Models.UserLoginResult;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    protected static final String packageId = "com.shoppingmall.smms";
    protected static final String ELEMENTID_USERID = "userid";
    protected static final String ELEMENTID_EMAIL = "email";
    protected static final String ELEMENTID_PASSWORD = "password";
    protected static final String ELEMENTID_DONTASKBIOMETRIC = "dontaskagainbiometric";
    protected static final String ELEMENTID_USEBIOMETRIC = "usebiometric";
    private final static int FCR = 1;
    private final static int FILECHOOSER_RESULTCODE = 1;
    /// Permission Stage
    private static final int MY_PERMISSION_REQUEST_CODE = 123;
    protected static WebView webViewSMMS;
    protected static String urlStr = "";
    protected static String webSiteURL = "https://dev.visionf.com.tr";
    protected static SwipeRefreshLayout swipeRefreshLayout;
    private static Context _mainContext;
    private static ConnectionStatus connectionStatus;
    // The BroadcastReceiver that tracks network connectivity changes.
    private NetworkReceiver receiver = new NetworkReceiver();
    // File Chooser
    private String mCM;
    private Uri mCUri;
    private ValueCallback<Uri> mUM;
    private ValueCallback<Uri[]> mUMA;
    private SharedPreferences sharedPreferences;

    public static void gotoURL(String url) {
        if (url != null && !url.isEmpty()) {
            MainActivity.evaluateJavascriptStr("document.location = \"" + url + "\";");
        }
    }

    // TODO: Ne kadar gerekli ?
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

    protected static void showAlertDialog(String title, String message, String okButtonText, String cancelButtonText, final RunnableArg<Boolean> runnableArg, Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);

        if (okButtonText != null) {
            builder.setPositiveButton(okButtonText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (runnableArg != null) {
                        runnableArg.run(true);
                    }
                }
            });
        }

        if (cancelButtonText != null) {
            builder.setNeutralButton(cancelButtonText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (runnableArg != null) {
                        runnableArg.run(false);
                    }
                }
            });
        }

        if (okButtonText != null || cancelButtonText != null) {
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        _mainContext = this;
        final BiometricHelperBase biometricHelper = BiometricHelperBase.getInstance(this);
        sharedPreferences = getSharedPreferences(MainActivity.packageId, Activity.MODE_PRIVATE);
        FileHelper.setMainContext(_mainContext);
        NetworkHelper.setContext(getApplicationContext());
        connectionStatus = NetworkHelper.getCurrentlyConnectionStatus(_mainContext.getApplicationContext());

        checkPermission();

        // Fill User Credentials From Shared Preferences
        String _userID = sharedPreferences.getString(ELEMENTID_USERID, "");
        String _email = sharedPreferences.getString(ELEMENTID_EMAIL, "");
        String _password = sharedPreferences.getString(ELEMENTID_PASSWORD, "");
        Boolean _dontaskbiometric = Objects.equals(sharedPreferences.getString(ELEMENTID_DONTASKBIOMETRIC, "false"), "true");
        Boolean _usebiometric = Objects.equals(sharedPreferences.getString(ELEMENTID_USEBIOMETRIC, "false"), "true");

        if (!_userID.isEmpty() && !_email.isEmpty() && !_password.isEmpty()) {
            AuthHelper.setUserID(_userID);
            AuthHelper.setUserEmail(_email);
            AuthHelper.setPassword(_password);

            final Boolean canAbleBiometrichw = biometricHelper.checkBiometricHardware();

            if (_dontaskbiometric && _usebiometric && canAbleBiometrichw != null && canAbleBiometrichw) {
                biometricHelper.authUser(new RunnableArg<Boolean>() {
                    @Override
                    public void run() {
                        if (this.getArg()) {
                            if (!AuthHelper.isLoggedIn()) {
                                loginProcess();
                            } else {
                                initialWebView();
                            }
                        } else {
                            clearStoredUserData(true);
                            initialWebView();
                        }
                    }
                });
            } else if (!_dontaskbiometric) {
                if (canAbleBiometrichw == null || canAbleBiometrichw) {
                    showAlertDialog("Biyometrik Kimlik Doğrulama", "Otomatik giriş işlemini daha güvenli bir hale getirmek için biyometrik doğrulama kullanmak istermisiniz ? ", "Evet", "Bir Daha Sorma", new RunnableArg<Boolean>() {
                        @Override
                        public void run() {
                            if (this.getArg()) {
                                if (canAbleBiometrichw == null) {
                                    biometricHelper.openFingerprintSetting();
                                } else {
                                    storeElement(ELEMENTID_DONTASKBIOMETRIC, "true");
                                    storeElement(ELEMENTID_USEBIOMETRIC, "true");
                                }
                            } else {
                                storeElement(ELEMENTID_DONTASKBIOMETRIC, "true");
                                storeElement(ELEMENTID_USEBIOMETRIC, "false");
                            }
                            
                            if (!AuthHelper.isLoggedIn()) {
                                loginProcess();
                            }
                        }
                    }, _mainContext);
                }
            } else {
                if (!AuthHelper.isLoggedIn()) {
                    loginProcess();
                }
            }
        } else {
            initialWebView();
        }

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) return;

                        String fcmToken = task.getResult().getToken();
                        AuthHelper.setFcmToken(fcmToken);
                        AuthHelper.sendFCMTokenToServer();
                    }
                });

        initialSecurtyPersonelProcess();
        processIntent(getIntent());
        initialSwipeRefreshLayout();
    }

    private void loginProcess() {
        if (connectionStatus.mobileConnected || connectionStatus.wifiConnected) {
            final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this,
                    R.style.AppTheme_Dark_Dialog);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage(getApplication().getResources().getString(R.string.loginprocess));
            progressDialog.show();

            AuthHelper.login(new RunnableArg<ResponseMessage<UserLoginResult>>() {
                @Override
                public void run() {
                    ResponseMessage<UserLoginResult> responseMessage = this.getArg();
                    if (!responseMessage.success) {
                        clearStoredUserData();
                    }
                    progressDialog.dismiss();
                    initialWebView();
                }
            });
        } else {
            final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this,
                    R.style.AppTheme_Dark_Dialog);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage(getResources().getString(R.string.errorHttpLoad));
            progressDialog.show();
        }
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

    private void processIntent(final Intent intent) {
        String urlValue = intent.getStringExtra("url");
        String messageTypes = intent.getStringExtra("type");
        String actionCode = intent.getStringExtra("actionCode");
        final int notificationId = intent.getIntExtra("notificationId", -1);

        if (actionCode != null) {
            switch (actionCode) {
                case "manualReply":
                    NotificationHelper.cancelNotification(this, notificationId);
                    showAlertDialog("Toplantı Katılım Teyidi", "Lütfen yeni açılacak olan pencedeki \"Toplantı Katılım Teyidi\" bölümüde bulunan \"Katılmayacağım\" butonuna tıkladıktan sonra açılan yazı alanına katılmama sebebinizi yazınız.",
                            "Tamam", null, null);
                    break;
                case "sendingError":
                    showAlertDialog("Bir Sorun Oluştu", "Toplantı katılım teyidine vermiş olduğunuz cevap sisteme iletilemedi.",
                            "Daha sonra tekrar dene", "Yeniden Dene", new RunnableArg<Boolean>() {
                                @Override
                                public void run() {
                                    if (!this.getArg()) {
                                        NotificationHelper.cancelNotification(_mainContext, notificationId);
                                        NotificationHelper.showInviteNotification(_mainContext, NotificationHelper.bundleToMap(intent.getExtras()));
                                    } else {

                                    }
                                }
                            });
                    break;
            }
        }

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

    @SuppressLint({"JavascriptInterface", "SetJavaScriptEnabled"})
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

                SharedPreferences prefs = getSharedPreferences(MainActivity.packageId, Activity.MODE_PRIVATE);
                String email = prefs.getString(ELEMENTID_EMAIL, "");
                String password = prefs.getString(ELEMENTID_PASSWORD, "");
                String signinURL = webSiteURL + "/#!/login/signin";
                String profilePageURL = webSiteURL + "/#!/app/pages/user";
                String signoutURL = webSiteURL + "/#!/login/signout";

                if (email.length() > 0 && password.length() > 0 && url.equals(signinURL)) {
                    view.evaluateJavascript("javascript:window.actions.initLoginForAndroid('" + email + "' , '" + password + "') ; ", null);
                } else if (url.equals(signoutURL)) {
                    clearStoredUserData();
                } else if (url.equals(profilePageURL)) {
                    User userInfo = AuthHelper.getUserInfo();
                    if (userInfo.role.equals("SECURITYDEBUG")) {
                        if (connectionStatus.mobileConnected) {
                            forceWifiConnection(getResources().getString(R.string.denymobileconnection));
                        } else if (connectionStatus.wifiConnected) {
                            // TODO: Loading Start

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    ResponseMessage<String> macRegisterRes = AuthHelper.sendMacAddressToServer();

                                    if (macRegisterRes != null) {

                                        switch (macRegisterRes.responseCode) {
                                            case 200: // Mac adresi zaten bu kullanıcıya kayıtlı
                                                StaffCard _staffCard = AuthHelper.getStaffCardFromServer();
                                                if (_staffCard != null) {
                                                    boolean checkSSID = false;
                                                    for (String _ssid : _staffCard.acceptableSSIDs) {
                                                        checkSSID |= _ssid.equals(connectionStatus.SSID);
                                                    }

                                                    if (checkSSID) {
                                                        showStaffCard(_staffCard);
                                                    } else {
                                                        forceWifiConnection(getResources().getString(R.string.wrongwifiSSID));
                                                    }
                                                } else {

                                                }
                                                break;
                                            case 201: // Yeni mac adresi ilk defa eklendi.
                                                showStaticLayout(R.layout.staff_new_record);
                                                break;
                                            case 203: // TODO: Yetkisiz işlem - Farklı bir cihazdan giriş yapıldı
                                                showStaticLayout(R.layout.staff_different_macaddress);
                                                break;
                                            case 404: // Hata Oluştu
                                                break;
                                            default: // Beklenmeyen Hata
                                                break;
                                        }
                                    } else {
                                        // Tekrar Dene
                                    }
                                }
                            }).start();

                            // TODO: Loading Stop
                        } else { // İnternete çıkmak için aktif bağlantı yok
                            // TODO: Bir diyalog penceresi ile uyarılmalı veya Ayarlara Yönlendirilmeli (Wifi Seçme Ekranına)
                        }
                    }
                }

                if (url.indexOf("#!/app/pages/inbox") != -1) {
                    swipeRefreshLayout.setEnabled(false);
                } else {
                    swipeRefreshLayout.setEnabled(true);
                }
            }

            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (request.getMethod().equals("GET") && request.getUrl().toString().startsWith(webSiteURL) && error.getErrorCode() == -2) {
                    MainActivity.evaluateJavascriptStr("document.body.innerHTML = \"<span></span>\";");
                    final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this,
                            R.style.AppTheme_Dark_Dialog);
                    progressDialog.setIndeterminate(true);
                    progressDialog.setMessage(getResources().getString(R.string.errorHttpLoad));
                    progressDialog.show();
                }
                super.onReceivedError(view, request, error);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    MainActivity.evaluateJavascriptStr("document.body.innerHTML = \"<span></span>\";");
                    final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this,
                            R.style.AppTheme_Dark_Dialog);
                    progressDialog.setIndeterminate(true);
                    progressDialog.setMessage(getResources().getString(R.string.errorHttpLoad));
                    progressDialog.show();
                }
                super.onReceivedError(view, errorCode, description, failingUrl);
            }
        });

        webViewSMMS.setWebChromeClient(new WebChromeClient() {
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
                if (mUMA != null) {
                    mUMA.onReceiveValue(null);
                }
                mUMA = filePathCallback;
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(MainActivity.this.getPackageManager()) != null) {
                    File photoFile = null;
                    Uri photoUri = null;
                    try {
                        if (Build.VERSION.SDK_INT >= 29) {
                            photoUri = createImageUri();
                            mCUri = photoUri;
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                        } else {
                            photoFile = createImageFile();
                            takePictureIntent.putExtra("PhotoPath", mCM);
                        }
                    } catch (IOException ex) {
                        Log.e("Webview", "Image file creation failed", ex);
                    }
                    if (photoFile != null) {
                        mCM = "file:" + photoFile.getAbsolutePath();
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                    } else if (photoUri == null) {
                        takePictureIntent = null;
                    }
                }

                Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
                contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
                contentSelectionIntent.setType("*/*");
                Intent[] intentArray;
                if (takePictureIntent != null) {
                    intentArray = new Intent[]{takePictureIntent};
                } else {
                    intentArray = new Intent[0];
                }

                Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
                chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
                chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
                startActivityForResult(chooserIntent, FCR);
                return true;
            }
        });

        // WebView.setWebContentsDebuggingEnabled(true);
        webViewSMMS.getSettings().setAppCachePath(this.getApplicationContext().getCacheDir().getAbsolutePath() + "/cache");
        webViewSMMS.getSettings().setDatabaseEnabled(true);
        webViewSMMS.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        webViewSMMS.getSettings().setDomStorageEnabled(true);

        webViewSMMS.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimeType, long contentLength) {
                String fileName = URLUtil.guessFileName(url, contentDisposition, mimeType);
                if (url.startsWith("blob")) {
                    webViewSMMS.loadUrl(FileHelper.getBase64StringFromBlobUrl(url, fileName, mimeType));
                } else {

                }
            }
        });

        webViewSMMS.getSettings().setJavaScriptEnabled(true);
        webViewSMMS.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webViewSMMS.addJavascriptInterface(new WebAppInterface(this), "Android");
        webViewSMMS.getSettings().setAllowFileAccess(true);
        webViewSMMS.getSettings().setAllowContentAccess(true);
        webViewSMMS.getSettings().setAllowFileAccessFromFileURLs(true);
        if (AuthHelper.isLoggedIn()) {
            webViewSMMS.loadUrl(MainActivity.webSiteURL + "/#!/login/fastlogin/" + AuthHelper.getApiToken());
        } else {
            String landingPageURL = webSiteURL + "/#!/landing/welcome";
            webViewSMMS.loadUrl(landingPageURL);
        }
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

    private void initialSecurtyPersonelProcess() {
        // Registers BroadcastReceiver to track network connection changes.
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        receiver = new NetworkReceiver(this);
        this.registerReceiver(receiver, filter);

        NetworkReceiver.addConnectionTypeChangeListener(new RunnableArg<ConnectionStatus>() {
            @Override
            public void run() {
                if (this.getArg() != null) {
                    MainActivity.connectionStatus = this.getArg();
                    if (connectionStatus.wifiConnected) {
                        // Toast.makeText(getApplicationContext(), String.format("Wifi -> %s", MainActivity.connectionStatus.SSID), Toast.LENGTH_SHORT).show();
                    }
                    // Toast.makeText(getApplicationContext(), String.format("Wifi -> %b, BroadBand -> %b", MainActivity.connectionStatus.wifiConnected, MainActivity.connectionStatus.mobileConnected), Toast.LENGTH_SHORT).show();
                }
            }
        });

        String macAddress = NetworkHelper.getMacAddr(this);
        // Toast.makeText(getApplicationContext(), macAddress, Toast.LENGTH_LONG).show();
    }

    private void showStaffCard(final StaffCard staffCard) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (connectionStatus != null) {
                    if (connectionStatus.wifiConnected) {
                        final Dialog staffCardDialog = new Dialog(_mainContext);
                        staffCardDialog.setContentView(R.layout.personel_card);

                        final ImageView staffImageView = staffCardDialog.findViewById(R.id.staffProfileImageViewer);
                        final ImageView qrCodeViewer = staffCardDialog.findViewById(R.id.qrCodeImageView);

                        final TextView staffNameAndSurname = staffCardDialog.findViewById(R.id.nameSurname);
                        final TextView latestEntryDate = staffCardDialog.findViewById(R.id.lastEntryDate);
                        final TextView cellPhone = staffCardDialog.findViewById(R.id.cellphone);

                        staffNameAndSurname.setText(String.format("%s  %s", staffCard.name, staffCard.surname));
                        latestEntryDate.setText(staffCard.latestEnrtyDate);
                        cellPhone.setText(staffCard.phone);

                        byte[] decodedString = Base64.decode(staffCard.Img.base64Data, Base64.DEFAULT);
                        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        staffImageView.setImageBitmap(decodedByte);

                        Bitmap bitmap = QRCodeHelper
                                .newInstance(_mainContext)
                                .setContent(staffCard.QRCode)
                                .setErrorCorrectionLevel(ErrorCorrectionLevel.Q)
                                .setMargin(2)
                                .getQRCOde();
                        qrCodeViewer.setImageBitmap(bitmap);

                        staffCardDialog.show();
                    }
                }
            }
        });
    }

    private void showStaticLayout(@LayoutRes final int layoutID) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final Dialog staticContentDialog = new Dialog(_mainContext);
                staticContentDialog.setContentView(layoutID);

                staticContentDialog.show();
            }
        });
    }

    private void forceWifiConnection(final String toastMessage) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(_mainContext, toastMessage, Toast.LENGTH_LONG).show();
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
            }
        });
    }

    // For File Chooser
    public void openFileChooser(ValueCallback<Uri> uploadMsg) {
        this.openFileChooser(uploadMsg, "*/*");
    }

    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
        this.openFileChooser(uploadMsg, acceptType, null);
    }

    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("*/*");
        MainActivity.this.startActivityForResult(Intent.createChooser(i, "File Browser"), FILECHOOSER_RESULTCODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (Build.VERSION.SDK_INT >= 21) {
            Uri[] results = null;
            //Check if response is positive
            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == FCR) {
                    if (null == mUMA) {
                        return;
                    }
                    if (intent == null) {
                        //Capture Photo if no image available
                        if (mCM != null) {
                            results = new Uri[]{Uri.parse(mCM)};
                        } else if (mCUri != null) {
                            results = new Uri[]{mCUri};
                        }
                    } else {
                        String dataString = intent.getDataString();
                        if (dataString != null) {
                            results = new Uri[]{Uri.parse(dataString)};
                        } else if (mCUri != null) {
                            results = new Uri[]{mCUri};
                        }
                    }
                }
            }
            mUMA.onReceiveValue(results);
            mUMA = null;
        } else {
            if (requestCode == FCR) {
                if (null == mUM) return;
                Uri result = intent == null || resultCode != RESULT_OK ? null : intent.getData();
                mUM.onReceiveValue(result);
                mUM = null;
            }
        }
    }

    // Create an image file
    private File createImageFile() throws IOException {
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "img_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    private Uri createImageUri() {
        String status = Environment.getExternalStorageState();
        if (status.equals(Environment.MEDIA_MOUNTED)) {
            return MainActivity.this.getApplicationContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues());
        } else {
            return MainActivity.this.getApplicationContext().getContentResolver().insert(MediaStore.Images.Media.INTERNAL_CONTENT_URI, new ContentValues());
        }
    }

    private void clearStoredUserData() {
        clearStoredUserData(false);
    }

    private void clearStoredUserData(Boolean allDelete) {
        String dontAskMeBiometric = sharedPreferences.getString(ELEMENTID_DONTASKBIOMETRIC, "false");
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.clear();
        if (!allDelete) {
            edit.putString(ELEMENTID_DONTASKBIOMETRIC, dontAskMeBiometric);
        }
        edit.apply();
    }

    private void showAlertDialog(String title, String message, String okButtonText, String cancelButtonText, final RunnableArg<Boolean> runnableArg) {
        showAlertDialog(title, message, okButtonText, cancelButtonText, runnableArg, this);
    }

    private void storeElement(String id, String element) {
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putString(id, element);
        edit.apply();
    }
}
