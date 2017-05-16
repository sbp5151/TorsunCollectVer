package com.jld.torsun.activity.loginAndRegies;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonRequest;
import com.jld.torsun.MyApplication;
import com.jld.torsun.R;
import com.jld.torsun.activity.BaseActivity;
import com.jld.torsun.activity.MainFragment;
import com.jld.torsun.db.MemberDao;
import com.jld.torsun.http.VolleyJsonUtil;
import com.jld.torsun.modle.MyDialog;
import com.jld.torsun.service.MulcastService;
import com.jld.torsun.util.AndroidUtil;
import com.jld.torsun.util.Constats;
import com.jld.torsun.util.LogUtil;
import com.jld.torsun.util.MyHttpUtil;
import com.jld.torsun.util.ToastUtil;
import com.jld.torsun.util.UserInfo;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.HttpHandler;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import cn.jpush.android.api.JPushInterface;

/**
 * 启动页
 */
public class StartActivity extends BaseActivity {
    private ViewPager vPager;
    private View loadView;
    private View startView;
    private ViewPagerAdapter adapter;
    private TextView tv_info;
    private List<View> views = new ArrayList<View>();
    private static final int SKIP_MAIN = 3;// 跳转主函数
    private static final int SHOW_UPDATE_DIALOG = 1;// 有更新，显示更新提示
    private String downloadUrl;// 新版本下载链接
    private SharedPreferences sp;
    private MyApplication ma;
    private RequestQueue rq;// volley下载队列
    private PackageInfo pi;
    private boolean is_skip;

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            LogUtil.d("StartActivity", "接收message");
            switch (msg.what) {
                case SKIP_MAIN:
                    LogUtil.d("StartActivity启动UI");
                    if (myDialog != null)
                        return;
                    long waitTime = System.currentTimeMillis() - startTime;
                    LogUtil.d("StartActivity", "waitTime：" + waitTime);
                    if (waitTime < 1000) {
                        mHandler.sendEmptyMessageDelayed(SKIP_MAIN, 1000 - waitTime);
                        return;
                    }
                    skipMain();
                    break;
                case SHOW_UPDATE_DIALOG:
                    if (is_skip)
                        return;
                    myDialog = new MyDialog(false, StartActivity.this);
                    myDialog.setTitle(getResources().getString(R.string.t_start_dia_title));
                    myDialog.setContent(getResources().getString(R.string.t_start_dia_msg));
                    myDialog.setConfirm(getResources().getString(R.string.t_start_dia_sure));
                    myDialog.setCancel(getResources().getString(R.string.t_start_dia_can));
                    myDialog.setIsTouchCancel(false);
                    myDialog.showTwo(new MyDialog.TwoOnclick() {
                        @Override
                        public void cancel_method() {
                            ma.isVersionUpdate = true;// 设置有更新
                            skipMain();//
                            myDialog.dismiss();
                        }

                        @Override
                        public void confirm_method() {
                            if (downloadUrl != null)
                                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
                                    Download(downloadUrl);// 下载最新版本
                                else
                                    ToastUtil.showToast(StartActivity.this, getResources().getString(R.string.sd_disabled), 3000);
                            skipMain();//
                            myDialog.dismiss();
                        }

                        @Override
                        public void close_method() {
                            ma.isVersionUpdate = true;// 设置有更新
                            skipMain();//
                            myDialog.dismiss();
                        }
                    });
                    break;
            }
        }

    };
    private NotificationCompat.Builder notifyBuilder;
    private NotificationManager nm;
    private MyDialog myDialog;
    private Long startTime;
    public static HttpHandler<File> download;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        sp = getSharedPreferences(Constats.SHARE_KEY, Context.MODE_PRIVATE);
        sp.edit().putString(UserInfo.LAST_TEAM_ID, "").apply();
        initLanguage();

//        sp.edit().putBoolean(UserInfo.ISLOAD, false).commit();
        vPager = (ViewPager) findViewById(R.id.vPager);
