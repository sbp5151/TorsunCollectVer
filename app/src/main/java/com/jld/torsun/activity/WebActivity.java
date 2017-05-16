package com.jld.torsun.activity;

import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jld.torsun.R;
import com.jld.torsun.config.Config;
import com.jld.torsun.util.AndroidUtil;
import com.jld.torsun.util.DensityUtil;
import com.jld.torsun.util.LogUtil;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class WebActivity extends BaseActivity {

	private static final String TAG = "WebActivity";

	private WebView webview;
	private String url;
	private ProgressBar progressBar, progressBar2;

	private View titleView;
	private TextView titleTextView;
	private ImageView backIV;
	private TextView tv_prompt;
	private String typeString;

	//判断是否是从导游认证入口进入的
	private boolean isGuider = false;
	//判断当前网页是否是导游认证网页
	private boolean isGuiderUrl = false;
	//https://dyb.torsun.com.cn/daoyou/daoyou.html
	private String guiderUrl = "https://dyb.torsun.com.cn/daoyou";

	private String startStr = "https://dyb.torsun.com.cn/cgi-bin/luci/";
	private String endStr = "admin/network/tswifi/";
	private boolean isRootUrl = true;
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
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.web_activity_layout);
		typeString = getIntent().getStringExtra(Config.TYPE);
		url = getIntent().getStringExtra("url");
		initView();
		LogUtil.d("WebActivity:","onCreate");

		backIV.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (isRootUrl){
					finish();
				}else if (webview.canGoBack()) {
					webview.goBack();
				} else {
					finish();
				}
			}
		});

		WebSettings webSettings = webview.getSettings();
		webSettings.setUseWideViewPort(true);
		webSettings.setLoadWithOverviewMode(true);
		// 设置WebView属性，能够执行Javascript脚本
		webSettings.setJavaScriptEnabled(true);
		// 设置Web视图
		webview.setWebChromeClient(webChromeClient);
		webview.setWebViewClient(webViewClient);
		// 加载需要显示的网页
		webview.loadUrl(url);

	}
	/**
	 * 获取参数指定的网页代码，将其返回给调用者，由调用者对其解析 返回String
	 */
	public String posturl(String url) {
		InputStream is = null;
		String result = "";
		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(url);
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();
			is = entity.getContent();
		} catch (Exception e) {
			return "Fail to establish http connection!" + e.toString();
		}
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					is, "utf-8"));
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			is.close();

			result = sb.toString();
		} catch (Exception e) {
			return "Fail to convert net stream!";
		}
		return result;
	}

	private void initView() {
		titleView=findViewById(R.id.web_title);
		titleTextView = (TextView) titleView.findViewById(R.id.tv_title_title);
		backIV = (ImageView) titleView.findViewById(R.id.title_image_title_back);
		TextView tv_sure = (TextView) titleView.findViewById(R.id.tv_title_sure);
		tv_sure.setVisibility(View.INVISIBLE);
		progressBar = (ProgressBar) findViewById(R.id.web_progressBar);
		progressBar2 = (ProgressBar) findViewById(R.id.web_progressBar2);
		tv_prompt = (TextView) findViewById(R.id.textView1);
		progressBar2.setVisibility(View.GONE);
		tv_prompt.setVisibility(View.GONE);

		webview = (WebView) findViewById(R.id.webview);

		if (Config.TYPE_GUIDER.equals(typeString)) {
			isGuider = true;
			titleTextView.setText(R.string.web_title_guider);
		} else if (Config.TYPE_NORMAL_PROMBLE.equals(typeString)) {
			titleTextView.setText(R.string.normal_problem);
		} else if (Config.TYPE_CALL_US.equals(typeString)) {
			titleTextView.setText(R.string.call_us);
		} else if (Config.TYPE_SET_PROTOCOL.equals(typeString)) {
			titleTextView.setText(R.string.user_protocol);
		}

		setFlingView(webview);
	}

	private WebChromeClient webChromeClient = new WebChromeClient() {
		// 设置页面顶端的进度条
		@Override
		public void onProgressChanged(WebView view, int newProgress) {
			super.onProgressChanged(view, newProgress);
			progressBar.setProgress(newProgress);
//			if (newProgress == 100){
//				if (isGuider){
//					LogUtil.d(TAG,"newProgress == 100 phone ; " + MainFragment.PHONE_NUM);
//					webview.loadUrl("javascript:isAndroid('" + MainFragment.PHONE_NUM + "')"	);
//				}
//			}

		}

		//设置title
		@Override
		public void onReceivedTitle(WebView view, String title) {
			super.onReceivedTitle(view, title);
//				titleTextView.setText(title);
		}
	};

	private WebViewClient webViewClient = new WebViewClient(){

		//网页开始加载
		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			super.onPageStarted(view, url, favicon);
			progressBar.setVisibility(View.VISIBLE);
//			if (isGuider){
//				LogUtil.d(TAG,"onPageStarted phone ; " + MainFragment.PHONE_NUM);
//				webview.loadUrl("javascript:isAndroid('" + MainFragment.PHONE_NUM + "')"	);
//			}
		}

		//网页加载完成
		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
			progressBar.setVisibility(View.GONE);
			isRootUrl = (url.startsWith(startStr) && url.endsWith(endStr));
			if (isGuider){
				LogUtil.d(TAG,"onPageFinished phone ; " + MainFragment.PHONE_NUM + "  url  "+ url + "\nisRootUrl " + isRootUrl);
				webview.loadUrl("javascript:isAndroid('" + MainFragment.PHONE_NUM + "')"	);
			}
		}

		// 设置跳转一直在当前activity
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			view.loadUrl(url);
			LogUtil.d(TAG, "shouldOverrideUrlLoading  url : " + url);
//			if (isGuider){
//				isGuiderUrl = url.startsWith(guiderUrl);
//			}
			return super.shouldOverrideUrlLoading(view, url);
		}

		// 重写网页出错后显示的网页内容
		@Override
		public void onReceivedError(WebView view, int errorCode,
									String description, String failingUrl) {
			super.onReceivedError(view, errorCode, description, failingUrl);
			view.loadUrl("file:///android_asset/nonetwork.html");
			LogUtil.d(TAG, "onReceivedError");
		}
	};

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (isRootUrl){
			return super.onKeyDown(keyCode, event);
		}
		if (keyCode == KeyEvent.KEYCODE_BACK && webview.canGoBack()){
			webview.goBack();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

}
