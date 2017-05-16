package com.jld.torsun.activity.baiduMap;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ZoomControls;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.google.gson.Gson;
import com.jld.torsun.ActivityManageFinish;
import com.jld.torsun.R;
import com.jld.torsun.activity.BaseActivity;
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
import com.jld.torsun.util.imagecache.MyImageLoader;
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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import clusterutil.clustering.Cluster;
import clusterutil.clustering.ClusterItem;
import clusterutil.clustering.ClusterManager;

public class MapLocation extends BaseActivity implements View.OnClickListener, BaiduMap.OnMapClickListener, BaiduMap.OnMapLoadedCallback {
    /**
     * 地图控件
     */
    private MapView mMapView = null;
    /**
     * 地图实例
     */
    private BaiduMap mBaiduMap;
    /**
     * 最新一次的经纬度
     */
    private double mCurrentLantitude;
    private double mCurrentLongitude;

    // 定位相关
    LocationClient mLocClient;
    /**
     * 定位监听
     */
    public MyLocationListenner myListener = new MyLocationListenner();
    private MyLocationConfiguration.LocationMode mCurrentMode;
    BitmapDescriptor myBd;
    // UI相关
    boolean isFirstLoc = true; // 是否首次定位
    private SharedPreferences sp;
    private ImageLoader imageLoader;
    private View rl_item_back;
    private static final String TAG = "MapLocation";
    private MyOrientationListener myOrientationListener;
    private float mCurrentAccracy;
    private float mXDirection;
    private ImageView iv_map_icon_1;
    private ImageView iv_map_icon_2;
    private ImageView iv_map_icon_3;
    private static boolean LogIsShow = false;//make刷新完成
    /**
     * 图标是否展开
     */
    public final int FINISH = 1;
    public final int ANIMATION_OUT = 2;
    public final int ANIMATION_IN = 3;
    public static final int SHOW_ITEM = 4;
    private final int ADD_ITEM = 5;
    private final int SHOW = 6;
    public final int SETING_HINT = 7;
    public static final int HINT = 8;
    public final int DIALOG_DISMISS = 9;
    public final int DIALOG_DISMISS2 = 10;
    public final int UPDATA_ICON = 11;

    private ClusterManager<MyItem> mClusterManager;
    private BitmapUtils bitmapUtils;
    private Lock lock;
    private Lock lock_handler;
    private int addNum;
    public ArrayList<Integer> online_infos = new ArrayList<>();
    public List<Info> all_info = new ArrayList<>();
    public Map<Integer, Info> all_infos = new HashMap<>();
    public List<MyItem> myItems = new ArrayList<>();
    public Map<Integer, BitmapDescriptor> bds = new HashMap();
    public Map<Integer, Info> infos = new HashMap<>();

