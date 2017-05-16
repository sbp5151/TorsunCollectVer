package com.jld.torsun.activity.messageCenter;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

import com.android.volley.VolleyError;
import com.jld.torsun.R;
import com.jld.torsun.activity.BaseActivity;
import com.jld.torsun.util.AndroidUtil;
import com.jld.torsun.util.Constats;
import com.jld.torsun.util.DensityUtil;
import com.jld.torsun.util.LogUtil;
import com.jld.torsun.util.MD5Util;
import com.jld.torsun.util.MyHttpUtil;
import com.jld.torsun.util.ToastUtil;

import org.json.JSONObject;

import java.util.HashMap;

public class SystemMessageWebActivity extends BaseActivity {

    private static final String TAG = "SystemMessageWebActivity";
    private WebView webview;
    private String url;
    private ProgressBar progressBar, progressBar2;

    private View titleView;
    private TextView titleTextView;
    private ImageButton backIV;

    private String userId;
    private String newsId;
    private String sign;

    private static final int RETURN_URL = 0xaaaa;
    private static final int RETURN_FAIL = 0xaaab;
    private static final int UPDATA_FAIL = 0xaaac;
    public final String LoadreUrl = Constats.HTTP_URL + Constats.MESSAGE_GET_TUAN_ITEM_URL;
    private final String requestReadUrl = Constats.HTTP_URL + Constats.MESSAGE_GET_TUAN_READ_URL;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int type = msg.what;
            switch (type) {
                case RETURN_URL:
                    webview.loadUrl(url);
                    break;
                case RETURN_FAIL:
                    webview.loadUrl("file:///android_asset/nonetwork.html");
                    break;
                case UPDATA_FAIL:
                    ToastUtil.showToast(SystemMessageWebActivity.this, R.string.loaddata_un, 3000);
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            SystemMessageWebActivity.this.finish();
                        }
                    }, 3000);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LogUtil.d(TAG,"onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.web_activity_layout_1);
        initData();
        initTopView();
        progressBar = (ProgressBar) findViewById(R.id.web_progressBar_1);
        progressBar2 = (ProgressBar) findViewById(R.id.web_progressBar2_1);
        final TextView tv_prompt = (TextView) findViewById(R.id.textView1_1);

        webview = (WebView) findViewById(R.id.webview_1);
        setFlingView(webview);

        WebSettings settings = webview.getSettings();
        // 设置WebView属性，能够执行Javascript脚本
        settings.setJavaScriptEnabled(true);
        //适应屏幕
        //webview.setInitialScale(50);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setSupportZoom(true);

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

        // 设置Web视图
        webview.setWebViewClient(new HelloWebViewClient());
        upload();
        read();
        // 加载需要显示的网页
        //webview.loadUrl(url);
    }

    private void initData() {
        //url = getIntent().getStringExtra("url");
        userId = getIntent().getStringExtra("userId");
        newsId = getIntent().getStringExtra("newsId");
        sign = MD5Util.getMD5(Constats.S_KEY + userId + newsId);
        LogUtil.d(TAG, "userId" + userId + ":newsId" + newsId);
    }

    private void initTopView() {
        titleView = findViewById(R.id.web_title_1);
        titleTextView = (TextView) titleView.findViewById(R.id.tv_title_message_center_title);
        backIV = (ImageButton) titleView.findViewById(R.id.iv_title_message_center_back);
        TextView textView = (TextView) titleView.findViewById(R.id.tv_title_message_center_release);
        textView.setVisibility(View.GONE);
        titleTextView.setText(R.string.app_name);
        backIV.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                //finish();
                if (webview.canGoBack()) {
                    webview.goBack();
                } else {
                    finish();
                }
            }
        });
    }

    private boolean isfristFocus = true;

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (isfristFocus && hasFocus) {
            isfristFocus = false;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                float height = AndroidUtil.getStatusHeight(this);
                LogUtil.d("--------------状态栏的高度为:" + height);

                int viewHeight = DensityUtil.px2dip(this, (216f - height * 2));
                LogUtil.d("--------------viewHeight的高度:" + viewHeight);
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) titleView.getLayoutParams();
                params.height = viewHeight;
                titleView.setLayoutParams(params);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isfristFocus = true;
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
        if (keyCode == KeyEvent.KEYCODE_BACK && webview.canGoBack()) {
            webview.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void upload() {
        HashMap<String, String> params = new HashMap<>();
        params.put("id", newsId);
        params.put("userid", userId);
        params.put("sign", sign);
        MyHttpUtil.VolleyPost(LoadreUrl, this, params, new MyHttpUtil.VolleyInterface() {
            @Override
            public void win(JSONObject response) {
                try {
                    LogUtil.d(TAG, "response = " + response.toString());
                    int result = response.getInt("result");
                    if (0 == result) {
                        String desc = response.getString("desc");
                        LogUtil.d(TAG, "desc: = " + desc);
                        if (desc.startsWith("http:") || desc.startsWith("https:") || desc.startsWith("ftp:")) {
                            url = desc;
                            mHandler.sendEmptyMessage(RETURN_URL);
                        } else {
                            mHandler.sendEmptyMessage(UPDATA_FAIL);
                        }
                    } else {
                        mHandler.sendEmptyMessage(RETURN_FAIL);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void error(VolleyError error) {
                mHandler.sendEmptyMessage(RETURN_FAIL);
            }
        });
    }

    private void read() {
        HashMap<String, String> params = new HashMap<>();
        params.put("userid", userId);
        params.put("nid", newsId);
        params.put("sign", MD5Util.getMD5(Constats.S_KEY + userId + newsId));
        LogUtil.d(TAG, "userId：" + userId + "--newsId" + newsId + "--" + requestReadUrl);
        MyHttpUtil.VolleyPost(requestReadUrl, this, params, new MyHttpUtil.VolleyInterface() {
            @Override
            public void win(JSONObject response) {
                LogUtil.d(TAG, "response = " + response.toString());
            }

            @Override
            public void error(VolleyError error) {
            }
        });
    }
}
