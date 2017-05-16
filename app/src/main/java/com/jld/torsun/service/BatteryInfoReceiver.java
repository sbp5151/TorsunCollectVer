package com.jld.torsun.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.TextView;

import com.jld.torsun.R;
import com.jld.torsun.util.LogUtil;
import com.jld.torsun.view.RoundProgressBar;

import java.util.Map;

public class BatteryInfoReceiver extends BroadcastReceiver {
    
    private TextView textView;
    private RoundProgressBar roundProgressBar;
    private View showView;

    public static final String BATTERYINFO_FLAG = "battery_info_flag";

    private static int savaBatteryInfoNum = -1;

    public BatteryInfoReceiver(){};

    public BatteryInfoReceiver(TextView textView) {
        this.textView=textView;
    }

    public BatteryInfoReceiver(RoundProgressBar roundProgressBar) {
        this.roundProgressBar=roundProgressBar;
    }

    public BatteryInfoReceiver(RoundProgressBar roundProgressBar , View showView) {
        if (null != this.roundProgressBar){
            this.roundProgressBar = null;
        }
        if (null != this.showView){
            showView.setVisibility(View.INVISIBLE);
            this.showView = null;
        }
        this.roundProgressBar=roundProgressBar;
        this.showView = showView;
        if (savaBatteryInfoNum > 0 ){
            this.showView.setVisibility(View.VISIBLE);
            this.roundProgressBar.setProgress(savaBatteryInfoNum);
            this.roundProgressBar.postInvalidate();
        }
    }

    private boolean isShow = true;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        int info = intent.getIntExtra(BATTERYINFO_FLAG,-1);
        if (info < 0){
            return;
        }
        savaBatteryInfoNum = info;
        if (null !=showView){
            showView.setVisibility(View.VISIBLE);
        }
        //&& BATTERY_INFO_CHANGE.equals(action)
        if (null != roundProgressBar ){
            roundProgressBar.setProgress(info);
            roundProgressBar.postInvalidate();
        }
        // && BATTERY_INFO_CHANGE.equals(action)
        if (null != textView){
            textView.setText(info);
        }

    }

}
