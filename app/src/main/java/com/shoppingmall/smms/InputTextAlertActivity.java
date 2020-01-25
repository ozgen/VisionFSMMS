package com.shoppingmall.smms;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.shoppingmall.smms.Models.ResponseMessage;

public class InputTextAlertActivity extends AppCompatActivity {

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert);

        context = InputTextAlertActivity.this.getApplicationContext();
        processIntent(getIntent());
    }

    private void processIntent(final Intent intent) {
        String urlValue = intent.getStringExtra("url");
        String messageTypes = intent.getStringExtra("type");
        String actionCode = intent.getStringExtra("actionCode");
        final int notificationId = intent.getIntExtra("notificationId", -1);

        if (actionCode != null) {
            switch (actionCode) {
                case "manualReply":
                    showAlertDialogWithInputText("Toplantı Katılım Teyidi", "Lütfen aşağıdaki kutucuğa toplantıya neden katılamayacağınızı yazınız",
                            "Gönder", "Kapat", new RunnableArg<ResponseMessage<String>>() {
                                @Override
                                public void run() {
                                    ResponseMessage<String> alertDialogResponseMessage = this.getArg();

                                    if (alertDialogResponseMessage.success) {

                                    } else {

                                    }
                                }
                            });
                    break;
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        processIntent(intent);
        super.onNewIntent(intent);
    }

    private void showAlertDialogWithInputText (final String title, final String message, final String okButtonText, final String cancelButtonText, final RunnableArg<ResponseMessage<String>> runnableArg) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);

        LayoutInflater inflater = LayoutInflater.from(context);
        final View dialogView = inflater.inflate(R.layout.alert_dialog_with_inputtext, null);
        builder.setView(dialogView);

        final TextView cInputText = dialogView.findViewById(R.id.cEditText);
        final ResponseMessage<String> responseMessage = new ResponseMessage<>();

        if (okButtonText != null) {
            builder.setPositiveButton(okButtonText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (runnableArg != null) {
                        responseMessage.success = true;
                        responseMessage.message = cInputText.getText().toString();
                        runnableArg.run(responseMessage);
                    }
                }
            });
        }

        if (cancelButtonText != null) {
            builder.setNeutralButton(cancelButtonText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (runnableArg != null) {
                        responseMessage.success = false;
                        responseMessage.message = cInputText.getText().toString();
                        runnableArg.run(responseMessage);
                    }
                }
            });
        }

        if (okButtonText != null || cancelButtonText != null) {
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }
}
