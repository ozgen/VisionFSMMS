package com.shoppingmall.smms;

import android.app.Activity;
import android.app.IntentService;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.RemoteInput;

import com.shoppingmall.smms.Helpers.AuthHelper;
import com.shoppingmall.smms.Helpers.NotificationHelper;
import com.shoppingmall.smms.Models.InviteResponse;
import com.shoppingmall.smms.Models.ResponseMessage;

import static com.shoppingmall.smms.MainActivity.ELEMENTID_EMAIL;
import static com.shoppingmall.smms.MainActivity.ELEMENTID_PASSWORD;
import static com.shoppingmall.smms.MainActivity.ELEMENTID_USERID;

public class NotificationIntentService extends IntentService {

    public NotificationIntentService() {
        super("NotificationIntentService");
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        final Context context = getApplicationContext();

        Boolean acceptState = null;

        String reply = intent.getStringExtra("reply");
        String actionName = intent.getStringExtra("actionNotification");

        // Fill User Credentials From Shared Preferences
        SharedPreferences prefs = context.getSharedPreferences(MainActivity.packageId, Activity.MODE_PRIVATE);
        String _userID = prefs.getString(ELEMENTID_USERID, "");
        String _email = prefs.getString(ELEMENTID_EMAIL, "");
        String _password = prefs.getString(ELEMENTID_PASSWORD, "");

        if (actionName.equals("ACTION_ACCEPT")) {
            acceptState = true;
        } else if (actionName.equals("ACTION_NOTACCEPT")) {
            acceptState = false;
        } else {
            openIntentWithUrlExtra(context, MainActivity.class, intent.getExtras(), "openUrl");
        }

        if (intent != null) {
            final int notifyID = intent.getIntExtra("notificationId", -1);
            String meetingId = intent.getStringExtra("meetingId");

            InviteResponse inviteReqObj = new InviteResponse();
            inviteReqObj.accept = acceptState;
            inviteReqObj.MeetingId = meetingId;
            inviteReqObj.UserId = AuthHelper.getUserID();

            if (reply != null) inviteReqObj.responseInvitation = reply;

            if (!acceptState && reply == null) { // if Android Version < Android N
                if (!_userID.isEmpty() && !_email.isEmpty() && !_password.isEmpty()) {
                    openIntentWithUrlExtra(context, InputTextAlertActivity.class, intent.getExtras(), "manualReply");
                } else {
                    openIntentWithUrlExtra(context, MainActivity.class, intent.getExtras(), "manualReply");
                }
            } else {
                if (AuthHelper.isLoggedIn()) {
                    AuthHelper.sendInviteResponse(context, notifyID, inviteReqObj, new RunnableArg<ResponseMessage<String>>() {
                        @Override
                        public void run() {
                            ResponseMessage<String> responseMessage = this.getArg();

                            if (responseMessage.success) {
                                NotificationHelper.cancelNotification(context, notifyID);
                            } else {
                                openIntentWithUrlExtra(context, MainActivity.class, intent.getExtras(), "sendingError");
                            }
                        }
                    });
                } else {
                    if (!_userID.isEmpty() && !_email.isEmpty() && !_password.isEmpty()) {
                        AuthHelper.setUserID(_userID);
                        AuthHelper.setUserEmail(_email);
                        AuthHelper.setPassword(_password);

                        inviteReqObj.UserId = AuthHelper.getUserID();

                        if (AuthHelper.login()) {
                            AuthHelper.sendInviteResponse(context, notifyID, inviteReqObj, new RunnableArg<ResponseMessage<String>>() {
                                @Override
                                public void run() {
                                    ResponseMessage<String> responseMessage = this.getArg();

                                    if (responseMessage.success) {
                                        NotificationHelper.cancelNotification(context, notifyID);
                                    } else {
                                        openIntentWithUrlExtra(context, MainActivity.class, intent.getExtras(), "sendingError");
                                    }
                                }
                            });
                        } else {
                            openIntentWithUrlExtra(context, MainActivity.class, intent.getExtras(), "loginError");
                        }
                    } else {
                        openIntentWithUrlExtra(context, MainActivity.class, intent.getExtras(), "loginRequired");
                    }
                }
            }
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
