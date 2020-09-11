package com.shoppingmall.smms;

import android.app.Activity;
import android.app.AlertDialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.shoppingmall.smms.Helpers.AuthHelper;
import com.shoppingmall.smms.Helpers.NotificationHelper;
import com.shoppingmall.smms.Models.InviteResponse;
import com.shoppingmall.smms.Models.ResponseMessage;

public class InputTextAlertActivity extends Activity {

    Context context;
    Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert);

        activity = this;
        context = this.getApplicationContext();

        processIntent(getIntent());
    }

    private void processIntent(final Intent intent) {
        String actionCode = intent.getStringExtra("actionCode");
        final int notificationId = intent.getIntExtra("notificationId", -1);
        final String meetingId = intent.getStringExtra("meetingId");
        final String userId = intent.getStringExtra("userId");

        if (actionCode != null) {
            switch (actionCode) {
                case "manualReply":
                    showAlertDialogWithInputText("Toplantı Katılım Teyidi", "Lütfen aşağıdaki kutucuğa toplantıya neden katılamayacağınızı yazınız",
                            "Gönder", "Kapat", new RunnableArg<ResponseMessage<String>>() {
                                @Override
                                public void run() {
                                    final ResponseMessage<String> alertDialogResponseMessage = this.getArg();

                                    if (alertDialogResponseMessage.success) {
                                        if (!alertDialogResponseMessage.message.isEmpty()) {

                                            final InviteResponse inviteReqObj = new InviteResponse();
                                            inviteReqObj.accept = false;
                                            inviteReqObj.MeetingId = meetingId;
                                            inviteReqObj.UserId = userId;
                                            inviteReqObj.responseInvitation = alertDialogResponseMessage.message;

                                            AuthHelper.sendInviteResponse(context, notificationId, inviteReqObj, new RunnableArg<ResponseMessage<String>>() {
                                                @Override
                                                public void run() {
                                                    ResponseMessage<String> responseMessage = this.getArg();

                                                    if (responseMessage.success) {
                                                        NotificationHelper.cancelNotification(context, notificationId);
                                                        activity.finish();
                                                    } else {
                                                        openIntentWithUrlExtra(context, MainActivity.class, intent.getExtras(), "sendingError");
                                                        activity.finish();
                                                    }
                                                }
                                            });
                                        }
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

        AlertDialog.Builder builder  = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);

        LayoutInflater inflater = LayoutInflater.from(this);
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

    private void openIntentWithUrlExtra (Context c, Class cls, Bundle bundle, String actionCode) {
        if (actionCode == null) actionCode = "loginRequired";
        Intent intent = new Intent(c, cls);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtras(bundle);
        intent.putExtra("actionCode", actionCode);
        c.startActivity(intent);
    }
}
