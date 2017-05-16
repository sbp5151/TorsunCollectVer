package com.jld.torsun.activity;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonRequest;
import com.jld.torsun.MyApplication;
import com.jld.torsun.R;
import com.jld.torsun.activity.loginAndRegies.RepeatLoginActivity;
import com.jld.torsun.http.VolleyJsonUtil;
import com.jld.torsun.util.AndroidUtil;
import com.jld.torsun.util.Constats;
import com.jld.torsun.util.DensityUtil;
import com.jld.torsun.util.LogUtil;
import com.jld.torsun.util.MD5Util;
import com.jld.torsun.util.ToastUtil;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * 
 * @ClassName: ResetPasswordActivity
 * @Description: 重设密码
 * @author liuzhi
 * @date 2015-12-2 下午1:55:08
 */
public class ResetPasswordActivity extends BaseActivity implements
		OnClickListener {
	// private static final String TAG = "ResetPasswordActivity";

	private TextView titleTextView;
	private ImageView titleBackImageView;

	private EditText et_reset_password, et_reset_password_sure;
	private Button bt_resetpassword_sure;
	/** 手机号 */
	private String number;
	private String newpasswd;
	private String newpasswd2;

	private RequestQueue mRequestQueue;

	private View titleView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reset_password_layout);
		// mRequestQueue = Volley.newRequestQueue(this);
		MyApplication ma = (MyApplication) getApplication();
		mRequestQueue = ma.getRequestQueue();
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
		if (null != this.getIntent()) {
			number = this.getIntent().getStringExtra(
					FindBackPassWordActivity.NUMBER);
		}
		titleView=findViewById(R.id.layout_reste_password_title);
		titleTextView = (TextView) titleView.findViewById(R.id.tv_view_title_message);
		titleTextView.setText(R.string.t_reset_pwd_info);

		titleBackImageView = (ImageView) titleView.findViewById(R.id.iv_view_title_back);
		titleBackImageView.setOnClickListener(this);

		et_reset_password = (EditText) findViewById(R.id.et_reset_password);
		et_reset_password_sure = (EditText) findViewById(R.id.et_reset_password_sure);
		bt_resetpassword_sure = (Button) findViewById(R.id.bt_resetpassword_sure);
		bt_resetpassword_sure.setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		int id = view.getId();
		switch (id) {
		case R.id.iv_view_title_back:
			finish();
			break;
		case R.id.bt_resetpassword_sure:
			// LogUtil.i(TAG, "number:"+number);
			if (null == number) {
				break;
			}
			String original_newpasswd = et_reset_password.getText().toString()
					.trim();
			String original_newpasswd2 = et_reset_password_sure.getText()
					.toString().trim();
			
			newpasswd = MD5Util.getMD5(Constats.S_KEY + original_newpasswd);
			newpasswd2 = MD5Util.getMD5(Constats.S_KEY + original_newpasswd2);

			if (TextUtils.isEmpty(newpasswd) || TextUtils.isEmpty(newpasswd2)) {
				ToastUtil.showToast(this, R.string.t_reset_pwd_new_pwd, 2000);
				break;
			}
			// 找回密码的需提交的加密字符串
			String sign = MD5Util.getMD5(Constats.S_KEY + number + newpasswd);
			String url = Constats.HTTP_URL + Constats.FINDBACK_PASSWORD;
			Map<String, String> sparams = new HashMap<String, String>();
			sparams.put("mobile", number);
			sparams.put("newpasswd", newpasswd);
			sparams.put("newpasswd2", newpasswd2);
			sparams.put("sign", sign);
			commitToResetPasswd(url, sparams);
			break;
		}
	}

	/** 将重设的密码提交给服务器 */
	@SuppressWarnings("unchecked")
	private void commitToResetPasswd(String url, Map<String, String> params) {
		JsonRequest<JSONObject> jsonRequest = VolleyJsonUtil
				.createJsonObjectRequest(Method.POST, url, params,
						new Listener<JSONObject>() {
							@Override
							public void onResponse(JSONObject response) {
								try {
									int result = response.getInt("result");
									switch (result) {
									case 0:
										ToastUtil
												.showToast(
														ResetPasswordActivity.this,
														R.string.t_reset_pwd_new_pwd_sure,
														3000);
										ResetPasswordActivity.this
												.toActivity(RepeatLoginActivity.class);
										finish();
										break;
									case 1001:
										ToastUtil
												.showToast(
														ResetPasswordActivity.this,
														R.string.t_reset_pwd_new_pwd_err_1,
														3000);
										break;
									default:
										ToastUtil
												.showToast(
														ResetPasswordActivity.this,
														R.string.t_reset_pwd_new_pwd_err_2,
														3000);
										break;
									}
								} catch (JSONException e) {
									e.printStackTrace();
								}
							}
						}, new ErrorListener() {
							@Override
							public void onErrorResponse(VolleyError error) {
								// LogUtil.d(TAG, "error = " +
								// error.toString());
								ToastUtil.showToast(ResetPasswordActivity.this,
										R.string.t_frag_set_network_err, 3000);
							}
						});
		if (null != mRequestQueue) {
			mRequestQueue.add(jsonRequest);
		}

	}

}
