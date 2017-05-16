package com.jld.torsun.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonRequest;
import com.jld.torsun.MyApplication;
import com.jld.torsun.R;
import com.jld.torsun.db.MemberDao;
import com.jld.torsun.http.VolleyJsonUtil;
import com.jld.torsun.modle.ActionConstats;
import com.jld.torsun.util.AndroidUtil;
import com.jld.torsun.util.Constats;
import com.jld.torsun.util.DensityUtil;
import com.jld.torsun.util.LogUtil;
import com.jld.torsun.util.MD5Util;
import com.jld.torsun.util.ToastUtil;
import com.jld.torsun.util.UserInfo;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * 修改昵称和姓名
 * */
public class NikChangeActivity extends BaseActivity {
	// private static final String TAG = "NikChangeActivity";
	public static final String FROM = "from";
	public static final String FROM_NIKE = "FROM_NIKE";
	public static final String FROM_NAME = "FROM_NAME";

	private View title_change_nik;
//	private ImageView imagev_nik_delete;
	private EditText et_change_nik;

	private boolean isNik;// 记录是从修改昵称还是修改名字进来

	private RequestQueue mRequestQueue;
	private SharedPreferences sp;
	private String nik;
	private String name;

	private MemberDao memberDao;

	// private IntentFilter intentFilter;
	// private MyBroadcast myBroadcast;


	// private MulticastServer multicastServer;

	private GestureDetector mGestureDetector;

