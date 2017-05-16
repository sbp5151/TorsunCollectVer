package com.jld.torsun;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.jld.torsun.util.Constats;
import com.jld.torsun.util.UserInfo;

import java.util.ArrayList;
import java.util.List;

import cn.jpush.android.api.JPushInterface;

public class MyApplication extends Application {

    private RequestQueue mRequestQueue;
    /**
     * 判断是否有版本更新
     */
    public Boolean isVersionUpdate = false;
    /**
     * wifi名称，用来判断是否有数据接收
     */
    public String wifiName = "";
    /**
     * 网络发生变化
     */
    public static boolean netChange = true;

    /**
     * 导游宝是否发送数据
     */
    public static boolean isNotData = false;
    /**
     * 导游权限取消dialog弹框
     */
    public static boolean Toru_Power_cancel = false;
    /**
     * 存储短信验证码集合，保证手机接收到的验证码都能 验证
     */
    private List<String> verifiCodes = new ArrayList<String>();
    /**
     * 是否保存了最新的团队id判断
     */
    public static boolean isfristSavaLastTeamID = true;
    /**
     * 打开GPS提示
     */
    public boolean isOpenGps = false;
    public boolean trouTeamSyncData = false;
    /**
     * 是否显示过信息中心的dialog判断
     */
    public static boolean isgetMSGCount = false;
    private static MyApplication sInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化JPush
        JPushInterface.setDebugMode(true);
        JPushInterface.init(this);
        sInstance = this;
        SharedPreferences sp = getSharedPreferences(Constats.SHARE_KEY,
                Context.MODE_PRIVATE);
        if (sp.getBoolean(UserInfo.IS_VOICE_PAUSE, false) && sp.getBoolean(UserInfo.ISLOAD, false))
            isNotData = true;

    }

    public static Context getAppContext() {
        return sInstance;
    }

    public List<String> getverifiCodes() {
        if (verifiCodes == null)
            verifiCodes = new ArrayList<String>();
        return verifiCodes;
    }

    /**
     * volley请求队列，共用一个
     *
     * @return
     */
    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(this);
        }
        return mRequestQueue;
    }


}


