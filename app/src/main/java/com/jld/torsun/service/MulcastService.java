package com.jld.torsun.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import com.jld.torsun.activity.tours.MulticastClient;
import com.jld.torsun.activity.tours.MulticastServer;
import com.jld.torsun.modle.ActionConstats;
import com.jld.torsun.util.MyHttpUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

//import java.net.InetAddress;
//import java.net.MulticastSocket;
//import android.net.wifi.WifiManager;
//import android.net.wifi.WifiManager.MulticastLock;

/**
 * 晶凌达科技有限公司所有，
 * 受到法律的保护，任何公司或个人，未经授权不得擅自拷贝。
 *
 * @author 单柏平
 * @time 2016/1/12 17:39
 */
public class MulcastService extends Service {

    public static Map<String, String> memerys = new HashMap<String, String>();

    /**
     * 组团
     */
    private MulticastServer mServerSend = null;
    private MulticastClient mClientReceive = null;

    @Override
    public void onCreate() {
        super.onCreate();
        /**
         * 发送广播
         * */
        try {
            if (mServerSend == null) {
                mServerSend = MulticastServer.getInstanceMulticastServer(this);
            }
            mServerSend.startSend();//启用发送广播服务
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (MyHttpUtil.isWifiConn(this)) {//如果有wifi连接便启动广播接收服务
            clientReceive();
        } else {//如果没有wifi连接则开启网络状态变化的广播接收者
            netchangBroad = new NetchangBroad();
            netchangeIntentFilter = new IntentFilter(
                    "android.net.conn.CONNECTIVITY_CHANGE");
            registerReceiver(netchangBroad, netchangeIntentFilter);
        }
        intentFilter = new IntentFilter(ActionConstats.STRCHANGE);
        changeBroadcast = mServerSend.new ChangeBroadcast();
        registerReceiver(changeBroadcast, intentFilter);
    }

    private boolean isFrist = true;//是否是第一次启动
    private NetchangBroad netchangBroad;
    private IntentFilter netchangeIntentFilter;

    class NetchangBroad extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, Intent intent) {
            String action = intent.getAction();
            if ("android.net.conn.CONNECTIVITY_CHANGE".equals(action)) {
                if (isFrist && MyHttpUtil.isWifiConn(MulcastService.this)) {
                    clientReceive();//如果有wifi连接便启动广播接收服务
                    MulcastService.this.unregisterReceiver(netchangBroad);
                }
            }
        }
    }

    /**
     * 启动接受广播
     */
    private void clientReceive() {
        if (!MyHttpUtil.isWifiConn(this)) {
            return;
        }
        try {
            if (mClientReceive == null) {
                mClientReceive = MulticastClient
                        .getInstanceMulticastClient(this);
            }
            mClientReceive.startReceive();
            isFrist = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private IntentFilter intentFilter;
    private MulticastServer.ChangeBroadcast changeBroadcast;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        if (null != mClientReceive) {
            mClientReceive.stopReceive();
        }
        if (null != mServerSend){
            mServerSend.stopSend();
        }
        if (null != changeBroadcast){
            unregisterReceiver(changeBroadcast);
        }
        super.onDestroy();
    }

}
