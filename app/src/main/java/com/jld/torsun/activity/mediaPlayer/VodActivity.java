package com.jld.torsun.activity.mediaPlayer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.jld.torsun.R;
import com.jld.torsun.util.LogUtil;
import com.jld.torsun.util.TimeUtil;
import com.jld.torsun.util.ToastUtil;
import com.jld.torsun.util.imagecache.MyImageLoader;
import com.umeng.analytics.MobclickAgent;

import java.io.IOException;

public class VodActivity extends Activity implements Handler.Callback, OnClickListener, OnSeekBarChangeListener {

    private static final String TAG = "VodActivity";
    private SurfaceView mVideoSurface;
    private ProgressBar mProgressBar;
    private MediaPlayer mMediaPlayer;
    private SurfaceHolder mHolder;
    private DisplayMetrics mMetrics;

    private boolean isVideoPlayed = false;
    private boolean isPlayErr = true;

    private static final int SHOW_PROGRESS = 0;
    private static final int ON_LOADED = 1;
    private static final int HIDE_OVERLAY = 2;
    private Handler mHandler;

    //    //当前播放的位置
    private int seekToNum = 0;

    private String movieName, mUrl;//电影名称,电影地址
    private String adURL, adPic;      //广告视频、图片地址
    private int adTime; //广告时间

    private View topView, controllerView;
    private View backView;
    private TextView tvTitle, tvTime, tvLength;
    private ImageButton ibLock, ibFarward, ibBackward, ibPlay, ibSize;
    private SeekBar sbVideo;

    private Context context;
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
        LogUtil.d(TAG, "onCreate");
        if (savedInstanceState != null) {
            seekToNum = savedInstanceState.getInt("Position");
            isVideoPlayed = savedInstanceState.getBoolean("startPlay");
            adTime = savedInstanceState.getInt("adTime");
            LogUtil.d(TAG, "onCreate---savedInstanceState:" + seekToNum + "  isVideoPlayed:" + isVideoPlayed);
        }
        getIntentData();
        if (TextUtils.isEmpty(mUrl)) {
            ToastUtil.showToast(this, "error:no url in intent!", 3000);
            return;
        }
        setContentView(R.layout.activity_vod);
        imageLoader = MyImageLoader.getInstance(this);
        mHandler = new Handler(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        context = this;
        // 获取屏幕长宽
        mMetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
        initView();
        sfCallback();
        initPlayer();
        if (isVideoPlayed) {
            startPlay();
        } else {
            if (initAD()) {
                startAD();
            } else {
                startPlay();
            }
        }
    }

