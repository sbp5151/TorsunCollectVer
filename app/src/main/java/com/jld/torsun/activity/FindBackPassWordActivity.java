package com.jld.torsun.activity;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.Request.Method;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.JsonRequest;
import com.jld.torsun.MyApplication;
import com.jld.torsun.R;
import com.jld.torsun.activity.countryCode.CountryPageActivity;
import com.jld.torsun.http.VolleyJsonUtil;
import com.jld.torsun.util.AndroidUtil;
import com.jld.torsun.util.Constats;
import com.jld.torsun.util.DensityUtil;
import com.jld.torsun.util.GetCode;
import com.jld.torsun.util.LogUtil;
import com.jld.torsun.util.MD5Util;
import com.jld.torsun.util.ToastUtil;
import com.jld.torsun.util.UserInfo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * 找回密码,获取验证码
 * */
public class FindBackPassWordActivity extends BaseActivity implements OnClickListener{
//	private static final String TAG = "FindBackPassWordActivity";
	public static final String NUMBER = "number";
	public static final String CODE = "code";
	
	private RequestQueue mRequestQueue;
	
	private EditText et_find_password_number,et_security_code;
	private TextView tv_find_password_country;
	private Button bt_findpassword_get_code,bt_findpassword_sure;
	private SharedPreferences sp;
	private TimeCount time;
	private ImageView backImageView;

	private View titleView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_findback_password);
