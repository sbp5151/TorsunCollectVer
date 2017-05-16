package com.jld.torsun.activity.fragment;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.jld.torsun.MyApplication;
import com.jld.torsun.R;
import com.jld.torsun.activity.GuiderInfoActivity2;
import com.jld.torsun.activity.MainFragment;
import com.jld.torsun.activity.tours.MulticastClient;
import com.jld.torsun.modle.ActionConstats;
import com.jld.torsun.service.AudioPlayService;
import com.jld.torsun.service.AudioPlayService.MyBinder;
import com.jld.torsun.service.MulcastService;
import com.jld.torsun.service.netChangeReceiver;
import com.jld.torsun.util.Constats;
import com.jld.torsun.util.ImageUtil;
import com.jld.torsun.util.LogUtil;
import com.jld.torsun.util.MyHttpUtil;
import com.jld.torsun.util.ToastUtil;
import com.jld.torsun.util.UserInfo;
import com.jld.torsun.util.WifiUtil;
import com.jld.torsun.util.imagecache.MyImageLoader;
import com.jld.torsun.view.RoundImageViewByXfermode;
import com.umeng.analytics.MobclickAgent;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * 播放语音界面
 * <p>
 * 晶凌达科技有限公司所有， 受到法律的保护，任何公司或个人，未经授权不得擅自拷贝。
 *
 * @creator 单柏平 <br/>
 * @create-time 2015-12-1 下午2:16:59
 */
public class FragmentMainVoice extends Fragment implements OnClickListener {

    private static final String TAG = "FragmentMainVoice";
    private ImageLoader imageLoader;
    private RelativeLayout top_back_img_rl;
    public RoundImageViewByXfermode imagev_main_guider_icon;
    private RoundImageViewByXfermode image_main_head_icon;
    private SharedPreferences sp;

    //private CircleWaveView imagev_wava_voice;
    private ImageView imagev_wava_voice;
    // private RelativeLayout imagev_wava_voice;
    private ImageView imagev_wava_voice_1;
    private TextView tv_wava_nooff;
    private TextView tv_main_wave_lost_sum;

//    private AnimationDrawable animationDrawable;

    private MenuCallback mCallback;

    private MulcastService mService;

    private String image;
    private boolean first = true;// 第一次
    private boolean isDestory = false;// 第一次
    private TextView tv_device_name;
    public static MyBinder binder;
    public Boolean threadStop = false;
    public Boolean isPlay = false;
    public Boolean isPlayStop = true;
    public Boolean isStop = false;
    private NotificationManager nm;

    //private String wifiName = "\"tucson\"";
    private SharedPreferences.Editor editor;
    private TextView tv_main_wave_lost;
    private int lostNum = 0;
    private int lostNumSum = 0;
    public static final int GET_HEAD_ICON = 1;
    public static final int GET_LOST_NUMB = 2;
    public static final int LOST_NUMB_CLEAR = 3;
    public static final int STOP_VOICE = 4;
    public static final int WAVE_ISRUN = 5;
    public final int STRAT_VOICE0 = 6;
    public static final int STRAT_VOICE = 7;
    public static final int STRAT_VOICE1 = 8;
    public static final int STRAT_VOICE2 = 9;
    public static final int STRAT_VOICE3 = 10;
    public static final int SEND_STATE_CHANGGE = 11;

    private int[] voiceNum = new int[]{STRAT_VOICE0, STRAT_VOICE1, STRAT_VOICE2, STRAT_VOICE3};
    int wave_num = 0;
    private TextView tv_guider_name;//导游名称
    private ImageView zuo, you;
    private final int ANIMATION_INTERVAL = 260;// 200ms
    private int lostNums = 0;

