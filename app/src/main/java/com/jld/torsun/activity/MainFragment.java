package com.jld.torsun.activity;

import android.app.Activity;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonRequest;
import com.google.gson.Gson;
import com.jld.torsun.ActivityCollector;
import com.jld.torsun.ActivityManageFinish;
import com.jld.torsun.MyApplication;
import com.jld.torsun.R;
import com.jld.torsun.SlidingMenu;
import com.jld.torsun.activity.GoogleMap.GoogleLocation;
import com.jld.torsun.activity.baiduMap.MapLocation;
import com.jld.torsun.activity.baiduMap.SendLocationService;
import com.jld.torsun.activity.fragment.FragmentMainVoice;
import com.jld.torsun.activity.fragment.FragmentSet;
import com.jld.torsun.activity.fragment.MenuCallback;
import com.jld.torsun.activity.loginAndRegies.LoginActivity;
import com.jld.torsun.activity.loginAndRegies.RegiesUser;
import com.jld.torsun.activity.loginAndRegies.RepeatLoginActivity;
import com.jld.torsun.activity.loginAndRegies.StartActivity;
import com.jld.torsun.activity.mediaPlayer.MediaPlayerFragment;
import com.jld.torsun.activity.messageCenter.MessageCenterFragment;
import com.jld.torsun.activity.tours.FragmentTrouManger;
import com.jld.torsun.barcode.MipcaActivityCapture;
import com.jld.torsun.db.TeamDao;
import com.jld.torsun.http.VolleyJsonUtil;
import com.jld.torsun.modle.ActionConstats;
import com.jld.torsun.modle.MyDialog;
import com.jld.torsun.modle.TrouTeam;
import com.jld.torsun.modle.User;
import com.jld.torsun.service.AudioPlayService;
import com.jld.torsun.service.BatteryInfoReceiver;
import com.jld.torsun.service.JPushReceiver;
import com.jld.torsun.util.AndroidUtil;
import com.jld.torsun.util.Constats;
import com.jld.torsun.util.DialogUtil;
import com.jld.torsun.util.ExampleUtil;
import com.jld.torsun.util.LanguageUtil;
import com.jld.torsun.util.LogUtil;
import com.jld.torsun.util.MD5Util;
import com.jld.torsun.util.MyHttpUtil;
import com.jld.torsun.util.MyHttpUtil.VolleyInterface;
import com.jld.torsun.util.ToastUtil;
import com.jld.torsun.util.UserInfo;
import com.jld.torsun.util.imagecache.MyImageLoader;
import com.jld.torsun.view.RoundImageViewByXfermode;
import com.jld.torsun.view.RoundProgressBar;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;

/**
 * 主activity
 */
public class MainFragment extends FragmentActivity implements MenuCallback, OnClickListener, SlidingMenu.MenuOpenClose {

    private SlidingMenu mMenu;
    private RelativeLayout rl_fragment_container;
    private RelativeLayout layout_menu;
    private static SharedPreferences sp;

    private static ImageLoader imageLoader;

    private LinearLayout ll_menu_voice_chat;
    private static LinearLayout ll_menu_trou_team_manager;
    private LinearLayout ll_menu_team_orientation;
    private LinearLayout ll_menu_set;
    private LinearLayout ll_menu_certification_scan_code;
    private LinearLayout ll_menu_vod;
    private LinearLayout ll_menu_message_center;
    private View isHavaMSG;
    private static TextView tv;
    public static Boolean isSetAlias = false;
    public static int fragmentNum = 1;//判断切到哪个fragment
    private TeamDao teamDao;
    private RequestQueue mRequestQueue;
    // private MemberDao mDao;
    /**
     * 当前用户的ID
     */
    public static String MYSELFID;
    public static String PHONE_NUM;
    private IntentFilter intentFilter;
    //    private IntentFilter intentFilter2;
//    private IntentFilter intentFilter3;
    private MyBroadcast myBroadcast;
    private String UniqueId;
    private static RoundImageViewByXfermode head_icon;

    private String tag = "";
    private static final String VOICETAG = "voiceTag";
    private static final String SETTAG = "setTag";
    private static final String DOWNTAG = "downTag";
    private static final String MANAGETAG = "manageTag";
    private static final String MESSAGETAG = "messageTag";
    private static final String MEDIAPLAYTAG = "mediaplayTag";
    public static final int GET_IP = 0;
    public static final int TROU_POWER_CANCEL = 1;
    private static final String TAG = "MainFragment";
    private static final int SHOW_MSG_VIEW = 0x2221;
    private static final int HIDE_MSG_VIEW = 0x2222;
    private static final int INIT_GPS = 0x2223;
    public Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case GET_IP:
                    Bundle data = msg.getData();
                    String ip = data.getString("IP");
                    params.put("ip", ip);
                    String url = Constats.HTTP_URL + Constats.LOGIN_FUN;
                    login(url, params);
                    break;
                case TROU_POWER_CANCEL:
                    MyApplication.Toru_Power_cancel = false;
                    ll_menu_team_orientation.setVisibility(View.GONE);
                    ll_menu_trou_team_manager.setVisibility(View.GONE);
                    ActivityManageFinish.finishAll();
                    show_trou_Dialog();

//                    if (isShow) {
//                        LogUtil.d(TAG, "启动activity1");
////
//
//                    } else {
//                        LogUtil.d(TAG, "启动activity2");
//                        Intent intent = new Intent(MainFragment.this, MainFragment.class);
//                        startActivity(intent);
//                    }

