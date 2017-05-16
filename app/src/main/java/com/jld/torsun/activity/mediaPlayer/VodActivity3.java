package com.jld.torsun.activity.mediaPlayer;

import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.MediaController.MediaPlayerControl;
import android.widget.ProgressBar;

import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.jld.torsun.R;
import com.jld.torsun.util.LogUtil;

import java.util.Timer;
import java.util.TimerTask;

public class VodActivity3 extends Activity  {

    private static final String TAG = "VodActivity";
    private SurfaceView mVideoSurface;
    private ProgressBar mProgressBar;
    private MediaPlayer mMediaPlayer;
    private SurfaceHolder mHolder;
    private DisplayMetrics mMetrics;

    private SeekBar seekBar;
    /** 视频的宽高 */
    private int vWidth, vHeight;

    private String mIntentData = null;

    private Timer mTimer = new Timer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vod);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //mIntentData = getIntent().getStringExtra("url");
        mIntentData ="http://192.168.1.1/movie/movie3/move3.m3u8";
        // 获取屏幕长宽
        mMetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
        initView();
    }

    private void initView() {
        LogUtil.d(TAG, "initView");
//        seekBar = (SeekBar) findViewById(R.id.sb_vod);
        seekBar.setVisibility(View.VISIBLE);
        seekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
        seekBar.postDelayed(showRunnable, 3000);
        mVideoSurface = (SurfaceView)findViewById(R.id.vod_sfv);
        mProgressBar = (ProgressBar)findViewById(R.id.vod_progressbar);
        mProgressBar.setVisibility(View.VISIBLE);
        // 给SurfaceView添加CallBack监听 
        mHolder = mVideoSurface.getHolder();
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                LogUtil.d(TAG, "surfaceCreated");
                initPlayer();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                LogUtil.d(TAG, "surfaceChanged");
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                LogUtil.d(TAG, "surfaceDestroyed");
            }
        });
        mTimer.schedule(mTimerTask, 0, 1000);
    }

    private void initPlayer() {
        LogUtil.d(TAG, "initPlayer");
        // 下面开始实例化MediaPlayer对象
        if (mMediaPlayer == null){
            mMediaPlayer = new MediaPlayer();
        }
        try{
            mMediaPlayer.reset();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setDisplay(mHolder);
            mMediaPlayer.setDataSource(this, Uri.parse(mIntentData));
            mMediaPlayer.setLooping(true);
            mMediaPlayer.setOnBufferingUpdateListener(onBufferingUpdateListener);
            mMediaPlayer.setOnInfoListener(onInfoListener);
            mMediaPlayer.setOnErrorListener(onErrorListener);
            mMediaPlayer.setOnPreparedListener(onPreparedListener);
            mMediaPlayer.setOnSeekCompleteListener(onSeekCompleteListener);
            mMediaPlayer.prepareAsync();
        }catch (Exception e){

        }
    }


    private  OnSeekCompleteListener onSeekCompleteListener = new OnSeekCompleteListener() {

        //seekto完成后实际定位播放时调用该方法
        @Override
        public void onSeekComplete(MediaPlayer mp) {

        }
    };

    //当onPrepared时才是mediaplayer准备完毕可以进行播放了
    private OnPreparedListener onPreparedListener = new OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            LogUtil.d(TAG, "onPrepared");
            mp.start();
            mProgressBar.setVisibility(View.INVISIBLE);
            LogUtil.d(TAG, "mMediaPlayer.getDuration():" + mp.getDuration());
            LogUtil.d(TAG, "mMediaPlayer.getCurrentPosition():" + mp.getCurrentPosition());
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlayer();
    }

    private void releasePlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    int showProgress;
    private OnSeekBarChangeListener onSeekBarChangeListener = new OnSeekBarChangeListener() {

        //该方法拖动进度条进度改变的时候调用
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            final int indexTime = progress * mMediaPlayer.getDuration()/seekBar.getMax();
            if (indexTime > 0){
                showProgress =indexTime;
            }
            LogUtil.d(TAG, "进度改变:"+showProgress);
        }

        //该方法拖动进度条开始拖动的时候调用
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            // seekTo()的参数是相对与影片时间的数字，而不是与seekBar.getMax()相对的数字
            //mMediaPlayer.seekTo(showProgress);
            LogUtil.d(TAG, "开始拖动getCurrentPosition:" + mMediaPlayer.getCurrentPosition());
            LogUtil.d(TAG, "开始拖动showProgress:"+showProgress);
        }

        //该方法拖动进度条停止拖动的时候调用
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            LogUtil.d(TAG, "停止拖动:"+showProgress);
            mMediaPlayer.seekTo(showProgress);
        }
    };

    /*******************************************************
     * 通过定时器和Handler来更新进度条
     ******************************************************/
    private TimerTask mTimerTask = new TimerTask() {
        @Override
        public void run() {
            if (mMediaPlayer == null)
                return;
            if (mMediaPlayer.isPlaying() && seekBar.isPressed() == false) {
                handleProgress.sendEmptyMessage(0);
            }
        }
    };

    Handler handleProgress = new Handler() {
        public void handleMessage(Message msg) {
            int position = mMediaPlayer.getCurrentPosition();
            int duration = mMediaPlayer.getDuration();

            if (duration > 0 && position > 0) {
                long pos = seekBar.getMax() * position / duration;
                seekBar.setProgress((int) pos);
            }
        };
    };
    // *****************************************************


    //等待缓存监听
    private OnInfoListener onInfoListener = new OnInfoListener() {
        @Override
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
            LogUtil.d(TAG, "onInfo:what:"+what+"extra:"+extra);
            switch (what) {
                case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                    LogUtil.d(TAG, "MEDIA_INFO_BUFFERING_START");
                    if (null != mProgressBar) {
                        mProgressBar.setVisibility(View.VISIBLE);
                    }
                    //mMediaPlayer.pause();
                    break;
                case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                    //缓存完成，继续播放
                    LogUtil.d(TAG, "缓存完成，继续播放");
                    if (null != mProgressBar) {
                        mProgressBar.setVisibility(View.INVISIBLE);
                    }
                    break;
                case MediaPlayer.MEDIA_INFO_NOT_SEEKABLE:// 媒体不能正确定位，意味着它可能是一个在线流
                    LogUtil.d(TAG, "媒体不能正确定位，意味着它可能是一个在线流");
                    break;
                case MediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING://当无法播放视频时，可能是将要播放视频，但是视频太复杂
                    LogUtil.d(TAG, "当无法播放视频时，可能是将要播放视频，但是视频太复杂");
                    break;
                case MediaPlayer.MEDIA_INFO_UNKNOWN:
                    LogUtil.d(TAG, "未知错误");
                    break;
            }
            return false;
        }
    };

    //错误监听
    private OnErrorListener onErrorListener = new OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            LogUtil.d(TAG, "setOnErrorListener:what:" + what+"extra:"+extra);
            //mMediaPlayer.reset();
            return false;
        }
    };

    //加载
    private OnBufferingUpdateListener onBufferingUpdateListener = new OnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(MediaPlayer mp, int percent) {
            LogUtil.d(TAG, "onBufferingUpdate");
            seekBar.setSecondaryProgress(percent);
            int currentProgress = seekBar.getMax() * mp.getCurrentPosition() / mp.getDuration();
            LogUtil.d(TAG, currentProgress + "% play"+ percent + "% buffer");
        }
    };

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        LogUtil.d(TAG, "onTouchEvent");
        if (!seekBar.isShown()){
            seekBar.setVisibility(View.VISIBLE);
            seekBar.postDelayed(showRunnable,3000);
        }

        return false;
    }

    private Runnable showRunnable = new Runnable() {
        @Override
        public void run() {
            seekBar.setVisibility(View.INVISIBLE);
        }
    };
}
