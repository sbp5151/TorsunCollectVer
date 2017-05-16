package com.jld.torsun.activity.GoogleMap;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.jld.torsun.ActivityManageFinish;
import com.jld.torsun.R;
import com.jld.torsun.activity.MainFragment;
import com.jld.torsun.activity.fragment.MyOrientationListener;
import com.jld.torsun.db.MemberDao;
import com.jld.torsun.db.TeamDao;
import com.jld.torsun.http.VolleyJsonUtil;
import com.jld.torsun.modle.TeamMember;
import com.jld.torsun.util.Constats;
import com.jld.torsun.util.DialogUtil;
import com.jld.torsun.util.LogUtil;
import com.jld.torsun.util.MD5Util;
import com.jld.torsun.util.ToastUtil;
import com.jld.torsun.util.UserInfo;
import com.jld.torsun.util.distanceUtil;
import com.jld.torsun.view.RoundImageViewByXfermode2;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.bitmap.BitmapDisplayConfig;
import com.lidroid.xutils.bitmap.callback.BitmapLoadCallBack;
import com.lidroid.xutils.bitmap.callback.BitmapLoadFrom;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class GoogleLocation extends FragmentActivity implements
        LocationListener, LocationSource, GoogleMap.OnCameraChangeListener, OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {
    GoogleMap mMap;
    public static final String TAG = "GoogleLocation";
    private TextView lacation_num;
    private Button iv_map_icon_1;
    private Button iv_map_icon_2;
    private Button iv_map_icon_3;
    private SharedPreferences sp;
    private Criteria criteria;
    private GoogleApiClient mGoogleApiClient;
    private android.location.LocationListener locationListener;
    public OnLocationChangedListener locationSource;
    LocationManager locationManager;
    private Location mCurrentLocation;
    private LatLng mCurrentLatLng;
    private HashMap<String, TeamMember> tourists = new HashMap<>();
    public Map<Integer, MyItem> all_infos = new HashMap<>();
    public List<MyItem> send_info = new ArrayList<>();
    public List<Integer> icon_win = new ArrayList<>();

    private boolean isDestroy = false;
    private final int FINISH = 1;
    private final int ANIMATION_OUT = 2;
    private final int ANIMATION_IN = 3;
    private final int SHOW_ITEM = 4;
    private final int DIALOG_DISMISS = 6;
    public final int SETING_HINT = 7;
    public final int GET_ITEM_LOCATION = 8;
    public final int CHANGE_CAMERA = 9;
    public int count;
    private String user_id;
    public int number;//生成marker的数量
    private Lock lock;
    public ArrayList<Integer> online_infos = new ArrayList<>();
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int what = msg.what;
            switch (what) {
                case FINISH://退出
                    finish();
                    break;
                case ANIMATION_OUT://按钮退出动画
                    anim_out();
                    break;
                case ANIMATION_IN://按钮进入动画
                    anim_in();
                    break;
                case SHOW_ITEM://接收所有成员的经纬度，并生成marker
                    MyLocation location = (MyLocation) msg.obj;
                    if (location.count < 1) {
                        lacation_num.setText(location_num_front + "(" + 1 + ")");
                        return;
                    }
                    online_infos.clear();
                    for (TouristLocation item : location.item) {
                        //不在线的不处理
                        if ("0".equals(item.timeout) || userId.equals(item.userid))
                            continue;
                        String[] split = item.jwd.split(",");
                        LatLng latLng = new LatLng(Double.parseDouble(split[1]), Double.parseDouble(split[0]));
                        //与团队成员进行匹配
                        LogUtil.d(TAG, "all_infos.containsKey(item.userid):" + all_infos.containsKey(Integer.parseInt(item.userid)));
                        LogUtil.d(TAG, "all_infos.containsKey(item.userid):" + item.userid);
                        if (all_infos.containsKey(Integer.parseInt(item.userid))) { //匹配成功 在线
                            LogUtil.d(TAG, "匹配成功在线:" + item.userid);
                            MyItem myItem = all_infos.get(Integer.parseInt(item.userid));
                            online_infos.add(Integer.parseInt(item.userid));
                            /**在指定位置添加infoWindow*/
                            myItem.setmPosition(latLng);
                            if (TextUtils.isEmpty(myItem.getIcon())) {
                                LogUtil.d(TAG, "头像连接为空----:");
                                Marker marker = all_infos.get(Integer.parseInt(item.userid)).getMarker();
                                if (null != marker) {
                                    marker.setPosition(latLng);
                                    all_infos.get(Integer.parseInt(item.userid)).setMarker(marker);
                                    continue;
                                }
                                Marker melbourne = mMap.addMarker(new MarkerOptions()
                                        .position(latLng)
                                        .title(myItem.getNick())
                                        .snippet(distanceUtil.getDistance_google(mCurrentLatLng, latLng) + "m")
                                        .icon(default_bitmapDescriptor));
                                all_infos.get(Integer.parseInt(item.userid)).setMarker(melbourne);
                            } else if (!icon_win.contains(Integer.parseInt(item.userid))) {
                                LogUtil.d(TAG, "加载头像----:");
                                Marker melbourne = mMap.addMarker(new MarkerOptions()
                                        .position(latLng)
                                        .title(myItem.getNick())
                                        .snippet(distanceUtil.getDistance_google(mCurrentLatLng, latLng) + "m")
                                        .icon(default_bitmapDescriptor));
                                all_infos.get(Integer.parseInt(item.userid)).setMarker(melbourne);
                                all_infos.get(Integer.parseInt(item.userid)).setmPosition(latLng);
                                setMarker(myItem.getIcon(), Integer.parseInt(item.userid));
                            } else {
                                LogUtil.d(TAG, "头像已经加载过----:");
                                Marker marker = all_infos.get(Integer.parseInt(item.userid)).getMarker();
                                marker.setPosition(latLng);
                                all_infos.get(Integer.parseInt(item.userid)).setMarker(marker);
                            }
                        }
                        LogUtil.d(TAG, "teamMember.img:加载完成");
                    }
                    lacation_num.setText(location_num_front + "(" + (online_infos.size() + 1) + ")");
//                    mHandler.sendEmptyMessageDelayed(SHOW, 3000);
//                    mHandler.postDelayed(GetRun, 1000 * 60);
                    break;
                case DIALOG_DISMISS:
                    loadingDialog.dismiss();
                    break;
                case SETING_HINT://设置提示
                    String str = (String) msg.obj;
                    lacation_num.setText(str);
                    break;
                case CHANGE_CAMERA:
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mCurrentLatLng, 16));
//                    locationSource.onLocationChanged(mLastLocation);
                    if (myMarker != null)
                        myMarker.remove();
                    if (mXDirection != 0)
                        myMarker = mMap.addMarker(new MarkerOptions()
                                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.direction_arrow))
                                .position(mCurrentLatLng)
                                .flat(true)
                                .rotation(mXDirection));
                    break;
            }
        }
    };
    private BitmapUtils bitmapUtils;
    private View rl_item_back;
    private String location_num_front;
    private float mXDirection;
    private Marker myMarker;
    private GoogleApiClient mGoogleApiClient1;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    private Animation animation1;
    private Animation animation2;
    private UiSettings uiSettings;
    private RoundImageViewByXfermode2 iv_icon;
    private String nik;
    private String phone;
    private String icon;
    private Dialog loadingDialog;
    private double mCurrentLantitude = 0;
    private double mCurrentLongitude = 0;

    public void setMarker(String requestUrl, int index) {
        View view = LayoutInflater.from(GoogleLocation.this).inflate(R.layout.map_item_icon, null);        /**设置头像*/
        iv_icon = (RoundImageViewByXfermode2) view.findViewById(R.id.iv_map_item_icon);
        iv_icon.setTag(index);
        bitmapUtils.display(iv_icon, requestUrl, new BitmapLoadCallBack<RoundImageViewByXfermode2>() {
            @Override
            public void onLoadCompleted(RoundImageViewByXfermode2 roundImageViewByXfermode2, String s, Bitmap bitmap, BitmapDisplayConfig bitmapDisplayConfig, BitmapLoadFrom bitmapLoadFrom) {
                /**从布局文件获取覆盖物*/
                int tag = (int) roundImageViewByXfermode2.getTag();
                icon_win.add(tag);
                LogUtil.d(TAG, "图片" + tag + "加载成功");
                view2 = LayoutInflater.from(GoogleLocation.this).inflate(R.layout.map_item_icon, null);
                RoundImageViewByXfermode2 iv_icon = (RoundImageViewByXfermode2) view2.findViewById(R.id.iv_map_item_icon);
                iv_icon.setImageBitmap(compressImage(bitmap));
                Marker marker = all_infos.get(tag).getMarker();
                if (marker != null) {
                    marker.setIcon(BitmapDescriptorFactory.fromBitmap(convertViewToBitmap(view2)));
                } else {
                    Marker melbourne = mMap.addMarker(new MarkerOptions()
                            .position(all_infos.get(tag).getmPosition())
                            .title(all_infos.get(tag).getNick())
                            .snippet(distanceUtil.getDistance_google(mCurrentLatLng, all_infos.get(tag).getmPosition()) + "m")
                            .icon(BitmapDescriptorFactory.fromBitmap(convertViewToBitmap(view2))));
                    all_infos.get(tag).setMarker(melbourne);
                }
            }

            @Override
            public void onLoadFailed(RoundImageViewByXfermode2 roundImageViewByXfermode2, String s, Drawable drawable) {
                int tag = (int) roundImageViewByXfermode2.getTag();
                LogUtil.d(TAG, "图片" + tag + "加载失败");
                Marker melbourne = mMap.addMarker(new MarkerOptions()
                        .position(all_infos.get(tag).getmPosition())
                        .title(all_infos.get(tag).getNick())
                        .snippet(distanceUtil.getDistance_google(mCurrentLatLng, all_infos.get(tag).getmPosition()) + "m")
                        .icon(default_bitmapDescriptor));
                all_infos.get(tag).setMarker(melbourne);
            }
        });
    }

    private Bitmap default_bitmap;
    private BitmapDescriptor default_bitmapDescriptor;
    private String userId;
    private String teamId;
    private String guiderId;
    private RequestQueue mRequestQueue;
    private long timeMillis = 0;
    private String getUrl;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_location);
        ActivityManageFinish.addActivity(this);
        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.google_map);
        supportMapFragment.getMapAsync(this);
        initData();
        initView();
        mHandler.sendEmptyMessageDelayed(ANIMATION_OUT, 130);
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        LogUtil.d(TAG, "onMapReady");
        mMap = googleMap;
        if (mMap == null) {
            String string = getResources().getString(R.string.chekeGooglePlay);
            ToastUtil.showToast(this, string, 3000);
            finish();
            return;
        }
        initMap();
    }

    public void initData(){
        sp = this.getSharedPreferences(Constats.SHARE_KEY,
                Context.MODE_PRIVATE);
        userId = sp.getString(UserInfo.USER_ID, "");
        nik = sp.getString(UserInfo.NIK, "");
        phone = sp.getString(UserInfo.LOGIN_ACCOUT, "");
        icon = sp.getString(UserInfo.HEAD_ICON_URL, "");
        guiderId = sp.getString(UserInfo.GUIDER_ID, "");
        teamId = sp.getString(UserInfo.LAST_SERVICE_TEAM_ID, "");
        user_id = sp.getString(UserInfo.USER_ID, "");
        getUrl = Constats.HTTP_URL
                + Constats.GET_JWD;
        default_bitmapDescriptor = getDefault_bitmapDescriptor(false);
        location_num_front = getResources().getString(R.string.map_location_num);
        lock = new ReentrantLock();
        bitmapUtils = new BitmapUtils(this);
        mRequestQueue = Volley.newRequestQueue(this);

        MemberDao memberDao = MemberDao.getInstance(this);
        TeamDao teamDao = TeamDao.getInstance(this);
        ArrayList<TeamMember> mList = (ArrayList<TeamMember>) memberDao.findMemberByteamid(teamDao.selectLastTeamid(MainFragment.MYSELFID));
        for (TeamMember item : mList) {
            if (userId.equals(item.getUserid()))
                all_infos.put(Integer.parseInt(item.getUserid()), new MyItem(item.getNick(), item.getImg(), item.getMobile(), "1", "1", false));
            else
                all_infos.put(Integer.parseInt(item.getUserid()), new MyItem(item.getNick(), item.getImg(), item.getMobile(), "0", "0", false));
        }
    }
    public void initView() {
        lacation_num = (TextView) findViewById(R.id.tv_google_location_num);
        if (TextUtils.isEmpty(sp.getString(UserInfo.LAST_SERVICE_TEAM_ID, ""))) {
            if (sp.getBoolean(UserInfo.ISLOAD, false)) {
                lacation_num.setText(getResources().getString(R.string.map_hint_1));
            } else {
                lacation_num.setText(getResources().getString(R.string.map_hint_3));
            }
        }
        iv_map_icon_1 = (Button) findViewById(R.id.iv_google_map_icon_1);//展开团员列表
        iv_map_icon_1.setVisibility(View.INVISIBLE);
        iv_map_icon_2 = (Button) findViewById(R.id.iv_google_map_icon_2);//定位
        iv_map_icon_2.setVisibility(View.INVISIBLE);
        iv_map_icon_3 = (Button) findViewById(R.id.iv_google_map_icon_3);//退出
        iv_map_icon_1.setOnClickListener(this);
        iv_map_icon_2.setOnClickListener(this);
        iv_map_icon_3.setOnClickListener(this);
    }

    public void initMap() {
        MapsInitializer.initialize(this);
//        //激活定位
        mMap.setMyLocationEnabled(true);
        uiSettings = mMap.getUiSettings();
        //隐藏自带定位按钮
        uiSettings.setMyLocationButtonEnabled(false);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//      mMap.setLocationSource(this);//获取locationSource用于定位
        createLocationRequest();
        initOritationListener();
    }

    protected void createLocationRequest() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        //GooglePlayAPI获取经纬度
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        LogUtil.d(TAG, "onConnected" + mLastLocation);
        if (mLastLocation != null) {
            mHandler.post(GetRun);//获取团队成员经纬度
            mCurrentLatLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
//            mCurrentLatLng = LocationTransition.LatLng_Transition(mCurrentLatLng);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mCurrentLatLng, 16));
            if (myMarker != null)
                myMarker.remove();
            if (mXDirection != 0)
                myMarker = mMap.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.direction_arrow))
                        .position(mCurrentLatLng)
                        .flat(true)
                        .rotation(mXDirection));
        } else {
            ToastUtil.showToast(this, getResources().getString(R.string.open_gps), 5000);
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        LogUtil.d(TAG, "onLocationChanged");

        if (mCurrentLocation == null) {
            mHandler.post(GetRun);//获取团队成员经纬度
            mCurrentLocation = location;
            mCurrentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
//            mCurrentLatLng = LocationTransition.LatLng_Transition(mCurrentLatLng);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mCurrentLatLng, 16));
            if (myMarker != null)
                myMarker.remove();
            myMarker = mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.direction_arrow))
                    .position(mCurrentLatLng)
                    .flat(true)
                    .rotation(mXDirection));
        } else {
            mCurrentLocation = location;
            mCurrentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
//            mCurrentLatLng = LocationTransition.LatLng_Transition(mCurrentLatLng);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.iv_google_map_icon_1://展开团员列表
                if (mCurrentLocation == null) {
                    ToastUtil.showToast(this, getResources().getString(R.string.open_gps), 3000);
                    return;
                }
                send_info.clear();
                int onlineNum = 0;
                for (int item : online_infos) {
                    LogUtil.d(TAG, "keyP:" + item);
                    if (all_infos.containsKey(item)) {
                        onlineNum++;
                        all_infos.get(item).setTimeout("1");
                    } else
                        all_infos.get(item).setTimeout("0");
                }
                Iterator<Map.Entry<Integer, MyItem>> iterator_all = all_infos.entrySet().iterator();
                while (iterator_all.hasNext()) {
                    Map.Entry<Integer, MyItem> next = iterator_all.next();
                    send_info.add(next.getValue());
                }
                LogUtil.d(TAG, "all_infos:" + all_infos.size());
                Intent intent = new Intent(GoogleLocation.this, GoogleMapMemberList.class);
                intent.putParcelableArrayListExtra("online_infos", (ArrayList<? extends Parcelable>) send_info);
                intent.putExtra("mCurrentLantitude", mCurrentLocation.getLatitude());
                intent.putExtra("mCurrentLongitude", mCurrentLocation.getLongitude());
                intent.putExtra("onlineNum", onlineNum);
                startActivity(intent);
                break;
            case R.id.iv_google_map_icon_2://定位
                if (mCurrentLatLng != null) {
                    loadingDialog = DialogUtil.createLoadingDialog(this, getResources().getString(R.string.loaddata));
                    loadingDialog.show();
                    if (timeMillis == 0 || (System.currentTimeMillis() - timeMillis) > 5000) {
                        mHandler.post(requesRun);
                        timeMillis = System.currentTimeMillis();
                    } else {
                        mHandler.sendEmptyMessageDelayed(DIALOG_DISMISS, 1000);
                    }
                }
                break;
            case R.id.iv_google_map_icon_3://退出
                mHandler.sendEmptyMessage(ANIMATION_IN);
                mHandler.sendEmptyMessageDelayed(FINISH, 220);
                break;
        }
    }

    /**
     * 方向传感器
     */
    private void initOritationListener() {
        MyOrientationListener myOrientationListener = new MyOrientationListener(
                getApplicationContext());
        myOrientationListener
                .setOnOrientationListener(new MyOrientationListener.OnOrientationListener() {
                    @Override
                    public void onOrientationChanged(float x) {
                        mXDirection = x;
                        if (mCurrentLatLng == null || mCurrentLocation == null)
                            return;
                        if (myMarker != null) {
                            myMarker.remove();
                        }
                        myMarker = mMap.addMarker(new MarkerOptions()
                                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.direction_arrow))
                                .position(mCurrentLatLng)
                                .flat(true)
                                .rotation(mXDirection));
                    }
                });
        myOrientationListener.start();
    }


    @Override
    public void onConnectionSuspended(int i) {
        LogUtil.d(TAG, "onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        LogUtil.d(TAG, "onConnectionFailed");
    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        locationSource = onLocationChangedListener;
        LogUtil.d(TAG, "activate");
    }

    @Override
    public void deactivate() {
        locationSource = null;
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        LogUtil.d(TAG, "cameraPosition" + cameraPosition);
    }

    private View view2;

    /**
     * view 转bitmap
     *
     * @param view
     * @return
     */
    public static Bitmap convertViewToBitmap(View view) {
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.buildDrawingCache();
        Bitmap bitmap = view.getDrawingCache();
        return bitmap;
    }

    /**
     * 获取默认的覆盖物头像
     *
     * @param isLoad
     * @return
     */
    public BitmapDescriptor getDefault_bitmapDescriptor(Boolean isLoad) {
        //获取默认头像的布局文件
        if (view2 == null)
            view2 = LayoutInflater.from(GoogleLocation.this).inflate(R.layout.map_item_icon, null);
        //将布局文件转换成覆盖物
        return BitmapDescriptorFactory.fromBitmap(convertViewToBitmap(view2));
    }

    Runnable requesRun = new Runnable() {
        @Override
        public void run() {
            String url = Constats.HTTP_URL + Constats.MAP_REQUES_LOCATION_URL;
            String userId = sp.getString(UserInfo.USER_ID, "");
            String tuanId = sp.getString(UserInfo.LAST_SERVICE_TEAM_ID, "");
            String sign = MD5Util.getMD5(Constats.S_KEY + userId + tuanId);
            HashMap<String, String> Params = new HashMap<>();
            Params.put("userid", userId);
            Params.put("tuanid", tuanId);
            Params.put("sign", sign);
            LogUtil.d(TAG, Params.toString());
            JsonRequest result = new VolleyJsonUtil().createJsonObjectRequest(Request.Method.POST, url, Params, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject jsonObject) {
                    loadingDialog.dismiss();
                    LogUtil.d(TAG, jsonObject.toString());
                    try {
                        String result = jsonObject.getString("result");
                        LogUtil.d(TAG, "result:" + result);

                        if (!TextUtils.isEmpty(result) && result.equals("0")) {
                            mHandler.postDelayed(GetRun, 4000);
                            mHandler.sendEmptyMessage(CHANGE_CAMERA);
                        } else {
                            ToastUtil.showToast(GoogleLocation.this, GoogleLocation.this.getResources().getString(R.string.loaddata_un), 3000);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    ToastUtil.showToast(GoogleLocation.this, GoogleLocation.this.getResources().getString(R.string.network_latency), 3000);
                    loadingDialog.dismiss();
                    mHandler.sendEmptyMessage(CHANGE_CAMERA);
                }
            });
            if (mRequestQueue != null)
                mRequestQueue.add(result);
        }
    };

    /**
     * 获取团成员经纬度线程
     */
    Runnable GetRun = new Runnable() {
        @Override
        public void run() {

            if (TextUtils.isEmpty(guiderId) && mHandler != null) {
                Message message = mHandler.obtainMessage();
                message.obj = getResources().getString(R.string.again_tour_guide);
                message.what = SETING_HINT;
                mHandler.sendMessage(message);
                return;
            }
            if (TextUtils.isEmpty(teamId) && mHandler != null) {
                Message message = mHandler.obtainMessage();
                message.obj = getResources().getString(R.string.map_hint_1);
                message.what = SETING_HINT;
                mHandler.sendMessage(message);
                return;
            }
            if (TextUtils.isEmpty(userId))//获取用户ID
                return;

            String getSign = MD5Util.getMD5(Constats.S_KEY + userId + teamId);
            HashMap<String, String> getParams = new HashMap<>();
            getParams.put("userid", userId);
            getParams.put("tuanid", teamId);
            getParams.put("guideid", guiderId);
            getParams.put("sign", getSign);
            LogUtil.d(TAG, getParams.toString());
            LogUtil.d(TAG, "经纬度获取开始");
            JsonRequest jsonRequest = new VolleyJsonUtil().createJsonObjectRequest(Request.Method.POST, getUrl, getParams,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject jsonObject) {
                            LogUtil.d(TAG, "jsonObject:" + jsonObject);

                            int result = 1;
                            try {
                                result = jsonObject.getInt("result");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            if (result == 1001 && mHandler != null) {//不是最新团ID
                                boolean aBoolean = sp.getBoolean(UserInfo.ISLOAD, false);
                                if (aBoolean) {
                                    Message message = mHandler.obtainMessage();
                                    message.obj = getResources().getString(R.string.map_hint_1_again);
                                    message.what = SETING_HINT;
                                    mHandler.sendMessage(message);
                                } else {
                                    Message message = mHandler.obtainMessage();
                                    message.obj = getResources().getString(R.string.map_hint_2);
                                    message.what = SETING_HINT;
                                    mHandler.sendMessage(message);
                                }
                            }
                            if (result == 0) {
                                int count = 0;
                                try {
                                    count = jsonObject.getInt("count");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                if (count != 0) {
                                    Gson gson = new Gson();
                                    MyLocation location = gson.fromJson(jsonObject.toString(), MyLocation.class);
                                    LogUtil.d(TAG, "location:" + location);
                                    Message message = mHandler.obtainMessage();
                                    message.obj = location;
                                    message.what = SHOW_ITEM;
                                    mHandler.sendMessage(message);
                                }
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            ToastUtil.showToast(GoogleLocation.this, getResources().getString(R.string.to_server_failed), 3000);
                        }
                    });
            if (mRequestQueue != null)
                mRequestQueue.add(jsonRequest);
        }
    };

    private class MyLocation {
        public int count;
        public ArrayList<TouristLocation> item;

        @Override
        public String toString() {
            return "Location{" +
                    "count='" + count + '\'' +
                    ", list=" + item +
                    '}';
        }
    }

    private class TouristLocation {
        public String userid;
        public String jwd;
        public String timeout;

        @Override
        public String toString() {
            return "TouristLocation{" +
                    "userid='" + userid + '\'' +
                    ", jwd='" + jwd + '\'' +
                    ", timeout='" + timeout + '\'' +
                    '}';
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("谷歌地图"); // （仅有Activity的应用中SDK自动调用，不需要单独写）保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息。"SplashScreen"为页面名称，可自定义
        MobclickAgent.onPause(this);
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("谷歌地图"); // （仅有Activity的应用中SDK自动调用，不需要单独写）保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息。"SplashScreen"为页面名称，可自定义
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(GetRun);
        isDestroy = true;
        if (mCurrentLocation != null)
            mGoogleApiClient.disconnect();
        ActivityManageFinish.removeActivity(this);
    }

    //图片压缩
    private Bitmap compressImage(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        LogUtil.d(TAG, "compressImage1:" + baos.toByteArray().length);
        int options = 100;
        while (baos.toByteArray().length / 1024 > 3) {    //循环判断如果压缩后图片是否大于100kb,大于继续压缩
            baos.reset();//重置baos即清空baos
            options -= 10;//每次都减少10
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
        }
        LogUtil.d(TAG, "compressImage2:" + baos.toByteArray().length);
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片
        return bitmap;
    }

    private void anim_out() {
        animation1 = AnimationUtils.loadAnimation(GoogleLocation.this, R.anim.translate_map_icon1_out);
        animation2 = AnimationUtils.loadAnimation(GoogleLocation.this, R.anim.translate_map_icon2_out);
        animation2.setFillAfter(true);
        animation1.setFillAfter(true);
        iv_map_icon_1.setVisibility(View.VISIBLE);
        iv_map_icon_2.setVisibility(View.VISIBLE);
        iv_map_icon_1.startAnimation(animation1);
        iv_map_icon_2.startAnimation(animation2);
        iv_map_icon_1.setClickable(true);
        iv_map_icon_2.setClickable(true);
    }

    private void anim_in() {
        animation1 = AnimationUtils.loadAnimation(GoogleLocation.this, R.anim.translate_map_icon1_in);
        animation2 = AnimationUtils.loadAnimation(GoogleLocation.this, R.anim.translate_map_icon2_in);
        animation2.setFillAfter(true);
        animation1.setFillAfter(true);
        iv_map_icon_1.startAnimation(animation1);
        iv_map_icon_2.startAnimation(animation2);
        iv_map_icon_1.setClickable(false);
        iv_map_icon_2.setClickable(false);
    }
}
