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
import com.jld.torsun.http.VolleyJsonUtil;
import com.jld.torsun.util.AndroidUtil;
import com.jld.torsun.util.Constats;
import com.jld.torsun.util.DensityUtil;
import com.jld.torsun.util.LogUtil;
import com.jld.torsun.util.MD5Util;
import com.jld.torsun.util.ToastUtil;
import com.jld.torsun.util.UserInfo;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * 意见反馈
 * */
public class FeedBackActivity extends BaseActivity implements OnClickListener,OnTouchListener,OnGestureListener{
	private ImageView image_feedback_title_back;
	private TextView tv_feedback_sure;
	private EditText et_feedback;
	private CheckBox checkb_feedback_suggest, checkb_feedback_complaints;

	private RelativeLayout suggest_rl , complaints_rl ;

	private SharedPreferences sp;
	private RequestQueue mRequestQueue;
	private String type;//记录建议的类型    0表示建议  1 表示投诉

	private GestureDetector mGestureDetector;

	private View titleView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_feed_back);

		titleView=findViewById(R.id.feed_back_title);
		mGestureDetector = new GestureDetector((OnGestureListener) this);
		LinearLayout linearLayout=(LinearLayout)findViewById(R.id.ll_feed_back_layout);
		linearLayout.setOnTouchListener(this);
		linearLayout.setLongClickable(true);

//		mRequestQueue = Volley.newRequestQueue(this);
		MyApplication ma = (MyApplication)getApplication();
		mRequestQueue = ma.getRequestQueue();
		sp =getSharedPreferences(Constats.SHARE_KEY, Context.MODE_PRIVATE);
		initView();
	}
	@Override
	protected void onResume() {
		super.onResume();
		type = "0";
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
		suggest_rl = (RelativeLayout) findViewById(R.id.feed_back_suggestion_rl);
		suggest_rl.setOnClickListener(this);
		complaints_rl = (RelativeLayout) findViewById(R.id.feed_back_complaints_rl);
		complaints_rl.setOnClickListener(this);
		image_feedback_title_back = (ImageView) titleView.findViewById(R.id.title_image_title_back);
		image_feedback_title_back.setOnClickListener(this);
		tv_feedback_sure = (TextView) titleView.findViewById(R.id.tv_title_sure);
		tv_feedback_sure.setOnClickListener(this);
		et_feedback = (EditText) findViewById(R.id.et_feedback);
		checkb_feedback_suggest = (CheckBox) findViewById(R.id.checkb_feedback_suggest);
		checkb_feedback_complaints = (CheckBox) findViewById(R.id.checkb_feedback_complaints);
		checkb_feedback_suggest.setOnClickListener(myOncl);
		checkb_feedback_complaints.setOnClickListener(myOncl);
		checkb_feedback_suggest.setChecked(true);
	}

	@Override
	public void onClick(View view) {
		int id = view.getId();
		switch (id) {
		case R.id.title_image_title_back:
			finish();
			break;
		case R.id.feed_back_complaints_rl:
			type = "1";
			checkb_feedback_suggest.setChecked(false);
			checkb_feedback_complaints.setChecked(true);
			break;
		case R.id.feed_back_suggestion_rl:
			type = "0";
			checkb_feedback_complaints.setChecked(false);
			checkb_feedback_suggest.setChecked(true);
			break;
		case R.id.tv_title_sure:
			// 提交反馈
			String feed_str = et_feedback.getText().toString().trim();
			if(TextUtils.isEmpty(feed_str)){
				ToastUtil.showToast(this, R.string.t_feed_back_text, 3000);
				return;
			}
			if(TextUtils.isEmpty(type)){
				ToastUtil.showToast(this, R.string.t_feed_back_text_type, 3000);
				return;
			}
			String mobile = sp.getString(UserInfo.LOGIN_ACCOUT,"");
			String sign = MD5Util.getMD5(Constats.S_KEY + mobile);
			String url = Constats.HTTP_URL + Constats.FEED_BACK_FUN;
			Map<String, String> params = new HashMap<String, String>();
			params.put("mobile", mobile);
			params.put("type", type);
			params.put("info", feed_str);
			params.put("sign", sign);
			feedBackhttp(url,params);
			finish();
			break;
		}
	}
	
	OnClickListener myOncl = new OnClickListener() {
		
		@Override
		public void onClick(View view) {
			int id  = view.getId();
			if(id == R.id.checkb_feedback_suggest){
				type = "0";
				checkb_feedback_complaints.setChecked(false);
				checkb_feedback_suggest.setChecked(true);
			}
			
			if(id == R.id.checkb_feedback_complaints){

				type = "1";
				checkb_feedback_suggest.setChecked(false);
				checkb_feedback_complaints.setChecked(true);
			}
			
		}
	};
	
	
	
	/**
	 * 意见反馈
	 * */
	@SuppressWarnings("unchecked")
	public void feedBackhttp(String url, Map<String, String> params){
		JsonRequest<JSONObject> jsonRequest = VolleyJsonUtil
				.createJsonObjectRequest(Method.POST, url, params,
						new Listener<JSONObject>() {
							@Override
							public void onResponse(JSONObject response) {
								try {
									int result = response.getInt("result");
									String message = response.getString("msg");
									if (0 == result) {
										ToastUtil.showToast(FeedBackActivity.this, message, 3000);
									}else{
										ToastUtil.showToast(FeedBackActivity.this, message, 3000);
									}
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}, new ErrorListener() {
							@Override
							public void onErrorResponse(VolleyError error) {
							}
						});
		if (null != mRequestQueue) {
			mRequestQueue.add(jsonRequest);
		}
	}

	/********************************************
	 * 滑屏切换
	 * *****************************************/

	@Override
	public boolean onDown(MotionEvent e) {
		return false;
	}

	private int verticalMinDistance = 20;
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
			finish();
			overridePendingTransition(R.anim.left_in, R.anim.right_out);
			//Toast.makeText(this, "向右手势", Toast.LENGTH_SHORT).show();
		}
		return false;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		return mGestureDetector.onTouchEvent(event);
	}
}
