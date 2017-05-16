package com.jld.torsun.barcode;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonRequest;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.jld.torsun.MyApplication;
import com.jld.torsun.R;
import com.jld.torsun.activity.BaseActivity;
import com.jld.torsun.activity.MainFragment;
import com.jld.torsun.activity.WebActivity;
import com.jld.torsun.barcode.camera.CameraManager;
import com.jld.torsun.barcode.decoding.CaptureActivityHandler;
import com.jld.torsun.barcode.decoding.InactivityTimer;
import com.jld.torsun.barcode.view.ViewfinderView;
import com.jld.torsun.config.Config;
import com.jld.torsun.db.MemberDao;
import com.jld.torsun.db.TeamDao;
import com.jld.torsun.http.VolleyJsonUtil;
import com.jld.torsun.modle.ActionConstats;
import com.jld.torsun.util.AndroidUtil;
import com.jld.torsun.util.Constats;
import com.jld.torsun.util.DensityUtil;
import com.jld.torsun.util.LogUtil;
import com.jld.torsun.util.MD5Util;
import com.jld.torsun.util.MyHttpUtil;
import com.jld.torsun.util.ToastUtil;
import com.jld.torsun.util.UserInfo;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * 二维码扫描界面
 *
 * 晶凌达科技有限公司所有， 受到法律的保护，任何公司或个人，未经授权不得擅自拷贝。
 *
 * @creator 单柏平 <br/>
 * @create-time 2015-12-7 下午2:02:52
 */
public class MipcaActivityCapture extends BaseActivity implements Callback{

	private static final String TAG = "MipcaActivityCapture";

	private CaptureActivityHandler handler;
	private ViewfinderView viewfinderView;
	private boolean hasSurface;
	private Vector<BarcodeFormat> decodeFormats;
	private String characterSet;
	private InactivityTimer inactivityTimer;
	private MediaPlayer mediaPlayer;
	private boolean playBeep;
	private static final float BEEP_VOLUME = 1.00f;
	private boolean vibrate;
	private SharedPreferences sp;

	private MemberDao mDao;

	private GestureDetector mGestureDetector;

	private MyApplication myApplication;

	private RequestQueue mRequestQueue;

	private View titleView;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//透明状态栏
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
		//透明导航栏
		//getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
		setContentView(R.layout.activity_capture);
		// ViewUtil.addTopView(getApplicationContext(), this,
		// R.string.scan_card);

		mDao = MemberDao.getInstance(this);

		titleView=findViewById(R.id.include1);

		myApplication=(MyApplication)getApplication();
		mRequestQueue=myApplication.getRequestQueue();

		RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.rl_capture);
		relativeLayout.setOnTouchListener(this);
		relativeLayout.setLongClickable(true);
		setFlingView(relativeLayout);

		CameraManager.init(getApplication());
		viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);

		ImageView mButtonBack = (ImageView) findViewById(R.id.button_back);
		sp = getSharedPreferences(Constats.SHARE_KEY, Context.MODE_PRIVATE);

		mButtonBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				MipcaActivityCapture.this.finish();
				overridePendingTransition(R.anim.left_in, R.anim.right_out);
			}
		});
		hasSurface = false;
		inactivityTimer = new InactivityTimer(this);
	}

	private boolean isfristFocus = true;

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (isfristFocus && hasFocus){
			isfristFocus=false;
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT){
				float height= AndroidUtil.getStatusHeight(this);
				LogUtil.d(TAG,"--------------状态栏的高度为:" + height);

				int viewHeight = DensityUtil.px2dip(this, (216f - height * 2));
				LogUtil.d(TAG,"--------------viewHeight的高度:" + viewHeight);
				RelativeLayout.LayoutParams params =(RelativeLayout.LayoutParams)titleView.getLayoutParams();
				params.height=viewHeight;
				titleView.setLayoutParams(params);
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
		SurfaceHolder surfaceHolder = surfaceView.getHolder();
		if (hasSurface) {
			initCamera(surfaceHolder);
		} else {
			surfaceHolder.addCallback(this);
			surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}
		decodeFormats = null;
		characterSet = null;

		playBeep = true;
		AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
		if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
			playBeep = false;
		}
		initBeepSound();
		vibrate = true;
		MobclickAgent.onPageStart("二维码扫描"); //统计页面(仅有Activity的应用中SDK自动调用，不需要单独写。"SplashScreen"为页面名称，可自定义)
		MobclickAgent.onResume(this);          //统计时长
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}
		CameraManager.get().closeDriver();
		MobclickAgent.onPageStart("二维码扫描"); //统计页面(仅有Activity的应用中SDK自动调用，不需要单独写。"SplashScreen"为页面名称，可自定义)
		MobclickAgent.onPause(this);          //统计时长
	}

	@Override
	protected void onDestroy() {
		inactivityTimer.shutdown();
		super.onDestroy();
		isfristFocus=true;
	}

	//http://dyb.torsun.com.cn/dl2.html?torsun=v1120160512
	private String codeString = "http://dyb.torsun.com.cn/dl2.html?torsun=v";
	/**
	 * 扫描完成返回结果
	 *
	 * @param result
	 * @param barcode
	 */
	public void handleDecode(Result result, Bitmap barcode) {
		inactivityTimer.onActivity();
		playBeepSoundAndVibrate();
		String resultString = result.getText();
		LogUtil.d(TAG, "resultString:" + resultString);
		// String keyString = MD5Util.getMD5("torsun"+Constats.S_KEY);
		// String getKey = resultString.split("V")[0];
		if (resultString.equals("")) {
			Toast.makeText(MipcaActivityCapture.this, "Scan failed!",
					Toast.LENGTH_SHORT).show();
		} else if (resultString.contains("{\"torsun\":\"v")) {
			guiderCode();
		}else if (resultString.startsWith(codeString)){
			guiderCode();
		}else if (resultString.startsWith("http://")
				|| resultString.startsWith("https://")) {
			Intent intent = new Intent("android.intent.action.VIEW");
			Uri content_url = Uri.parse(resultString);
			intent.setData(content_url);
			startActivity(intent);
		} else {
			ToastUtil.showToast(this, "认证不成功", 1000);
		}
        MipcaActivityCapture.this.finish();
	}

	//导游认证
	private void guiderCode() {
		LogUtil.d(TAG, "------导游认证-----");
		createTime();
		// 获取最新的本地团ID
		if (!MyApplication.isfristSavaLastTeamID) {
            TeamDao teamDao = TeamDao.getInstance(this);
            String lastTeamid = teamDao.selectLastTeamid(MainFragment.MYSELFID);
            if (!TextUtils.isEmpty(lastTeamid)) {
                sp.edit().putString(UserInfo.LAST_TEAM_ID, lastTeamid).apply();
                LogUtil.d(TAG, "-----FragmentTrouManger--当前本地团id:" + lastTeamid);
            }
        }
		if (isConnTucson()){
            new Thread(upGuiderInfo).start();
            Intent intent = new Intent(this, WebActivity.class);
            intent.putExtra(Config.TYPE, Config.TYPE_GUIDER);
            intent.putExtra("url", Constats.COMMIT_GUIDER_INFO_URL);
            startActivity(intent);
        }else {
            ToastUtil.showToast(this, R.string.capture_no_conn, 3000);
        }
	}

	private void createTime(){
        long time = System.currentTimeMillis();
        time = time / 1000;
        LogUtil.d(TAG, "-----createTime-1----");
        sp.edit().putLong(UserInfo.TIME, time).commit();
        sp.edit().putBoolean(UserInfo.ISLOAD, true).commit();
		sp.edit().putString(UserInfo.LAST_SERVICE_TEAM_ID,"").commit();
        LogUtil.d(TAG, "-----createTime2:" + time);
        Intent intent = new Intent();
        intent.setAction(ActionConstats.GET_LOAD_POWER);
        Intent intent2 = new Intent();
        intent2.putExtra("CreatTime",time);
        intent2.setAction(ActionConstats.STRCHANGE);
        //intent2.setAction(MulticastServer.GET_LOAD_POWER);
        MipcaActivityCapture.this.sendBroadcast(intent);
        MipcaActivityCapture.this.sendBroadcast(intent2);
        LogUtil.d(TAG, "-----sendBroadcast-----");

    }

	String wifiName = "";

	//判断是否连上了设备
	private  boolean isConnTucson(){
		wifiName = MyHttpUtil.getWifiName1(this);
		String wifi_name = myApplication.wifiName;
		if (TextUtils.isEmpty(wifiName)){
			LogUtil.d(TAG,"----没有连wifi-----");
			return false;
		}
		if (TextUtils.isEmpty(wifi_name)){
			LogUtil.d(TAG,"----没有连上了torsun-----");
			return false;
		}
		if (wifiName.equals(wifi_name)){
			//连上了设备wifi
			LogUtil.d(TAG, "---------wifi名：" + wifiName);
			return true;
		}
		return false;
	}

	//判断是否能正常上网
	private boolean isNetconn(){
		return MyHttpUtil.ping();
	}

	/**
	 * 判断是否连上tucsonWiFi并且能正常上网
	 *
	 * @return
	 */
	private boolean isTucson() {
		wifiName = MyHttpUtil.getWifiName1(this);
		String wifi_name = myApplication.wifiName;
		if (TextUtils.isEmpty(wifiName)){
			LogUtil.d(TAG,"----没有连wifi-----");
			return false;
		}
		if (TextUtils.isEmpty(wifi_name)){
			LogUtil.d(TAG,"----没有连上了torsun-----");
			return false;
		}
		if (wifiName.equals(wifi_name)) {
			LogUtil.d(TAG,"---------wifi名：" + wifiName);
			if (MyHttpUtil.ping()){
				LogUtil.d(TAG,"----连上了torsun并且能够上网-----");
				return true;
			}
			LogUtil.d(TAG,"----连上了torsun但是不能够上网----");
		}else {
			LogUtil.d(TAG,"----没有连上了torsun-----");
		}
		return false;
	}

	/**上传当前导游信息到服务器*/
	private Runnable upGuiderInfo = new Runnable() {
		@Override
		public void run() {
			if (isNetconn()){
				LogUtil.d(TAG,"----上传导游信息到服务器-----");
				final String userid = MainFragment.MYSELFID;
				final String myWifiName =new String(wifiName.replace("\"",""));
				final String sign = MD5Util.getMD5(Constats.S_KEY + userid + myWifiName);
				final Map<String,String> params = new HashMap<String,String>();
				params.put("userid",userid);
				params.put("name",myWifiName);
				params.put("time","");
				params.put("sign",sign);
				final String url = Constats.HTTP_URL+ Constats.GUIDE_LOGIN_INFO_URL;
				JsonRequest<JSONObject> jsonRequest = VolleyJsonUtil
						.createJsonObjectRequest(Request.Method.POST, url, params,
								new Response.Listener<JSONObject>() {
									@Override
									public void onResponse(JSONObject response) {
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
		}
	};

	private void initCamera(SurfaceHolder surfaceHolder) {
		try {
			CameraManager.get().openDriver(surfaceHolder);
//		} catch (IOException ioe) {
//			return;
//		} catch (RuntimeException e) {
//			return;
		}catch (Exception e) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.FragmentSet_logout_title);
			builder.setMessage(R.string.dialog_msg);
			builder.setCancelable(false);
			builder.setPositiveButton(R.string.FragmentSet_logout_exit, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					finish();
				}
			});
			builder.create().show();
			return;
		}
		if (handler == null) {
			handler = new CaptureActivityHandler(this, decodeFormats,
					characterSet);
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (!hasSurface) {
			hasSurface = true;
			initCamera(holder);
		}

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		hasSurface = false;

	}

	public ViewfinderView getViewfinderView() {
		return viewfinderView;
	}

	public Handler getHandler() {
		return handler;
	}

	public void drawViewfinder() {
		viewfinderView.drawViewfinder();

	}

	private void initBeepSound() {
		if (playBeep && mediaPlayer == null) {
			// The volume on STREAM_SYSTEM is not adjustable, and users found it
			// too loud,
			// so we now play on the music stream.
			setVolumeControlStream(AudioManager.STREAM_MUSIC);
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mediaPlayer.setOnCompletionListener(beepListener);

			AssetFileDescriptor file = getResources().openRawResourceFd(
					R.raw.qrcode);
//			AssetFileDescriptor file = getResources().openRawResourceFd(
//					R.raw.beep);
			try {
				mediaPlayer.setDataSource(file.getFileDescriptor(),
						file.getStartOffset(), file.getLength());
				file.close();
				mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
				mediaPlayer.prepare();
			} catch (IOException e) {
				mediaPlayer = null;
			}
		}
	}

	private static final long VIBRATE_DURATION = 200L;

	private void playBeepSoundAndVibrate() {
		if (playBeep && mediaPlayer != null) {
			mediaPlayer.start();
		}
		if (vibrate) {
			Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
			vibrator.vibrate(VIBRATE_DURATION);
		}
	}

	/**
	 * When the beep has finished playing, rewind to queue up another one.
	 */
	private final OnCompletionListener beepListener = new OnCompletionListener() {
		public void onCompletion(MediaPlayer mediaPlayer) {
			mediaPlayer.seekTo(0);
		}
	};

}