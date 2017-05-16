package com.jld.torsun.activity.mediaPlayer;

import android.app.Activity;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.jld.torsun.R;
import com.jld.torsun.util.LogUtil;
import com.jld.torsun.util.TimeUtil;
import com.jld.torsun.util.ToastUtil;
import com.jld.torsun.util.imagecache.MyImageLoader;
import com.jld.torsun.view.PlayerView;
import com.jld.torsun.view.PlayerView.OnChangeListener;
import com.squareup.picasso.Picasso;

import java.io.IOException;

public class PlayerActivity extends Activity implements OnChangeListener, OnClickListener, OnSeekBarChangeListener, Callback {

    private static final String TAG = "PlayerActivity";

    private static final int SHOW_PROGRESS = 0;
    private static final int ON_LOADED = 1;
    private static final int HIDE_OVERLAY = 2;

    private View rlLoading;
    private PlayerView mPlayerView;
    private TextView tvTitle, tvBuffer, tvTime, tvLength;
    private View backTitle;
    private SeekBar sbVideo;
    private ImageButton ibLock, ibFarward, ibBackward, ibPlay, ibSize;
    private View llOverlay, rlOverlayTitle;
    private Handler mHandler;

    private String movieName, mUrl;//电影名称,电影地址
    private String adURL, adPic;      //广告视频、图片地址
    private int adTime; //广告时间

    private boolean isVodAD = false;
    private View adView;
    private SurfaceView adSurfaceView;
    private NetworkImageView adPicNIV;
    private TextView adTimeView;
    private MediaPlayer mADMediaPlayer;

