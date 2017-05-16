package com.jld.torsun.activity.baiduMap;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.model.LatLng;
import com.jld.torsun.http.VolleyJsonUtil;
import com.jld.torsun.modle.ActionConstats;
import com.jld.torsun.service.JPushReceiver;
import com.jld.torsun.util.Constats;
import com.jld.torsun.util.LogUtil;
import com.jld.torsun.util.MD5Util;
import com.jld.torsun.util.UserInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * 后台上传经纬度
 */
public class SendLocationService extends Service {

    private final String TAG = "SendLocationService";
    private LocationManager locationManager;
    private String locationProvider;
    private Location current_location;
    private SharedPreferences sp;
    public String jwd;
    public HashMap<String, String> sendParams;
    public String userId;
    public String sendSign;
    private boolean isGps = false;
    private LatLng latLng;
    public static final int RECIVER_LOCATION = 1;
    public static final int SEND_GPS = 2;

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int what = msg.what;
            switch (what) {
                case RECIVER_LOCATION:
                    if (mLocClient != null) {
                        LogUtil.d(TAG, "mLocClient:" + mLocClient);
                        BDLocation bdLocation = mLocClient.getLastKnownLocation();
                        LogUtil.d(TAG, "bdLocation:" + bdLocation);
                        if (bdLocation != null) {
                            jwd = bdLocation.getLongitude() + "," + bdLocation.getLatitude();
                            mHandler.post(SendRun);
                        }
                    }
                    mHandler.sendEmptyMessageDelayed(RECIVER_LOCATION, 1000 * 70);
                    break;
                case SEND_GPS:
                    break;
            }
        }
    };
    private String sendUrl;
    private RequestQueue mRequestQueue;
    private LocationClient mLocClient;

    @Override
    public void onCreate() {
        LogUtil.d(TAG, "onCreate");
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        sp = this.getSharedPreferences(Constats.SHARE_KEY,
                Context.MODE_PRIVATE);
        sendUrl = Constats.HTTP_URL
                + Constats.SEND_JWD;
        mRequestQueue = Volley.newRequestQueue(this);
        userId = sp.getString(UserInfo.USER_ID, "");
        sendSign = MD5Util.getMD5(Constats.S_KEY + userId);
        ProviderReceiver providerReceiver = new ProviderReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ActionConstats.GET_LOAD_POWER);
        intentFilter.addAction(ActionConstats.GUIDE_INFO_CHANGE);
        intentFilter.addAction(JPushReceiver.JPush_request_location);
        registerReceiver(providerReceiver, intentFilter);
        initLocation();
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtil.d(TAG, "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    private void initLocation() {
        mLocClient = new LocationClient(this);
        final LocationClientOption option = mLocClient.getLocOption();
        mLocClient.registerLocationListener(new BDLocationListener() {
            @Override
            public void onReceiveLocation(BDLocation bdLocation) {
                jwd = bdLocation.getLongitude() + "," + bdLocation.getLatitude();
                LogUtil.d(TAG, "onReceiveLocation:" + jwd);
                LogUtil.d(TAG, "getScanSpan:" + option.getScanSpan());
                LogUtil.d(TAG, "LocationMode:" + option.getLocationMode());
                LogUtil.d(TAG, "isGPS:" + option.isOpenGps());
                LogUtil.d(TAG, "getVersion:" + mLocClient.getVersion());
                mHandler.post(SendRun);
                mHandler.sendEmptyMessageDelayed(RECIVER_LOCATION, 6000 * 10);
                mLocClient.stop();
            }
        });
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//高精度
        option.setPriority(Criteria.POWER_LOW);//低功耗
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        mLocClient.setLocOption(option);
        //发送经纬度
//        sendGps();
        mLocClient.start();//开始定位
    }


    @Override
    public IBinder onBind(Intent intent) {
        LogUtil.d(TAG, "onBind");
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        LogUtil.d(TAG, "onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        LogUtil.d(TAG, "onDestroy");
        mLocClient.stop();
        super.onDestroy();
    }

    /***
     * 发送自己经纬度线程
     */
    Runnable SendRun = new Runnable() {
        @Override
        public void run() {
            if (TextUtils.isEmpty(userId) || TextUtils.isEmpty(sendSign)) {
                LogUtil.d(TAG, "---userId:" + userId + "---sendSign:" + sendSign);
                userId = sp.getString(UserInfo.USER_ID, "");
                sendSign = MD5Util.getMD5(Constats.S_KEY + userId);
            } else {
                sendParams = new HashMap<>();
                LogUtil.d(TAG, "---jwd:" + jwd + "---userId:" + userId + "---sendSign:" + sendSign);
                sendParams.put("userid", userId);
                sendParams.put("jwd", jwd);
                sendParams.put("sign", sendSign);
                LogUtil.d(TAG, "经纬度上传开始");
                JsonRequest jsonRequest = new VolleyJsonUtil().createJsonObjectRequest(Request.Method.POST, sendUrl, sendParams,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject jsonObject) {
                                LogUtil.d(TAG, "上传经纬度：" + jsonObject.toString());
                                int result = 1;
                                try {
                                    result = jsonObject.getInt("result");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                if (result == 0) {
                                    LogUtil.d(TAG, "上传成功");
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError volleyError) {
                                LogUtil.d(TAG, "网络错误");
                            }
                        });
                if (mRequestQueue != null)
                    mRequestQueue.add(jsonRequest);
            }
        }
    };

    public class ProviderReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            LogUtil.d(TAG, "onReceive:");
            String action = intent.getAction();
            switch (action) {
                case ActionConstats.GET_LOAD_POWER://扫描成为导游不再发送经纬度
                    if (mLocClient != null) {
                        mLocClient.stop();
                        LogUtil.d(TAG, "mLocClient.stop():");
                        mLocClient = null;
                    }
                    break;
                case ActionConstats.GUIDE_INFO_CHANGE://失去导游权限重新定位

                    boolean aBoolean = sp.getBoolean(UserInfo.ISLOAD, false);
                    if (mLocClient != null) {
                        mLocClient.start();
                        LogUtil.d(TAG, "mLocClient.start");
                    } else {
                        initLocation();
                        LogUtil.d(TAG, "initLocation()");
                    }
                    break;
                case JPushReceiver.JPush_request_location://导游请求经纬度
                    mHandler.removeMessages(RECIVER_LOCATION);
                    mHandler.sendEmptyMessage(RECIVER_LOCATION);
                    break;
            }
        }
    }
}