    /**
     * 获取导游头像
     */
    Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GET_HEAD_ICON:
                    if (!TextUtils.isEmpty(sp.getString(UserInfo.GUIDER_NICK, ""))) {
                        tv_guider_name.setText(getResources().getString(R.string.tour_guide) + sp.getString(UserInfo.GUIDER_NICK, ""));//设置导游名字
                    }
                    if (null != image_main_head_icon) {
                        final String guider_nik = sp.getString(UserInfo.GUIDER_NICK, "");
                        if (TextUtils.isEmpty(guider_nik)) {//设置导游昵称
                            image_main_head_icon.setEnabled(false);
                        } else {
                            image_main_head_icon.setEnabled(true);
                        }
                        image = sp.getString(UserInfo.LOAD_ICON, "");
                        LogUtil.d(TAG, "获取导游头像:" + image + "imageLoader:" + imageLoader + "guider_nik:" + guider_nik);
                        if (!TextUtils.isEmpty(image)) {//设置导游头像
                            image_main_head_icon.setErrorImageResId(R.mipmap.default_hear_ico);
                            image_main_head_icon.setImageUrl(image, imageLoader);
                            LogUtil.d(TAG, "获取导游头像成功");
                            return;
                        } else {
                            image_main_head_icon.setImageResource(R.mipmap.default_hear_ico);
                        }
                    }
                    break;
                case GET_LOST_NUMB://统计丢包数量
                    if (tv_main_wave_lost != null) {
                        int lostNum = msg.arg1;
                        lostNums += lostNum;
                        tv_main_wave_lost.setText(lostNums + "");
                        if (lostNums > 1000)
                            lostNums = 0;
                    }
                    break;
                case LOST_NUMB_CLEAR://丢包数量清零
                    if (tv_main_wave_lost_sum != null) {
                        lostNumSum = lostNum;
                        tv_main_wave_lost_sum.setText(lostNumSum + "");
                        lostNum = 0;
                        tv_main_wave_lost.setText(lostNum + "");
                    }
                    break;
                case STOP_VOICE://暂停播放语音
                    pauseWave();
                    break;
                case WAVE_ISRUN:
                    LogUtil.d(TAG, "动画切换" + wave_num);
                    if (imagev_wava_voice != null) {
                        NoAndOff();
                        if (++wave_num < 15) {
                            mHandler.sendEmptyMessageDelayed(WAVE_ISRUN, 500);
                        } else {
                            wave_num = 0;
                        }
                    }
                    break;
                case STRAT_VOICE:
                    ToastUtil.showToast(context,"确定",3000);

