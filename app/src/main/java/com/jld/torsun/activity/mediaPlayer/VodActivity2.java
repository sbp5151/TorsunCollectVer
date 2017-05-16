package com.jld.torsun.activity.mediaPlayer;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.VideoView;

import com.jld.torsun.R;
import com.jld.torsun.util.LogUtil;
import com.jld.torsun.util.MyHttpUtil;

import org.apache.http.conn.util.InetAddressUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class VodActivity2 extends Activity {

    //private String url = "/storage_int/0/Movies/TorsunMovie/movie1/prog_index.m3u8";
    //private String url = "/mnt/shell/emulated/0/Movies/TorsunMovie/movie1/prog_index.m3u8";
    //private String url = "/mnt/sdcard/Movies/TorsunMovie/movie1/prog_index.m3u8";
    //private String url = "/mnt/storage/emulated/0/Movies/TorsunMovie/movie1/prog_index.m3u8";
    private String url;
    private VideoView videoView;
    private MediaController mediaController;

    private static final String TAG = "VodActivity";

    private String filePath;
    private String movieName;

    private String localhostIp = "127.0.0.1";
    private int localPort = 23456;


    private AudioManager mAudioManager;
    /**
     * 最大声音
     */
    private int mMaxVolume;
    /**
     * 当前声音
     */
    private int mVolume = -1;
    /**
     * 当前亮度
     */
    private float mBrightness = -1f;
    private GestureDetector mGestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vod2);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        url = getIntent().getStringExtra("url");
        movieName = getIntent().getStringExtra("movieName");
        videoView = (VideoView) findViewById(R.id.vod_video_view);
        mediaController = new MediaController(this);
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        mGestureDetector = new GestureDetector(this, new MyGestureListener());
        if (!TextUtils.isEmpty(url)) {
            videoView.setVideoURI(Uri.parse(url));
            //videoView.setVideoPath(url);
            videoView.setMediaController(mediaController);
            mediaController.setMediaPlayer(videoView);
            videoView.requestFocus();

        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == MyHttpUtil.DOWNFILE) {
                LogUtil.d(TAG, "MyHttpUtil.DOWNFILE");
                filePath = msg.getData().getString("url");
                String urlString = "http://" + localhostIp + ":" + localPort + filePath;
                LogUtil.d(TAG, "path:" + urlString);

            } else if (msg.what == MyHttpUtil.DOWN_ADDR_ERR) {
                LogUtil.d(TAG, "MyHttpUtil.DOWN_ADDR_ERR");
            }
        }
    };

    public String getLocalIpAddress() {
        try {
            // 遍历网络接口
            Enumeration<NetworkInterface> infos = NetworkInterface
                    .getNetworkInterfaces();
            while (infos.hasMoreElements()) {
                // 获取网络接口
                NetworkInterface niFace = infos.nextElement();
                Enumeration<InetAddress> enumIpAddr = niFace.getInetAddresses();
                while (enumIpAddr.hasMoreElements()) {
                    InetAddress mInetAddress = enumIpAddr.nextElement();
                    // 所获取的网络地址不是127.0.0.1时返回得得到的IP
                    if (!mInetAddress.isLoopbackAddress() && InetAddressUtils.isIPv4Address(mInetAddress
                            .getHostAddress())) {
                        return mInetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException e) {

        }
        return null;
    }

    private Runnable savaM3u8File = new Runnable() {
        @Override
        public void run() {
            LogUtil.d(TAG, "savaM3u8File");
            MyHttpUtil.parseStringFromUrl(url, movieName, mHandler);
        }
    };

    private Runnable openServer = new Runnable() {
        @Override
        public void run() {
            toOpenHttpServer();
        }
    };

    private NanoHTTPD nanoHTTPD;

    private void toOpenHttpServer() {
        LogUtil.d(TAG, "toOpenHttpServer");
        if (null == nanoHTTPD) {
            localhostIp = getLocalIpAddress();
            nanoHTTPD = new MyNanoHTTPD(localhostIp, localPort);
        }
        try {
            nanoHTTPD.start();
        } catch (IOException e) {
            LogUtil.d(TAG, "toOpenHttpServer--IOException" + e.toString());
            e.printStackTrace();
        }
    }

    class MyNanoHTTPD extends NanoHTTPD {

        public MyNanoHTTPD(int port) {
            super(port);
        }

        public MyNanoHTTPD(String hostName, int port) {
            super(hostName, port);
        }

        public Response serve(IHTTPSession session) {
            Method method = session.getMethod();
            FileInputStream fis = null;
            LogUtil.e(TAG, "Method:" + method.toString());
            if (Method.GET.equals(method)) {
                //get方式
                String queryParams = session.getQueryParameterString();

                LogUtil.e(TAG, "session.getUri():" + session.getUri());
                try {
                    fis = new FileInputStream(session.getUri());
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    LogUtil.e(TAG, "FileNotFoundException:");
                }
                //LogUtil.e(TAG,"params:"+queryParams);
            } else if (Method.POST.equals(method)) {
                //post方式
            }
            //return super.serve(session);
            return new Response(Response.Status.OK, "audio/mp4", fis);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mGestureDetector.onTouchEvent(event))
            return true;

        // 处理手势结束
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:
                endGesture();
                break;
        }

        return super.onTouchEvent(event);
    }

    /** 手势结束 */
    private void endGesture() {
        mVolume = -1;
        mBrightness = -1f;

        // 隐藏
        mDismissHandler.removeMessages(0);
        mDismissHandler.sendEmptyMessageDelayed(0, 500);
    }

    private class MyGestureListener extends GestureDetector.SimpleOnGestureListener {

        /**
         * 双击
         */
        @Override
        public boolean onDoubleTap(MotionEvent e) {
//            if (mLayout == VideoView.VIDEO_LAYOUT_ZOOM)
//                mLayout = VideoView.VIDEO_LAYOUT_ORIGIN;
//            else
//                mLayout++;
//            if (mVideoView != null)
//                mVideoView.setVideoLayout(mLayout, 0);
            return true;
        }

        /**
         * 滑动
         */
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY) {
            float mOldX = e1.getX(), mOldY = e1.getY();
            int y = (int) e2.getRawY();
            Display disp = getWindowManager().getDefaultDisplay();
            int windowWidth = disp.getWidth();
            int windowHeight = disp.getHeight();

            if (mOldX > windowWidth * 4.0 / 5) {// 右边滑动
                onVolumeSlide((mOldY - y) / windowHeight);
            } else if (mOldX < windowWidth / 5.0) {// 左边滑动
                onBrightnessSlide((mOldY - y) / windowHeight);
            }
            return super.onScroll(e1, e2, distanceX, distanceY);
        }
    }

    /**
     * 定时隐藏
     */
    private Handler mDismissHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
//            mVolumeBrightnessLayout.setVisibility(View.GONE);
        }
    };

    /**
     * 滑动改变声音大小
     *
     * @param percent
     */
    private void onVolumeSlide(float percent) {
        if (mVolume == -1) {
//            mVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            if (mVolume < 0)
                mVolume = 0;

            // 显示
//            mOperationBg.setImageResource(R.drawable.video_volumn_bg);
//            mVolumeBrightnessLayout.setVisibility(View.VISIBLE);
        }

        int index = (int) (percent * mMaxVolume) + mVolume;
        if (index > mMaxVolume)
            index = mMaxVolume;
        else if (index < 0)
            index = 0;

        // 变更声音
//        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, index, 0);

        // 变更进度条
//        ViewGroup.LayoutParams lp = mOperationPercent.getLayoutParams();
//        lp.width = findViewById(R.id.operation_full).getLayoutParams().width
//                * index / mMaxVolume;
//        mOperationPercent.setLayoutParams(lp);
    }

    /**
     * 滑动改变亮度
     *
     * @param percent
     */
    private void onBrightnessSlide(float percent) {
        if (mBrightness < 0) {
            mBrightness = getWindow().getAttributes().screenBrightness;
            if (mBrightness <= 0.00f)
                mBrightness = 0.50f;
            if (mBrightness < 0.01f)
                mBrightness = 0.01f;

            // 显示
//            mOperationBg.setImageResource(R.drawable.video_brightness_bg);
//            mVolumeBrightnessLayout.setVisibility(View.VISIBLE);
        }
        WindowManager.LayoutParams lpa = getWindow().getAttributes();
        lpa.screenBrightness = mBrightness + percent;
        if (lpa.screenBrightness > 1.0f)
            lpa.screenBrightness = 1.0f;
        else if (lpa.screenBrightness < 0.01f)
            lpa.screenBrightness = 0.01f;
        getWindow().setAttributes(lpa);

//        ViewGroup.LayoutParams lp = mOperationPercent.getLayoutParams();
//        lp.width = (int) (findViewById(R.id.operation_full).getLayoutParams().width * lpa.screenBrightness);
//        mOperationPercent.setLayoutParams(lp);
    }
}
