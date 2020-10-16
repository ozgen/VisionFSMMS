package com.shoppingmall.smms;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.shoppingmall.smms.Helpers.NetworkHelper;
import com.shoppingmall.smms.Models.ConnectionStatus;

import java.util.ArrayList;
import java.util.List;

class NetworkReceiver extends BroadcastReceiver {
    protected static List<RunnableArg> subscribers = new ArrayList<>();
    private Activity _activity;

    public NetworkReceiver() {
    }

    public NetworkReceiver(Activity activity) {
        _activity = activity;
    }

    public static void addConnectionTypeChangeListener(RunnableArg _runnable) {
        subscribers.add(_runnable);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectionStatus connectionStatus = NetworkHelper.getCurrentlyConnectionStatus(_activity.getApplicationContext());
        this.emitAllSubscribers(connectionStatus);
    }

    private void emitAllSubscribers(ConnectionStatus connectionStatus) {
        for (RunnableArg runnable : subscribers) {
            runnable.run(connectionStatus);
        }
    }
}