    private void startPlay() {
        LogUtil.d(TAG, "startPlay");
        playVideo(Uri.parse(mUrl));
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
        adSurfaceView.getHolder().addCallback(new Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (mADMediaPlayer != null)
                    mADMediaPlayer.setDisplay(holder);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });
        mADMediaPlayer.reset();
        mADMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mADMediaPlayer.setOnCompletionListener(new OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (mADMediaPlayer != null)
                    mADMediaPlayer.release();
            }
        });
        try {
            mADMediaPlayer.setDataSource(adURL);
            mADMediaPlayer.prepare();
//            mADMediaPlayer.seekTo(0);
            mADMediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("播放视频"); //统计页面(仅有Activity的应用中SDK自动调用，不需要单独写。"SplashScreen"为页面名称，可自定义)
        MobclickAgent.onResume(this);          //统计时长
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("播放视频"); //统计页面(仅有Activity的应用中SDK自动调用，不需要单独写。"SplashScreen"为页面名称，可自定义)
        MobclickAgent.onPause(this);          //统计时长
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            ibPlay.setBackgroundResource(R.drawable.ic_play);
        }
    }

    private void initView() {
        mVideoSurface = (SurfaceView) findViewById(R.id.vod_sfv);
        mProgressBar = (ProgressBar) findViewById(R.id.vod_progressbar);

        topView = findViewById(R.id.movie_play_top_view);
        controllerView = findViewById(R.id.vod_my_controller_view);

        tvTitle = (TextView) topView.findViewById(R.id.movie_play_top_name);
        backView = topView.findViewById(R.id.movie_play_top_back);
        tvTitle.setText(movieName);
        backView.setOnClickListener(this);

        tvTime = (TextView) controllerView.findViewById(R.id.tv_time);
        tvLength = (TextView) controllerView.findViewById(R.id.tv_length);
        sbVideo = (SeekBar) controllerView.findViewById(R.id.sb_video);
        sbVideo.setOnSeekBarChangeListener(this);
        ibLock = (ImageButton) controllerView.findViewById(R.id.ib_lock);
        ibLock.setOnClickListener(this);
        ibBackward = (ImageButton) controllerView.findViewById(R.id.ib_backward);
        ibBackward.setOnClickListener(this);
        ibPlay = (ImageButton) controllerView.findViewById(R.id.ib_play);
        ibPlay.setOnClickListener(this);
        ibFarward = (ImageButton) controllerView.findViewById(R.id.ib_forward);
        ibFarward.setOnClickListener(this);
        ibSize = (ImageButton) controllerView.findViewById(R.id.ib_size);
        ibSize.setOnClickListener(this);

        adView = findViewById(R.id.il_vod_ad_view);
        adSurfaceView = (SurfaceView) adView.findViewById(R.id.ad_sfv_video);
        adPicNIV = (NetworkImageView) adView.findViewById(R.id.ad_niv_pic);
        adTimeView = (TextView) adView.findViewById(R.id.ad_tv_time);
        adView.setVisibility(View.GONE);
    }

    private void sfCallback() {
        // 给SurfaceView添加CallBack监听 
        mHolder = mVideoSurface.getHolder();
        mHolder.addCallback(callback);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    private void initPlayer() {
        LogUtil.d(TAG, "initPlayer");
        // 下面开始实例化MediaPlayer对象
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
        }
        mMediaPlayer.reset();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnCompletionListener(onCompletionListener);
        mMediaPlayer.setOnErrorListener(onErrorListener);
        mMediaPlayer.setOnInfoListener(onInfoListener);
        mMediaPlayer.setOnPreparedListener(onPreparedListener);
        mMediaPlayer.setOnSeekCompleteListener(onSeekCompleteListener);
        mMediaPlayer.setOnVideoSizeChangedListener(onVideoSizeChangedListener);
        mMediaPlayer.setOnBufferingUpdateListener(onBufferingUpdateListener);
        //mMediaPlayer.setLooping(true);
    }

    private void playVideo(Uri uri) {
        LogUtil.d(TAG, "playVideo");
        if (mMediaPlayer == null)
            return;
        try {
//            isVideoPlayed = true;
            // 指定需要播放文件的路径，初始化MediaPlayer
            // mMediaPlayer.setDataSource("http://192.168.0.250:81/bus/download/201501201127019302.rmvb");
            mMediaPlayer.setDataSource(this, uri);
//            mMediaPlayer.setDataSource(uri.toString());
            // 在指定了Url后，我们就可以使用prepare或者prepareAsync来准备播放了
            mMediaPlayer.prepareAsync();
        } catch (Exception e) {
            LogUtil.d(TAG, "playVideo---Exception" + e.toString());
            e.printStackTrace();
        }
    }

    private void seekTo(int pos) {
        if (pos <= 0)
            return;
        if (mMediaPlayer != null) {
            mMediaPlayer.seekTo(pos);
            seekToNum = pos;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (controllerView == null)
                return false;

            if (controllerView.getVisibility() != View.VISIBLE) {
                showOverlay();
            } else {
                hideOverlay();
            }
        }
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        LogUtil.d(TAG, "onSaveInstanceState");
//       if (adTime > 0){
//           outState.putInt("adTime",adTime);
//       }else if (adPicTime > 0){
//           outState.putInt("adTime",adPicTime);
//       }else {
//           outState.putInt("adTime",0);
//       }
        if (adTime > 0) {
            outState.putInt("adTime", adTime);
        } else {
            outState.putInt("adTime", 0);
        }
        if (mMediaPlayer == null)
            return;
        LogUtil.d(TAG, "onSaveInstanceState  getCurrentPosition:" + mMediaPlayer.getCurrentPosition() + "  isVideoPlayed:" + isVideoPlayed);
        if (isVideoPlayed) {
            int num = mMediaPlayer.getCurrentPosition();
            if (num > 0) {
                seekToNum = num;
                outState.putInt("Position", num);
            } else {
                outState.putInt("Position", seekToNum);
            }
            outState.putBoolean("startPlay", isVideoPlayed);
        }
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            ibPlay.setBackgroundResource(R.drawable.ic_play);
        }

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        adTime = savedInstanceState.getInt("adTime");
        seekToNum = savedInstanceState.getInt("Position");
        isVideoPlayed = savedInstanceState.getBoolean("startPlay");
        LogUtil.d(TAG, "onRestoreInstanceState:" + seekToNum + "  isVideoPlayed:" + isVideoPlayed);
        seekTo(seekToNum);
    }

    //释放播放的资源
    private void releasePlayer() {
        LogUtil.d(TAG, "releasePlayer");
        if (mMediaPlayer != null) {
            //mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    @Override
    protected void onDestroy() {
        releasePlayer();
        super.onDestroy();
        LogUtil.d(TAG, "onDestroy");
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

    private void showOverlay() {
        topView.setVisibility(View.VISIBLE);
        controllerView.setVisibility(View.VISIBLE);
        mHandler.sendEmptyMessage(SHOW_PROGRESS);
        mHandler.removeMessages(HIDE_OVERLAY);
        mHandler.sendEmptyMessageDelayed(HIDE_OVERLAY, 5 * 1000);
    }

    private void hideOverlay() {
        topView.setVisibility(View.GONE);
        controllerView.setVisibility(View.GONE);
        mHandler.removeMessages(SHOW_PROGRESS);
    }

    private int setOverlayProgress() {
        if (mMediaPlayer == null) {
            return 0;
        }
        int time = mMediaPlayer.getCurrentPosition();
        int length = mMediaPlayer.getDuration();
//        boolean isSeekable = mMediaPlayer.canSeekable() && length > 0;
//        ibFarward.setVisibility(isSeekable ? View.VISIBLE : View.GONE);
//        ibBackward.setVisibility(isSeekable ? View.VISIBLE : View.GONE);
        ibFarward.setVisibility(View.VISIBLE);
        ibBackward.setVisibility(View.VISIBLE);
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

    private void showLoading() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideLoading() {
        mProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ib_lock:
                break;
            case R.id.ib_forward:
                if (mMediaPlayer == null) {
                    break;
                }
                seek(10000);
                break;
            case R.id.ib_play:
                if (mMediaPlayer == null) {
                    break;
                }
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.pause();
                    ibPlay.setBackgroundResource(R.drawable.ic_play);
                } else {
                    mMediaPlayer.start();
                    ibPlay.setBackgroundResource(R.drawable.ic_pause);
                }
                break;
            case R.id.ib_backward:
                if (mMediaPlayer == null) {
                    break;
                }
                seek(-10000);
                break;
            case R.id.ib_size:
                break;
            case R.id.movie_play_top_back:
                finish();
                break;
            default:
                break;
        }
    }

    private void seek(int time) {

        if (mMediaPlayer == null || mMediaPlayer.getDuration() <= 0) {
            return;
        }
        int position = mMediaPlayer.getCurrentPosition() + time;
        if (position < 0)
            position = 0;
        mMediaPlayer.seekTo(position);
        seekToNum = position;
    }

    //    int showProgress;
    //该方法拖动进度条进度改变的时候调用
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
//            showProgress = progress;
            mMediaPlayer.seekTo(progress);
            seekToNum = progress;
            setOverlayProgress();
        }
    }

    //该方法拖动进度条开始拖动的时候调用
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    //该方法拖动进度条停止拖动的时候调用
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    /****************************************************
     * ***********以下是实现播放器的各种监听方法*********
     ***************************************************/
    private Callback callback = new Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            LogUtil.d(TAG, "surfaceCreated");
            if (mMediaPlayer == null)
                return;
            // 当SurfaceView中的Surface被创建的时候被调用
            // 在这里我们指定MediaPlayer在当前的Surface中进行播放
            LogUtil.d(TAG, "surfaceCreated---setDisplay");
            mMediaPlayer.setDisplay(holder);
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            // 当Surface尺寸等参数改变时触发
            LogUtil.d(TAG, "surfaceChanged");
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            LogUtil.d(TAG, "surfaceDestroyed");
        }
    };

    private OnCompletionListener onCompletionListener = new OnCompletionListener() {

        //             if (null != mList && playIndex < mList.size() -1 && null != mMediaPlayer){
//             playIndex++;
//             releasePlayer();
//             initPlayer();
//             playVideo(Uri.parse(mList.get(playIndex)));
//             }
//             当MediaPlayer播放完成后触发
//             if (mUrl != null && !isVideoPlayed){
//             mProgressBar.setVisibility(View.VISIBLE);
//             releasePlayer();
//             initPlayer();
//             playVideo(Uri.parse(mUrl));
//             isVideoPlayed = true;
//             }else {
//             pause();
//             }
        @Override
        public void onCompletion(MediaPlayer mp) {
            LogUtil.d(TAG, "onCompletion--播放完成后触发");
            if (mMediaPlayer != null) {
                mMediaPlayer.release();
            }
        }
    };

    private OnErrorListener onErrorListener = new OnErrorListener() {

        //        MediaPlayer.MEDIA_ERROR_UNKNOWN;
        //MEDIA_ERROR_UNKNOWN  what :1
        //what :1/ extra :-2147483648 url地址访问不了
        //what :-38/ extra :0 能正常播放刚点进来时
        //what :1/ extra :-38 在正在播放时退出关闭
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            if (mProgressBar.getVisibility() == View.VISIBLE) {
                mProgressBar.setVisibility(View.GONE);
            }
            LogUtil.i(TAG, "Play Error---what :" + what + "/ extra :" + extra);
            if (what == -38) {
                return true;
            }
            if (isPlayErr) {
                if (!(seekToNum > 0)) {
                    if (mVideoSurface != null && mVideoSurface.getWindowToken() != null) {
                        int message = what == MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK ? R.string.movie_play_error_text_invalid_progressive_playback : R.string.movie_play_error_text_unknown;
                        new AlertDialog.Builder(context).setTitle(R.string.movie_play_error_title).setMessage(message)
                                .setPositiveButton(R.string.movie_play_error_button, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        onCompletionListener.onCompletion(mMediaPlayer);
                                        if (mMediaPlayer != null){
                                            mMediaPlayer.release();
                                            mMediaPlayer = null;
                                        }
                                    }
                                }).setCancelable(false).show();
                    }
                    return true;
                }
            }
            return false;
        }
    };

    private OnInfoListener onInfoListener = new OnInfoListener() {
        // 当一些特定信息出现或者警告时触发
        @Override
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
            switch (what) {
                case MediaPlayer.MEDIA_INFO_BUFFERING_START://缓存时,显示加载
                    if (null != mProgressBar) {
                        mProgressBar.setVisibility(View.VISIBLE);
                    }
                    break;
                case MediaPlayer.MEDIA_INFO_BUFFERING_END://缓存完成，继续播放
                    if (null != mProgressBar) {
                        mProgressBar.setVisibility(View.INVISIBLE);
                    }
                    break;
            }
            return false;
        }
    };

    /**
     * 缓存监听
     */
    private OnBufferingUpdateListener onBufferingUpdateListener = new OnBufferingUpdateListener() {

        /**缓存的进度值
         *  percent
         *播放的进度值
         *  100 * mp.getCurrentPosition() / mp.getDuration();
         * */
        @Override
        public void onBufferingUpdate(MediaPlayer mp, int percent) {
            sbVideo.setSecondaryProgress(percent);
        }
    };

    private OnPreparedListener onPreparedListener = new OnPreparedListener() {

        /**当prepare完成后，该方法触发，在这里我们播放视频首先取得video的宽和高*/
        @Override
        public void onPrepared(MediaPlayer mp) {
            LogUtil.d(TAG, "onPrepared");

            int vWidth = mMediaPlayer.getVideoWidth();
            int vHeight = mMediaPlayer.getVideoHeight();
            if (vWidth > mMetrics.widthPixels || vHeight > mMetrics.heightPixels) {
                // 如果video的宽或者高超出了当前屏幕的大小，则要进行缩放
                float wRatio = (float) vWidth / (float) mMetrics.widthPixels;
                float hRatio = (float) vHeight / (float) mMetrics.heightPixels;
                // 选择大的一个进行缩放
                float ratio = Math.max(wRatio, hRatio);

                vWidth = (int) Math.ceil((float) vWidth / ratio);
                vHeight = (int) Math.ceil((float) vHeight / ratio);

                // 设置surfaceView的布局参数
                mVideoSurface.setLayoutParams(new FrameLayout.LayoutParams(vWidth,
                        vHeight));
            }
            if (vWidth * vHeight > 0) {
                isVideoPlayed = true;
                isPlayErr = false;
                // 然后开始播放视频
                if (seekToNum > 0) {
                    mMediaPlayer.seekTo(seekToNum);
                    mMediaPlayer.start();
                } else {
                    LogUtil.d(TAG, "onPrepared--start");
                    mMediaPlayer.start();
                }
            }
            if (mProgressBar.getVisibility() == View.VISIBLE) {
                mProgressBar.setVisibility(View.GONE);
            }
        }
    };

    private OnSeekCompleteListener onSeekCompleteListener = new OnSeekCompleteListener() {
        // seek操作完成后触发
        @Override
        public void onSeekComplete(MediaPlayer mp) {
            LogUtil.i(TAG, "onSeekComplete:" + seekToNum);
        }
    };

    private OnVideoSizeChangedListener onVideoSizeChangedListener = new OnVideoSizeChangedListener() {
        // 当video大小改变时触发
        // 这个方法在设置player的source后至少触发一次
        @Override
        public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
            LogUtil.i(TAG, "onVideoSizeChanged");
            if (width > 0 && height > 0) {
                mp.start();
            }
        }
    };
}
