package com.jld.torsun.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.jld.torsun.activity.fragment.FragmentMainVoice;
import com.jld.torsun.util.LogUtil;

public class PhoneReceiver extends BroadcastReceiver {
    public PhoneReceiver() {
    }

    private final String TAG = "PhoneReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtil.i(TAG, "action:" + intent.getAction());

        if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
            //如果是去电（拨出）
            LogUtil.i(TAG, "=============打电话============");
            if (FragmentMainVoice.binder != null) {
                LogUtil.d(TAG, "stopAudio");
                FragmentMainVoice.binder.stopAudio();
            }
        } else {
            //查了下android文档，貌似没有专门用于接收来电的action,所以，非去电即来电
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);
            tm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
            //设置一个监听器
        }
    }
    PhoneStateListener listener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            //state 当前状态 incomingNumber,貌似没有去电的API
            super.onCallStateChanged(state, incomingNumber);
            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE:
                    LogUtil.d(TAG, "=============挂电话============");
                    if (FragmentMainVoice.binder != null) {
                        FragmentMainVoice.binder.startAudio();
                    }
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    LogUtil.d(TAG, "=============接听============");
                    if (FragmentMainVoice.binder != null) {
                        FragmentMainVoice.binder.stopAudio();
                    }
                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                    LogUtil.d(TAG, "=============incomingNumber============" + incomingNumber);
                    //输出来电号码
                    break;
            }
        }
    };
}