	private String text;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_change_nik_layout);

		LinearLayout linearLayout=(LinearLayout)findViewById(R.id.ll_change_nik_layout);
		linearLayout.setLongClickable(true);

		setFlingView(linearLayout);
		// mRequestQueue = Volley.newRequestQueue(this);
		MyApplication ma = (MyApplication) getApplication();
		// multicastServer=MulticastServer.getInstanceMulticastServer(this);
		mRequestQueue = ma.getRequestQueue();
		memberDao = MemberDao.getInstance(this);
		sp = getSharedPreferences(Constats.SHARE_KEY, Context.MODE_PRIVATE);
		// myBroadcast = new MainFragment().new MyBroadcast();
		// intentFilter = new IntentFilter();
		if (FROM_NIKE.equals(getIntent().getStringExtra(FROM))) {
			// intentFilter.addAction(NICKCHANGE);
			isNik = true;
		} else if (FROM_NAME.equals(getIntent().getStringExtra(FROM))) {
			isNik = false;
		}
		// registerReceiver(myBroadcast, intentFilter);
		initView();
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
				LinearLayout.LayoutParams params =(LinearLayout.LayoutParams)title_change_nik.getLayoutParams();
				params.height=viewHeight;
				title_change_nik.setLayoutParams(params);
			}
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		isfristFocus=true;
	}

	private void initView() {
		title_change_nik = findViewById(R.id.title_change_nik);
		TextView tv_title = (TextView) title_change_nik
				.findViewById(R.id.tv_view_title_message);
		et_change_nik = (EditText) findViewById(R.id.et_change_nik);

		if (isNik) {
			tv_title.setText(R.string.nik_change);
			//et_change_nik.setHint(sp.getString(UserInfo.NIK, ""));
			et_change_nik.setHint(R.string.nik_change);
			et_change_nik.setText(sp.getString(UserInfo.NIK, ""));
			text = sp.getString(UserInfo.NIK,"");
		} else {
			tv_title.setText(R.string.name_change);
			//et_change_nik.setHint(sp.getString(UserInfo.USER_NAME, ""));
			String name_string=sp.getString(UserInfo.USER_NAME, "");
			text = sp.getString(UserInfo.USER_NAME,"");
			if (!TextUtils.isEmpty(name_string)){
				et_change_nik.setText(name_string);
			}
		}

		ImageView image_title_back = (ImageView) title_change_nik
				.findViewById(R.id.iv_view_title_back);
		TextView tv_sure = (TextView) title_change_nik
				.findViewById(R.id.tv_set_title_sure);
		tv_sure.setVisibility(View.VISIBLE);

		image_title_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
		tv_sure.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				toFinish();
			}
		});
	}

	private void toFinish(){
		if (isNik) {
			nik = et_change_nik.getText().toString().trim();
			if (TextUtils.isEmpty(nik)) {
				finish();
				return;
			}
			if (nik.equals(text)){
				finish();
				return;
			}
			String userId = sp.getString(UserInfo.USER_ID, "");
			String mobile = sp.getString(UserInfo.LOGIN_ACCOUT, "");
			String sign = MD5Util.getMD5(Constats.S_KEY + mobile
					+ userId);
			String url = Constats.HTTP_URL
					+ Constats.CHANGE_NIK_FUN;
			Map<String, String> params = new HashMap<String, String>();
			params.put("userid", userId);
			params.put("mobile", mobile);
			params.put("nick", nik);
			params.put("sign", sign);
			changeNik(url, params);
		} else {
			name = et_change_nik.getText().toString().trim();
			if (name.equals(text)){
				finish();
				return;
			}
			String userId = sp.getString(UserInfo.USER_ID, "");
			String mobile = sp.getString(UserInfo.LOGIN_ACCOUT, "");
			String sign = MD5Util.getMD5(Constats.S_KEY + mobile
					+ userId);

			String url = Constats.HTTP_URL
					+ Constats.CHANGE_NAME_FUN;
			Map<String, String> params = new HashMap<String, String>();
			params.put("userid", userId);
			params.put("mobile", mobile);
			params.put("username", name);
			params.put("sign", sign);
			changeName(url, params);
		}
		NikChangeActivity.this.finish();
	}

	/**
	 * 修改用户昵称
	 * */
	@SuppressWarnings("unchecked")
	public void changeNik(String url, Map<String, String> params) {
		JsonRequest<JSONObject> jsonRequest = VolleyJsonUtil
				.createJsonObjectRequest(Method.POST, url, params,
						new Listener<JSONObject>() {
							@Override
							public void onResponse(JSONObject response) {
								try {
									int result = response.getInt("result");
									String message = response.getString("msg");
									if (0 == result) {
										memberDao.updateUserNik(MainFragment.MYSELFID, nik);
										ToastUtil.showToast(NikChangeActivity.this, R.string.t_nik_change_info_sure, 3000);

										JSONObject item = response.getJSONObject("item");

										sp.edit().putString(UserInfo.JSONSTR, item.toString()).commit();

										sp.edit().putString(UserInfo.NIK, nik).commit();

										Intent intent = new Intent(ActionConstats.NICK_CHANGE);
										sendBroadcast(intent);

										Intent intent2 = new Intent(ActionConstats.STRCHANGE);
										sendBroadcast(intent2);
									} else {
										ToastUtil.showToast(
												NikChangeActivity.this,
												message, 3000);
									}
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}, new ErrorListener() {
							@Override
							public void onErrorResponse(VolleyError error) {
								ToastUtil.showToast(NikChangeActivity.this,
										R.string.t_nik_change_info_err, 3000);
							}
						});
		if (null != mRequestQueue) {
			mRequestQueue.add(jsonRequest);
		}
	}

	/**
	 * 修改用户姓名
	 * */
	@SuppressWarnings("unchecked")
	public void changeName(String url, Map<String, String> params) {
		JsonRequest<JSONObject> jsonRequest = VolleyJsonUtil
				.createJsonObjectRequest(Method.POST, url, params,
						new Listener<JSONObject>() {
							@Override
							public void onResponse(JSONObject response) {
								try {
									int result = response.getInt("result");
									String message = response.getString("msg");
									if (0 == result) {
										memberDao.updateUserName(
												MainFragment.MYSELFID, name);
										ToastUtil
												.showToast(
														NikChangeActivity.this,
														R.string.t_nik_change_info_sure,
														3000);
										JSONObject item = response
												.getJSONObject("item");
										sp.edit()
												.putString(UserInfo.JSONSTR,
														item.toString())
												.commit();
										sp.edit()
												.putString(UserInfo.USER_NAME,
														name).commit();
										Intent intent = new Intent(ActionConstats.NAME_CHANGE);
										sendBroadcast(intent);

										Intent intent2 = new Intent(
												ActionConstats.STRCHANGE);
										sendBroadcast(intent2);
									} else {
										ToastUtil.showToast(
												NikChangeActivity.this,
												message, 3000);
									}
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}, new ErrorListener() {
							@Override
							public void onErrorResponse(VolleyError error) {
								ToastUtil.showToast(NikChangeActivity.this,
										R.string.t_nik_change_info_err, 3000);
							}
						});
		if (null != mRequestQueue) {
			mRequestQueue.add(jsonRequest);
		}
	}
}
