package com.jld.torsun.util;

import com.baidu.mapapi.model.LatLng;

/**
 * Created by boping on 2016/3/14.
 */
public class distanceUtil {


    /**
     * 计算两点之间距离
     *
     * @param start
     * @param end
     * @return 四舍五入，保留一位小数
     */
    public static double getDistance(LatLng start, LatLng end) {
        double lat1 = (Math.PI / 180) * start.latitude;
        double lat2 = (Math.PI / 180) * end.latitude;

        double lon1 = (Math.PI / 180) * start.longitude;
        double lon2 = (Math.PI / 180) * end.longitude;
        //地球半径
        double R = 6371;
        //两点间距离 km，如果想要米的话，结果*1000就可以了
        double d = Math.acos(Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon2 - lon1)) * R;
        return (double) Math.round(d * 1000 * 10) / 10;
    }
    public static double getDistance_google(com.google.android.gms.maps.model.LatLng start, com.google.android.gms.maps.model.LatLng end) {
        double lat1 = (Math.PI / 180) * start.latitude;
        double lat2 = (Math.PI / 180) * end.latitude;

        double lon1 = (Math.PI / 180) * start.longitude;
        double lon2 = (Math.PI / 180) * end.longitude;
        //地球半径
        double R = 6371;
        //两点间距离 km，如果想要米的话，结果*1000就可以了
        double d = Math.acos(Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon2 - lon1)) * R;
        return (double) Math.round(d * 1000 * 10) / 10;
    }

}