//		mRequestQueue = Volley.newRequestQueue(this);
		titleView=findViewById(R.id.find_back_title_rl);
		MyApplication ma = (MyApplication)getApplication();
		mRequestQueue = ma.getRequestQueue();
		countryName=getResources().getString(R.string.code_country_name);
		sp = getSharedPreferences(Constats.SHARE_KEY, Context.MODE_PRIVATE);
		initView();
		time = new TimeCount(60000, 1000);// 构造CountDownTimer对象
	}

	private boolean isfristFocus = true;

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (isfristFocus && hasFocus){
			isfristFocus=false;
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT){
				float height= AndroidUtil.getStatusHeight(this);
				LogUtil.d("--------------状态栏的高度为:" + height);

				int viewHeight = DensityUtil.px2dip(this, (216f - height * 2));
				LogUtil.d("--------------viewHeight的高度:" + viewHeight);
				LinearLayout.LayoutParams params =(LinearLayout.LayoutParams)titleView.getLayoutParams();
				params.height=viewHeight;
				titleView.setLayoutParams(params);
			}
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		isfristFocus=true;
	}

	private void initView() {
		backImageView=(ImageView)findViewById(R.id.imagev_find_password_back);
		backImageView.setOnClickListener(this);

		tv_find_password_country=(TextView)findViewById(R.id.tv_find_password_country);
		tv_find_password_country.setOnClickListener(this);

		et_find_password_number = (EditText) findViewById(R.id.et_find_password_number);
		et_security_code = (EditText) findViewById(R.id.et_security_code);
		
		bt_findpassword_get_code = (Button) findViewById(R.id.bt_findpassword_get_code);
		bt_findpassword_get_code.setOnClickListener(this);
		bt_findpassword_sure = (Button) findViewById(R.id.bt_findpassword_sure);
		bt_findpassword_sure.setOnClickListener(this);
		//没获取验证码之前,"确定按钮不能被点击"
		bt_findpassword_sure.setEnabled(false);
		if(!TextUtils.isEmpty(sp.getString(UserInfo.LOGIN_ACCOUT, ""))){
			et_find_password_number.setText(sp.getString(UserInfo.LOGIN_ACCOUT, ""));
		}
	}

	@Override
	public void onClick(View view) {
		int id  = view.getId();
		switch (id) {
		case R.id.imagev_find_password_back:
			finish();
			break;

		case R.id.bt_findpassword_get_code:
			String number = et_find_password_number.getText().toString().trim();
			if (TextUtils.isEmpty(number)) {
				ToastUtil.showToast(this, R.string.t_find_back_pwd_phone, 2000);
				break;
			}

			String da = countryCode;
			String getsign = MD5Util.getMD5(Constats.S_KEY + number);
			String geturl = Constats.HTTP_URL + Constats.SMS_GET;
			Map<String, String> sparams = new HashMap<String, String>();
			sparams.put("mobile", number);
			sparams.put("da", da);
			sparams.put("sign", getsign);
			getSecurityCode(geturl, sparams);
			break;
		case R.id.bt_findpassword_sure:
			String number_2 = et_find_password_number.getText().toString().trim();
			String code = et_security_code.getText().toString().trim();
			if(TextUtils.isEmpty(number_2) || TextUtils.isEmpty(code)){
				ToastUtil.showToast(this, R.string.t_find_back_pwd_text_info, 3000);
				break;
			}
			if ((scode+"").equals(code)) {
				Intent intent = new Intent();
				intent.setClass(FindBackPassWordActivity.this, ResetPasswordActivity.class);
				intent.putExtra(NUMBER, number_2);
				intent.putExtra(CODE,code);
				startActivity(intent);
				overridePendingTransition(R.anim.right_in, R.anim.left_out);
				finish();
			}else {
				ToastUtil.showToast(this, R.string.t_find_back_pwd_code_info, 3000);
			}
			break;
		case R.id.tv_find_password_country:
			Intent intent = new Intent(FindBackPassWordActivity.this, CountryPageActivity.class);
			startActivityForResult(intent,999);
			overridePendingTransition(R.anim.right_in, R.anim.left_out);
			break;
		default:
			break;
		}
	}

	private String countryCode="86";

	private String countryName;
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (null!=data){
			countryCode = data.getExtras().getString("CountryCode");
			countryName = data.getExtras().getString("CountryName");
			if (!TextUtils.isEmpty(countryCode)
					&& !TextUtils.isEmpty(countryName)){
				tv_find_password_country.setText(countryName + "(+" + countryCode + ")");
			}
		}
	}

	/**从服务器获取到的验证码*/
	private int scode;
	
	/**
	 * 获取验证码
	 * */
	@SuppressWarnings("unchecked")
	private void getSecurityCode(String url, Map<String, String> params) {
		JsonRequest<JSONObject> jsonRequest = VolleyJsonUtil
				.createJsonObjectRequest(Method.POST, url, params,
						new Listener<JSONObject>() {
							@Override
							public void onResponse(JSONObject response) {
								try {
									int result = response.getInt("result");
									scode = response.getInt("code");
									LogUtil.d("================scode:"+scode);
									if (0 == result) {
//										et_security_code.setText(scode+"");
										//获取到验证码后,"确定"按钮可以被点击
										time.start();
										bt_findpassword_sure.setEnabled(true);
									}else{
										ToastUtil.showToast(FindBackPassWordActivity.this, R.string.t_find_back_pwd_get_code_info, 3000);
									}
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}, new ErrorListener() {
							@Override
							public void onErrorResponse(VolleyError error) {
								//LogUtil.d(TAG, "error = " + error.toString());
								ToastUtil.showToast(FindBackPassWordActivity.this, R.string.t_frag_set_network_err, 3000);
							}
						});
		if (null != mRequestQueue) {
			mRequestQueue.add(jsonRequest);
		}
	}	
	/**
	 * 计时器
	 * */
	class TimeCount extends CountDownTimer {
		public TimeCount(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);// 参数依次为总时长,和计时的时间间隔
		}

		@Override
		public void onFinish() {// 计时完毕时触发
			bt_findpassword_get_code.setText(R.string.t_find_back_pwd_re_code_info);
			bt_findpassword_get_code.setClickable(true);
		}

		@Override
		public void onTick(long millisUntilFinished) {// 计时过程显示
			bt_findpassword_get_code.setClickable(false);
			bt_findpassword_get_code.setText(millisUntilFinished / 1000 + "s");
		}
	}
	
}
