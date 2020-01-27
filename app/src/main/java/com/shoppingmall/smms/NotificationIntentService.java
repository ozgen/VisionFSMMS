package com.shoppingmall.smms;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.shoppingmall.smms.Helpers.AuthHelper;
import com.shoppingmall.smms.Helpers.NotificationHelper;
import com.shoppingmall.smms.Models.InviteResponse;
import com.shoppingmall.smms.Models.ResponseMessage;

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
            String userId = intent.getStringExtra("userId");

            InviteResponse inviteReqObj = new InviteResponse();
            inviteReqObj.accept = acceptState;
            inviteReqObj.MeetingId = meetingId;
            inviteReqObj.UserId = userId;

            if (reply != null) inviteReqObj.responseInvitation = reply;

            if (!acceptState && reply == null) { // if Android Version < Android N
                openIntentWithUrlExtra(context, InputTextAlertActivity.class, intent.getExtras(), "manualReply");
                // openIntentWithUrlExtra(context, MainActivity.class, intent.getExtras(), "manualReply");
            } else {
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
