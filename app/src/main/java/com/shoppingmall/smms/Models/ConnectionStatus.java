package com.shoppingmall.smms.Models;

import android.annotation.SuppressLint;

public class ConnectionStatus {
    public Boolean wifiConnected = false;
    public Boolean mobileConnected = false;
    public String SSID = null;
    private String IPAddress = null;

    private int IpAddressInt = 0;

    @SuppressLint("DefaultLocale")
    public void setIpAddress(int ipAddress) {
        IpAddressInt = ipAddress;
        IPAddress = String.format("%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff),
                (ipAddress >> 24 & 0xff));
    }

    public int getIpAddressInt() {
        return IpAddressInt;
    }

    public String getIPAddress() {
        return IPAddress;
    }

    public boolean isSecure() {
        return wifiConnected && !mobileConnected;
    }
}