    private TimeCount timeCount;
    private ImageLoader imageLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getIntentData();
        if (TextUtils.isEmpty(mUrl)) {
            ToastUtil.showToast(this, "error:no url in intent!", Toast.LENGTH_SHORT);
            return;
        }
        setContentView(R.layout.activity_player);
        imageLoader = MyImageLoader.getInstance(this);
        mHandler = new Handler(this);
        initView();
        if (initAD()) {
            startAD();
        } else {
            startPlay();
        }
    }

    private void startPlay() {
        LogUtil.d(TAG, "startPlay");
        //使用步骤
        //第一步 ：通过findViewById或者new PlayerView()得到mPlayerView对象
        //mPlayerView= new PlayerView(PlayerActivity.this);
        mPlayerView = (PlayerView) findViewById(R.id.pv_video);
        //第二步：设置参数，毫秒为单位
        mPlayerView.setNetWorkCache(30000);
        //第三步:初始化播放器
        mPlayerView.initPlayer(mUrl);
        //第四步:设置事件监听，监听缓冲进度等
        mPlayerView.setOnChangeListener(this);
        //第五步：开始播放
        mPlayerView.start();
        showLoading();
        hideOverlay();
    }

    private void startAD() {
        LogUtil.d(TAG, "startAD");
        adView.setVisibility(View.VISIBLE);
        if (isVodAD) {
            adSurfaceView.setVisibility(View.VISIBLE);
            adPicNIV.setVisibility(View.GONE);
            adTimeView.setVisibility(View.VISIBLE);
            showVideoAD();

        } else {
            adPicNIV.setDefaultImageResId(R.mipmap.default_image);
            adPicNIV.setErrorImageResId(R.mipmap.default_image);
            adPicNIV.setImageUrl(adPic, imageLoader);
            adSurfaceView.setVisibility(View.GONE);
            adPicNIV.setVisibility(View.VISIBLE);
            adTimeView.setVisibility(View.VISIBLE);
        }
        if (timeCount != null) {
            timeCount.start();
        }
    }

    private void getIntentData() {
        movieName = getIntent().getStringExtra("movieName");
        mUrl = getIntent().getStringExtra("url");

        adURL = getIntent().getStringExtra("ad_url");
        adPic = getIntent().getStringExtra("ad_pic");

        String t = getIntent().getStringExtra("ad_time");
        if (TextUtils.isEmpty(t))
            adTime = 0;
        else
            adTime = Integer.parseInt(t);


        LogUtil.d(TAG, "movieName:" + movieName);
        LogUtil.d(TAG, "mUrl:" + mUrl);
        LogUtil.d(TAG, "adURL:" + adURL);
        LogUtil.d(TAG, "adPic:" + adPic);
        LogUtil.d(TAG, "adTime:" + adTime);
    }

    private boolean initAD() {
        if (adTime > 0){
            if (!TextUtils.isEmpty(adURL)){
                isVodAD = true;
            }else if (!TextUtils.isEmpty(adPic)){
                isVodAD = false;
            }else {
                return false;
            }
            adTimeView.setText(adTime + getResources().getString(R.string.regies_second_text2));
            timeCount = new TimeCount(adTime * 1000, 1000);
            return true;
        }
        return false;
    }

    private void showVideoAD() {
        if (mADMediaPlayer == null) {
            mADMediaPlayer = new MediaPlayer();
        }
        adSurfaceView.getHolder().addCallback(callback);
        mADMediaPlayer.reset();
        mADMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mADMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (mADMediaPlayer != null)
                    mADMediaPlayer.release();
            }
        });
        try {
            mADMediaPlayer.setDataSource(adURL);
            mADMediaPlayer.prepare();
            mADMediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isPlayAD = false;
    private boolean isPauseAD = false;
    private SurfaceHolder.Callback callback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            if (mADMediaPlayer != null && !isPlayAD){
                mADMediaPlayer.setDisplay(holder);
                isPlayAD = true;
                if (isPauseAD){
                    mADMediaPlayer.start();
                    isPauseAD = false;
                }
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            if (mADMediaPlayer != null && mADMediaPlayer.isPlaying()){
                mADMediaPlayer.pause();
                isPauseAD = true;
                isPlayAD = false;
            }
        }
    };

    private void hideAD() {
        adView.setVisibility(View.GONE);
        if (isVodAD) {
            if (mADMediaPlayer != null) {
                mADMediaPlayer.release();
                mADMediaPlayer = null;
            }
        }
        startPlay();
    }

    /**
     * 计时器
     */
    private class TimeCount extends CountDownTimer {
        public TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);// 参数依次为总时长,和计时的时间间隔
        }

        @Override
        public void onFinish() {// 计时完毕时触发
            hideAD();
        }

        @Override
        public void onTick(long millisUntilFinished) {// 计时过程显示
            adTimeView.setText((--adTime) + getResources().getString(R.string.regies_second_text2));
//            if (isVodAD){
//                adTimeView.setText((--adTime) + getResources().getString(R.string.regies_second_text2));
//            }else {
//                adTimeView.setText((--adPicTime) + getResources().getString(R.string.regies_second_text2));
//            }
        }
    }

    private void initView() {
        llOverlay = findViewById(R.id.ll_overlay);
        rlOverlayTitle = findViewById(R.id.rl_title);

        tvTitle = (TextView) rlOverlayTitle.findViewById(R.id.movie_play_top_name);
        backTitle = rlOverlayTitle.findViewById(R.id.movie_play_top_back);
        backTitle.setOnClickListener(this);
        tvTime = (TextView) llOverlay.findViewById(R.id.tv_time);
        tvLength = (TextView) llOverlay.findViewById(R.id.tv_length);
        sbVideo = (SeekBar) llOverlay.findViewById(R.id.sb_video);
        sbVideo.setOnSeekBarChangeListener(this);
        ibLock = (ImageButton) llOverlay.findViewById(R.id.ib_lock);
        ibLock.setOnClickListener(this);
        ibBackward = (ImageButton) llOverlay.findViewById(R.id.ib_backward);
        ibBackward.setOnClickListener(this);
        ibPlay = (ImageButton) llOverlay.findViewById(R.id.ib_play);
        ibPlay.setOnClickListener(this);
        ibFarward = (ImageButton) llOverlay.findViewById(R.id.ib_forward);
        ibFarward.setOnClickListener(this);
        ibSize = (ImageButton) llOverlay.findViewById(R.id.ib_size);
        ibSize.setOnClickListener(this);

        rlLoading = findViewById(R.id.rl_loading);
        tvBuffer = (TextView) findViewById(R.id.tv_buffer);

        //init view
        tvTitle.setText(movieName);

        adView = findViewById(R.id.il_play_ad_view);
        adSurfaceView = (SurfaceView) adView.findViewById(R.id.ad_sfv_video);
        adPicNIV = (NetworkImageView) adView.findViewById(R.id.ad_niv_pic);
        adTimeView = (TextView) adView.findViewById(R.id.ad_tv_time);
        adView.setVisibility(View.GONE);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (llOverlay.getVisibility() != View.VISIBLE) {
                showOverlay();
            } else {
                hideOverlay();
            }
        }
        return false;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (mPlayerView != null)
            mPlayerView.changeSurfaceSize();
        super.onConfigurationChanged(newConfig);
    }

    private long pauseTime;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        LogUtil.d(TAG, "onSaveInstanceState");
        if (mPlayerView != null) {
            long time = mPlayerView.getTime();
            LogUtil.d(TAG, "onSaveInstanceState----time : " + time);
            if (time > 0) {
                pauseTime = time;
                outState.putLong("Position", time);
            } else {
                outState.putLong("Position", pauseTime);
            }
            if (mPlayerView.isPlaying()) {
                mPlayerView.pause();
                ibPlay.setBackgroundResource(R.drawable.ic_play);
            }
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        LogUtil.d(TAG, "onRestoreInstanceState");
        pauseTime = savedInstanceState.getLong("Position");
        if (mPlayerView == null)
            return;
        mPlayerView.seekTo(pauseTime);
        if (mPlayerView.isPlaying()) {
            mPlayerView.pause();
        }
    }

    @Override
    public void onPause() {
        if (mPlayerView != null && mPlayerView.isPlaying()) {
            hideOverlay();
            mPlayerView.pause();
            ibPlay.setBackgroundResource(R.drawable.ic_play);
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBufferChanged(float buffer) {
        if (buffer >= 100) {
            hideLoading();
        } else {
            showLoading();
        }
        tvBuffer.setText("正在缓冲中..." + (int) buffer + "%");
    }

    private void showLoading() {
        rlLoading.setVisibility(View.VISIBLE);

    }

    private void hideLoading() {
        rlLoading.setVisibility(View.GONE);
    }

    @Override
    public void onLoadComplet() {
        mHandler.sendEmptyMessage(ON_LOADED);
    }

    @Override
    public void onError() {
        Toast.makeText(getApplicationContext(), "Player Error Occur！", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void onEnd() {
        finish();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ib_lock:
                break;
            case R.id.ib_forward:
                if (mPlayerView == null) {
                    break;
                }
                mPlayerView.seek(10000);
                break;
            case R.id.ib_play:
                if (mPlayerView == null) {
                    break;
                }
                if (mPlayerView.isPlaying()) {
                    mPlayerView.pause();
                    ibPlay.setBackgroundResource(R.drawable.ic_play);
                } else {
                    mPlayerView.play();
                    ibPlay.setBackgroundResource(R.drawable.ic_pause);
                }
                break;
            case R.id.ib_backward:
                if (mPlayerView == null) {
                    break;
                }
                mPlayerView.seek(-10000);
                break;
            case R.id.ib_size:
                break;
            case R.id.movie_play_top_back:
                if (mPlayerView != null) {
                    LogUtil.d(TAG, "getPlayerState() :" + mPlayerView.getPlayerState());
                    if (mPlayerView.getPlayerState() != 1) {
                        mPlayerView.stop();
                    }
                }
                finish();
                break;
            default:
                break;
        }
    }

    private void showOverlay() {
        rlOverlayTitle.setVisibility(View.VISIBLE);
        llOverlay.setVisibility(View.VISIBLE);
        mHandler.sendEmptyMessage(SHOW_PROGRESS);
        mHandler.removeMessages(HIDE_OVERLAY);
        mHandler.sendEmptyMessageDelayed(HIDE_OVERLAY, 5 * 1000);
    }

    private void hideOverlay() {
        rlOverlayTitle.setVisibility(View.GONE);
        llOverlay.setVisibility(View.GONE);
        mHandler.removeMessages(SHOW_PROGRESS);
    }

    private int setOverlayProgress() {
        if (mPlayerView == null) {
            return 0;
        }
        int time = (int) mPlayerView.getTime();
        int length = (int) mPlayerView.getLength();
        boolean isSeekable = mPlayerView.canSeekable() && length > 0;
        ibFarward.setVisibility(isSeekable ? View.VISIBLE : View.GONE);
        ibBackward.setVisibility(isSeekable ? View.VISIBLE : View.GONE);
        sbVideo.setMax(length);
        sbVideo.setProgress(time);
        if (time >= 0) {
            tvTime.setText(TimeUtil.millisToString(time, false));
        }
        if (length >= 0) {
            tvLength.setText(TimeUtil.millisToString(length, false));
        }
        return time;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser && mPlayerView.canSeekable()) {
            mPlayerView.setTime(progress);
            setOverlayProgress();
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case SHOW_PROGRESS:
                setOverlayProgress();
                mHandler.sendEmptyMessageDelayed(SHOW_PROGRESS, 20);
                break;
            case ON_LOADED:
                showOverlay();
                hideLoading();
                break;
            case HIDE_OVERLAY:
                hideOverlay();
                break;
            default:
                break;
        }
        return false;
    }

    /**
     * getPlayerState():
     * play 3
     * pause 4
     * unknown 1  还在加载地址中//该状态下执行stop时会出异常
     * stop -1
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            if (mPlayerView != null) {
                LogUtil.d(TAG, "getPlayerState() :" + mPlayerView.getPlayerState());
                if (mPlayerView.getPlayerState() != 1) {
                    mPlayerView.stop();
                }
            }
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }
}
