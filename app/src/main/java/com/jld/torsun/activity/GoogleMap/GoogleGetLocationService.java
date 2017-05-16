package com.jld.torsun.activity.GoogleMap;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.maps.model.LatLng;
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


public class GoogleGetLocationService extends Service implements GoogleApiClient.ConnectionCallbacks, LocationListener, GoogleApiClient.OnConnectionFailedListener {

    private final String TAG = "GoogleGetLocationService";
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

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };
    private String sendUrl;
    private RequestQueue mRequestQueue;
    private Location location;
    private String provider;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    @Override
    public void onCreate() {
        LogUtil.d(TAG, "onCreate");
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//        openGPSSettings();
        sp = this.getSharedPreferences(Constats.SHARE_KEY,
                Context.MODE_PRIVATE);
        sendUrl = Constats.HTTP_URL
                + Constats.SEND_JWD;
        mRequestQueue = Volley.newRequestQueue(this);
        userId = sp.getString(UserInfo.USER_ID, "");
        sendSign = MD5Util.getMD5(Constats.S_KEY + userId);

        ProviderReceiver providerReceiver = new ProviderReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.location.PROVIDERS_CHANGED");
        intentFilter.addAction(JPushReceiver.JPush_request_location);
        intentFilter.addAction(ActionConstats.GET_LOAD_POWER);
        registerReceiver(providerReceiver, intentFilter);
        createLocationRequest();
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mGoogleApiClient.connect();
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        LogUtil.d(TAG, "onStartCommand:" + mLastLocation);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        LogUtil.d(TAG, "onDestroy");
        mGoogleApiClient.disconnect();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
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

//    /**
//     * 位置改变监听方法
//     *
//     * @param location
//     */
//    @Override
//    public void onLocationChanged(Location location) {
//        LogUtil.d(TAG, "onLocationChanged");
////        //火星转换
////        location = LocationTransition.Location_Transition(location);
//        this.location = location;
//        jwd = location.getLongitude() + "," + location.getLatitude();
//        mHandler.post(SendRun);
//    }

    //    private void getLocation() {
//        // 查找到服务信息
//        Criteria criteria = new Criteria();
//        criteria.setAccuracy(Criteria.ACCURACY_FINE); // 高精度
//        criteria.setAltitudeRequired(false);
//        criteria.setBearingRequired(false);
//        criteria.setCostAllowed(true);
//        criteria.setPowerRequirement(Criteria.POWER_LOW); // 低功耗
//        provider = locationManager.getBestProvider(criteria, true);
//        location = locationManager.getLastKnownLocation(provider); // 通过GPS获取位置
//
//        LogUtil.d(TAG, "location:" + location);
//        // 设置监听器，自动更新的最小时间为间隔N秒(1秒为1*1000，这样写主要为了方便)或最小位移变化超过N米
//        locationManager.requestLocationUpdates(provider, 1000 * 10, 0,
//                this);
//        LocationProvider gpsProvider = locationManager.getProvider(LocationManager.GPS_PROVIDER);
//        LogUtil.d(TAG, "gpsProvider：" + gpsProvider);
//        if (gpsProvider != null && location != null) {
//            isGps = true;
//        }
//    }
    protected void createLocationRequest() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                        builder.build());

    }


    private void startActivityForResult(Intent intent, int i) {
        LogUtil.d(TAG, "startActivityForResult:" + i);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        LogUtil.d(TAG, "onConnected:" + mLastLocation);

        if (mLastLocation != null) {
            isGps = true;
            jwd = mLastLocation.getLongitude() + "," + mLastLocation.getLatitude();
            mHandler.post(SendRun);
            mGoogleApiClient.disconnect();
//            LocationServices.FusedLocationApi.requestLocationUpdates(
//                    mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        //        //火星转换
//        location = LocationTransition.Location_Transition(location);
        LogUtil.d(TAG, "onLocationChanged:" + location);

        this.location = location;
        jwd = location.getLongitude() + "," + location.getLatitude();
        mHandler.post(SendRun);
    }

    public class ProviderReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            LogUtil.d(TAG, "onReceive:");
            String action = intent.getAction();
            switch (action) {
                case "android.location.PROVIDERS_CHANGED":
                    break;
                case ActionConstats.GET_LOAD_POWER://如果导游获取导游权限则不再发送经纬度
                    LogUtil.d(TAG, "GET_LOAD_POWER:");

                    if (mGoogleApiClient != null)
                        mGoogleApiClient.disconnect();
                    break;
                case ActionConstats.GUIDE_INFO_CHANGE://失去导游权限重新定位
                    LogUtil.d(TAG, "GUIDE_INFO_CHANGE:");

                    if (mGoogleApiClient != null)
                        mGoogleApiClient.connect();
                    else
                        createLocationRequest();
                    break;
                case JPushReceiver.JPush_request_location:
                    LogUtil.d(TAG, "JPush_request_location:");

                    if (mGoogleApiClient != null) {
//                        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
//                                mGoogleApiClient);
//                        jwd = mLastLocation.getLongitude() + "," + mLastLocation.getLatitude();
//                        mHandler.post(SendRun);
                        mGoogleApiClient.connect();
                    }
                    break;
            }

        }
    }
}
