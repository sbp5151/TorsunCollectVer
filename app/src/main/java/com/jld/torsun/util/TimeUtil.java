package com.jld.torsun.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * 项目名称：Torsun
 * 晶凌达科技有限公司所有，
 * 受到法律的保护，任何公司或个人，未经授权不得擅自拷贝。
 *
 * @creator 单柏平 <br/>
 * @create-time ${date} ${time}
 */
public class TimeUtil {

    public static String getCurrentTime() {
        Calendar calendar = Calendar.getInstance();
        long unixTime = calendar.getTimeInMillis();//这是时间戳
        return unixTime + "";
    }

    /**
     * 时间格式转换
     *
     * @return
     */
    public static String timeFormat(String time) {
        Long longTime = Long.decode(time);
        if (time.length() == 13) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date d1 = new Date(longTime);
            String t1 = format.format(d1);
            LogUtil.d("timeFormat", t1);
            return t1;
        } else if (time.length() == 10) {
            longTime = Long.decode(time + "000");
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date d1 = new Date(longTime);
            String t1 = df.format(d1);
            LogUtil.d("timeFormat", t1);
            return t1;
        }
        return "";
    }

    public static String timeFormatOther(String time) {
        Long longTime = Long.decode(time);
        if (time.length() == 13) {
            SimpleDateFormat format = new SimpleDateFormat("MM.dd  HH:mm");
            Date d1 = new Date(longTime);
            String t1 = format.format(d1);
            LogUtil.d("timeFormat", t1);
            return t1;
        } else if (time.length() == 10) {
            longTime = Long.decode(time + "000");
            SimpleDateFormat df = new SimpleDateFormat("MM.dd  HH:mm");
            Date d1 = new Date(longTime);
            String t1 = df.format(d1);
            LogUtil.d("timeFormat", t1);
            return t1;
        }
        return "";
    }

    public static String millisToString(long millis, boolean text) {
        boolean negative = millis < 0;
        millis = Math.abs(millis);
        int mini_sec = (int) millis % 1000;
        millis /= 1000;
        int sec = (int) (millis % 60);
        millis /= 60;
        int min = (int) (millis % 60);
        millis /= 60;
        int hours = (int) millis;

        String time;
        DecimalFormat format = (DecimalFormat) NumberFormat.getInstance(Locale.US);
        format.applyPattern("00");

        DecimalFormat format2 = (DecimalFormat) NumberFormat.getInstance(Locale.US);
        format2.applyPattern("000");
        if (text) {
            if (millis > 0)
                time = (negative ? "-" : "") + hours + "h" + format.format(min) + "min";
            else if (min > 0)
                time = (negative ? "-" : "") + min + "min";
            else
                time = (negative ? "-" : "") + sec + "s";
        } else {
            if (millis > 0)
//				time = (negative ? "-" : "") + hours + ":" + format.format(min) + ":" + format.format(sec) + ":" + format2.format(mini_sec);
                time = (negative ? "-" : "") + hours + ":" + format.format(min) + ":" + format.format(sec);
            else
//				time = (negative ? "-" : "") + min + ":" + format.format(sec) + ":" + format2.format(mini_sec);
                time = (negative ? "-" : "") + min + ":" + format.format(sec);
        }
        return time;
    }
}