//        MyHttpUtil.getWifiName(this);// 获取WiFi名称
        getSimStart();// 获取SIM卡状态

        // 初始化扉页1
        loadView = LayoutInflater.from(this).inflate(
                R.layout.activity_load_layout, null);// 扉页1

        ImageView imagev_load_delete = (ImageView) loadView
                .findViewById(R.id.imagev_load_delete);
        imagev_load_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (vPager.getCurrentItem() == 0)//如果三秒钟用户还没有切换页面，则切换
                    vPager.setCurrentItem(1);
            }
        });
        ImageView imagev_load_txt = (ImageView) loadView
                .findViewById(R.id.imagev_load_txt);
        imagev_load_txt.getLayoutParams().width = (int) (AndroidUtil
                .getScreenWidth(this) * 0.7);

        // 初始化扉页2
        startView = LayoutInflater.from(this).inflate(
                R.layout.activity_start_layout, null);// 扉页2
        ImageView imagev_start_pager_text = (ImageView) startView
                .findViewById(R.id.imagev_start_pager_text);
        imagev_start_pager_text.getLayoutParams().width = (int) (AndroidUtil
                .getScreenWidth(this) * 0.6);
        tv_info = (TextView) startView.findViewById(R.id.tv_info);

        // 获取版本
        PackageManager pm = getPackageManager();
        try {
            pi = pm.getPackageInfo("com.jld.torsun", 0);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        ma = (MyApplication) getApplication();
        rq = ma.getRequestQueue();

        /**
         * 开始组播(组团)
         * */
        LogUtil.i("", "--------开始组播(组团)");
        Intent service = new Intent();
        service.setClass(this, MulcastService.class);
        startService(service);
        MyHttpUtil.isWifiConnected(this);// 判断有没有WiFi连接

        //友盟统计
        MobclickAgent.openActivityDurationTrack(false);
        //半个小时后启动一次算重新启动
        MobclickAgent.setSessionContinueMillis(6000 * 10 * 30);

        //清空团队成员在线状态
        MemberDao mDao = MemberDao.getInstance(this);
        mDao.cancelOnlineAll();
    }

    /**
     * 初始化应用语言，从本地SharedPreferences中读取，保存的key为"language_choice"
     */
    private void initLanguage() {
        final SharedPreferences sharedPreferences = getSharedPreferences("language_choice", Context.MODE_PRIVATE);
        final int id = sharedPreferences.getInt("language_id", 0);
        Resources resources = getResources();//获得res资源对象
        DisplayMetrics dm = resources.getDisplayMetrics();//获得屏幕参数：主要是分辨率，像素等。
        Configuration config = resources.getConfiguration();//获得设置对象
        switch (id) {
            case 0://默认，跟随系统语言
                config.locale = Locale.getDefault();
                break;
            case 1://中文简体
                config.locale = Locale.SIMPLIFIED_CHINESE;
                break;
            case 2://English
                config.locale = Locale.ENGLISH;
                break;
            default:
                config.locale = Locale.getDefault();
                break;
        }
        resources.updateConfiguration(config, dm);
    }

    private void initView() {
        if (!sp.getBoolean("nofirst", true)) {// 如果是第一次启动
            String wifiName = MyHttpUtil.getRegiesUpWifiName(this);
            if (TextUtils.isEmpty(sp.getString(UserInfo.download_wifi, "")))
                sp.edit().putString(UserInfo.download_wifi, wifiName).apply();
            views.add(startView);
            views.add(loadView);
            adapter = new ViewPagerAdapter(views);
            vPager.setAdapter(adapter);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (vPager.getCurrentItem() == 0)//如果三秒钟用户还没有切换页面，则切换
                        vPager.setCurrentItem(1);
                }
            }, 3000);

            vPager.setOnPageChangeListener(new OnPageChangeListener() {
                @Override
                public void onPageSelected(int position) {
                    if (1 == position) {
                        sp.edit().putBoolean("nofirst", true).apply();// 如果进入扉页2设置启动过标志
                        vPager.setOnTouchListener(new OnTouchListener() {// 禁止滑动
                            @Override
                            public boolean onTouch(View arg0, MotionEvent arg1) {
                                return true;
                            }
                        });
                        new Handler().postDelayed(new Runnable() {
                            public void run() {
                                skipMain();
                            }
                        }, 2000);
                    }
                }

                @Override
                public void onPageScrolled(int arg0, float arg1, int arg2) {
                }

                @Override
                public void onPageScrollStateChanged(int arg0) {
                }
            });
        } else {// 如果登陆过
            startTime = System.currentTimeMillis();
            views.add(startView);// 直接跳转扉页2
            adapter = new ViewPagerAdapter(views);
            vPager.setAdapter(adapter);
            if (MyHttpUtil.isWifiConn(this))
                isUpdate();
            mHandler.sendEmptyMessageDelayed(SKIP_MAIN, 1500);
        }
    }
    /**
     * 跳转到主页面或者注册页面
     */
    public void skipMain() {
        if (is_skip)
            return;
        is_skip = true;
//        boolean logining = sp.getBoolean(UserInfo.LOGINING, false);
//        String userid = sp.getString(UserInfo.USER_ID, "");

//        String wifiName = MyHttpUtil.getWifiName1(this);
//        LogUtil.d("StartActivity", "logining = " + logining);
//        LogUtil.d("StartActivity", "userid = " + userid);
//        boolean isMovieWifi = MyHttpUtil.isMovieWifi(this);
        Intent mainIntent = new Intent(StartActivity.this, MainFragment.class);
//        mainIntent.putExtra("isMovieWifi", isMovieWifi);
        StartActivity.this.startActivity(mainIntent);
        StartActivity.this.finish();

//        if (!sp.getBoolean(UserInfo.SIM_START, true)) {// 如果没有SIM卡直接跳转主页面
//            Intent mainIntent = new Intent(StartActivity.this,
//                    MainFragment.class);
//            StartActivity.this.startActivity(mainIntent);
//            StartActivity.this.finish();
//        } else if (!logining && TextUtils.isEmpty(userid)) {// 跳转注册页面
//            Intent mainIntent = new Intent(StartActivity.this, RegiesUser.class);
//            StartActivity.this.startActivity(mainIntent);
//            StartActivity.this.finish();
//        } else if (!logining && !TextUtils.isEmpty(userid)) {// 跳转重复登陆页面
//            Intent mainIntent = new Intent(StartActivity.this,
//                    RepeatLoginActivity.class);
//            StartActivity.this.startActivity(mainIntent);
//            StartActivity.this.finish();
//        } else if (logining && !TextUtils.isEmpty(userid)) {// 跳转主页面
//            Intent mainIntent = new Intent(StartActivity.this,
//                    MainFragment.class);
//            StartActivity.this.startActivity(mainIntent);
//            StartActivity.this.finish();
//        }
    }

    /**
     * 判断是否有版本更新
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    public void isUpdate() {
        LogUtil.i("----------StartActivity--------update-----");
        JsonRequest<JSONObject> jsonRequest = VolleyJsonUtil
                .createJsonObjectRequest(Method.POST, Constats.GET_VERSION_CODE
                        + pi.versionCode, new Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (null != response) {
                            try {
                                // 获取packagemanager的实例
                                PackageManager packageManager = getPackageManager();
                                // getPackageName()是你当前类的包名，0代表是获取版本信息
                                PackageInfo packInfo = null;
                                try {
                                    packInfo = packageManager.getPackageInfo(getPackageName(), 0);
                                } catch (NameNotFoundException e) {
                                    e.printStackTrace();
                                }
                                String version = packInfo.versionName;
                                String requesVersion = response.getString("version");
                                LogUtil.d("StartActivity", "version:" + version);
                                LogUtil.d("StartActivity", "response:" + requesVersion);
                                if (version.equals(requesVersion)) {
                                    mHandler.sendEmptyMessage(SKIP_MAIN);
                                    return;
                                }
                                downloadUrl = response.getString("url").trim();
                                // int versisonCode =
                                // response.getInt("vernum");
                                LogUtil.d("StartActivity", "downloadUrl:"
                                        + downloadUrl);
                                if (TextUtils.isEmpty(downloadUrl)) {
                                    mHandler.sendEmptyMessage(SKIP_MAIN);
                                } else {// 没有更新，跳转主页面
                                    mHandler.sendEmptyMessage(SHOW_UPDATE_DIALOG);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                mHandler.sendEmptyMessage(SKIP_MAIN);
                            }
                        }
                    }
                }, new ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mHandler.sendEmptyMessage(SKIP_MAIN);
                    }
                });
        if (null != rq) {
            rq.add(jsonRequest);
        }
    }

    /**
     * 下载最新版本
     *
     * @param str
     */
    public void Download(String str) {
        HttpUtils hu = new HttpUtils();
        setNotification();
        download = hu.download(str, "/sdcard/torsun.apk", new RequestCallBack<File>() {
            // 下载成功
            @Override
            public void onSuccess(ResponseInfo<File> arg0) {
                Intent intent = new Intent();
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setAction("android.intent.action.VIEW");
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setDataAndType(Uri.fromFile(new File(Environment
                                .getExternalStorageDirectory(), "torsun.apk")),
                        "application/vnd.android.package-archive");
                startActivity(intent);
                nm.cancel(111);
            }

            // 下载失败
            @Override
            public void onFailure(HttpException arg0, String arg1) {
                LogUtil.d("StartActivity", "获取更新错误	" + arg0.toString());
                arg0.printStackTrace();
                ma.isVersionUpdate = true;// 设置有更新
                nm.cancel(111);
            }
            // 下载中...
            @Override
            public void onLoading(long total, long current, boolean isUploading) {
                // tv_info.setText(current / 1000 + "K/" + total / 1000 + "K");
                float myTotal = (float) (total / 1000);
                float myCurrent = (float) (current / 1000);
                LogUtil.d("Download---", "myCurrent:" + myCurrent + "");
                LogUtil.d("Download---", "total:" + myTotal + "");
                int progress = (int) (myCurrent * (100 / myTotal));
                LogUtil.d("---Download---", "progress:" + progress + "");
                notifyBuilder.setProgress((int) myTotal, (int) myCurrent, false);
                notifyBuilder.setContentTitle(getResources().getString(R.string.download_ing) + progress + "%");
                nm.notify(111, notifyBuilder.build());
                super.onLoading(total, current, isUploading);
            }
        });
    }

    /**
     * 设置notification
     */
    private void setNotification() {

        //点击的意图ACTION是跳转到Intent
        Intent resultIntent = new Intent(this, MainFragment.NotificationClickReceiver.class);
        resultIntent.setAction("downloadNotification");
        int id = (int) (System.currentTimeMillis() / 1000);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, id, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        nm = (NotificationManager) this
                .getSystemService(Context.NOTIFICATION_SERVICE);
        //点击的意图ACTION是跳转到Intent
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        notifyBuilder = new NotificationCompat.Builder(this)
            /*设置large icon*/
                .setLargeIcon(bitmap)
             /*设置small icon*/
                .setSmallIcon(R.mipmap.ic_launcher)
            /*设置title*/
                .setContentTitle(getResources().getString(R.string.download_ing) + "...0%")
                .setProgress(100, 0, false)
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
                /*取消时间显示*/
                .setShowWhen(true).setDeleteIntent(pendingIntent);
        nm.notify(111, notifyBuilder.build());
    }

    /**
     * 安装完成，跳转主页面
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        skipMain();
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * viewpager适配器
     */
    public class ViewPagerAdapter extends PagerAdapter {
        List<View> viewLists;

        public ViewPagerAdapter(List<View> lists) {
            viewLists = lists;
        }

        @Override
        public int getCount() {
            return viewLists.size();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public void destroyItem(View view, int position, Object object) {
            ((ViewPager) view).removeView(viewLists.get(position));
        }

        @Override
        public Object instantiateItem(View view, int position) {

            ViewGroup group = (ViewGroup) viewLists.get(position).getParent();
            if (group != null) {
                group.removeView(viewLists.get(position));
            }
            ((ViewPager) view).addView(viewLists.get(position));
            return viewLists.get(position);

        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        JPushInterface.onPause(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initView();
        JPushInterface.onResume(this);

    }

    /**
     * 判断SIM卡的状态，如果不可用就直接进入
     */
    public void getSimStart() {
        TelephonyManager manager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);

        switch (manager.getSimState()) {
            case TelephonyManager.SIM_STATE_READY:// 良好
                sp.edit().putBoolean(UserInfo.SIM_START, true).commit();
                break;
            case TelephonyManager.SIM_STATE_ABSENT:// 无SIM卡，直接进入登录界面
//                sp.edit().putBoolean(UserInfo.SIM_START, false).commit();
//
//                sp.edit().putBoolean(UserInfo.LOGINING, false).commit();
//                sp.edit().putBoolean(UserInfo.ISLOAD, false).commit();
                ToastUtil.showToast(this, R.string.start_no_sim1, 3000);
                break;
            default:// SIM卡被锁定或未知状态，直接进入登录界面
//                sp.edit().putBoolean(UserInfo.SIM_START, false).commit();
//
//                sp.edit().putBoolean(UserInfo.LOGINING, false).commit();
//                sp.edit().putBoolean(UserInfo.ISLOAD, false).commit();
                ToastUtil.showToast(this, R.string.start_no_sim2, 3000);
                break;
        }
    }

}
