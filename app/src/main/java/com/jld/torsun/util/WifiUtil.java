package com.jld.torsun.util;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

/**
 * 项目名称：branches
 * 晶凌达科技有限公司所有，
 * 受到法律的保护，任何公司或个人，未经授权不得擅自拷贝。
 *
 * @creator 单柏平 <br/>
 * @create-time ${date} ${time}
 */
public class WifiUtil {

    public static String getWifiName(Context context) {
        String wifiName;
        WifiManager wm = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
        WifiInfo wi = wm.getConnectionInfo();
        wifiName = wi.getSSID();
        return wifiName;
    }
}
