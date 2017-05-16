package com.jld.torsun.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.jld.torsun.activity.MainFragment;
import com.jld.torsun.util.Constats;
import com.jld.torsun.util.JsonUtil;
import com.jld.torsun.util.LogUtil;
import com.jld.torsun.util.UserInfo;

import org.json.JSONObject;

import cn.jpush.android.api.JPushInterface;

public class JPushReceiver extends BroadcastReceiver {

    private static final String TAG = "JPushReceiver";
    public static Boolean dialogIsShow = false;
    public static final String JPush_request_location = "JPush_request_location";
    private SharedPreferences sp;

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        sp = context.getSharedPreferences(Constats.SHARE_KEY,
                Context.MODE_PRIVATE);
        Bundle bundle = intent.getExtras();
        Log.d(TAG, "[MyReceiver] onReceive - " + intent.getAction()
                + ", extras: " + printBundle(bundle));

        if (JPushInterface.ACTION_REGISTRATION_ID.equals(intent.getAction())) {
            String regId = bundle
                    .getString(JPushInterface.EXTRA_REGISTRATION_ID);
            Log.d(TAG, "[MyReceiver] 接收Registration Id : " + regId);

        } else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent
                .getAction())) {
            Log.d(TAG, "[MyReceiver] 接收到推送下来的自定义消息:");
            processCustomMessage(context, bundle);

        } else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent
                .getAction())) {
            Log.d(TAG, "[MyReceiver] 接收到推送下来的通知");
            receivingNotification(context, bundle);

        } else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent
                .getAction())) {
            Log.d(TAG, "[MyReceiver] 用户点击打开了通知");
            openNotification(context, bundle);
        } else if (JPushInterface.ACTION_RICHPUSH_CALLBACK.equals(intent
                .getAction())) {
            Log.d(TAG,
                    "[MyReceiver] 用户收到到RICH PUSH CALLBACK: "
                            + bundle.getString(JPushInterface.EXTRA_EXTRA));
            // 在这里根据 JPushInterface.EXTRA_EXTRA 的内容处理代码，比如打开新的Activity，
            // 打开一个网页等..

        } else if (JPushInterface.ACTION_CONNECTION_CHANGE.equals(intent
                .getAction())) {
            boolean connected = intent.getBooleanExtra(
                    JPushInterface.EXTRA_CONNECTION_CHANGE, false);
            Log.w(TAG, "[MyReceiver]" + intent.getAction()
                    + " connected state change to " + connected);
        } else {
            Log.d(TAG, "[MyReceiver] Unhandled intent - " + intent.getAction());
        }
    }

    /**
     * 接收自定义消息
     *
     * @param context
     * @param bundle
     */
    private void processCustomMessage(Context context, Bundle bundle) {
        // 接收的内容
        String title = bundle.getString(JPushInterface.EXTRA_TITLE);
        String message = bundle.getString(JPushInterface.EXTRA_MESSAGE);
        String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);

        LogUtil.d(TAG, "title:" + title + "\n" + "message：" + message + "\n"
                + "extras：" + extras);
        String type = "";
        LogUtil.d(TAG, JsonUtil.isJson(message) + "");
        try {
            JSONObject extrasJson = new JSONObject(extras);
            type = extrasJson.getString("type");
        } catch (Exception e) {
            Log.w(TAG, "Unexpected: extras is not a valid json", e);
        }
        Log.d(TAG, "type:" + type);
        boolean isLogin = sp.getBoolean(UserInfo.LOGINING, false);// 获取登录状态
        if (!TextUtils.isEmpty(type) && type.equals("0") && isLogin) {// 0为强制退出
            dialogIsShow = true;// 显示dialog
            Intent mIntent = new Intent(context, MainFragment.class);
            mIntent.putExtras(bundle);
            mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(mIntent);
        } else if (!TextUtils.isEmpty(type) && type.equals("1") && isLogin) {// 1为导游请求经纬度
            // TODO 发送请求经纬度广播
            Intent intent = new Intent(JPush_request_location);
            context.sendBroadcast(intent);
        } else if (!TextUtils.isEmpty(type) && type.equals("2") && isLogin) {// 2为系统消息

        }
    }

    /**
     * 接收通知
     *
     * @param context
     * @param bundle
     */

    private void receivingNotification(Context context, Bundle bundle) {
        String title = bundle
                .getString(JPushInterface.EXTRA_NOTIFICATION_TITLE);
        String message = bundle.getString(JPushInterface.EXTRA_ALERT);
        String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);

        Log.d(TAG, "title:" + title + "\n" + "message：" + message + "\n"
                + "extras：" + extras);
    }

    /**
     * 点击通知
     *
     * @param context
     * @param bundle
     */
    private void openNotification(Context context, Bundle bundle) {
        String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);
        String myValue = "";
        Log.d(TAG, "extras：" + extras);

        try {
            JSONObject extrasJson = new JSONObject(extras);
            myValue = extrasJson.optString("myKey");
            Log.d(TAG, "myValue : " + myValue);

        } catch (Exception e) {
            Log.w(TAG, "Unexpected: extras is not a valid json", e);
            return;
        }
    }

    private String printBundle(Bundle bundle) {
        return null;
    }

}