    public List<Integer> icon_win = new ArrayList<>();
    private View view2;
    private Handler serviceHandler;
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int what = msg.what;
            switch (what) {
                case FINISH://退出
                    finish();
                    break;
                case ANIMATION_OUT://按钮动画
                    Animation animation1 = AnimationUtils.loadAnimation(MapLocation.this, R.anim.translate_map_icon1_out);
                    Animation animation2 = AnimationUtils.loadAnimation(MapLocation.this, R.anim.translate_map_icon2_out);
                    animation2.setFillAfter(true);
                    animation1.setFillAfter(true);
                    iv_map_icon_1.setVisibility(View.VISIBLE);
                    iv_map_icon_2.setVisibility(View.VISIBLE);
                    iv_map_icon_1.startAnimation(animation1);
                    iv_map_icon_2.startAnimation(animation2);
                    iv_map_icon_1.setClickable(true);
                    iv_map_icon_2.setClickable(true);
                    break;
                case ANIMATION_IN://按钮动画
                    animation1 = AnimationUtils.loadAnimation(MapLocation.this, R.anim.translate_map_icon1_in);
                    animation2 = AnimationUtils.loadAnimation(MapLocation.this, R.anim.translate_map_icon2_in);
                    animation2.setFillAfter(true);
                    animation1.setFillAfter(true);
                    iv_map_icon_1.setVisibility(View.INVISIBLE);
                    iv_map_icon_2.setVisibility(View.INVISIBLE);
                    iv_map_icon_1.startAnimation(animation1);
                    iv_map_icon_2.startAnimation(animation2);
                    iv_map_icon_1.setClickable(false);
                    iv_map_icon_2.setClickable(false);
                    break;
                case SHOW_ITEM://接收服务器经纬度
                    Location location = (Location) msg.obj;
                    if (location.count < 1) {
                        lacation_num.setText(location_num_front + "(" + 1 + ")");
                        return;
                    }
                    mClusterManager.clearItems();
                    myItems.clear();
                    online_infos.clear();
                    LogUtil.d(TAG, "加载头像：" + location.item.size());
                    LogUtil.d(TAG, "加载头像：" + userId);

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
                            Info info = all_infos.get(Integer.parseInt(item.userid));

                            online_infos.add(Integer.parseInt(item.userid));
                            /**在指定位置添加infoWindow*/
                            LatLng mylatLng = new LatLng(mCurrentLantitude, mCurrentLongitude);
                            info.setLatLng(latLng);
                            info.setDistance(distanceUtil.getDistance(mylatLng, latLng));
                            if (TextUtils.isEmpty(info.getIcon())) {
                                LogUtil.d(TAG, "头像连接为空----:");
                                bds.put(Integer.parseInt(item.userid), default_bitmapDescriptor);
                                MyItem myItem = new MyItem(Integer.parseInt(item.userid), info);
                                mClusterManager.addItem(myItem);
                                mClusterManager.cluster();
                            } else if (!icon_win.contains(Integer.parseInt(item.userid))) {
                                LogUtil.d(TAG, "加载头像----:");
                                setMarker(info.getIcon(), Integer.parseInt(item.userid));
                            } else {
                                LogUtil.d(TAG, "头像已经加载过----:");
                                MyItem myItem = new MyItem(Integer.parseInt(item.userid), info);
                                mClusterManager.addItem(myItem);
                                mClusterManager.cluster();
                            }
                            /**添加myItem*/
//                            MyItem myItem = new MyItem(Integer.parseInt(item.userid), latLng, info);
//                            myItems.add(myItem);
                        }
                        LogUtil.d(TAG, "teamMember.img:加载完成");
                    }
                    lacation_num.setText(location_num_front + "(" + (online_infos.size() + 1) + ")");
//                    mHandler.sendEmptyMessageDelayed(SHOW, 3000);
//                    mHandler.postDelayed(GetRun, 1000 * 60);
                    break;
                case ADD_ITEM:
//                    LogUtil.d(TAG, "tourists:" + tourists.size());
                    mHandler.sendEmptyMessageDelayed(ADD_ITEM, 10000);
                    break;
                case SHOW:
                    LogUtil.d(TAG, "---------加载完成--------：" + myItems.size() + "---" + bds.size());
                    mClusterManager.clearItems();//清空上一次所有覆盖物

                    mClusterManager.cluster();//进行聚合运算一次
                    break;
                case UPDATA_ICON:
