package com.shoppingmall.smms.Helpers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.RemoteInput;

import com.shoppingmall.smms.NotificationReceiver;
import com.shoppingmall.smms.R;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class NotificationHelper {
    private static int notificationCount = 0;
    public static int getNotificationCount() {
        return ++notificationCount;
    }

    public static void showInviteNotification (Context c, Map<String, String> notificationData) {

        Integer notificationID = getNotificationCount();

        String userId = notificationData.get("userId");
        String meetingId = notificationData.get("meetingId");
        String title = notificationData.get("title");
        String notificationContent = notificationData.get("content");

        Bundle bundle = new Bundle();
        for (Map.Entry<String,String> entry : notificationData.entrySet()) {
            bundle.putString(entry.getKey(), entry.getValue());
        }

        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.setBigContentTitle(title);
        bigTextStyle.bigText(notificationContent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationManager mNotificationManager =
                    (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
            String channelId = "Invite";
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "SMMS Meeting Invite",
                    NotificationManager.IMPORTANCE_HIGH);
            mNotificationManager.createNotificationChannel(channel);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //Create notification builder
            NotificationCompat.Builder builder = new NotificationCompat.Builder(c, "Invite")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(title)
                    .setContentText(notificationContent)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .setStyle(bigTextStyle);

            Intent acceptIntent = new Intent(c, NotificationReceiver.class);
            acceptIntent.setAction("ACTION_ACCEPT");
            acceptIntent.putExtras(bundle);
            acceptIntent.putExtra("notificationId", notificationID);
            acceptIntent.putExtra("meetingId", meetingId);
            acceptIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent acceptPendingIntent = PendingIntent.getBroadcast(c, 500, acceptIntent, 0);

            builder.addAction(R.drawable.ic_done_black_24dp, c.getString(R.string.acceptinvite), acceptPendingIntent);

            String replyLabel = "Katılamama nedeninizi yazınız";

            //Initialise RemoteInput
            RemoteInput remoteInput = new RemoteInput.Builder("key_reply")
                    .setLabel(replyLabel)
                    .build();

            Intent notAcceptIntent = new Intent(c, NotificationReceiver.class);
            notAcceptIntent.setAction("ACTION_NOTACCEPT");
            notAcceptIntent.putExtras(bundle);
            notAcceptIntent.putExtra("notificationId", notificationID);
            notAcceptIntent.putExtra("meetingId", meetingId);
            notAcceptIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent notAcceptPendingIntent = PendingIntent.getBroadcast(c, 200, notAcceptIntent, 0);

            //Notification Action with RemoteInput instance added.
            NotificationCompat.Action replyAction = new NotificationCompat.Action.Builder(
                    R.drawable.ic_close_black_24dp, c.getString(R.string.notacceptinvite), notAcceptPendingIntent)
                    .addRemoteInput(remoteInput)
                    .setAllowGeneratedReplies(true)
                    .build();

            //Notification.Action instance added to Notification Builder.
            builder.addAction(replyAction);

            //Create Notification.
            NotificationManager notificationManager = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.notify(notificationID ,builder.build());
        } else {

            Intent acceptIntent = new Intent(c, NotificationReceiver.class);
            acceptIntent.setAction("ACTION_ACCEPT");
            acceptIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            acceptIntent.putExtras(bundle);
            acceptIntent.putExtra("notificationId", notificationID);
            acceptIntent.putExtra("inviteId", meetingId);
            PendingIntent acceptPendingIntent = PendingIntent.getBroadcast(c, 200, acceptIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            Intent notAcceptIntent = new Intent(c, NotificationReceiver.class);
            notAcceptIntent.setAction("ACTION_NOTACCEPT");
            notAcceptIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            notAcceptIntent.putExtras(bundle);
            notAcceptIntent.putExtra("notificationId", notificationID);
            notAcceptIntent.putExtra("inviteId", meetingId);
            PendingIntent notAcceptPendingIntent = PendingIntent.getBroadcast(c, 500, notAcceptIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(c, "Invite")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setStyle(bigTextStyle)
                    .setContentTitle(title)
                    .setContentText(notificationContent)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .setContentIntent(acceptPendingIntent)
                    .addAction(R.drawable.ic_done_black_24dp, c.getString(R.string.acceptinvite), acceptPendingIntent)
                    .addAction(R.drawable.ic_close_black_24dp, c.getString(R.string.notacceptinvite), notAcceptPendingIntent);

            builder.setChannelId("Invite");

            NotificationManagerCompat manager = NotificationManagerCompat.from(c);
            manager.notify(notificationID, builder.build());
        }
    }

    public static void cancelNotification(Context ctx, int notifyId) {
//        String ns = Context.NOTIFICATION_SERVICE;
//        NotificationManager nMgr = (NotificationManager) ctx.getSystemService(ns);
//        nMgr.cancel(notifyId);

        NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(ctx.getApplicationContext());
        mNotificationManager.cancel(notifyId);
    }

    public static Map<String, String> bundleToMap(Bundle extras) {
        Map<String, String> map = new HashMap<String, String>();

        Set<String> ks = extras.keySet();
        Iterator<String> iterator = ks.iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            map.put(key, extras.getString(key));
        }
        return map;
    }
}