                    break;
                case SHOW_MSG_VIEW:
                    isHavaMSG.setVisibility(View.VISIBLE);
                    break;
                case HIDE_MSG_VIEW:
                    isHavaMSG.setVisibility(View.GONE);
                    break;
                case INIT_GPS:
                    initGPS();
                    break;
                default:
                    break;

            }
        }
    };
    private Map<String, String> params;

    private BatteryInfoReceiver batteryInfoReceiver;

    private RoundProgressBar roundProgressBar;

    private static LinearLayout batteryShowLL;
    public static int density;
    private Boolean isShow = false;
    private AudioPlayService.MyBinder binder;

    private boolean isMovieWifi;
    private FragmentManager fm;
    private FragmentTransaction ft;

    /**
     * 到有权限剥夺dialog
     */
    private void show_trou_Dialog() {
        // 退出登录的对话框

        // 获取布局
        View view = this.getLayoutInflater().inflate(
                R.layout.dialog_update_prompt, null);

        // 设置dialog样式
        final Dialog dialog = new Dialog(this, R.style.CustomDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 设置布局
        dialog.setContentView(view, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT));

        // 获取子控件
        Button confirm = (Button) view
                .findViewById(R.id.bt_update_dialog_confirm);
        ImageView close = (ImageView) view
                .findViewById(R.id.iv_updete_dialog_close);
        TextView update_dialog_message = (TextView) view
                .findViewById(R.id.tv_update_dialog_message);
        TextView tv_single_dialog_title = (TextView) view
                .findViewById(R.id.tv_single_dialog_title);


        update_dialog_message.setPadding(0, 0, 0, 10 * density);
        update_dialog_message.setText(sp.getString(UserInfo.GUIDER_NAME,
                getResources().getString(R.string.team_jurisdiction_cancel_prompt2)) + getResources().getString(
                R.string.team_jurisdiction_cancel_prompt));

        String guiderName = sp.getString(UserInfo.GUIDER_NAME,
                "");
        if (TextUtils.isEmpty(guiderName)) {
            guiderName = sp.getString(UserInfo.GUIDER_NICK, "");
        }

        String message = getResources().getString(
                R.string.team_jurisdiction_cancel_prompt0) + "\"" + guiderName + "\"" + getResources().getString(
                R.string.team_jurisdiction_cancel_prompt);
        LogUtil.d("message", message);
        int i = message.lastIndexOf("\"") + 1;

        SpannableStringBuilder builder = new SpannableStringBuilder(message);

        ForegroundColorSpan red1 = new ForegroundColorSpan(getResources().getColor(R.color.backgroud_red));
        ForegroundColorSpan red2 = new ForegroundColorSpan(getResources().getColor(R.color.backgroud_red));
        ForegroundColorSpan red3 = new ForegroundColorSpan(getResources().getColor(R.color.backgroud_red));

        builder.setSpan(red1, 2, i, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.setSpan(red2, i + 2, i + 6, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.setSpan(red3, i + 9, i + 13, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        update_dialog_message.setText(builder);

        confirm.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                dialog.dismiss();
            }
        });
        close.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.setCanceledOnTouchOutside(false);
        Window window = dialog.getWindow();
        // 设置显示动画
        window.setWindowAnimations(R.style.main_menu_animstyle);
        WindowManager.LayoutParams wl = window.getAttributes();
        wl.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        wl.height = ViewGroup.LayoutParams.WRAP_CONTENT;
//        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);//指定会全局,可以在后台弹出
        dialog.show();

    }

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);

        LogUtil.d(TAG, "onCreate");
        //透明状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        ActivityCollector.finishAll();// 清空注册页面
        ActivityCollector.addActivity(this);
        sp = getSharedPreferences(Constats.SHARE_KEY, Context.MODE_PRIVATE);


        setContentView(R.layout.activity_main_fragment_layout);
        isMovieWifi = getIntent().getBooleanExtra("isMovieWifi", false);
        teamDao = TeamDao.getInstance(this);
        mRequestQueue = ((MyApplication) getApplication()).getRequestQueue();
        //用来接收昵称，图像以及导游权限等改变时的广播
        intentFilter = new IntentFilter();
        myBroadcast = MyBroadcast.getInstance(handler);
        intentFilter.addAction(ActionConstats.IMGCHANGE);
        intentFilter.addAction(ActionConstats.NICK_CHANGE);
        intentFilter.addAction(ActionConstats.GUIDE_INFO_CHANGE);
        registerReceiver(myBroadcast, intentFilter);

        imageLoader = MyImageLoader.getInstance(this);
        mMenu = (SlidingMenu) findViewById(R.id.id_menu);
        mMenu.setInterface(this);

        layout_menu = (RelativeLayout) findViewById(R.id.layout_menu);
        rl_fragment_container = (RelativeLayout) findViewById(R.id.rl_fragment_container);

        batteryShowLL = (LinearLayout) mMenu.findViewById(R.id.menu_battery_info_show_ll);
        roundProgressBar = (RoundProgressBar) mMenu.findViewById(R.id.menu_round_progress_bar);
        roundProgressBar.setMax(100);

        NotificationClickReceiver notif = new NotificationClickReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("downloadNotification");
        registerReceiver(notif, filter);
        //roundProgressBar.setProgress(batteryinfo);

        //测试注册电池信息广播接收
        batteryInfoReceiver = new BatteryInfoReceiver(roundProgressBar, batteryShowLL);
        IntentFilter infoFilter = new IntentFilter(ActionConstats.BATTERY_INFO_CHANGE);
        LogUtil.d("----注册电池信息广播-----");
        registerReceiver(batteryInfoReceiver, infoFilter);
        /**
         * 开启接收电池信息
         * */
//        LogUtil.d("----------开启接收电池信息------");
//        Intent batteryIntent = new Intent(this, BatteryInfoService.class);
//        startService(batteryIntent);
        initMenu();


        tv = (TextView) mMenu.findViewById(R.id.tv_menu_name);
        tv.setText(sp.getString(UserInfo.NIK, ""));
        head_icon = (RoundImageViewByXfermode) mMenu
                .findViewById(R.id.rivf_menu_head_icon);
        head_icon.setDefaultImageResId(R.mipmap.default_hear_ico);
        String requestUrl = sp.getString(UserInfo.HEAD_ICON_URL, "");
        boolean is_login = sp.getBoolean(UserInfo.LOGINING, false);
        if (is_login && !TextUtils.isEmpty(requestUrl)) {
            head_icon.setErrorImageResId(R.mipmap.default_hear_ico);
            head_icon.setImageUrl(requestUrl, imageLoader);
        }


        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager
                .beginTransaction();
        if (isMovieWifi) {
            switchContent(R.id.ll_menu_vod);
        } else {
            switchContent(R.id.ll_menu_voice_chat);
        }
        fragmentTransaction.commit();

        MyHttpUtil.isWifiConnected(this);// 判断有没有WiFi连接
