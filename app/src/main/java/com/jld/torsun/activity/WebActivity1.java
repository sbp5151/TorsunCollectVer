package com.jld.torsun.activity;

import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jld.torsun.R;
import com.jld.torsun.config.Config;
import com.jld.torsun.util.AndroidUtil;
import com.jld.torsun.util.DensityUtil;
import com.jld.torsun.util.LogUtil;

public class WebActivity1 extends BaseActivity {

	private WebView webview;
	private String url;
	private ProgressBar progressBar, progressBar2;

	private View titleView;
	private TextView titleTextView;
	private ImageButton backIV;

	private String typeString;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.web_activity_layout_1);
		typeString = getIntent().getStringExtra(Config.TYPE);
		url = getIntent().getStringExtra("url");
		LogUtil.d("WebActivity1:","onCreate");
		initTopView();
		if (Config.TYPE_GUIDER.equals(typeString)) {
			titleTextView.setText(R.string.web_title_guider);
		} else if (Config.TYPE_NORMAL_PROMBLE.equals(typeString)) {
			titleTextView.setText(R.string.normal_problem);
		} else if (Config.TYPE_CALL_US.equals(typeString)) {
			titleTextView.setText(R.string.call_us);
		} else if (Config.TYPE_SET_PROTOCOL.equals(typeString)) {
			titleTextView.setText(R.string.user_protocol);
		}

		backIV.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
//				finish();
				if (webview.canGoBack()) {
					webview.goBack();
				} else {
					finish();
				}
			}
		});
		progressBar = (ProgressBar) findViewById(R.id.web_progressBar_1);
		progressBar2 = (ProgressBar) findViewById(R.id.web_progressBar2_1);
		final TextView tv_prompt = (TextView) findViewById(R.id.textView1_1);

		webview = (WebView) findViewById(R.id.webview_1);
		WebSettings webSettings = webview.getSettings();
		webSettings.setUseWideViewPort(true);
		webSettings.setLoadWithOverviewMode(true);
		// 设置WebView属性，能够执行Javascript脚本
		webSettings.setJavaScriptEnabled(true);

		// 设置页面顶端的进度条
		webview.setWebChromeClient(new WebChromeClient() {
			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				super.onProgressChanged(view, newProgress);
				progressBar.setProgress(newProgress);
				if (100 == newProgress) {
					progressBar.setVisibility(View.GONE);
					progressBar2.setVisibility(View.GONE);
					tv_prompt.setVisibility(View.GONE);
					webview.setVisibility(View.VISIBLE);
				} 
			}
		});

		setFlingView(webview);
		// 设置Web视图
		webview.setWebViewClient(new HelloWebViewClient());

		// 加载需要显示的网页
		webview.loadUrl(url);
	}

	private void initTopView() {
		titleView=findViewById(R.id.web_title_1);
		titleTextView = (TextView) titleView.findViewById(R.id.tv_title_message_center_title);
		backIV = (ImageButton) titleView.findViewById(R.id.iv_title_message_center_back);
		TextView textView = (TextView)titleView.findViewById(R.id.tv_title_message_center_release);
		textView.setVisibility(View.GONE);
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

	// Web视图
	private class HelloWebViewClient extends WebViewClient {

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			view.loadUrl(url);
			// 设置跳转一直在当前activity
			return super.shouldOverrideUrlLoading(view, url);
		}

		// 重写网页出错后显示的网页内容
		@Override
		public void onReceivedError(WebView view, int errorCode,
				String description, String failingUrl) {
			super.onReceivedError(view, errorCode, description, failingUrl);
			view.loadUrl("file:///android_asset/nonetwork.html");
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && webview.canGoBack()){
			webview.goBack();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
