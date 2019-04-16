package com.dyh.movienow.core.parser;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.dyh.movienow.App;
import com.dyh.movienow.util.ToastMgr;

import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * 作者：By hdy
 * 日期：On 2018/11/1
 * 时间：At 19:17
 */
public class LocalServerParser {

    public static String getRealUrl(Context context, String url) {
        url = getRealUrl(url);
        if (!url.startsWith("http://127.0.0.1")) {
            return url;
        }
        if (context != null) {
            String[] urlss = url.replace("http://", "").split(":");
            return "http://" + getIP(context) + ":" + urlss[1];
        }
        return url;
    }


    public static String getRealUrl(String url) {
        if (!url.startsWith("http://127.0.0.1")) {
            return url;
        }
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        String[] urls = url.replace("http://", "").split("/");
        if (urls.length < 3) {
            return url;
        }
        int rootPathPos = urls.length - 2;
        String rootPath = urls[rootPathPos];
        String www = App.appConfig.rootPath + File.separator + rootPath;
        App.webServerManager.startServer(www);
        return "http://" + urls[0] + "/" + urls[urls.length - 1];
    }

    public static String getIP(Context context) {
        try {
            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            //判断wifi是否开启
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int ipAddress = wifiInfo.getIpAddress();
            return intToIp(ipAddress);
        } catch (Exception e) {
            e.printStackTrace();
            ToastMgr.toastShortBottomCenter(context, "出错：" + e.toString());
            try {
                return getLocalIPAddress();
            } catch (Exception e1) {
                e1.printStackTrace();
                ToastMgr.toastShortBottomCenter(context, "出错：" + e.toString());
            }
        }
        return "127.0.0.1";
    }

    private static String getLocalIPAddress() {
        try {
            for (Enumeration<NetworkInterface> mEnumeration = NetworkInterface
                    .getNetworkInterfaces(); mEnumeration.hasMoreElements(); ) {
                NetworkInterface intf = mEnumeration.nextElement();
                for (Enumeration<InetAddress> enumIPAddr = intf
                        .getInetAddresses(); enumIPAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIPAddr.nextElement();
                    // 如果不是回环地址
                    if (!inetAddress.isLoopbackAddress()) {
                        // 直接返回本地IP地址
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            System.err.print("error");
        }
        return "127.0.0.1";
    }

    public static InetAddress getLocalINetAddress() throws SocketException{
        for (Enumeration<NetworkInterface> mEnumeration = NetworkInterface
                .getNetworkInterfaces(); mEnumeration.hasMoreElements(); ) {
            NetworkInterface intf = mEnumeration.nextElement();
            for (Enumeration<InetAddress> enumIPAddr = intf
                    .getInetAddresses(); enumIPAddr.hasMoreElements(); ) {
                InetAddress inetAddress = enumIPAddr.nextElement();
                // 如果不是回环地址
                if (!inetAddress.isLoopbackAddress()) {
                    // 直接返回本地IP地址
                    return inetAddress;
                }
            }
        }
        throw new SocketException("获取本地IP地址失败！");
    }

    private static String intToIp(int i) {
        return (i & 0xFF) + "." +
                ((i >> 8) & 0xFF) + "." +
                ((i >> 16) & 0xFF) + "." +
                (i >> 24 & 0xFF);
    }

}
