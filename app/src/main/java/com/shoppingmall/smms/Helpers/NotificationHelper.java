package com.shoppingmall.smms.Helpers;

public class NotificationHelper {
    private static int notificationCount = 0;

    public static int getNotificationCount() {
        return ++notificationCount;
    }
}
