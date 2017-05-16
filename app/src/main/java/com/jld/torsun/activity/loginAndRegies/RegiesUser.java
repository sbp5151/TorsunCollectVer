package com.jld.torsun.activity.loginAndRegies;

import com.jld.torsun.R;
import com.jld.torsun.activity.BaseManageActivity;
import com.jld.torsun.activity.MainFragment;
import com.jld.torsun.activity.WebActivity;
import com.jld.torsun.util.Constats;
import com.jld.torsun.util.LogUtil;
import com.jld.torsun.util.UserInfo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Locale;

/**
 * 注册填写用户名页面
 * 
 * 晶凌达科技有限公司所有， 受到法律的保护，任何公司或个人，未经授权不得擅自拷贝。
 * 
 * @creator 单柏平 <br/>
 * @create-time 2016-1-4 上午10:08:25
 */
public class RegiesUser extends BaseManageActivity implements OnClickListener,OnTouchListener,OnGestureListener {

	/** 昵称 */
	private EditText et_regiest_nike;
	/** 真实姓名 */
	private EditText et_regiest_name;
	/** 下一步 */
	private Button bt_regiest_next;
	/** 已有账户登录 */
	private TextView tv_regiest_have;
	/** 用户协议 */
	private TextView tv_regiest_protocol;
	/** 是否激活下一步 */
	private Boolean threadStop = true;
	/** 昵称 */
	private String nike = "";

	private ImageView backImg;

	private static final String TAG = "RegiesUser";

	private SharedPreferences sp;

	private boolean isToLogin = false;

	private GestureDetector mGestureDetector;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_regies_user);
		sp = getSharedPreferences(Constats.SHARE_KEY, Context.MODE_PRIVATE);
		mGestureDetector = new GestureDetector((OnGestureListener) this);
		RelativeLayout relativeLayout =(RelativeLayout)findViewById(R.id.regies_user_layout);
		relativeLayout.setLongClickable(true);
		relativeLayout.setOnTouchListener(this);
		initView();

	}

	public void initView() {
		backImg = (ImageView) findViewById(R.id.regies_user_back);
		String userid = sp.getString(UserInfo.USER_ID,"");
		if (!TextUtils.isEmpty(userid)){
			isToLogin = true;
			backImg.setVisibility(View.GONE);
		}else {
			backImg.setVisibility(View.VISIBLE);
			isToLogin = false;
		}
		et_regiest_nike = (EditText) findViewById(R.id.et_regies_nike);// 昵称
		et_regiest_name = (EditText) findViewById(R.id.et_regies_name);// 姓名
		bt_regiest_next = (Button) findViewById(R.id.bt_regiest_user_next);// 下一步
		tv_regiest_have = (TextView) findViewById(R.id.tv_regiest_have);// 已有账户登录
		tv_regiest_protocol = (TextView) findViewById(R.id.tv_regiest_protocol);// 用户协议

		backImg.setOnClickListener(this);
		tv_regiest_protocol.setOnClickListener(this);
		bt_regiest_next.setEnabled(false);// 非激活状态
		bt_regiest_next.setOnClickListener(this);
		tv_regiest_have.setOnClickListener(this);
		threadStop = true;
		new Thread(mRunnable).start();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		threadStop = true;
	}

	@Override
	public void onClick(View v) {

		int id = v.getId();
		switch (id) {
		case R.id.bt_regiest_user_next:
			// 跳转输入手机号页面
			Intent intent = new Intent(this, RegiesPhone.class);
			intent.putExtra("nike", et_regiest_nike.getText().toString().trim());// 传递昵称

			if (et_regiest_name.getText() != null) {
				nike = et_regiest_name.getText().toString().trim();
			}
			intent.putExtra("name", nike);// 传递姓名
			startActivity(intent);
			overridePendingTransition(R.anim.right_in, R.anim.left_out);
			break;

		case R.id.tv_regiest_have:// 已有账号进入登录界面
			Intent intent2 = new Intent(this, RepeatLoginActivity.class);
			startActivity(intent2);
			overridePendingTransition(R.anim.right_in, R.anim.left_out);
			finish();
			break;
		case R.id.tv_regiest_protocol:// 用户协议
			if (isZh()){
				Intent intent3 = new Intent(this, WebActivity.class);
				intent3.putExtra(com.jld.torsun.config.Config.TYPE, com.jld.torsun.config.Config.TYPE_SET_PROTOCOL);
				intent3.putExtra("url", "file:///android_asset/userProtocol_cn.html");
				startActivity(intent3);
			}else {
				Intent intent3 = new Intent(this, WebActivity.class);
				intent3.putExtra(com.jld.torsun.config.Config.TYPE, com.jld.torsun.config.Config.TYPE_SET_PROTOCOL);
				intent3.putExtra("url", "file:///android_asset/userProtocol_en.html");
				startActivity(intent3);
			}

			break;
		case R.id.regies_user_back:
			overridePendingTransition(R.anim.right_in, R.anim.left_out);
			this.finish();
			break;
		default:
			break;
		}

	}

	//判断当前系统语言是否为中文
	private boolean isZh() {
		Locale locale = getResources().getConfiguration().locale;
		String language = locale.getLanguage();
		if (language.endsWith("zh"))
			return true;
		else
			return false;
	}

	/**
	 * 判断昵称是否为空
	 */
	private Runnable mRunnable = new Runnable() {
		@Override
		public void run() {
			while (threadStop) {
				if (!TextUtils.isEmpty(et_regiest_nike.getText().toString()
						.trim())
						&& !bt_regiest_next.isEnabled()) {// 如果昵称不为空并且下一步不是激活状态
					runOnUiThread(new Runnable() {
						@Override
						public void run() {

							bt_regiest_next.setEnabled(true);// 设置为激活状态
						}
					});

				} else if (TextUtils.isEmpty(et_regiest_nike.getText()
						.toString().trim())
						&& bt_regiest_next.isEnabled()) {// 如果昵称为空并且下一步是激活状态
					runOnUiThread(new Runnable() {
						@Override
						public void run() {

							bt_regiest_next.setEnabled(false);// 设置为非激活状态
						}
					});
				}
				try {
					Thread.sleep(300);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		}

	};

	/********************************************
	 * 滑屏切换
	 * *****************************************/

	@Override
	public boolean onDown(MotionEvent e) {
		return false;
	}

	private int verticalMinDistance = 100;
	private int minVelocity = 5;

	@Override
	public void onShowPress(MotionEvent e) {

	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {

	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

		if (e1.getX() - e2.getX() > verticalMinDistance
				&& Math.abs(velocityX) > minVelocity) {
			//Toast.makeText(this, "向左手势", Toast.LENGTH_SHORT).show();
		} else if (e2.getX() - e1.getX() > verticalMinDistance
				&& Math.abs(velocityX) > minVelocity) {

			// 切换Activity
			// Intent intent = new Intent(ViewSnsActivity.this,
			// UpdateStatusActivity.class);
			// startActivity(intent);
			if (isToLogin){
				Intent regiest = new Intent();
				regiest.setClass(this, RepeatLoginActivity.class);
				startActivity(regiest);
				overridePendingTransition(R.anim.left_in, R.anim.right_out);
				finish();
			}else {
				Intent login_intent = new Intent();
				login_intent.setClass(this, MainFragment.class);
				startActivity(login_intent);
				finish();
				//onBackPressed();
				overridePendingTransition(R.anim.left_in, R.anim.right_out);
				//Toast.makeText(this, "向右手势", Toast.LENGTH_SHORT).show();
			}

		}
		return false;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		return mGestureDetector.onTouchEvent(event);
	}
}
