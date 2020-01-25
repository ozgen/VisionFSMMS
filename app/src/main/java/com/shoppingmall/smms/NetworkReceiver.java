package com.shoppingmall.smms;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.shoppingmall.smms.Helpers.NetworkHelper;
import com.shoppingmall.smms.Helpers.NotificationHelper;
import com.shoppingmall.smms.Models.ConnectionStatus;

import java.util.ArrayList;
import java.util.List;

class NetworkReceiver extends BroadcastReceiver {
    private Activity _activity;
    protected static List<RunnableArg> subscribers = new ArrayList<>();

    public NetworkReceiver() { }

    public NetworkReceiver(Activity activity) {
        _activity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectionStatus connectionStatus = NetworkHelper.getCurrentlyConnectionStatus(_activity.getApplicationContext());
        this.emitAllSubscribers(connectionStatus);
    }

    public static void addConnectionTypeChangeListener(RunnableArg _runnable) {
        subscribers.add(_runnable);
    }

    private void emitAllSubscribers (ConnectionStatus connectionStatus) {
        for (RunnableArg runnable : subscribers) {
            runnable.run(connectionStatus);
        }
    }
}