//        MulticastClient.sendMainHandler(handler);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        density = (int) displayMetrics.density;
        isClose();
        //半个小时后启动一次算重新启动
        MobclickAgent.setSessionContinueMillis(6000 * 10 * 30);
    }

    public void setBind(IBinder iBinder) {
        binder = (AudioPlayService.MyBinder) iBinder;
    }

    public AudioPlayService.MyBinder getBinder() {
        return binder;
    }

    private void initMenu() {
        isHavaMSG = layout_menu.findViewById(R.id.menu_is_msg);
        // 语音 视频点播 团队定位 消息中心 扫码认证 团队管理 设置
        ll_menu_voice_chat = (LinearLayout) layout_menu.findViewById(R.id.ll_menu_voice_chat);
        ll_menu_vod = (LinearLayout) layout_menu.findViewById(R.id.ll_menu_vod);
        ll_menu_team_orientation = (LinearLayout) layout_menu.findViewById(R.id.ll_menu_team_orientation);
        ll_menu_message_center = (LinearLayout) layout_menu.findViewById(R.id.ll_menu_message_center);
        ll_menu_certification_scan_code = (LinearLayout) layout_menu.findViewById(R.id.ll_menu_certification_scan_code);
        ll_menu_trou_team_manager = (LinearLayout) layout_menu.findViewById(R.id.ll_menu_trou_team_manager);
        ll_menu_set = (LinearLayout) layout_menu.findViewById(R.id.ll_menu_set);

        ll_menu_voice_chat.setOnClickListener(this);
        ll_menu_vod.setOnClickListener(this);
        ll_menu_team_orientation.setOnClickListener(this);
        ll_menu_message_center.setOnClickListener(this);
        ll_menu_certification_scan_code.setOnClickListener(this);
        ll_menu_trou_team_manager.setOnClickListener(this);
        ll_menu_set.setOnClickListener(this);

        Button no_sim = (Button) layout_menu.findViewById(R.id.bt_no_sim);
        no_sim.setOnClickListener(this);

        if (sp.getBoolean(UserInfo.ISLOAD, false)) {
            ll_menu_team_orientation.setVisibility(View.VISIBLE);
            ll_menu_trou_team_manager.setVisibility(View.VISIBLE);
        } else {
            ll_menu_team_orientation.setVisibility(View.GONE);
            ll_menu_trou_team_manager.setVisibility(View.INVISIBLE);
        }
//        if (!sp.getBoolean(UserInfo.SIM_START, true)) {// 如果没有SIM卡就不显示设置和扫码认证
//            ll_menu_certification_scan_code.setVisibility(View.GONE);
//            ll_menu_set.setVisibility(View.GONE);
//            no_sim.setVisibility(View.VISIBLE);
//            User user = new User();
//            user.setUserid(UniqueId);
//            String mUser = user.toJsonString();
//            sp.edit().putString(UserInfo.JSONSTR, mUser).apply();
//        }
    }

    private Runnable getMSG = new Runnable() {
        @Override
        public void run() {
            final String userId = sp.getString(UserInfo.USER_ID, "");
            if (TextUtils.isEmpty(userId)) {
                return;
            }
            LogUtil.d(TAG, "getMSG");
            MyApplication.isgetMSGCount = true;
            final String requestUrl = Constats.HTTP_URL + Constats.GET_READ_MSG_COUNT;
            final String sign = MD5Util.getMD5(Constats.S_KEY + userId);
            final Map<String, String> params = new HashMap<String, String>();
            params.put("userid", userId);
            params.put("sign", sign);
            MyHttpUtil.VolleyPost(requestUrl, MainFragment.this, params, new VolleyInterface() {
                @Override
                public void win(JSONObject response) {
                    LogUtil.d(TAG, "getMSG--win:" + response.toString());
                    try {
                        int result = response.getInt("result");
                        LogUtil.d(TAG, "getMSG--result:" + result);
                        if (1 == result) {//有未读
                            handler.sendEmptyMessage(SHOW_MSG_VIEW);
                        } else {
                            handler.sendEmptyMessage(HIDE_MSG_VIEW);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void error(VolleyError error) {
                    LogUtil.d(TAG, "getMSG--error");
                    handler.sendEmptyMessage(HIDE_MSG_VIEW);
                }
            });
        }
    };

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.ll_menu_team_orientation://团队定位
                toTeamOrientation();
                break;
            case R.id.ll_menu_certification_scan_code://权限扫描:
                toScanCode();
                break;
            case R.id.bt_no_sim://无sim卡退出登录
                show_Logout_Dialog2();
                break;
            default:
                switchContent(id);
                break;
        }
    }

    private String fragment1Tag = "fragment1Tag";
    private String fragment2Tag = "fragment2Tag";
    private String fragment3Tag = "fragment3Tag";
    private String fragment4Tag = "fragment4Tag";
    private String fragment5Tag = "fragment5Tag";
    static Fragment fragment1;
    Fragment fragment2;
    Fragment fragment3;
    Fragment fragment4;
    Fragment fragment5;

    public void switchContent(int id) {
        FragmentManager fm = getSupportFragmentManager();
        ft = fm.beginTransaction();
        fragment1 = fm.findFragmentByTag(fragment1Tag);
        fragment2 = fm.findFragmentByTag(fragment2Tag);
        fragment3 = fm.findFragmentByTag(fragment3Tag);
        fragment4 = fm.findFragmentByTag(fragment4Tag);
        fragment5 = fm.findFragmentByTag(fragment5Tag);

        LogUtil.d(TAG, "" + id + "---" + tag);
        switch (id) {
            case R.id.ll_menu_voice_chat://语音
                if (VOICETAG.equals(tag)) {
                    mMenu.toggle();
//                    ft.show(fragment1);
                } else if (fragment1 == null) {
                    fragment1 = new FragmentMainVoice();
                    ft.add(R.id.rl_fragment_container, fragment1, fragment1Tag);
                    tag = VOICETAG;
                    fragmentNum = 1;
                    if (fragment2 != null)
                        mMenu.toggle();
                } else {
                    ft.show(fragment1);
                    tag = VOICETAG;
                    fragmentNum = 1;
                    mMenu.toggle();
                }
                break;
            case R.id.ll_menu_vod://视频播放
                if (MEDIAPLAYTAG.equals(tag)) {
                    mMenu.toggle();
//                    ft.show(fragment2);
                } else if (fragment2 == null) {
                    fragment2 = new MediaPlayerFragment();
                    ft.add(R.id.rl_fragment_container, fragment2, fragment2Tag);
                    tag = MEDIAPLAYTAG;
                    fragmentNum = 2;
                    if (fragment1 != null)
                        mMenu.toggle();
                } else {
                    ft.show(fragment2);
                    tag = MEDIAPLAYTAG;
                    fragmentNum = 2;
                    mMenu.toggle();
                }
                break;
            case R.id.ll_menu_message_center://消息中心
                if (!inspectLoginOrRegies()) {
                    return;
                }
                if (MESSAGETAG.equals(tag)) {
                    mMenu.toggle();
//                    ft.show(fragment3);
                } else if (fragment3 == null) {
                    if (isHavaMSG.isShown()) {
                        isHavaMSG.setVisibility(View.GONE);
                    }
                    fragment3 = new MessageCenterFragment();
                    ft.add(R.id.rl_fragment_container, fragment3,
                            fragment3Tag);
                    tag = MESSAGETAG;
                    fragmentNum = 3;
                    mMenu.toggle();
                } else {
                    ft.show(fragment3);
                    tag = MESSAGETAG;
                    fragmentNum = 3;
                    mMenu.toggle();
                }
                break;
            case R.id.ll_menu_trou_team_manager://团队管理
                if (!inspectLoginOrRegies()) {
                    return;
                }
                if (MANAGETAG.equals(tag)) {
                    mMenu.toggle();
//                    ft.show(fragment4);
                } else if (fragment4 == null) {
                    fragment4 = new FragmentTrouManger();
                    ft.add(R.id.rl_fragment_container, fragment4, fragment4Tag);
                    tag = MANAGETAG;
                    fragmentNum = 4;
                    mMenu.toggle();
                } else {
                    ft.show(fragment4);
                    tag = MANAGETAG;
                    fragmentNum = 4;
                    mMenu.toggle();
                }
                break;
            case R.id.ll_menu_set://个人设置
                if (!inspectLoginOrRegies()) {
                    return;
                }
                if (SETTAG.equals(tag)) {
                    mMenu.toggle();
                    LogUtil.d(TAG, "show fragment5");
//                    ft.show(fragment5);
                } else if (fragment5 == null) {
                    fragment5 = new FragmentSet();
                    ft.add(R.id.rl_fragment_container, fragment5, fragment5Tag);
                    tag = SETTAG;
                    fragmentNum = 5;
                    LogUtil.d(TAG, "new fragment5");

                    mMenu.toggle();
                } else {
                    ft.show(fragment5);
                    tag = SETTAG;
                    fragmentNum = 5;
                    LogUtil.d(TAG, "showfragment5");
                    mMenu.toggle();
                }
                break;
            default:
                break;
        }
        if (fragment1 != null && !VOICETAG.equals(tag)) {
            ft.hide(fragment1);
        }
        if (fragment2 != null && !MEDIAPLAYTAG.equals(tag)) {
            ft.hide(fragment2);
        }
        if (fragment3 != null && !MESSAGETAG.equals(tag)) {
            ft.hide(fragment3);
        }
        if (fragment4 != null && !MANAGETAG.equals(tag)) {
            ft.hide(fragment4);
            LogUtil.d(TAG, "hide fragment4");

        }
        if (fragment5 != null && !SETTAG.equals(tag)) {
            LogUtil.d(TAG, "hide fragment5");
            ft.hide(fragment5);
        }
        ft.commit();
    }

    private void toTeamOrientation() {
        if (!inspectLoginOrRegies()) {
            return;
        }
        LogUtil.d(TAG, "locale:" + getResources().getConfiguration().locale.getCountry());
        if (getResources().getConfiguration().locale.getCountry().equals("CN")) {
            Intent intent = new Intent(MainFragment.this, MapLocation.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(MainFragment.this, GoogleLocation.class);
            startActivity(intent);
        }
        overridePendingTransition(R.anim.right_in,
                R.anim.left_out);
    }

    private void toScanCode() {
        fragmentNum = 5;
        if (!inspectLoginOrRegies()) {
            return;
        }
        // 修改的扫码权限,点进来就有了
        //sp.edit().putBoolean(UserInfo.ISLOAD, true).commit();
        Intent intent_4 = new Intent();
        intent_4.setClass(MainFragment.this,
                MipcaActivityCapture.class);
        intent_4.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent_4);
        overridePendingTransition(R.anim.right_in,
                R.anim.left_out);
    }

    //是否需要更新本地数据库的标记
    private boolean isUpdataDB = true;
    private String useridBuff = "";

    @Override
    protected void onResume() {
        super.onResume();
        LogUtil.d(TAG, "onResume");

        if (!MyApplication.isgetMSGCount) {
            new Thread(getMSG).start();
        }
        if (!isSetAlias && !TextUtils.isEmpty(sp.getString(UserInfo.USER_ID, ""))) {
            isSetAlias = true;
            UniqueId = AndroidUtil.getUniqueId(this);// 手机唯一标识码
            // 设置别名
            LogUtil.d(TAG, "setAlias:" + UniqueId + "_" + sp.getString(UserInfo.USER_ID, "0"));
            setAlias(UniqueId + "_" + sp.getString(UserInfo.USER_ID, "0"));
        }


        isShow = true;
        if (JPushReceiver.dialogIsShow) {// 如果是极光推送跳转过来的便显示dialog
            JPushReceiver.dialogIsShow = false;
            show_Logout_Dialog();
        }
        if (MyApplication.Toru_Power_cancel) {//如果是导游权限被取消而跳转过来，显示dialog提示
            MyApplication.Toru_Power_cancel = false;
            show_trou_Dialog();
        }
        // 获取当前用户的ID
        MYSELFID = sp.getString(UserInfo.USER_ID, "");
        PHONE_NUM = sp.getString(UserInfo.LOGIN_ACCOUT, "");
        // 昵称更新
        tv.setText(sp.getString(UserInfo.NIK, ""));

        // 是否显示旅游团管理
        // sp.getBoolean(UserInfo.ISLOAD, false)
        if (sp.getBoolean(UserInfo.ISLOAD, false)) {
            if (MyHttpUtil.isConnTorsun(this)) {
                batteryShowLL.setVisibility(View.VISIBLE);
            } else {
                batteryShowLL.setVisibility(View.INVISIBLE);
            }
            ll_menu_team_orientation.setVisibility(View.VISIBLE);
            ll_menu_trou_team_manager.setVisibility(View.VISIBLE);
        } else {
            batteryShowLL.setVisibility(View.INVISIBLE);
            ll_menu_team_orientation.setVisibility(View.GONE);
            ll_menu_trou_team_manager.setVisibility(View.GONE);
        }
        if (!useridBuff.equals(MYSELFID) || isUpdataDB) {
            LogUtil.d(TAG, "useridBuff : " + useridBuff + " MYSELFID : " + MYSELFID + " isUpdataDB : " + isUpdataDB);
            if (!TextUtils.isEmpty(MYSELFID)) {
                useridBuff = MYSELFID;
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        upLoadDataToServer();
                    }
                }.start();
            }
        }
    }

    @Override
    protected void onPause() {
        LogUtil.d(TAG, "onPause");

        super.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();
        LogUtil.d(TAG, "onStart");
        MyApplication application = (MyApplication) getApplication();
        if (!application.isOpenGps) {
            Intent intent = null;
            if (LanguageUtil.isZh(this) && sp.getBoolean(UserInfo.LOGINING, false)) {//如果是中文打开百度地图，否则打开Google地图
                intent = new Intent(this, SendLocationService.class);
                startService(intent);
                application.isOpenGps = true;
                LogUtil.d(TAG, "INIT_GPS");
                handler.sendEmptyMessageDelayed(INIT_GPS, 700);
            } else if (!LanguageUtil.isZh(this) && sp.getBoolean(UserInfo.LOGINING, false)) {
                intent = new Intent(this, SendLocationService.class);
                startService(intent);
                application.isOpenGps = true;
                handler.sendEmptyMessageDelayed(INIT_GPS, 700);
            } else if (!sp.getBoolean(UserInfo.LOGINING, false)) {//如果没有登录，登录后再一次开启
                application.isOpenGps = false;
            }
        }
    }

    private void initGPS() {
        LogUtil.d(TAG, "initGPS");
        LocationManager locationManager = (LocationManager) this
                .getSystemService(Context.LOCATION_SERVICE);
        // 判断GPS模块是否开启，如果没有则开启
        if (locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return;
        }
        final MyDialog myDialog = new MyDialog(false, this);
        myDialog.setTitle(getResources().getString(R.string.t_frag_set_dia_title));
        myDialog.setContent(getResources().getString(R.string.get_gps_hint));
        myDialog.setConfirm(getResources().getString(R.string.to_set_up));
        myDialog.setCancel(getResources().getString(R.string.ignore));
        myDialog.showTwo(new MyDialog.TwoOnclick() {
            @Override
            public void cancel_method() {
                myDialog.dismiss();
            }

            @Override
            public void confirm_method() {
                Intent intent = new Intent(
                        Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(intent, 0); // 设置完成后返回到原来的界面
                myDialog.dismiss();
            }

            @Override
            public void close_method() {
                myDialog.dismiss();

            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        LogUtil.d(TAG, "onStop");
        isShow = false;
    }

    private boolean inspectLoginOrRegies() {
        boolean login = sp.getBoolean(UserInfo.LOGINING, false);
        String userid = sp.getString(UserInfo.USER_ID, "");
        if (TextUtils.isEmpty(userid)) {//没有注册，跳转到注册界面
            Intent intent = new Intent(MainFragment.this, RegiesUser.class);
            MainFragment.this.startActivity(intent);
            return false;
        } else if (!login) {//没有登陆，跳转到登陆界面
            Intent intent = new Intent(MainFragment.this, RepeatLoginActivity.class);
            MainFragment.this.startActivity(intent);
            return false;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        LogUtil.d(TAG, "onDestroy");
        if (null != myBroadcast) {
            unregisterReceiver(myBroadcast);
        }
        if (binder != null)
            binder.stopThread();
        // 退出登录
        // sp.edit().putBoolean(UserInfo.LOGINING, false).commit();

        if (null != batteryInfoReceiver) {
            unregisterReceiver(batteryInfoReceiver);
        }

        NotificationManager manger = (NotificationManager) this
                .getSystemService(NOTIFICATION_SERVICE);

        manger.cancel(0);
//        Intent intent_Play = new Intent(this, AudioPlayService.class);
//        stopService(intent_Play);
//        Intent intent = new Intent(this, MulcastService.class);
//        stopService(intent);
        LogUtil.d(TAG, "stopService");
        sp.edit().putString(UserInfo.LAST_SERVICE_TEAM_ID, "").apply();//清空服务器团队ID

        super.onDestroy();
    }

    @Override
    public void callback() {
        if (null != mMenu) {
            mMenu.toggle();
        }
    }

    /**
     * 打开菜单
     */
    @Override
    public void isOpen() {
        LogUtil.d(TAG, "mianisOpen");

        ll_menu_voice_chat.setClickable(true);
        ll_menu_vod.setClickable(true);
        ll_menu_team_orientation.setClickable(true);
        ll_menu_message_center.setClickable(true);
        ll_menu_certification_scan_code.setClickable(true);
        ll_menu_trou_team_manager.setClickable(true);
        ll_menu_set.setClickable(true);
    }

    /**
     * 关闭菜单
     */
    @Override
    public void isClose() {
        LogUtil.d(TAG, "mianisClose");

        ll_menu_voice_chat.setClickable(false);
        ll_menu_vod.setClickable(false);
        ll_menu_team_orientation.setClickable(false);
        ll_menu_message_center.setClickable(false);
        ll_menu_certification_scan_code.setClickable(false);
        ll_menu_trou_team_manager.setClickable(false);
        ll_menu_set.setClickable(false);
    }

    //用来接收昵称，图像以及导游权限等改变时的广播
    static class MyBroadcast extends BroadcastReceiver {

        private static MyBroadcast broadcast;
        private static Handler handler;

        public static MyBroadcast getInstance(Handler mHandler) {
            handler = mHandler;
            if (null == broadcast) {

                broadcast = new MyBroadcast();
            }
            return broadcast;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            LogUtil.d(TAG, "---------action----" + action);
            if (action.equals(ActionConstats.NICK_CHANGE)) {
                tv.setText(sp.getString(UserInfo.NIK, ""));
            }
            if (action.equals(ActionConstats.GUIDE_INFO_CHANGE)) {
                LogUtil.d(TAG, "----------onReceive------GUIDE_INFO_CHANGE-------");
                if (sp.getBoolean(UserInfo.ISLOAD, false)) {
                    if (MyHttpUtil.isConnTorsun((Activity) context)) {
                        batteryShowLL.setVisibility(View.VISIBLE);
                    } else {
                        batteryShowLL.setVisibility(View.INVISIBLE);
                    }
                    ll_menu_trou_team_manager.setVisibility(View.VISIBLE);
                } else {//导游权限被取消，隐藏电池电量和旅游团管理并弹出dialog提示
                    batteryShowLL.setVisibility(View.INVISIBLE);

                    MyApplication.Toru_Power_cancel = true;
                    handler.sendEmptyMessage(TROU_POWER_CANCEL);
//                    Intent mIntent = new Intent(context, MainFragment.class);
//                    context.startActivity(mIntent);

                }

            }
            if (action.equals(ActionConstats.IMGCHANGE)) {
                String requestUrl = sp.getString(UserInfo.HEAD_ICON_URL, "");
                if (!TextUtils.isEmpty(requestUrl)) {
                    head_icon.setErrorImageResId(R.mipmap.default_hear_ico);
                    head_icon.setImageUrl(requestUrl, imageLoader);
                }
                if (fragment1 != null && fragment1 instanceof FragmentMainVoice) {
                    FragmentMainVoice fragmentMainVoice = (FragmentMainVoice) fragment1;

                    if (fragmentMainVoice.imagev_main_guider_icon != null) {
                        fragmentMainVoice.imagev_main_guider_icon.setErrorImageResId(R.mipmap.default_hear_ico);
                        fragmentMainVoice.imagev_main_guider_icon.setImageUrl(requestUrl, imageLoader);
                    }
                }
            }
        }

    }

    /*********************************
     * 下线通知dialog
     ****************************/
    private void show_Logout_Dialog() {
        MyDialog myDialog = new MyDialog(false, this);
        myDialog.setContent(getResources().getString(R.string.mainFragment_logoff_message));
        myDialog.setTitle(getResources().getString(R.string.mainFragment_logoff_title));
        myDialog.setCancel(getResources().getString(R.string.mainFragment_logoff_exit));
        myDialog.setConfirm(getResources().getString(R.string.mainFragment_logoff_relogin));
        myDialog.setShowClose(false);
        myDialog.setIsTouchCancel(false);
        myDialog.setCancelable(false);
        myDialog.showTwo(new MyDialog.TwoOnclick() {
            @Override
            public void cancel_method() {
                Intent intent = new Intent();
                intent.setClass(MainFragment.this, RepeatLoginActivity.class);
                startActivity(intent);
                sp.edit().putString(UserInfo.NIK, "").commit();
                sp.edit().putString(UserInfo.USER_NAME, "").commit();
                // sp.edit().putString(UserInfo.HEAD_ICON_URL,"").commit();
                sp.edit().putString(UserInfo.JSONSTR, "").commit();
                sp.edit().putBoolean(UserInfo.LOGINING, false).commit();
                sp.edit().putBoolean(UserInfo.ISLOAD, false).commit();
                MainFragment.this.finish();
            }
            @Override
            public void confirm_method() {
                againGetParams();
            }
            @Override
            public void close_method() {
            }
        });
    }

    /**
     * 退出登录dialog
     */
    private void show_Logout_Dialog2() {
        MyDialog myDialog = new MyDialog(false, this);
        myDialog.setContent(getResources().getString(R.string.FragmentSet_logout_message));
        myDialog.setTitle(getResources().getString(R.string.FragmentSet_logout_title));
        myDialog.setCancel(getResources().getString(R.string.FragmentSet_logout_cancel));
        myDialog.setConfirm(getResources().getString(R.string.FragmentSet_logout_confirm));
        myDialog.showTwo(new MyDialog.TwoOnclick() {
            @Override
            public void cancel_method() {
            }
            @Override
            public void confirm_method() {
                MyApplication.isfristSavaLastTeamID = true;
                sp.edit().putString(UserInfo.LAST_TEAM_ID, "").apply();
                sp.edit().putString(UserInfo.LAST_SERVICE_TEAM_ID, "").apply();

                sp.edit().putString(UserInfo.NIK, "").apply();
                sp.edit().putString(UserInfo.USER_NAME, "").apply();
                // sp.edit().putString(UserInfo.HEAD_ICON_URL,"").commit();
                sp.edit().putString(UserInfo.JSONSTR, "").apply();
                sp.edit().putBoolean(UserInfo.LOGINING, false).apply();
                sp.edit().putBoolean(UserInfo.ISLOAD, false).apply();
                sp.edit().putString(UserInfo.SAVE_CREATE_MESSAGE, "").apply();
                sp.edit().putString(UserInfo.FAIL_USERID_MESSAGE, "").apply();
                sp.edit().putString(UserInfo.FAIL_SAVE_CREATE_MESSAGE, "").apply();
                sp.edit().putString(UserInfo.FAIL_TUAN_ID_MESSAGE, "").apply();
                LogUtil.d(TAG, "bt_set_logout");
                MainFragment.isSetAlias = false;
                AudioPlayService.MyBinder binder = MainFragment.this.getBinder();
                Intent intent = new Intent();
                intent.setClass(MainFragment.this, RepeatLoginActivity.class);
                startActivity(intent);
            }
            @Override
            public void close_method() {
            }
        });
    }

    /******************************
     * 极光推送相关代码
     ****************************************/
    private static final int MSG_SET_ALIAS = 1001;
    private static final int MSG_SET_TAGS = 1002;

    // 给极光推送设置别名
    private void setAlias(String alias) {
        if (TextUtils.isEmpty(alias)) {
            return;
        }
        if (!isValidTagAndAlias(alias)) {
            return;
        }
        LogUtil.d("setAliasAndTags", "alias:" + alias);

        mHandler.sendMessage(
                mHandler.obtainMessage(MSG_SET_ALIAS, alias)
        );
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_SET_ALIAS:
                    LogUtil.d("setAliasAndTags", "alias:" + msg.obj + "\n" + "mAliasCallback:" + mAliasCallback);

                    // Log.d(TAG, "Set alias in handler.");
                    JPushInterface.setAliasAndTags(getApplicationContext(),
                            (String) msg.obj, null, mAliasCallback);
                    break;
//
                case MSG_SET_TAGS:
                    // Log.d(TAG, "Set tags in handler.");
                    JPushInterface.setAliasAndTags(getApplicationContext(), null,
                            (Set<String>) msg.obj, mTagsCallback);
                    break;
            }
        }
    };
    private final TagAliasCallback mAliasCallback = new TagAliasCallback() {

        @Override
        public void gotResult(int code, String alias, Set<String> tags) {
            String logs;
            switch (code) {
                case 0://设置成功，保存到sp下次不再设置
                    logs = "Set tag and alias success";
                    LogUtil.i("MainFragment", logs);
                    sp.edit().putBoolean("jpushAlias", true).apply();
                    break;
                case 6002://设置失败，每隔60s重新设置一次
                    logs = "Failed to set alias and tags due to timeout. Try again after 60s.";
                    LogUtil.i("MainFragment", logs);
                    if (ExampleUtil.isConnected(getApplicationContext())) {
                        mHandler.sendMessageDelayed(
                                mHandler.obtainMessage(MSG_SET_ALIAS, alias),
                                1000 * 60);
                    } else {
                        LogUtil.i("MainFragment", "No network");
                    }
                    break;

                default:
                    logs = "Failed with errorCode = " + code;
                    LogUtil.e("MainFragment", logs);
            }

        }

    };
    private final TagAliasCallback mTagsCallback = new TagAliasCallback() {

        @Override
        public void gotResult(int code, String alias, Set<String> tags) {
            String logs;
            switch (code) {
                case 0:
                    logs = "Set tag and alias success";
                    LogUtil.i("MainFragment", logs);
                    break;

                case 6002:
                    logs = "Failed to set alias and tags due to timeout. Try again after 60s.";
                    // Log.i(TAG, logs);
                    if (ExampleUtil.isConnected(getApplicationContext())) {
                        mHandler.sendMessageDelayed(
                                mHandler.obtainMessage(MSG_SET_TAGS, tags),
                                1000 * 60);
                    } else {
                        LogUtil.i("MainFragment", "No network");
                    }
                    break;

                default:
                    logs = "Failed with errorCode = " + code;
                    LogUtil.e("MainFragment", logs);
            }

            // ExampleUtil.showToast(logs, getApplicationContext());
        }

    };
    private Dialog mDialog;

    // 校验Tag Alias 只能是数字,英文字母和中文
    public static boolean isValidTagAndAlias(String s) {
        Pattern p = Pattern.compile("^[\u4E00-\u9FA50-9a-zA-Z_-]{0,}$");
        Matcher m = p.matcher(s);
        return m.matches();
    }

    /****************************** 重新登录代码 ****************************************/
    /**
     * 重复登录
     */
    public void againGetParams() {

        mDialog = DialogUtil.createLoadingDialog(this, "正在登陆...");
        mDialog.show();
        String mobile = sp.getString(UserInfo.LOGIN_ACCOUT, "");
        String password = sp.getString(UserInfo.PASS_WORD, "");// 获取密码

        if (TextUtils.isEmpty(mobile) || TextUtils.isEmpty(password)) {// 电话或者密码为空则跳转登录界面
            Intent intent = new Intent();
            intent.setClass(MainFragment.this, LoginActivity.class);
            startActivity(intent);
            MainFragment.this.finish();

        }
        String sign = MD5Util.getMD5(Constats.S_KEY + mobile + password);
        LogUtil.d("againGetParams", "重复登录mobile：" + mobile + "	passwd："
                + password + "sign：" + sign);

        String mtype = Constats.ANDROID + "";
        String mno = AndroidUtil.getUniqueId(this);// 获取手机唯一标识

        String mversion = AndroidUtil.getHandSetInfo();// 手机型号
        String devbrand = AndroidUtil.getVendor();// 手机品牌

        params = new HashMap<String, String>();
        params.put("mobile", mobile);
        params.put("passwd", password);
        params.put("sign", sign);
        params.put("mtype", mtype);
        params.put("mno", mno);
        params.put("mversion", mversion);
        params.put("devbrand", devbrand);
        AndroidUtil.getLocalHostIp(handler);
    }

    public void login(String url, Map<String, String> params) {
        MyHttpUtil.VolleyPost(url, this, params, new VolleyInterface() {
            @Override
            public void win(JSONObject response) {
                // TODO Auto-generated method stub
                try {
                    int result = response.getInt("result");
                    String message = response.getString("msg");
                    // LogUtil.d(TAG, "result = " + result);
                    if (0 == result) {
                        ToastUtil.showToast(MainFragment.this,
                                R.string.t_login_suc, 3000);
                        JSONObject item = response.getJSONObject("item");
                        // Log.d(TAG, "item = " + item);
                        Gson gson = new Gson();
                        User user = gson.fromJson(item.toString(), User.class);
                        sp.edit().putString(UserInfo.JSONSTR, item.toString())
                                .apply();
                        sp.edit().putBoolean(UserInfo.LOGINING, true).apply();
                        sp.edit().putString(UserInfo.USER_ID, user.userid)
                                .apply();
                        sp.edit().putString(UserInfo.NIK, user.nick).apply();
                        sp.edit().putString(UserInfo.USER_NAME, user.username)
                                .apply();
                        sp.edit().putString(UserInfo.HEAD_ICON_URL, user.img)
                                .apply();
                        sp.edit().putString(UserInfo.LOGIN_ACCOUT, user.mobile)
                                .apply();

                        mDialog.dismiss();
                    } else {
                        // 重复登录不成功跳转到登录界面
                        mDialog.dismiss();

                        ToastUtil.showToast(MainFragment.this, message + "",
                                3000);
                        // Intent intent = new Intent();
                        // intent.setClass(MainFragment.this,
                        // LoginActivity.class);
                        // startActivity(intent);
                        // MainFragment.this.finish();

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void error(VolleyError error) {
                // TODO Auto-generated method stub
                mDialog.dismiss();
                ToastUtil.showToast(MainFragment.this,
                        R.string.t_frag_set_network_err, 3000);
                // Intent intent = new Intent();
                // intent.setClass(MainFragment.this, LoginActivity.class);
                // startActivity(intent);
                // MainFragment.this.finish();
            }
        });

    }

    long exitTime;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            // 判断间隔时间 大于2秒就退出应用
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                ToastUtil.showToast(this, getResources().getString(R.string.again_down_home), 3000);
                // 计算两次返回键按下的时间差
                exitTime = System.currentTimeMillis();
            } else {
                // 关闭应用程序
//                finish();
                // 返回桌面操作
//                 Intent home = new Intent(Intent.ACTION_MAIN);
//                 home.addCategory(Intent.CATEGORY_HOME);
//                 startActivity(home);
                moveTaskToBack(false);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    //添加旅游团列表URL
    private String addUrl = Constats.HTTP_URL + Constats.ADD_TEAM_FUN;
    //获取旅游团列表URL
    private String getUrl = Constats.HTTP_URL + Constats.GET_TEAM_LIST_FUN;

    //更新本地团队数据库数据
    private void upLoadDataToServer() {
        final List<TrouTeam> mNeedUpdataList = teamDao.getNeedUpdataTrouTeam(MYSELFID);
        if (null != mNeedUpdataList && mNeedUpdataList.size() > 0) {
            LogUtil.d(TAG, "-------需上传的团队个数:" + mNeedUpdataList.size());
            String sign = MD5Util
                    .getMD5(Constats.S_KEY + MYSELFID);
            for (TrouTeam team : mNeedUpdataList) {
                Map<String, String> params = new HashMap<String, String>();
                params.put("userid", MYSELFID);
                params.put("localid", team.createtime);
                params.put("name", team.name);
                params.put("time", team.createtime);
                params.put("sign", sign);
                synchronized (params) {
                    LogUtil.d(TAG, "-------localid---createtime-:" + team.createtime);
                    createTeam(params, team.localID);
                }
            }
        }
        LogUtil.d(TAG, "-------从服务器端更新数据到终端-------");
        getTrouListHttp();
    }

    // 提交数据到服务器
    @SuppressWarnings("unchecked")
    private void createTeam(Map<String, String> params, final String localID) {
        JsonRequest<JSONObject> jsonRequest = VolleyJsonUtil
                .createJsonObjectRequest(Request.Method.POST, addUrl, params,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    int result = response.getInt("result");
                                    if (0 == result) {
                                        if (response.has("item")) {
                                            JSONObject jsonObject = response
                                                    .getJSONObject("item");
                                            String teamid = jsonObject
                                                    .getString("id");
                                            teamDao.updateTeam(teamid, localID);
                                            sp.edit().putString(UserInfo.LAST_SERVICE_TEAM_ID, teamid).apply();//最新一个旅游团 ID
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {

                            }
                        });
        if (null != mRequestQueue) {
            mRequestQueue.add(jsonRequest);
        }

    }

    /**
     * 从服务器获取旅游团列表
     */
    @SuppressWarnings("unchecked")
    private void getTrouListHttp() {
        String sign = MD5Util.getMD5(Constats.S_KEY + MainFragment.MYSELFID);
        Map<String, String> params = new HashMap<String, String>();
        params.put("userid", MainFragment.MYSELFID);
        params.put("sign", sign);
        // getTrouListHttp(params);
        JsonRequest<JSONObject> jsonRequest = VolleyJsonUtil
                .createJsonObjectRequest(Request.Method.POST, getUrl, params,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                LogUtil.d(TAG, "getTrouListHttp返回值 :" + response.toString());
                                try {
                                    int result = response.getInt("result");
                                    if (0 == result) {
                                        Gson gson = new Gson();
                                        Respon respon = gson.fromJson(response.toString(), Respon.class);
                                        LogUtil.d(TAG, "--------getTrouListHttp返回值:" + respon.item.toString());
                                        updateTeaminfo(respon.item);
                                        isUpdataDB = false;
                                    } else if (1004 == result) {//没有记录
                                        isUpdataDB = false;
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                isUpdataDB = true;
                            }
                        });
        if (null != mRequestQueue) {
            mRequestQueue.add(jsonRequest);
        }
    }

    /**
     * 将服务器团队信息同步到本地
     */
    private void updateTeaminfo(List<TrouTeam> mList) {

        Collections.reverse(mList);
        int size = mList.size();
        for (int i = 0; i < size; i++) {
            teamDao.insertTeamToDir(MainFragment.MYSELFID, mList.get(i));
        }
//        Collections.reverse(mList);
    }

    class Respon {
        public int result;
        public String msg;
        public List<TrouTeam> item;
    }

    public class NotificationClickReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            LogUtil.d("StartActivity", "NotificationClickReceiver");
            if (StartActivity.download != null)
                StartActivity.download.cancel();
        }
    }
}