                    break;
                case STRAT_VOICE1:
                    iv_vave_1.setVisibility(View.VISIBLE);
                    ImageUtil.myAnimation(iv_vave_1);
                    break;
                case STRAT_VOICE2:
                    iv_vave_2.setVisibility(View.VISIBLE);
                    ImageUtil.myAnimation(iv_vave_2);
                    break;
                case STRAT_VOICE3:
                    iv_vave_3.setVisibility(View.VISIBLE);
                    ImageUtil.myAnimation(iv_vave_3);
                    break;
                case STRAT_VOICE0:
                    iv_vave_1.setVisibility(View.INVISIBLE);
                    iv_vave_2.setVisibility(View.INVISIBLE);
                    iv_vave_3.setVisibility(View.INVISIBLE);
                    ImageUtil.togetherRun(imagev_wava_voice_1);
//                    if (isAdded() && !isHidden && waveState == 1)
//                        ImageUtil.togetherRun(imagev_wava_voice_1);
//                    else {
//                        mHandler.sendEmptyMessageDelayed(SERVICE_ENWS, 5000);
//                        return;
//                    }
//                    mHandler.sendEmptyMessageDelayed(SERVICE_ENWS, 3000);
                    break;
                case SEND_STATE_CHANGGE:
                    new Thread(sendRun).start();
                    break;
            }
        }
    };
    private MyApplication application;
    private static boolean isHidden = false;
    private MainFragment context;
    private RequestQueue mRequestQueue;
    private ImageView iv_vave_1;
    private ImageView iv_vave_2;
    private ImageView iv_vave_3;
    private WaveRun waveRun;
    private boolean isLoad;


    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        LogUtil.d(TAG, "onHiddenChanged：" + hidden);
        isHidden = hidden;
        if (hidden) {//隐藏
            hidden();
        } else {//显示
            show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        LogUtil.d(TAG, "onResume");
        if (!isHidden) {
            show();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        LogUtil.d(TAG, "onPause");
        if (!isHidden) {
            hidden();
        }
    }

    public void hidden() {
        stopPlayWave();
        MobclickAgent.onPageEnd("语音界面"); // （仅有Activity的应用中SDK自动调用，不需要单独写）保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息。"SplashScreen"为页面名称，可自定义
        MobclickAgent.onPause(getActivity());
    }

    public void show() {

        isLoad = sp.getBoolean(UserInfo.ISLOAD, false);

        if (waveState == 1)
            playWave();
        NoAndOff();
        //获取导游信息
        mHandler.sendEmptyMessageDelayed(GET_HEAD_ICON, 500);
        MobclickAgent.onPageStart("语音界面"); //统计页面(仅有Activity的应用中SDK自动调用，不需要单独写。"SplashScreen"为页面名称，可自定义)
        MobclickAgent.onResume(getActivity());//统计时长
    }

    @Override
    public void onStart() {
        LogUtil.d(TAG, "onStart:" + isHidden);
        isStop = false;
        if (waveState == 1)
            playWave();
        super.onStart();
    }

    @Override
    public void onStop() {
        LogUtil.d(TAG, "onStop");
        isStop = true;
        stopPlayWave();
        super.onStop();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mCallback = (MenuCallback) getActivity();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        LogUtil.d(TAG, "onCreate");
        MyApplication ma = (MyApplication) getActivity().getApplication();
        mRequestQueue = ma.getRequestQueue();
        context = (MainFragment) getActivity();

        GuiderLost guiderLost = new GuiderLost();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ActionConstats.BATTERY_INFO_CHANGE);
        intentFilter.addAction(ActionConstats.STRCHANGE);
        context.registerReceiver(guiderLost, intentFilter);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_main_layout, container,
                false);
        LogUtil.d(TAG, "onCreateView");

        imageLoader = MyImageLoader.getInstance(context);
        sp = context.getSharedPreferences(Constats.SHARE_KEY,
                Context.MODE_PRIVATE);

        application = (MyApplication) getActivity().getApplication();
        initView(view);
        startVoice();
        bindVoice();
        initSocket();
        netChangeReceiver.sendVoiceHandler(mHandler);
        MulticastClient.sendVoiceHandler(mHandler, context);
        setNotification();
        isDestory = false;
        //语音是否播放
        mHandler.sendEmptyMessageDelayed(FragmentMainVoice.WAVE_ISRUN, 300);
        return view;
    }

    public static int waveState = 3;//未连接设备状态


    public class WaveRun implements Runnable {
        int playNum = 0;

        @Override
        public void run() {
            if (isPlayStop || imagev_wava_voice_1 == null || waveState != 1) {
                return;
            }
            LogUtil.d(TAG, "WaveRun：" + playNum);
            mHandler.sendEmptyMessage(voiceNum[playNum]);
            playNum++;
            if (playNum >= 4) {
                playNum = 0;
                mHandler.postDelayed(this, 3000);
                return;
            }
            mHandler.postDelayed(this, 180);
        }
    }

    private void stopPlayWave() {
        isPlayStop = true;
        if (waveRun != null) {
            mHandler.removeCallbacks(waveRun);
            waveRun = null;
        }
    }

    private void playWave() {
        if (!isPlayStop || waveState != 1)
            return;
        isPlayStop = false;
        waveRun = new WaveRun();
        if (isLoad)
            imagev_wava_voice_1.setImageResource(R.mipmap.m_voice_ing);
        else
            imagev_wava_voice_1.setImageResource(R.mipmap.voice_ing);
        mHandler.post(waveRun);
    }

    private void startWave() {
        if (isLoad)
            imagev_wava_voice_1.setImageResource(R.mipmap.m_voice_ing);
        else
            imagev_wava_voice_1.setImageResource(R.mipmap.voice_ing);

        zuo.setVisibility(View.GONE);
        you.setVisibility(View.GONE);
        tv_wava_nooff.setVisibility(View.INVISIBLE);
        mHandler.sendEmptyMessage(STRAT_VOICE0);

        iv_vave_1.setVisibility(View.VISIBLE);
        iv_vave_2.setVisibility(View.VISIBLE);
        iv_vave_3.setVisibility(View.VISIBLE);
        if (waveState == 1) {
            return;
        }
        waveState = 1;//播放状态
        playWave();
    }

    private void stopWave() {
        if (isLoad)
            imagev_wava_voice_1.setImageResource(R.mipmap.m_voice_stop);
        else
            imagev_wava_voice_1.setImageResource(R.mipmap.voice_not_hava);
        tv_device_name.setText(R.string.t_main_voice_device_name_2);
        iv_vave_1.setVisibility(View.INVISIBLE);
        iv_vave_2.setVisibility(View.INVISIBLE);
        iv_vave_3.setVisibility(View.INVISIBLE);
        zuo.setVisibility(View.VISIBLE);
        you.setVisibility(View.VISIBLE);
        tv_wava_nooff.setVisibility(View.VISIBLE);
        imagev_wava_voice.setVisibility(View.INVISIBLE);
        if (waveState == 3)
            return;
        waveState = 3;//停止状态
        stopPlayWave();
    }

    private void pauseWave() {
        if (isLoad)
            imagev_wava_voice_1.setImageResource(R.mipmap.m_voice_pause);
        else
            imagev_wava_voice_1.setImageResource(R.mipmap.voice_stop_icon);
        iv_vave_1.setVisibility(View.INVISIBLE);
        iv_vave_2.setVisibility(View.INVISIBLE);
        iv_vave_3.setVisibility(View.INVISIBLE);
        tv_wava_nooff.setVisibility(View.VISIBLE);
        ImageUtil.togetherRun(imagev_wava_voice_1);
        binder.stopAudio();
        if (waveState == 2)
            return;
        waveState = 2;//暂停状态
        stopPlayWave();
    }

    /**
     * 初始化界面
     *
     * @param view
     */
    private void initView(View view) {
        LogUtil.d(TAG, "initView");
        top_back_img_rl = (RelativeLayout) view.findViewById(R.id.rl_main_back_img);
        top_back_img_rl.setOnClickListener(this);
        /**获取视图*/
        image_main_head_icon = (RoundImageViewByXfermode) view
                .findViewById(R.id.image_main_head_icon);//导游头像
        tv_main_wave_lost = (TextView) view
                .findViewById(R.id.tv_main_wave_lost);//丢包数量(零时)
        zuo = (ImageView) view.findViewById(R.id.zuo);//左
        zuo.setOnClickListener(this);
        you = (ImageView) view.findViewById(R.id.you);//右
        you.setOnClickListener(this);
        tv_guider_name = (TextView) view.findViewById(R.id.tv_guider_name);//导游名字
        imagev_main_guider_icon = (RoundImageViewByXfermode) view.findViewById(R.id.imagev_main_guider_icon);// 用户头像

        imagev_wava_voice = (ImageView) view.findViewById(R.id.imagev_wava_voice);

        imagev_wava_voice_1 = (ImageView) view.findViewById(R.id.imagev_wava_voice_1);//暂停动画
        if (isLoad) {
            if (sp.getBoolean(UserInfo.IS_VOICE_PAUSE, false)) {
                imagev_wava_voice_1.setImageResource(R.mipmap.m_voice_pause);
                waveState = 2;//暂停状态
            } else
                imagev_wava_voice_1.setImageResource(R.mipmap.m_voice_stop);
        }
        imagev_wava_voice_1.setOnClickListener(this);
        tv_device_name = (TextView) view.findViewById(R.id.tv_main_device_name);//设备名称
        tv_wava_nooff = (TextView) view.findViewById(R.id.tv_main_wave_nooff);//on/off
        tv_main_wave_lost_sum = (TextView) view.findViewById(R.id.tv_main_wave_lost_sum);//on/off

        /**设置内容和监听*/
        // 获取用户头像
        String requestUrl = sp.getString(UserInfo.HEAD_ICON_URL, "");
        boolean is_login = sp.getBoolean(UserInfo.LOGINING, false);
        if (is_login && !TextUtils.isEmpty(requestUrl)) {
            imagev_main_guider_icon
                    .setErrorImageResId(R.mipmap.default_hear_ico);
            imagev_main_guider_icon.setImageUrl(requestUrl, imageLoader);
        } else {
            imagev_main_guider_icon.setDefaultImageResId(R.mipmap.default_hear_ico);
        }
        image_main_head_icon.setOnClickListener(this);//用户头像监听

        imagev_main_guider_icon.setOnClickListener(this);//导游头像监听

        iv_vave_1 = (ImageView) view.findViewById(R.id.iv_voice_wave_1);
        iv_vave_2 = (ImageView) view.findViewById(R.id.iv_voice_wave_2);
        iv_vave_3 = (ImageView) view.findViewById(R.id.iv_voice_wave_3);
    }


    /**
     * 启动service
     */
    @SuppressWarnings("deprecation")
    public void startVoice() {
        LogUtil.d(TAG, "startVoice");
        //启动语音service
        Intent audioIntent = new Intent(context, AudioPlayService.class);
        context.startService(audioIntent);
    }

    /**
     * 绑定service
     */
    public void bindVoice() {
        Intent intent = new Intent(context, AudioPlayService.class);
        context.bindService(intent, conn, Context.BIND_AUTO_CREATE);
    }

    /**
     * 绑定服务器回调接口
     */
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            binder = (MyBinder) service;
            binder.setHandler(mHandler, FragmentMainVoice.this.getActivity());
            MainFragment activity = (MainFragment) FragmentMainVoice.this.getActivity();
            activity.setBind(binder);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    /**
     * 设置notification
     */
    private void setNotification() {
        nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);


        //点击的意图ACTION是跳转到Intent
        Intent resultIntent = new Intent(context, MainFragment.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);

        NotificationCompat.Builder notifyBuilder = new NotificationCompat.Builder(context)
            /*设置large icon*/
                .setLargeIcon(bitmap)
             /*设置small icon*/
                .setSmallIcon(R.mipmap.ic_launcher)
            /*设置title*/
                .setContentTitle(getResources().getString(R.string.t_main_voice_notifi_ticket))
            /*设置详细文本*/
                .setContentText(getResources().getString(R.string.t_main_voice_notifi_cont))
             /*设置发出通知的时间为发出通知时的系统时间*/
                .setWhen(System.currentTimeMillis())
             /*设置发出通知时在status bar进行提醒*/
