package com.shoppingmall.smms;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.shoppingmall.smms.Helpers.NotificationHelper;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(String token) {
        Log.d("token", "Refreshed token: " + token);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        final Intent intent = new Intent(this, MainActivity.class);

        String _msgUrl = remoteMessage.getData().get("url");
        String _msgType = remoteMessage.getData().get("type");

        if (_msgType == null) { _msgType = "standart"; }

        intent.putExtra("url", _msgUrl);
        intent.putExtra("type", _msgType);

        final PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.setBigContentTitle(remoteMessage.getNotification().getTitle());
        bigTextStyle.bigText(remoteMessage.getNotification().getBody());

        Notification notification = new NotificationCompat.Builder(this, "841561868486484")
                .setContentTitle(remoteMessage.getNotification().getTitle())
                .setContentText(remoteMessage.getNotification().getBody())
                .setStyle(bigTextStyle)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setColor(Integer.parseInt("FFFFFF",16))
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setGroup(_msgType)
                .build();

        NotificationManagerCompat manager = NotificationManagerCompat.from(getApplicationContext());
        manager.notify(NotificationHelper.getNotificationCount(), notification);
    }
}