//                    mClusterManager.addItem(myItems.get(0));//清空上一次所有覆盖物
//                    mClusterManager.cluster();//进行聚合运算一次
//                    mClusterManager.clearItems();
//                    mClusterManager.addItems(myItems);
//                    mClusterManager.cluster();//进行聚合运算一次
                    break;
                case SETING_HINT:
                    String str = (String) msg.obj;
                    lacation_num.setText(str);
                    break;
                case HINT:
                    LogUtil.d(TAG, "HINT" + sp.getString(UserInfo.LAST_SERVICE_TEAM_ID, ""));

                    if (TextUtils.isEmpty(sp.getString(UserInfo.LAST_SERVICE_TEAM_ID, ""))) {
                        if (sp.getBoolean(UserInfo.ISLOAD, false)) {
                            lacation_num.setText(getResources().getString(R.string.map_hint_1));
                        } else {
                            lacation_num.setText(getResources().getString(R.string.map_hint_3));
                        }
                    } else {
                        lacation_num.setText("");
                        return;
                    }
                    mHandler.sendEmptyMessageDelayed(HINT, 1000);
                    break;
                case DIALOG_DISMISS://请求经纬度
                    requesDialog.dismiss();
                    mHandler.postDelayed(GetRun, 4000);
                    break;
                case DIALOG_DISMISS2:
                    requesDialog.dismiss();
                    break;
            }
        }
    };
    private TextView lacation_num;
    private String location_num_front;
    private int count;
    private Bitmap default_bitmap;//默认头像
    private HashMap<String, TeamMember> tourists = new HashMap<>();
    private BitmapDescriptor default_bitmapDescriptor;
    private String userId;
    private String teamId;
    private String guiderId;
    private Dialog requesDialog;
    private RequestQueue mRequestQueue;
    private long timeMillis = 0;
    private String nik;
    private String phone;
    private String icon;
    private String getUrl;
    private RoundImageViewByXfermode2 iv_icon;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtil.d(TAG, "onCreate");
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_app_download);
        ActivityManageFinish.addActivity(this);
        initData();
        initView();
        initMap();
        mHandler.sendEmptyMessageDelayed(ANIMATION_OUT, 130);
    }


    public void initView() {
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        lacation_num = (TextView) findViewById(R.id.tv_location_num);
        iv_map_icon_1 = (ImageView) findViewById(R.id.iv_map_icon_1);//展开团员列表
        iv_map_icon_1.setVisibility(View.INVISIBLE);
        iv_map_icon_2 = (ImageView) findViewById(R.id.iv_map_icon_2);//定位
        iv_map_icon_2.setVisibility(View.INVISIBLE);
        iv_map_icon_3 = (ImageView) findViewById(R.id.iv_map_icon_3);//退出
        iv_map_icon_1.setOnClickListener(this);
        iv_map_icon_2.setOnClickListener(this);
        iv_map_icon_3.setOnClickListener(this);
        mHandler.sendEmptyMessage(HINT);
    }

    public void initMap() {
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        mClusterManager = new ClusterManager<MyItem>(this, mBaiduMap);
        // 添加Marker点
        // 设置地图监听，当地图状态发生改变时，进行点聚合运算
        mBaiduMap.setOnMapStatusChangeListener(mClusterManager);
        // 定位初始化
        mLocClient = new LocationClient(this);
        mLocClient.registerLocationListener(myListener);

        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(3000);//3000ms定位一次

        mLocClient.setLocOption(option);
        mLocClient.start();//开始定位
        initOritationListener();//初始化方向传感器
        mBaiduMap
                .setMyLocationConfigeration(new MyLocationConfiguration(
                        mCurrentMode, true, null));

        mBaiduMap.setOnMarkerClickListener(mcl);
        mBaiduMap.setOnMapClickListener(this);
        /**隐藏缩放按钮*/
        int count = mMapView.getChildCount();
        View scale = null;
        for (int i = 0; i < count; i++) {
            View child = mMapView.getChildAt(i);
            if (child instanceof ZoomControls) {
                scale = child;
                break;
            }
        }
        scale.setVisibility(View.GONE);
        addNum = 0;
    }

    private void initData() {
        imageLoader = MyImageLoader.getInstance(this);
        sp = this.getSharedPreferences(Constats.SHARE_KEY,
                Context.MODE_PRIVATE);
        bitmapUtils = new BitmapUtils(this);
        lock = new ReentrantLock();
        lock_handler = new ReentrantLock();
        default_bitmap = BitmapFactory.decodeResource(MapLocation.this.getResources(), R.mipmap.default_hear_ico);
        default_bitmap = zoomImage(default_bitmap, MainFragment.density * 45, MainFragment.density * 45);//默认头像

        location_num_front = getResources().getString(R.string.map_location_num);
        userId = sp.getString(UserInfo.USER_ID, "");
        nik = sp.getString(UserInfo.NIK, "");
        phone = sp.getString(UserInfo.LOGIN_ACCOUT, "");
        icon = sp.getString(UserInfo.HEAD_ICON_URL, "");
        guiderId = sp.getString(UserInfo.GUIDER_ID, "");

        teamId = TeamDao.getInstance(MapLocation.this).selectServiceLastTeamid(userId);
        if (TextUtils.isEmpty(teamId))
            teamId = sp.getString(UserInfo.LAST_SERVICE_TEAM_ID, "");

        getUrl = Constats.HTTP_URL
                + Constats.GET_JWD;
        //获取默认覆盖物头像
        default_bitmapDescriptor = getDefault_bitmapDescriptor(false);
        mRequestQueue = Volley.newRequestQueue(this);

        MemberDao memberDao = MemberDao.getInstance(this);
        TeamDao teamDao = TeamDao.getInstance(this);
        ArrayList<TeamMember> mList = (ArrayList<TeamMember>) memberDao.findMemberByteamid(teamDao.selectLastTeamid(MainFragment.MYSELFID));
        for (TeamMember item : mList) {
            if (userId.equals(item.getUserid()))
                all_infos.put(Integer.parseInt(item.getUserid()), new Info(item.getNick(), item.getImg(), item.getMobile(), "1", false, "1"));
            else
                all_infos.put(Integer.parseInt(item.getUserid()), new Info(item.getNick(), item.getImg(), item.getMobile(), "0", false, "0"));
        }
    }

    public void setMarker(String requestUrl, int index) {
        View view = LayoutInflater.from(MapLocation.this).inflate(R.layout.map_item_icon, null);        /**设置头像*/
        iv_icon = (RoundImageViewByXfermode2) view.findViewById(R.id.iv_map_item_icon);
        iv_icon.setTag(index);
        bitmapUtils.display(iv_icon, requestUrl, new BitmapLoadCallBack<RoundImageViewByXfermode2>() {
            @Override
            public void onLoadCompleted(RoundImageViewByXfermode2 roundImageViewByXfermode2, String s, Bitmap bitmap, BitmapDisplayConfig bitmapDisplayConfig, BitmapLoadFrom bitmapLoadFrom) {
                /**从布局文件获取覆盖物*/
                int tag = (int) roundImageViewByXfermode2.getTag();
                icon_win.add(tag);
                LogUtil.d(TAG, "图片" + tag + "加载成功");
                if (view2 == null)
                    view2 = LayoutInflater.from(MapLocation.this).inflate(R.layout.map_item_icon, null);
                rl_item_back = view2.findViewById(R.id.rl_map_item_back);
                RoundImageViewByXfermode2 iv_icon = (RoundImageViewByXfermode2) view2.findViewById(R.id.iv_map_item_icon);
                iv_icon.setImageBitmap(compressImage(bitmap));
                myBd = BitmapDescriptorFactory.fromView(rl_item_back);
                /**为聚合物添加头像*/
                bds.put(tag, myBd);
                MyItem myItem = new MyItem(tag, all_infos.get(tag));
                mClusterManager.addItem(myItem);
                mClusterManager.cluster();
            }

            @Override
            public void onLoadFailed(RoundImageViewByXfermode2 roundImageViewByXfermode2, String s, Drawable drawable) {
                int tag = (int) roundImageViewByXfermode2.getTag();
                LogUtil.d(TAG, "图片" + tag + "加载失败");
                bds.put(tag, default_bitmapDescriptor);
                MyItem myItem = new MyItem(tag, all_infos.get(tag));
                mClusterManager.addItem(myItem);
                mClusterManager.cluster();
            }
        });
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

    /**
     * 获取默认的覆盖物头像
     *
     * @param isLoad
     * @return
     */
    public BitmapDescriptor getDefault_bitmapDescriptor(Boolean isLoad) {
        //获取默认头像的布局文件
        if (view2 == null)
            view2 = LayoutInflater.from(MapLocation.this).inflate(R.layout.map_item_icon, null);
        rl_item_back = view2.findViewById(R.id.rl_map_item_back);
        RoundImageViewByXfermode2 iv_icon = (RoundImageViewByXfermode2) view2.findViewById(R.id.iv_map_item_icon);
        ImageView icon_back = (ImageView) view2.findViewById(R.id.iv_map_item_icon_back);
        iv_icon.setImageBitmap(default_bitmap);
        //如果是导游则改变边框颜色
        if (isLoad) {
            icon_back.setImageResource(R.mipmap.map_daoyou_icon);
        } else {
            icon_back.setImageResource(R.mipmap.map_youke_icon);
        }
        //将布局文件转换成覆盖物
        return BitmapDescriptorFactory.fromView(rl_item_back);
    }

    BaiduMap.OnMarkerClickListener mcl = new BaiduMap.OnMarkerClickListener() {

        @Override
        public boolean onMarkerClick(Marker marker) {
            Info info = (Info) marker.getExtraInfo().get("info");
            if (info != null) {
                if (info.getIsCluster()) {//聚合物
                    List<Info> grid_infos = new ArrayList<>();
                    Cluster<MyItem> cluster = mClusterManager.mRenderer2.getCluster(marker);
                    Collection<MyItem> items = cluster.getItems();
                    int onlineNum = 0;
                    for (MyItem item : items) {
                        grid_infos.add(item.getMarkerItem());
                    }
                    all_info.clear();
                    LogUtil.d(TAG, "keyP:" + infos.size());
                    for (int item : online_infos) {
                        LogUtil.d(TAG, "keyP:" + item);
                        if (all_infos.containsKey(item)) {
                            onlineNum++;
                            all_infos.get(item).setTimeout("1");
                        } else if (userId.equals(item + ""))
                            all_infos.get(Integer.parseInt(userId)).setTimeout("1");
                        else
                            all_infos.get(item).setTimeout("0");
                    }
                    Iterator<Map.Entry<Integer, Info>> iterator_all = all_infos.entrySet().iterator();
                    while (iterator_all.hasNext()) {
                        Map.Entry<Integer, Info> next = iterator_all.next();
                        all_info.add(next.getValue());
                    }
                    Intent intent = new Intent(MapLocation.this, MapMemberGuider.class);
                    intent.putParcelableArrayListExtra("online_infos", (ArrayList<? extends Parcelable>) grid_infos);
                    intent.putParcelableArrayListExtra("infoAll", (ArrayList<? extends Parcelable>) all_info);

                    intent.putExtra("mCurrentLantitude", mCurrentLantitude);
                    intent.putExtra("mCurrentLongitude", mCurrentLongitude);
                    intent.putExtra("onlineNum", onlineNum);
                    startActivity(intent);
                } else {//游客头像
                    View view = LayoutInflater.from(MapLocation.this).inflate(R.layout.map_popup_info, null);
                    TextView distance = (TextView) view.findViewById(R.id.tv_map_info_distance);
                    TextView nick = (TextView) view.findViewById(R.id.tv_map_info_nick);
                    Info info1 = (Info) marker.getExtraInfo().get("info");
                    //将marker所在的经纬度的信息转化成屏幕上的坐标
                    final LatLng ll = marker.getPosition();
                    LatLng mylatLng = new LatLng(mCurrentLantitude, mCurrentLongitude);
                    Point p = mBaiduMap.getProjection().toScreenLocation(ll);
                    Log.e(TAG, "--!" + p.x + " , " + p.y);
                    p.y -= 47;
                    LatLng llInfo = mBaiduMap.getProjection().fromScreenLocation(p);

                    nick.setText(info1.getNick());
//                    distance.setText(distanceUtil.getDistance(llInfo, mylatLng) + "m");
                    distance.setText(distanceUtil.getDistance(ll, mylatLng) + "m");

                    InfoWindow mInfoWindow = new InfoWindow(BitmapDescriptorFactory.fromView(view), llInfo, 0, new InfoWindow.OnInfoWindowClickListener() {
                        @Override
                        public void onInfoWindowClick() {
                            mBaiduMap.hideInfoWindow();
                        }
                    });
                    //显示InfoWindow
                    mBaiduMap.showInfoWindow(mInfoWindow);
                }
            }
            return true;
        }
    };

    @Override
    public void onMapClick(LatLng latLng) {
        mBaiduMap.hideInfoWindow();
    }

    @Override
    public boolean onMapPoiClick(MapPoi mapPoi) {
        return false;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.iv_map_icon_1://展开团员列表
                all_info.clear();
                LogUtil.d(TAG, "keyP:" + infos.size());
                int onlineNum = 0;
                for (int item : online_infos) {
                    LogUtil.d(TAG, "keyP:" + item);
                    if (all_infos.containsKey(item)) {
                        onlineNum++;
                        all_infos.get(item).setTimeout("1");
                    } else if (userId.equals(item + ""))
                        all_infos.get(Integer.parseInt(userId)).setTimeout("1");
                    else
                        all_infos.get(item).setTimeout("0");
                }
                Iterator<Map.Entry<Integer, Info>> iterator_all = all_infos.entrySet().iterator();
                while (iterator_all.hasNext()) {
                    Map.Entry<Integer, Info> next = iterator_all.next();
                    all_info.add(next.getValue());
                }
                LogUtil.d(TAG, "all_infos:" + all_infos.size());
                Intent intent = new Intent(MapLocation.this, MapMemberList.class);
                intent.putParcelableArrayListExtra("online_infos", (ArrayList<? extends Parcelable>) all_info);
                intent.putExtra("mCurrentLantitude", mCurrentLantitude);
                intent.putExtra("mCurrentLongitude", mCurrentLongitude);
                intent.putExtra("onlineNum", onlineNum);
                startActivity(intent);
                break;
            case R.id.iv_map_icon_2://定位
                if (mLocClient != null && mLocClient.isStarted()) {
                    LatLng ll = new LatLng(mCurrentLantitude, mCurrentLongitude);
                    MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
                    mBaiduMap.animateMapStatus(u);
                    MapStatus.Builder builder = new MapStatus.Builder();
                    builder.target(ll).zoom(19.0f);
                    mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
                    requesDialog = DialogUtil.createLoadingDialog(this, getResources().getString(R.string.loaddata));
                    requesDialog.show();
                    if (timeMillis == 0 || (System.currentTimeMillis() - timeMillis) > 5000) {
                        mHandler.post(requesRun);
                        timeMillis = System.currentTimeMillis();
                    } else {
                        mHandler.sendEmptyMessageDelayed(DIALOG_DISMISS2, 1000);
                    }
                }
                break;
            case R.id.iv_map_icon_3://退出
                mHandler.sendEmptyMessage(ANIMATION_IN);
                mHandler.sendEmptyMessageDelayed(FINISH, 220);
                break;
        }
    }

    /**
     * 指定的大小压缩图片
     *
     * @param bgimage
     * @param newWidth
     * @param newHeight
     * @return
     */
    public static Bitmap zoomImage(Bitmap bgimage, double newWidth,
                                   double newHeight) {
        // 获取这个图片的宽和高
        float width = bgimage.getWidth();
        float height = bgimage.getHeight();
        // 创建操作图片用的matrix对象
        Matrix matrix = new Matrix();
        // 计算宽高缩放率
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 缩放图片动作
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap bitmap = null;
        try {
            bitmap = Bitmap.createBitmap(bgimage, 0, 0, (int) width,
                    (int) height, matrix, true);
        } catch (Exception e) {

        }

        return bitmap;
    }

    @Override
    public void onMapLoaded() {
        MapStatus ms = new MapStatus.Builder().zoom(19.0f).build();
        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(ms));
    }

    /**
     * 每个Marker点，包含Marker点坐标以及图标
     */
    public class MyItem implements ClusterItem {
        private LatLng mPosition;
        private BitmapDescriptor bd;
        private Info info;
        private int userid;

        public MyItem(int userid, Info info) {
            this.userid = userid;
            this.mPosition = mPosition;
            this.info = info;
        }

        @Override
        public LatLng getPosition() {
            return info.getLatLng();
        }

        @Override
        public BitmapDescriptor getBitmapDescriptor() {
            LogUtil.d(TAG, "getBitmapDescriptor-----:" + bd);
            return bds.get(userid);
        }

        @Override
        public BitmapDescriptor getDefaultBitmapDescriptor() {
            LogUtil.d(TAG, "getDefaultBitmapDescriptor:");
            return default_bitmapDescriptor;
        }

        @Override
        public Info getMarkerItem() {
            return info;
        }
    }

    /**
     * 定位SDK监听函数
     */
    public class MyLocationListenner implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            // map view 销毁后不在处理新接收的位置
            if (location == null || mMapView == null) {
                return;
            }
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(60)//将定位精度设置成收听语音范围
                            // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(0).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);

            mCurrentLantitude = location.getLatitude();
            mCurrentLongitude = location.getLongitude();
            mCurrentAccracy = location.getRadius();
