package com.shoppingmall.smms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.core.app.RemoteInput;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {

        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
        String actionNotification = intent.getAction();
        String reply = null;

        if (remoteInput != null) {
            CharSequence charSequence = remoteInput.getCharSequence("key_reply");

            if (charSequence != null) {
                reply = charSequence.toString();
            }
        }

        Intent newIntent = new Intent(context, NotificationIntentService.class);
        newIntent.putExtras(intent.getExtras());

        if (reply != null) newIntent.putExtra("reply", reply);
        if (actionNotification != null)
            newIntent.putExtra("actionNotification", actionNotification);

        context.startService(newIntent);
    }
}