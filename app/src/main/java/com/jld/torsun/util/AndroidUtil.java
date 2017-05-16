package com.jld.torsun.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings.Secure;
import android.view.View;
import android.view.WindowManager;


import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;

/**
 * 常用的工具
 */
public class AndroidUtil {


    /**
     * 判断存储卡是否存
     */
    public static boolean isSdCardExist() {

        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            return true;
        }
        return false;
    }

    /**
     * 获取屏幕宽度
     */
    public static int getScreenWidth(Context context) {
        return ((WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay()
                .getWidth();
    }

    /**
     * 获取屏幕高度
     */
    public static int getScreenHeight(Context context) {
        return ((WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay()
                .getHeight();
    }

    /**
     * 获取当前系统语言
     */

    public static String getLangusge(Context context) {
        Locale locale = context.getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        if (language.endsWith("zh")) {
            return "zh";
        } else if (language.endsWith("en")) {
            return "en";
        }
        return "";
    }

    /**
     * 获得状态栏的高度px
     */
    public static float getStatusHeight(Context context) {

        float statusHeight = -1;
        try {
            Class<?> clazz = Class.forName("com.android.internal.R$dimen");
            Object object = clazz.newInstance();
            int height = Integer.parseInt(clazz.getField("status_bar_height")
                    .get(object).toString());
            statusHeight = context.getResources().getDimensionPixelSize(height);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statusHeight;
    }

    /**
     * 获取当前屏幕截图，包含状态栏
     */
    public static Bitmap snapShotWithStatusBar(Activity activity) {
        View view = activity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bmp = view.getDrawingCache();
        int width = getScreenWidth(activity);
        int height = getScreenHeight(activity);
        Bitmap bp = null;
        bp = Bitmap.createBitmap(bmp, 0, 0, width, height);
        view.destroyDrawingCache();
        return bp;
    }

    /**
     * 获取当前屏幕截图，不包含状态栏
     */
    public static Bitmap snapShotWithoutStatusBar(Activity activity) {
        View view = activity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bmp = view.getDrawingCache();
        Rect frame = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        int statusBarHeight = frame.top;

        int width = getScreenWidth(activity);
        int height = getScreenHeight(activity);
        Bitmap bp = null;
        bp = Bitmap.createBitmap(bmp, 0, statusBarHeight, width, height
                - statusBarHeight);
        view.destroyDrawingCache();
        return bp;
    }

    /**
     * 获取手机唯一标识码
     */
    public static String getUniqueId(Context context) {
        /*
         * String m_szDevIDShort = "35" + // we make this look like a valid IMEI
		 * Build.BOARD.length() % 10 + Build.BRAND.length() % 10 +
		 * Build.CPU_ABI.length() % 10 + Build.DEVICE.length() % 10 +
		 * Build.DISPLAY.length() % 10 + Build.HOST.length() % 10 +
		 * Build.ID.length() % 10 + Build.MANUFACTURER.length() % 10 +
		 * Build.MODEL.length() % 10 + Build.PRODUCT.length() % 10 +
		 * Build.TAGS.length() % 10 + Build.TYPE.length() % 10 +
		 * Build.USER.length() % 10; // 13 digits return m_szDevIDShort;
		 */
        return Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
    }

    /**
     * 获取手机型号
     */
    public static String getHandSetInfo() {
        String handSetInfo = Build.MODEL;
        return handSetInfo;
    }

    /**
     * 获取手机品牌
     */
    public static String getVendor() {
        return Build.BRAND;
    }

    /**
     * 获取手机ip
     */
    public static void getLocalHostIp(final Handler mHandler) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                URL infoUrl = null;
                InputStream inStream = null;
                BufferedReader reader = null;
                InputStreamReader inputStreamReader = null;
                HttpURLConnection httpConnection = null;
                String line = "";
                try {
                    //http://city.ip138.com/ip2city.asp
                    //http://whatismyip.com.tw
                    //http://ip.chinaz.com/getip.aspx
                    infoUrl = new URL("http://ip.chinaz.com/getip.aspx");
                   // URLConnection connection = infoUrl.openConnection();
                    httpConnection = (HttpURLConnection) infoUrl.openConnection();
                    httpConnection.setConnectTimeout(10000);
                    httpConnection.setReadTimeout(10000);
                    httpConnection.connect();
                    //httpConnection.setRequestMethod("GET");
                    int responseCode = httpConnection.getResponseCode();
                    LogUtil.d("LoginActivity", "-----responseCode------"+ responseCode );
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        inStream = httpConnection.getInputStream();
                        inputStreamReader = new InputStreamReader(inStream, "utf-8");
                        reader = new BufferedReader(inputStreamReader);
                        //StringBuilder strber = new StringBuilder();
                        String result = reader.readLine();
//                        while ((line = reader.readLine()) != null)
//                            strber.append(line);
                        LogUtil.d("LoginActivity", "strber:" + result);
                        JSONObject jsonObject = new JSONObject(result);
                        line = jsonObject.getString("ip");
//                        int start = strber.indexOf("[");
//                        int end = strber.indexOf("]", start + 1);
//                        line = strber.substring(start + 1, end);
                    }
                } catch (MalformedURLException e) {
                    LogUtil.d("LoginActivity", "--------MalformedURLException e--------");
                    e.printStackTrace();
                } catch (IOException e) {
                    LogUtil.d("LoginActivity", "--------IOException e--------");
                    e.printStackTrace();
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    if (null != inputStreamReader){
                        try {
                            inputStreamReader.close();
                        }catch (IOException e){

                        }
                    }
                    if (null != inStream){
                        try {
                            inStream.close();
                        }catch (IOException e){
                            e.printStackTrace();
                        }
                    }
                    if (null != reader){
                        try {
                            reader.close();
                        }catch (IOException e){
                            e.printStackTrace();
                        }
                    }
                    if (null != httpConnection){
                        httpConnection.disconnect();
                    }
                    Message message = new Message();
                    message.obj = line;
                    message.what = 0;
                    LogUtil.d("LoginActivity", "ip:" + line);
                    mHandler.sendMessage(message);

                }
            }
        }).start();
    }

    /**
     * 保存图片
     */
    public static void saveBitmapToFile(Bitmap bitmap, String _file)
            throws IOException {
        BufferedOutputStream os = null;
        try {
            File file = new File(_file);
            int end = _file.lastIndexOf(File.separator);
            String _filePath = _file.substring(0, end);
            File filePath = new File(_filePath);
            if (!filePath.exists()) {
                filePath.mkdirs();
            }
            file.createNewFile();
            os = new BufferedOutputStream(new FileOutputStream(file));
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                }
            }
        }

    }

}
