package com.jld.torsun.activity.messageCenter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonRequest;
import com.jld.torsun.MyApplication;
import com.jld.torsun.R;
import com.jld.torsun.activity.BaseActivity;
import com.jld.torsun.http.VolleyJsonUtil;
import com.jld.torsun.util.Constats;
import com.jld.torsun.util.LogUtil;
import com.jld.torsun.util.MD5Util;
import com.jld.torsun.util.ToastUtil;
import com.jld.torsun.util.UserInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;
import cn.sharesdk.onekeyshare.OnekeyShareTheme;
import cn.sharesdk.onekeyshare.ShareContentCustomizeCallback;
import cn.sharesdk.sina.weibo.SinaWeibo;

public class Share_Web extends BaseActivity {

    private ImageButton ib_back;
    private TextView tv_content;
    private TextView tv_release;
    private ImageButton iv_share;
    private WebView mWebView;
    private String url;

    public static final String TAG = "Share_Web";
    private ProgressBar progressBar;

    private String share_title;
    private String newsId;
    private String share_content;
    private Bitmap share_icon;
    private SharedPreferences sp;
    private String userid;
    private final String requestReadUrl = Constats.HTTP_URL + Constats.MESSAGE_GET_TUAN_READ_URL;
    private RequestQueue mRequestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share__web);