//                .setTicker("来自问月的祝福")
            /*setOngoing(boolean)设为true,notification将无法通过左右滑动的方式清除
            * 可用于添加常驻通知，必须调用cancle方法来清除
            */
                .setOngoing(false)
             /*设置点击后通知消失*/
                .setAutoCancel(false)
             /*设置通知数量的显示类似于QQ那种，用于同志的合并*/
//                .setNumber(2)
             /*点击跳转到MainActivity*/
                .setContentIntent(pendingIntent);

        nm.notify(121, notifyBuilder.build());
    }

    /**
     * 解绑service
     */
    @Override
    public void onDestroy() {
        context.unbindService(conn);
        isDestory = true;
        threadStop = true;
        LogUtil.d(TAG, "onDestroy");
        super.onDestroy();
    }


    private String wifiName;
    private boolean isPause = false;

    /**
     * WiFi连接与不连接之间动画切换
     */
    public void NoAndOff() {

        wifiName = WifiUtil.getWifiName(context);
        LogUtil.d(TAG, "wifiName:" + wifiName + "\n" + "application.wifiName:" + application.wifiName);

        // 如果连上TucsonWiFi并启动广播就启动动画
        if ((!TextUtils.isEmpty(application.wifiName)) && wifiName.equals(application.wifiName)) {
            LogUtil.d(TAG, "application.isNotData:" + application.isNotData);

            if (!isLoad && application.isNotData) {
                stopWave();
                return;
            }
            LogUtil.d(TAG, "waveState:" + waveState);
            wave_num = 20;
            zuo.setVisibility(View.GONE);
            you.setVisibility(View.GONE);
            if (isAdded()) {
                wifiName = wifiName.replace("\"", "");
                tv_device_name.setText(getResources().getString(R.string.t_main_voice_device_name_1) + " - " + wifiName);
            }
            if (isPause) {
                LogUtil.d(TAG, "NoAndOff:" + "关闭");
                pauseWave();
            } else if (waveState != 1) {
                LogUtil.d(TAG, "NoAndOff:" + "开启");
                startWave();
            }
        } else {// 未连接设备
            if (isLoad && sp.getBoolean(UserInfo.IS_VOICE_PAUSE, false) && sp.getString(UserInfo.PAUSE_WIFI_NAME, "").equals(wifiName)) {
                imagev_wava_voice_1.setImageResource(R.mipmap.m_voice_pause);
                iv_vave_1.setVisibility(View.INVISIBLE);
                iv_vave_2.setVisibility(View.INVISIBLE);
                iv_vave_3.setVisibility(View.INVISIBLE);
                tv_wava_nooff.setVisibility(View.VISIBLE);
                waveState = 2;//暂停状态
            } else
                stopWave();
        }
    }

    // 是否播放的切换

    public void isPlayVoice() {
        if (waveState == 1) {// 暂停
            LogUtil.d(TAG, "停止播放");
            isPause = true;
            lostNums = 0;
            tv_main_wave_lost.setText(lostNums + "");
            mHandler.sendEmptyMessage(STOP_VOICE);
        } else if (waveState == 2) {// 播放
            LogUtil.d(TAG, "播放语音");
            isPause = false;
            NoAndOff();
            binder.startAudio();
        }
    }

    private DatagramSocket mSocket;
    private InetAddress address;
    private byte[] buffer;
    private String sendIp = "192.168.1.1";
    private int lastState;

    private void initSocket() {
        try {
            mSocket = new DatagramSocket();
            address = InetAddress.getByName(sendIp);
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    Runnable sendRun = new Runnable() {
        @Override
        public void run() {
            LogUtil.d(TAG, "waveState:" + waveState);
            if (mSocket == null || lastState == waveState)
                return;
            final WifiManager wm = (WifiManager) context
                    .getSystemService(Context.WIFI_SERVICE);
            final WifiInfo wi = wm.getConnectionInfo();
            wifiName = wi.getSSID();
            if (waveState == 1) {
                try {
                    buffer = "1".getBytes("UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                sp.edit().putBoolean(UserInfo.IS_VOICE_PAUSE, false).apply();
                sp.edit().putString(UserInfo.PAUSE_WIFI_NAME, "").apply();
            } else {
                try {
                    buffer = "2".getBytes("UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                sp.edit().putBoolean(UserInfo.IS_VOICE_PAUSE, true).apply();
                sp.edit().putString(UserInfo.PAUSE_WIFI_NAME, wifiName).apply();

            }
            DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length, address, 5353);
            LogUtil.d(TAG, "buffer.length:" + new String(buffer));
            try {
                mSocket.send(datagramPacket);
                mSocket.send(datagramPacket);
                mSocket.send(datagramPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
            lastState = waveState;
        }
    };

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.rl_main_back_img:
                if (null != mCallback) {
                    mCallback.callback();
                }
                break;
            case R.id.imagev_main_guider_icon:
                if (null != mCallback) {
                    mCallback.callback();
                }
                break;
            case R.id.image_main_head_icon:// 跳转导游详情
                image_main_head_icon.setEnabled(false);
//                togetherRun(image_main_head_icon);
                ImageUtil.togetherRun(image_main_head_icon);
                image_main_head_icon.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent();
                        intent.setClass(getActivity(), GuiderInfoActivity2.class);
                        startActivity(intent);
                        getActivity().overridePendingTransition(R.anim.fangda_in, R.anim.no_change);
                        image_main_head_icon.setEnabled(true);
                    }
                }, 400);

                break;
            case R.id.imagev_wava_voice_1:// 播放语音
                if (waveState == 3) {
                    ImageUtil.togetherRun(imagev_wava_voice_1);
                } else {
                    isPlayVoice();
                    if (isLoad) {
                        if (sp.getBoolean(UserInfo.IS_VOICE_PAUSE, false)) {
                            waveState = 1;
                        }
                        mHandler.removeMessages(SEND_STATE_CHANGGE);
                        mHandler.sendEmptyMessage(SEND_STATE_CHANGGE);
                    }
                }
                break;
            case R.id.zuo://左箭头切换
                switchWavaImage();
                break;
            case R.id.you://右箭头切换
                switchWavaImage();
                break;
            default:
                break;
        }
    }

    public void switchWavaImage() {
        if (MyHttpUtil.isConnTorsun((Activity) context)) {
            return;
        }
        if (isZuoOrYou) {
            isZuoOrYou = false;
            ObjectAnimator.ofFloat(imagev_wava_voice_1, "rotationY", 90.0f, 0.0f).setDuration(370).start();
            imagev_wava_voice_1.setImageResource(R.mipmap.m_voice_stop);
            ObjectAnimator.ofFloat(imagev_wava_voice_1, "rotationY", 90.0f, 0.0f).setDuration(370).start();
        } else {
            isZuoOrYou = true;
            ObjectAnimator.ofFloat(imagev_wava_voice_1, "rotationY", 90.0f, 0.0f).setDuration(370).start();
            imagev_wava_voice_1.setImageResource(R.mipmap.voice_not_hava);
            ObjectAnimator.ofFloat(imagev_wava_voice_1, "rotationY", 90.0f, 0.0f).setDuration(370).start();
        }
    }

    private boolean isZuoOrYou = true;

    private class GuiderLost extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            isLoad = sp.getBoolean(UserInfo.ISLOAD, false);
            LogUtil.d(TAG, "接收到导游信息改变广播：" + isLoad);
//            switch (intent.getAction()) {
//                case ActionConstats.BATTERY_INFO_CHANGE://导游权限被剥夺
//
//                    break;
//                case ActionConstats.STRCHANGE://获取导游权限
//
//                    break;
//            }
            if (waveState == 1) {//播放
                startWave();
            } else if (waveState == 2) {//暂停
                pauseWave();
            } else if (waveState == 3) {//停止
                stopWave();
            }
        }
    }
}
