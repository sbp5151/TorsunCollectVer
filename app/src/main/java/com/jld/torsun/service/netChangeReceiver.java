package com.jld.torsun.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.jld.torsun.MyApplication;
import com.jld.torsun.activity.fragment.FragmentMainVoice;
import com.jld.torsun.modle.ActionConstats;
import com.jld.torsun.util.LogUtil;
import com.jld.torsun.util.MyHttpUtil;

/**
 * 网络变化就获取WiFi名称
 * <p/>
 * 晶凌达科技有限公司所有， 受到法律的保护，任何公司或个人，未经授权不得擅自拷贝。
 *
 * @creator 单柏平 <br/>
 * @create-time 2015-12-14 下午7:40:32
 */
public class netChangeReceiver extends BroadcastReceiver {

    private static final String TAG = "netChangeReceiver";
    private static Handler voiceHandler;

    @Override
    public void onReceive(Context context, Intent intent) {
        final Context context2 = context;

        String action = intent.getAction();
        LogUtil.d(TAG, "action : " + action);
        if (action.equals(ActionConstats.NET_CONNECT_CHANGE)) {
            MyApplication.isNotData = false;
            LogUtil.d(TAG, "网络变化:");
            // 判断有没有WiFi连接
            MyHttpUtil.isWifiConnected(context);
            MyApplication.netChange = true;
            if (voiceHandler != null) {
                //提醒广播动画网络变化，判断是否需要切换动画
                voiceHandler.sendEmptyMessageDelayed(FragmentMainVoice.WAVE_ISRUN, 300);
            }
        } else if (action.equals(Intent.ACTION_SCREEN_OFF)) {//锁屏状态（黑屏时）
            LogUtil.d(TAG, " 锁屏（黑屏)");
        } else if (action.equals(Intent.ACTION_SCREEN_ON)) {//亮屏幕时
            LogUtil.d(TAG, " 亮屏");
        }
    }

    public static void sendVoiceHandler(Handler handler) {
        voiceHandler = handler;
    }
}