//            if (serviceHandler != null) {
//                Message message = serviceHandler.obtainMessage();
//                message.obj = mCurrentLongitude + "," + mCurrentLongitude;
//                message.what = 1;
//                serviceHandler.sendMessage(message);
//            }
            if (isFirstLoc) {
                mHandler.post(GetRun);
                isFirstLoc = false;
                LatLng ll = new LatLng(location.getLatitude(),
                        location.getLongitude());
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(ll).zoom(19.0f);
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
            }
        }
    }

    /**
     * 初始化方向传感器
     */
    private void initOritationListener() {
        myOrientationListener = new MyOrientationListener(
                getApplicationContext());
        myOrientationListener
                .setOnOrientationListener(new MyOrientationListener.OnOrientationListener() {
                    @Override
                    public void onOrientationChanged(float x) {
                        mXDirection = (int) x;

                        // 构造定位数据
                        MyLocationData locData = new MyLocationData.Builder()
                                .accuracy(10)
                                        // 此处设置开发者获取到的方向信息，顺时针0-360
                                .direction(mXDirection)
                                .latitude(mCurrentLantitude)
                                .longitude(mCurrentLongitude).build();
                        // 设置定位数据
                        mBaiduMap.setMyLocationData(locData);
                    }
                });
        myOrientationListener.start();
    }

    /**
     * 布局文件转bitmap
     *
     * @param view
     * @return
     */
    public static Bitmap getBitmapFromView(View view) {
        view.destroyDrawingCache();
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.setDrawingCacheEnabled(true);
        Bitmap bitmap = view.getDrawingCache(true);
        return bitmap;
    }

    @Override
    protected void onDestroy() {
        LogUtil.d(TAG, "onDestroy");

        mHandler.removeCallbacks(GetRun);
        mHandler.removeMessages(SHOW);
        // 退出时销毁定位
        if (mLocClient != null && mLocClient.isStarted())
            mLocClient.stop();

        mLocClient = null;
        // 关闭定位图层
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
        // 关闭方向传感器
        myOrientationListener.stop();
//        unbindService(connection);//解绑service
        super.onDestroy();
        ActivityManageFinish.removeActivity(this);
    }

    @Override
    protected void onResume() {
        LogUtil.d(TAG, "onResume");

        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();

        MobclickAgent.onPageStart("百度地图"); //统计页面(仅有Activity的应用中SDK自动调用，不需要单独写。"SplashScreen"为页面名称，可自定义)
        MobclickAgent.onResume(this);          //统计时长
    }

    @Override
    protected void onStop() {
        LogUtil.d(TAG, "onStop");
        if (mLocClient != null && mLocClient.isStarted())
            mLocClient.stop();
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!LogIsShow) {
            showHintDialog();
            LogIsShow = true;
        }
        if (mLocClient != null && !mLocClient.isStarted())
            mLocClient.start();
        LogUtil.d(TAG, "onStart");
    }

    @Override
    protected void onPause() {
        super.onPause();
        LogUtil.d(TAG, "onPause");
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
        MobclickAgent.onPageEnd("百度地图"); // （仅有Activity的应用中SDK自动调用，不需要单独写）保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息。"SplashScreen"为页面名称，可自定义
        MobclickAgent.onPause(this);
    }

    private void showHintDialog() {
        View view = this.getLayoutInflater().inflate(R.layout.map_hint_dialog, null);
        final Dialog dialog = new Dialog(this, R.style.CustomDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(view, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT));
        Button confirm = (Button) view.findViewById(R.id.bt_map_hint_dialog_confirm);
        ImageView close = (ImageView) view.findViewById(R.id.iv_map_hint_dialog_close);
        TextView textView = (TextView) view.findViewById(R.id.tv_map_hint_dialog_message);
        textView.setPadding(0, 10, 0, getResources().getDimensionPixelSize(R.dimen.dialog_message_padding_button));
        textView.setText(R.string.map_hint_dialog_message);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                dialog.dismiss();
                mHandler.post(requesRun);
                requesDialog = DialogUtil.createLoadingDialog(MapLocation.this, getResources().getString(R.string.loaddata));
                requesDialog.show();
                timeMillis = System.currentTimeMillis();
            }
        });
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        Window window = dialog.getWindow();
        // 设置显示动画
        window.setWindowAnimations(R.style.main_menu_animstyle);
        WindowManager.LayoutParams wl = window.getAttributes();
        wl.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        wl.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        dialog.show();
    }

    Runnable requesRun = new Runnable() {
        @Override
        public void run() {
            String url = Constats.HTTP_URL + Constats.MAP_REQUES_LOCATION_URL;
            String userId = sp.getString(UserInfo.USER_ID, "");
            String tuanId = TeamDao.getInstance(MapLocation.this).selectServiceLastTeamid(userId);
            if (TextUtils.isEmpty(tuanId))
                tuanId = sp.getString(UserInfo.LAST_SERVICE_TEAM_ID, "");
            String sign = MD5Util.getMD5(Constats.S_KEY + userId + tuanId);
            HashMap<String, String> Params = new HashMap<>();
            Params.put("userid", userId);
            Params.put("tuanid", tuanId);
            Params.put("sign", sign);
            LogUtil.d(TAG, Params.toString());
            JsonRequest result = new VolleyJsonUtil().createJsonObjectRequest(Request.Method.POST, url, Params, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject jsonObject) {
                    requesDialog.dismiss();
                    LogUtil.d(TAG, jsonObject.toString());
                    try {
                        String result = jsonObject.getString("result");
                        LogUtil.d(TAG, "result:" + result);

                        if (!TextUtils.isEmpty(result) && result.equals("0")) {
                            mHandler.sendEmptyMessage(DIALOG_DISMISS);
                        } else {
                            requesDialog.dismiss();
                            ToastUtil.showToast(MapLocation.this, MapLocation.this.getResources().getString(R.string.loaddata_un), 3000);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    ToastUtil.showToast(MapLocation.this, MapLocation.this.getResources().getString(R.string.network_latency), 3000);
                    requesDialog.dismiss();
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
                                    message.obj = getResources().getString(R.string.map_hint_1);
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
                                    Location location = gson.fromJson(jsonObject.toString(), Location.class);
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
                            ToastUtil.showToast(MapLocation.this, getResources().getString(R.string.to_server_failed), 3000);
                        }
                    });
            if (mRequestQueue != null)
                mRequestQueue.add(jsonRequest);
        }
    };

    private class Location {
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

}