//        manageException();
        initData();
        initView();
        initWebView();
    }

    private void initWebView() {
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.addJavascriptInterface(new InJavaScriptLocalObj(), "local_obj");
        mWebView.getSettings().setSupportZoom(true);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.requestFocus();
        mWebView.getSettings().setUseWideViewPort(true);
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setSupportZoom(true);
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.loadUrl(url);

        //不用浏览器在webview中显示
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                view.loadUrl("javascript:window.local_obj.showSource('<head>'+" +
                        "document.getElementsByTagName('html')[0].innerHTML+'</head>');");
                super.onPageFinished(view, url);
            }
        });
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onReceivedTitle(WebView view, String title) {
                if (!TextUtils.isEmpty(title)) {
                    share_title = title;
                    tv_content.setText(share_title);
                }
                super.onReceivedTitle(view, title);
            }

            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                LogUtil.d(TAG, "consoleMessage:" + consoleMessage.message());
                return super.onConsoleMessage(consoleMessage);
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                LogUtil.d(TAG, "newProgress:" + newProgress);
                progressBar.setProgress(newProgress);
                if (newProgress == 100)
                    progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void initData() {
        url = getIntent().getStringExtra("url");
        share_title = getIntent().getStringExtra("title");
        newsId = getIntent().getStringExtra("newsId");
        sp = this.getSharedPreferences(Constats.SHARE_KEY, Context.MODE_PRIVATE);
        userid = sp.getString(UserInfo.USER_ID, "");
        mRequestQueue = ((MyApplication) getApplication()).getRequestQueue();
        LogUtil.d(TAG, "url:" + url);
        LogUtil.d(TAG, "title:" + share_title);
        LogUtil.d(TAG, "newsId:" + newsId);
        new Thread(readRun).start();
//        String posturl = posturl(url);
//        Document html = Jsoup.parse(posturl);
//        share_title = html.title();
    }

    private void initView() {
        progressBar = (ProgressBar) findViewById(R.id.pb_share_web);
        mWebView = (WebView) findViewById(R.id.wb_share_content);
        View title = findViewById(R.id.message_shareWeb_title);
        ib_back = (ImageButton) title.findViewById(R.id.iv_title_message_center_back);
        ib_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        tv_content = (TextView) title.findViewById(R.id.tv_title_message_center_title);
        tv_content.setText(share_title);
        tv_release = (TextView) title.findViewById(R.id.tv_title_message_center_release);
        tv_release.setVisibility(View.GONE);
        iv_share = (ImageButton) title.findViewById(R.id.iv_title_message_center_edit);
        iv_share.setBackgroundResource(R.mipmap.share_2);
        iv_share.setVisibility(View.VISIBLE);
        iv_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showShare();
            }
        });
    }

    public void showShare() {
        OnekeyShare oks = new OnekeyShare();
        //ShareSDK快捷分享提供两个界面第一个是九宫格 CLASSIC  第二个是SKYBLUE
        oks.setTheme(OnekeyShareTheme.CLASSIC);
        // 令编辑页面显示为Dialog模式
//        oks.setDialogMode();
        // 在自动授权时可以禁用SSO方式
        oks.disableSSOWhenAuthorize();
        //oks.setAddress("12345678901"); //分享短信的号码和邮件的地址
        oks.setTitle("Torsun");
        oks.setTitleUrl(url);
        oks.setText(share_title);
        oks.setImageUrl("http://www.torsun.cn/images/yuyin.png");  //分享sdcard目录下的图片
        oks.setUrl(url);
        oks.setShareContentCustomizeCallback(new ShareContentCustomizeCallback() {
            @Override
            public void onShare(Platform platform, Platform.ShareParams paramsToShare) {
                if(platform.getName().equalsIgnoreCase(SinaWeibo.NAME)){//新浪微博特殊设置网页链接格式
                    paramsToShare.setText(share_title + "  " + url);
                }
            }
        });
        // 启动分享
        oks.show(Share_Web.this);
    }

    private void showShare2() {
        ShareSDK.initSDK(this);
        OnekeyShare oks = new OnekeyShare();
        //关闭sso授权
        oks.disableSSOWhenAuthorize();

// 分享时Notification的图标和文字  2.5.9以后的版本不调用此方法
        //oks.setNotification(R.drawable.ic_launcher, getString(R.string.app_name));
        // title标题，印象笔记、邮箱、信息、微信、人人网和QQ空间使用
        oks.setTitle(getString(R.string.share));
        // titleUrl是标题的网络链接，仅在人人网和QQ空间使用
        oks.setTitleUrl("http://sharesdk.cn");
        // text是分享文本，所有平台都需要这个字段
        oks.setText("我是分享文本");
        // imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
        //oks.setImagePath("/sdcard/test.jpg");//确保SDcard下面存在此张图片
        // url仅在微信（包括好友和朋友圈）中使用
        oks.setUrl(url);
        // comment是我对这条分享的评论，仅在人人网和QQ空间使用
        oks.setComment("我是测试评论文本");
        // site是分享此内容的网站名称，仅在QQ空间使用
        oks.setSite(getString(R.string.app_name));
        // siteUrl是分享此内容的网站地址，仅在QQ空间使用
        oks.setSiteUrl("http://sharesdk.cn");


// 启动分享GUI
        oks.show(this);
    }

    /**
     * 朕已阅
     */
    Runnable readRun = new Runnable() {
        @Override
        public void run() {
            HashMap<String, String> params = new HashMap<>();
            params.put("userid", userid);
            params.put("nid", newsId);
            params.put("sign", MD5Util.getMD5(Constats.S_KEY + userid + newsId));
            LogUtil.d(TAG, "userId：" + userid + "--newsId" + newsId + "--" + requestReadUrl);
            JsonRequest jsonRequest = VolleyJsonUtil.createJsonObjectRequest(Request.Method.POST, requestReadUrl, params, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject jsonObject) {
                    try {
                        int result = jsonObject.getInt("result");
                        LogUtil.d(TAG, "jsonObject：" + jsonObject);
                        if (result == 0) {
                        } else {
                            ToastUtil.showToast(Share_Web.this, getResources().getString(R.string.to_server_failed), 4000);
                        }
                    } catch (JSONException e) {
                        LogUtil.d(TAG, "e：" + e.toString());
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    ToastUtil.showToast(Share_Web.this, getResources().getString(R.string.t_frag_set_network_err), 4000);
                }
            });
            LogUtil.d(TAG, "mRequestQueue：" + mRequestQueue);
            if (null != mRequestQueue) {
                mRequestQueue.add(jsonRequest);
            }
        }
    };

    //改写物理按键——返回的逻辑
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mWebView.canGoBack()) {
                mWebView.goBack();//返回上一页面
                return true;
            } else {
                finish();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    final class InJavaScriptLocalObj {
        public void showSource(String html) {
            LogUtil.d(TAG, "html-------:" + html);
        }
    }
}
