package com.shoppingmall.smms.Helpers;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;

import com.shoppingmall.smms.Models.ConnectionStatus;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.List;

public class NetworkHelper {

    private static Context _c;

    public static void setContext(Context c) { _c = c; }

    private static String getMacAddrFromNetworkInterface() {
        List<NetworkInterface> all = null;
        try {
            all = Collections.list(NetworkInterface.getNetworkInterfaces());
        } catch (SocketException e) {
            e.printStackTrace();
        }
        for (NetworkInterface nif : all) {
            if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

            byte[] macBytes = new byte[0];
            try {
                macBytes = nif.getHardwareAddress();
            } catch (SocketException e) {
                e.printStackTrace();
            }
            if (macBytes == null) {
                return "";
            }

            StringBuilder res1 = new StringBuilder();
            for (byte b : macBytes) {
                res1.append(String.format("%02X:",b));
            }

            if (res1.length() > 0) {
                res1.deleteCharAt(res1.length() - 1);
            }
            return res1.toString();
        }

        return "02:00:00:00:00:00";
    }

    public static String getMacAddr() {
        if (_c != null) {
            return getMacAddr(_c);
        } else {
            return null;
        }
    }

    public static String getMacAddr(Context _context) {
        String macAddr = "";
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            WifiManager wifiMan = (WifiManager) _context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInf = wifiMan.getConnectionInfo();
            macAddr = wifiInf.getMacAddress();
        } else {
            macAddr = getMacAddrFromNetworkInterface();
        }
        return macAddr.toLowerCase();
    }

    public static void setWifiInfo(Context context, ConnectionStatus _connectionStatus) {
        if (context == null) {
            return;
        }

        final Intent intent = context.registerReceiver(null, new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));

        if (intent != null) {
            final WifiInfo wifiInfo = intent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO);
            if (wifiInfo != null) {
                final String ssid = wifiInfo.getSSID();
                final Integer ipAddress = wifiInfo.getIpAddress();
                if (ssid != null) {
                    String trimedSSID = ssid.replace("\"", "");
                    _connectionStatus.SSID = trimedSSID;
                }
                if (ipAddress > 0) {
                    _connectionStatus.setIpAddress(ipAddress);
                }
            }
        }
    }


}
