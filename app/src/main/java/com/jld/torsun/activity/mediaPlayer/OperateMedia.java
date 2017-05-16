package com.jld.torsun.activity.mediaPlayer;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by lz on 2016/6/29.
 */
public class OperateMedia implements OnBufferingUpdateListener,OnCompletionListener,OnErrorListener {

    private int position = 0;// 保存播放的的位置
    private SurfaceView surfaceView;// surfaceVie对象
    private Context context;// 上下文对象
    private MediaPlayer mediaPlayer;// mediaplayer对象
    private int currentPlay = 0;// 保存当前正在播放的视频 0表示还没有记录
    private boolean justBack = false;// 是否刚才另外一个界面跳回，fasle 表示不是

    public OperateMedia(Context context, MediaPlayer mediaPlayer,
                        SurfaceView surfaceView) {
        this.context = context;
        this.mediaPlayer = mediaPlayer;
        this.surfaceView = surfaceView;
        // mediaPlayer的设置
        this.mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        this.mediaPlayer.setOnBufferingUpdateListener(this);
        this.mediaPlayer.setOnCompletionListener(this);
        // surfaceView的设置
        this.surfaceView.getHolder().setKeepScreenOn(true);
        this.surfaceView.getHolder().setType(
                SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        this.surfaceView.getHolder().addCallback(new SurfaceCallback()); // surfaceView的回调

    }

    // 设置播放不同的视频
    public void loadSrc(int num) {
        // 如果在次播放的是不同的视频，那么就将position初始化,并且reset,重新设置视频源
//        if (num != currentPlay) {
//            position = 0;
//            try {
//                currentPlay = num;
//                mediaPlayer.reset();
//                AssetFileDescriptor fd = null;
//                switch (num) {
//                    case 0:
//                        fd = context.getAssets().openFd("a.mp4");
//                        break;
//                    case 1:
//                        fd = context.getAssets().openFd("b.mp4");
//                        break;
//                    case 2:
//                        fd = context.getAssets().openFd("c.mp4");
//                        break;
//                    case 3:
//                        fd = context.getAssets().openFd("d.mp4");
//                        break;
//                }
//                mediaPlayer.setDataSource(fd.getFileDescriptor(),
//                        fd.getStartOffset(), fd.getLength());
//                play();
//            } catch (IllegalArgumentException e) {
//                e.printStackTrace();
//            } catch (IllegalStateException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            return;// 中断当前程序
//        }
        // 如果点击的是同一个视频。那么就不用reset了
        if (num == currentPlay) {
            if (justBack) {
                play();
                return;
            } else {//如果不是从另一个activity切换回来，那么，就直接设置到0，开始播放
                mediaPlayer.seekTo(0);
                mediaPlayer.start();
                mediaPlayer.setDisplay(surfaceView.getHolder());// 设置屏幕
            }
        }

    }

    // 播放视频
    public void play() {
        mediaPlayer.prepareAsync();
        mediaPlayer.setOnPreparedListener(new OnPreparedListener() {
            public void onPrepared(MediaPlayer mp) {
                if (position > 0) {
                    mediaPlayer.seekTo(position);
                    if (justBack) {
                        justBack = false;
                        position = 0;
                    }
                }
                mediaPlayer.start();
            }
        });
        mediaPlayer.setDisplay(surfaceView.getHolder());// 设置屏幕
    }

    // 视频播放完成的回调方法
    public void onCompletion(MediaPlayer mp) throws IllegalStateException {
//        if (currentPlay == 0) {
////            MediaVideo.justPlay = false;
////
//        }
    }

    //
    public void onBufferingUpdate(MediaPlayer mp, int percent)
            throws IllegalStateException {
    }

    // SurfaceView的callBack
    private class SurfaceCallback implements SurfaceHolder.Callback {
        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                   int height) {
        }

        public void surfaceCreated(SurfaceHolder holder) {
            if (position > 0) {
                loadSrc(currentPlay);
            }
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            // 界面销毁，即将跳转到另外一个界面
            if (mediaPlayer.isPlaying()) {
                justBack = true;
                position = mediaPlayer.getCurrentPosition();
                mediaPlayer.stop();
            }
        }

    }

    public boolean onError(MediaPlayer mp, int what, int extra) {

        return false;
    }
}
