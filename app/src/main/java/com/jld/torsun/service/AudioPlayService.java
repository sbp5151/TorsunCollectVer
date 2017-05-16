package com.jld.torsun.service;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;
import android.text.TextUtils;

import com.jld.torsun.MyApplication;
import com.jld.torsun.R;
import com.jld.torsun.activity.fragment.FragmentMainVoice;
import com.jld.torsun.modle.ActionConstats;
import com.jld.torsun.util.Constats;
import com.jld.torsun.util.LogUtil;
import com.jld.torsun.util.MyHttpUtil;
import com.jld.torsun.util.ToastUtil;
import com.jld.torsun.util.UserInfo;
import com.jld.torsun.util.WifiUtil;

import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.RandomAccessFile;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;

public class AudioPlayService extends Service {
    private MyApplication mApplication;
    private MyBinder mMyBinder;
    private SharedPreferences sp;
    public Activity activity;
    private MulticastLock wifiLock;
    private boolean screenOff;
    private AudioTrack mAudio;
    private Vibrator vibrator;
    public static final int TIME_OUT = 1;

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case TIME_OUT:
                    if (!mApplication.isNotData && handler != null) {
                        mApplication.isNotData = true;
                        LogUtil.d(TAG, "mApplication.isNotData2:" + mApplication.isNotData);
                        if (!sp.getBoolean(UserInfo.ISLOAD, false)) {
                            vibrator.vibrate(2000);
                        }
                        handler.sendEmptyMessage(FragmentMainVoice.WAVE_ISRUN);
                    }
                    break;
            }

        }
    };

    @Override
    public void onCreate() {
        LogUtil.d(TAG, "onCreate");
        mApplication = (MyApplication) getApplication();
        mMyBinder = new MyBinder();
        sp = this.getSharedPreferences(Constats.SHARE_KEY, Context.MODE_PRIVATE);
        /**
         * 等待wifi连接线程
         */
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!MyHttpUtil.isWifiConnect) {
                    try {
                        LogUtil.d(TAG, "等待WiFi连接");
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    LogUtil.d(TAG, "等待WiFi连接");
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                LogUtil.d(TAG, "WiFi已经连接");
                startAudio();
            }
        }).start();
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        LogUtil.d(TAG, "onBind");
        if (mMyBinder == null) {
            mMyBinder = new MyBinder();
        }
        return mMyBinder;
    }

    @Override
    public void unbindService(ServiceConnection conn) {
        LogUtil.d(TAG, "unbindService");
        super.unbindService(conn);
    }

    @Override
    public void onDestroy() {
        stopThread = true;
        stopPlayer = true;
        LogUtil.d(TAG, "onDestroy");
    }

    public class MyBinder extends Binder {
        // 对外提供开启服务方法
        public void startThread() {
            LogUtil.d(TAG, "startThread");
            stopThread = false;
            stopPlayer = false;
            new Thread(mRunnable).start();
//            new Thread(playRun).start();
        }

        public void stopThread() {
            LogUtil.d(TAG, "stopThread:" + stopThread);
            stopThread = true;
            stopPlayer = true;
        }

        // 对外提供关闭服务方法
        public void stopAudio() {
            LogUtil.d(TAG, "stopPlayer:" + stopPlayer);
            if (!stopPlayer)
                stopPlayer = true;
        }

        public void startAudio() {
            LogUtil.d(TAG, "startAudio:" + stopPlayer);
            if (stopPlayer)
                stopPlayer = false;
        }

        // 获取丢包数量
        public void setHandler(Handler handler, Activity activity) {
            AudioPlayService.this.handler = handler;
            AudioPlayService.this.activity = activity;
        }
    }

    /**
     * 接收音频线程
     */
    private static String TAG = "AudioPlayService";
    private static final int MULTICAST_PORT = 5350;
    private static final String GROUP_IP = "224.0.0.251";
    private int last_num = 0;
    private byte[] inBuff = new byte[528];
    private byte[] nullBuff = new byte[528];
    private byte[] readInBuff = new byte[528];
    private DatagramPacket inPacket = new DatagramPacket(inBuff, inBuff.length);
    private MulticastSocket ms = null;
    private InetAddress group = null;
    private int mpk_num = 0;
    private File file1, file2;
    public RandomAccessFile wfile1;
    /**
     * 统计丢包数量
     */
    private Handler handler;// 用于传输丢包数量

    private boolean stopPlayer = true;
    public boolean stopThread = false;
    private int numBuff = 999;
    static String filePath1;
    public RandomAccessFile afile1;

    private void initSocket() {
        try {
            // 获取连接到WiFi的所有地址
            group = InetAddress.getByName(GROUP_IP);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        try {
            // 创建多路广播socket
            ms = new MulticastSocket(MULTICAST_PORT);
            ms.setSoTimeout(1000);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            // 将获取到的地址加入多路广播
            ms.joinGroup(group);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            // 禁用多播数据报的本地回送
            ms.setLoopbackMode(true);
            ms.setReceiveBufferSize(1024 * 16);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    private void initFile() {
        //SDK不可用
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            ToastUtil.showToast(this, getResources().getString(R.string.sd_disabled), 3000);
            return;
        }
        //创建缓存区
        filePath1 = Environment.getExternalStorageDirectory()
                .getAbsolutePath() + "/mysound1.wav";
        file1 = new File(filePath1);
        if (file1.exists()) {
            file1.delete();
        }
        try {
            file1.createNewFile();
            wfile1 = new RandomAccessFile(file1, "rw");
            afile1 = new RandomAccessFile(file1, "rw");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startAudio() {
        initFile();
        //初始化socket，开始播放
        initSocket();
        mMyBinder.startThread();
    }

    private void initAudio() {
        int minBufSize = AudioTrack.getMinBufferSize(16000,//每秒8k个点
                AudioFormat.CHANNEL_OUT_MONO,//单声道
                AudioFormat.ENCODING_PCM_16BIT);//每个点16比特-----两个字节
        mAudio = new AudioTrack(AudioManager.STREAM_MUSIC, 16000,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, minBufSize,
                AudioTrack.MODE_STREAM);
        mAudio.play();
    }

    private Runnable mRunnable = new Runnable() {
        public int electric_num;

        public void run() {
            initAudio();
            while (!stopThread) {
                LogUtil.d(TAG, "stopPlayer:" + stopPlayer);
                if (!stopPlayer) {
                    //清空数组
                    Arrays.fill(inBuff, (byte) 0);
                    // 读取语音包
                    try {
                        ms.receive(inPacket);
                        if (mApplication.isNotData) {
                            LogUtil.d(TAG, "重新接收到语音包");
                            mApplication.isNotData = false;
                            if (!sp.getBoolean(UserInfo.ISLOAD, false)) {
                                vibrator.vibrate(2000);
                            }
                            handler.sendEmptyMessage(FragmentMainVoice.WAVE_ISRUN);
                        }
                    } catch (InterruptedIOException ie) {
                        LogUtil.d(TAG, "接收超时:" + ie.toString());
                        LogUtil.d(TAG, "mApplication.isNotData1:" + mApplication.isNotData);
                        try {
                            if (!mApplication.isNotData && handler != null && !TextUtils.isEmpty(mApplication.wifiName) && mApplication.wifiName.equals(WifiUtil.getWifiName(AudioPlayService.this))) {
                                mApplication.isNotData = true;
                                LogUtil.d(TAG, "mApplication.isNotData2:" + mApplication.isNotData);
                                if (!sp.getBoolean(UserInfo.ISLOAD, false)) {
                                    vibrator.vibrate(2000);
                                }
                                handler.sendEmptyMessage(FragmentMainVoice.WAVE_ISRUN);
                            }
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        LogUtil.d(TAG, "continue");
                        continue;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mpk_num = unsigned4BytesToInt(inBuff, 4);// 解析语音包已发数量
                    LogUtil.d(TAG, "mpk_num----------:" + mpk_num);

//                    LogUtil.d(TAG, " mpk_num:" + mpk_num);
//                    mdata_len = unsigned4BytesToInt(inBuff, 8);
//                    LogUtil.d(TAG, " mdata_len:" + mdata_len);
                    //      丢包统计
//                    if (mpk_num != last_num && (mpk_num - last_num) > 1 && last_num != 0 && !stopPlayer) {
//                        int lostNum = mpk_num - last_num - 1;
//                        LogUtil.d(TAG, "lostNum:" + lostNum);
//
//                        if (handler != null) {
//                            Message message = handler.obtainMessage();
//                            message.what = FragmentMainVoice.GET_LOST_NUMB;
//                            message.arg1 = lostNum;
//                            handler.sendMessage(message);
//                        }
//                        LogUtil.d(TAG, "丢包统计last_num:" + last_num + ":" + lostNum);
//                    }
//                    if (stopPlayer)
//                        last_num = mpk_num;
                    //播放
//                    isPlayer = false;
                    if ((mpk_num != last_num || (last_num == 0))) {
                        last_num = mpk_num;
//                            wfile1.write(inBuff);
                        mAudio.write(inBuff, 16, 512);

                        electric_num = unsigned4BytesToInt(inBuff, 12);// 电池电量检测
                        sendBatteryInfo(electric_num);//电池电量计算
                    }
//                    //如果网络发生变化便获取wifi名称以改变动画状态
                    if (mApplication.netChange) {
                        mApplication.wifiName = WifiUtil.getWifiName(AudioPlayService.this);
                        mApplication.netChange = false;
                        LogUtil.d(TAG, " 获取wifi名称:" + mApplication.wifiName);
                    }
                } else {
                    try {
                        LogUtil.d(TAG, "Thread.sleep(200):");
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            ms.close();
            stopPlayer = true;
            stopThread = true;
            stopSelf();//关闭服务
        }
    };
    Runnable playRun = new Runnable() {
        @Override
        public void run() {

            while (!stopThread) {
                if (!stopPlayer) {
                    try {
                        if (afile1.length() > 528) {
                            afile1.read(readInBuff);
                        } else {
                            Thread.sleep(10);
                        }
                        mAudio.write(readInBuff, 16, 512);
                        Arrays.fill(readInBuff, (byte) 0);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    file1.delete();
                    initFile();
                    try {
                        LogUtil.d(TAG, "Thread.sleep(100):");
                        Thread.sleep(180);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            LogUtil.d(TAG, " file1:" + file1.length());
            boolean isDelete = file1.delete();
            LogUtil.d(TAG, "isDelete:" + isDelete);
            mAudio.stop();
            mAudio.release();
        }
    };

    public static int unsigned4BytesToInt(byte[] buf, int pos) {
        int firstByte = 0;
        int secondByte = 0;
        int thirdByte = 0;
        int fourthByte = 0;
        int index = pos;
        firstByte = (0x000000FF & ((int) buf[index]));
        secondByte = (0x000000FF & ((int) buf[index + 1]));
        thirdByte = (0x000000FF & ((int) buf[index + 2]));
        fourthByte = (0x000000FF & ((int) buf[index + 3]));
//        index = index + 4;
        return ((int) (firstByte << 24 | secondByte << 16 | thirdByte << 8 | fourthByte));
    }

    private void sendBatteryInfo(int num) {
        if (!(numBuff == num) && num <= 100) {
            if (sp.getBoolean(UserInfo.ISLOAD, false) && activity != null && MyHttpUtil.isConnTorsun(activity)) {
                numBuff = num;
                LogUtil.d(TAG, "--flag--num--接收到的电池信息:" + num + "%");
                LogUtil.d(TAG, "--flag--numBuff--接收到的电池信息:" + numBuff + "%");
                //这里采用广播的方式来通知改变电池信息的显示
                Intent intent = new Intent(ActionConstats.BATTERY_INFO_CHANGE);
                intent.putExtra(BatteryInfoReceiver.BATTERYINFO_FLAG, num);
                this.sendBroadcast(intent);
            }
        }
    }
}
