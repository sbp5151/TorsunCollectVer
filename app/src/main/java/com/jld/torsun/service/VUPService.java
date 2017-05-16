package com.jld.torsun.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.android.volley.RequestQueue;
import com.jld.torsun.MyApplication;
import com.jld.torsun.R;
import com.jld.torsun.activity.MainFragment;
import com.jld.torsun.util.LogUtil;
import com.jld.torsun.util.ToastUtil;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;

import java.io.File;

/***
 * 版本更新service
 */
public class VUPService extends Service {

    private PackageInfo pi;
    private MyApplication ma;
    private RequestQueue rq;
    private final String TAG = "VUPService";
    private final int UPDATE_WIN = 1;
    private final int UPDATE_FAIL = 0;
    int currentJ = 0;
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            int what = msg.what;
            switch (what) {

                case UPDATE_WIN:
                    LogUtil.d(TAG, "下载成功");
                    // 点击安装PendingIntent
                    Uri uri = Uri.fromFile(updateFile);
                    Intent installIntent = new Intent(Intent.ACTION_VIEW);
                    installIntent.setDataAndType(uri,
                            "application/vnd.android.package-archive");

                    updatePendingIntent = PendingIntent.getActivity(
                            VUPService.this, 0, installIntent, 0);

                    notification.defaults = Notification.DEFAULT_SOUND;// 铃声提醒

                    notifyBuilder.setContentText(getResources().getString(R.string.download_finish));
                    notifyBuilder.setContentIntent(updatePendingIntent);
                    notificationManager.notify(123, notification);
                    // 停止服务
                    stopService(notificationIntent);
                    break;
                case UPDATE_FAIL:
                    ToastUtil.showToast(VUPService.this, getResources().getString(R.string.update_fail), 3000);
                    break;
            }
        }
    };
    private String downloadLink;
    private NotificationManager notificationManager;
    private Notification notification;
    private File updateFile;
    private PendingIntent updatePendingIntent;
    private Intent notificationIntent;
    private NotificationCompat.Builder notifyBuilder;

    public VUPService() {
    }

    @Override
    public void onCreate() {
        // 获取版本
        PackageManager pm = getPackageManager();
        try {
            pi = pm.getPackageInfo("com.jld.torsun", 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        ma = (MyApplication) getApplication();
        rq = ma.getRequestQueue();
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        downloadLink = intent.getStringExtra("downloadLink");
        LogUtil.d(TAG, "downloadLink:" + downloadLink);

        // 创建文件
        if (Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState())) {
            File updateDir = new File(Environment.getExternalStorageDirectory(),
                    "torsun_download");
            updateFile = new File(updateDir.getPath(), "torsun.apk");
        }

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // 点击跳转到本页面
        notificationIntent = new Intent(Intent.ACTION_MAIN);
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        notificationIntent.setClass(this, MainFragment.class);
        // 跳转到正在运行的activity
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

        updatePendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        CharSequence contentTitle = getResources().getString(
                R.string.t_main_voice_notifi_ticket);
        CharSequence contentText = getResources().getString(
                R.string.download_ing);

        notificationManager.notify(1, notification);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);

        notifyBuilder = new NotificationCompat.Builder(this)
            /*设置large icon*/
                .setLargeIcon(bitmap)
             /*设置small icon*/
                .setSmallIcon(R.mipmap.ic_launcher)
            /*设置title*/
                .setContentTitle(contentTitle)
            /*设置详细文本*/
                .setContentText(contentText)
             /*设置发出通知的时间为发出通知时的系统时间*/
                .setWhen(System.currentTimeMillis())
             /*设置发出通知时在status bar进行提醒*/
//                .setTicker("来自问月的祝福")
            /*setOngoing(boolean)设为true,notification将无法通过左右滑动的方式清除
            * 可用于添加常驻通知，必须调用cancle方法来清除
            */
                .setOngoing(true)
             /*设置点击后通知消失*/
                .setAutoCancel(false)
             /*设置通知数量的显示类似于QQ那种，用于同志的合并*/
//                .setNumber(2)
             /*点击跳转到MainActivity*/
                .setContentIntent(updatePendingIntent);

        notificationManager.notify(123, notifyBuilder.build());
        new Thread(UpdateRun).start();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    Runnable UpdateRun = new Runnable() {
        @Override
        public void run() {
            HttpUtils hu = new HttpUtils();
            hu.download(downloadLink, updateFile.getPath(), new RequestCallBack<File>() {
                // 下载成功
                @Override
                public void onSuccess(ResponseInfo<File> arg0) {
                    mHandler.sendEmptyMessage(UPDATE_WIN);
                }

                // 下载失败
                @Override
                public void onFailure(HttpException arg0, String arg1) {
                    LogUtil.d(TAG, "获取更新错误	" + arg0.toString());
                    arg0.printStackTrace();
                    mHandler.sendEmptyMessage(UPDATE_FAIL);
                }

                // 下载中...
                @Override
                public void onLoading(long total, long current, boolean isUploading) {

                    int i = (int) (total / 100);
                    int j = (int) (current / i);
                    LogUtil.d(TAG, "total:" + total);
                    LogUtil.d(TAG, "current:" + current);

                    if (currentJ == 0 || j - currentJ >= 1) {
                        LogUtil.d(TAG, "j:" + j);

                        /***
                         * 在这里我们用自定的view来显示Notification
                         */
                        notification.contentView = new RemoteViews(
                                getPackageName(), R.layout.notification_item);
                        notification.contentView.setTextViewText(
                                R.id.notificationTitle, "正在下载");
                        notification.contentView.setProgressBar(
                                R.id.notificationProgress, 100, j, false);

                        notificationManager.notify(1, notification);
                    }
                    currentJ = j;
                    super.onLoading(total, current, isUploading);
                }
            });
        }
    };
}
