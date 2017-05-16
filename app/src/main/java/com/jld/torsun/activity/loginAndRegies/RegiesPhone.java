package com.jld.torsun.activity.loginAndRegies;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonRequest;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.jld.torsun.MyApplication;
import com.jld.torsun.R;
import com.jld.torsun.activity.BaseManageActivity;
import com.jld.torsun.activity.countryCode.CountryPageActivity;
import com.jld.torsun.http.VolleyJsonUtil;
import com.jld.torsun.util.Constats;
import com.jld.torsun.util.DialogUtil;
import com.jld.torsun.util.LogUtil;
import com.jld.torsun.util.MD5Util;
import com.jld.torsun.util.ToastUtil;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RegiesPhone extends BaseManageActivity implements OnClickListener {

    /**
     * 获取国家区号
     */
    private TextView tv_regiest_code;
    /**
     * 手机号
     */
    private EditText et_regiest_mobile;
    /**
     * 密码
     */
    private EditText et_regiest_password;
    /**
     * 下一步
     */
    private Button bt_regiest_next;
    /**
     * 上一步
     */
    private LinearLayout ll_register_phone_back;
    /**
     * 是否激活下一步
     */
    private Boolean threadStop = true;

    /**
     * 昵称
     */
    private String nike;
    /**
     * 真实姓名
     */
    private String name;
    /**
     * 一键删除
     */
    private Boolean delete;
    private static final String TAG = "RegiesPhone";
    private RequestQueue mRequestQueue;
    private Dialog mDialog;
    /**
     * 验证码
     */
    private String mcode;
    /**
     * 国家编码
     */
    private String CountryCode = "86";
    /**
     * 国家名字
     */
    private String CountryName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regies_phone);
        CountryName=getResources().getString(R.string.code_country_name);
        Intent intent = getIntent();
        nike = intent.getStringExtra("nike");// 获取传过来的昵称和姓名
        name = intent.getStringExtra("name");
        // 获取volley队列
        MyApplication ma = (MyApplication) getApplication();
        mRequestQueue = ma.getRequestQueue();
        initView();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        threadStop = true;
    }

    public void initView() {
        tv_regiest_code = (TextView) findViewById(R.id.tv_regies_country_code2);// 获取国家验证码
        et_regiest_mobile = (EditText) findViewById(R.id.et_regies_mobile2);// 手机号码
        et_regiest_password = (EditText) findViewById(R.id.et_regiest_password2);// 密码
        bt_regiest_next = (Button) findViewById(R.id.bt_regiest_phone_next);// 下一步
        ll_register_phone_back = (LinearLayout) findViewById(R.id.ll_register_phone_back);// 上一步

        bt_regiest_next.setEnabled(false);// 不激活

        tv_regiest_code.setOnClickListener(this);// 设置监听
        bt_regiest_next.setOnClickListener(this);
        ll_register_phone_back.setOnClickListener(this);
        mDialog = DialogUtil.createLoadingDialog(RegiesPhone.this, "获取验证码....");
        new Thread(runnable).start();// 开启线程
    }

    @Override
    public void onClick(View v) {

        int id = v.getId();
        switch (id) {
            case R.id.tv_regies_country_code2:// 获取国家编码

                Intent intent = new Intent(RegiesPhone.this,
                        CountryPageActivity.class);
                startActivityForResult(intent, 888);
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
                break;
            case R.id.bt_regiest_phone_next:// 下一步
                mDialog.show();
                bt_regiest_next.setEnabled(false);
                getCode();// 获取验证码
                new Thread(new Runnable() {// 睡三秒在唤醒，以免多次点击
                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                // TODO Auto-generated method stub
                                bt_regiest_next.setEnabled(true);
                            }
                        });
                    }
                }).start();

                break;
            case R.id.ll_register_phone_back://上一步
                finish();
                overridePendingTransition(R.anim.page_left_in, R.anim.page_right_out);
                break;
        }

    }

    /**
     * 接收传过来的国家编码
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            CountryCode = data.getExtras().getString("CountryCode");
            CountryName = data.getExtras().getString("CountryName");
            if (!TextUtils.isEmpty(CountryCode)
                    && !TextUtils.isEmpty(CountryName)) {
                tv_regiest_code.setText(CountryName + "(+" + CountryCode + ")");
            }
        }

    }

    ;

    /**
     * 电话和密码不为空下一步才激活
     */
    Runnable runnable = new Runnable() {

        @Override
        public void run() {
            while (threadStop) {
                if (!TextUtils.isEmpty(et_regiest_mobile.getText().toString()
                        .trim())
                        && !TextUtils.isEmpty(et_regiest_password.getText()
                        .toString().trim())
                        && !bt_regiest_next.isEnabled()) {// 如果电话密码不为空且非激活状态
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            bt_regiest_next.setEnabled(true);// 设置为激活状态
                        }
                    });
                } else if ((TextUtils.isEmpty(et_regiest_mobile.getText()
                        .toString().trim()) || TextUtils
                        .isEmpty(et_regiest_password.getText().toString()
                                .trim()))
                        && bt_regiest_next.isEnabled()) {// 如果电话密码为空且激活状态
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

    /**
     * 获取验证码
     */
    public void getCode() {

        String getsign = MD5Util.getMD5(Constats.S_KEY
                + et_regiest_mobile.getText().toString().trim());
        String geturl = Constats.HTTP_URL + Constats.SMS_FUN;
        Map<String, String> sparams = new HashMap<String, String>();
        sparams.put("mobile", et_regiest_mobile.getText().toString().trim());// 电话
        String countryCode = tv_regiest_code.getText().toString().trim();// 获取国家编码
        int startIndex = countryCode.indexOf("+") + 1;
        int endIndex = countryCode.length() - 1;
        sparams.put("da", countryCode.substring(startIndex, endIndex));// 国家编码
        sparams.put("sign", getsign);// 签名
        getSecurityCode(geturl, sparams);
    }

    /**
     * 从服务器获取验证码
     */
    @SuppressWarnings("unchecked")
    private void getSecurityCode(String url, Map<String, String> params) {
        JsonRequest<JSONObject> jsonRequest = VolleyJsonUtil
                .createJsonObjectRequest(Method.POST, url, params,
                        new Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {

                                    JsonParser parser = new JsonParser();
                                    JsonElement element = parser.parse(response
                                            .toString());

                                    int result;
                                    int code;
                                    try {
                                        result = response.getInt("result");
                                        code = response.getInt("code");
                                        LogUtil.d(TAG,"code:"+code);
                                    } catch (Exception e) {// 网络可能被拦截
                                        ToastUtil
                                                .showToast(
                                                        RegiesPhone.this,
                                                        R.string.t_frag_set_network_approve,
                                                        4000);
                                        return;
                                    }
                                    if (0 == result) {// 获取成功设置mcode,启动计时
                                        Intent intent = new Intent(
                                                RegiesPhone.this,
                                                RegiesCode.class);
                                        Bundle bundle = new Bundle();
                                        bundle.putString("nike", nike);// 传递昵称
                                        bundle.putString("name", name);// 传递姓名
                                        bundle.putString("mcode", code + "");// 验证码

                                        bundle.putString("mobile",
                                                et_regiest_mobile.getText()
                                                        .toString().trim());// 传递电话
                                        bundle.putString("password",
                                                et_regiest_password.getText()
                                                        .toString().trim());// 传递密码

                                        String countryCode = tv_regiest_code
                                                .getText().toString().trim();// 获取国家编码
                                        int startIndex = countryCode
                                                .indexOf("+") + 1;
                                        int endIndex = countryCode.length() - 1;

                                        bundle.putString("code",
                                                countryCode.substring(
                                                        startIndex, endIndex));// 传递国家编码

                                        intent.putExtras(bundle);
                                        startActivity(intent);// 跳转页面
                                        overridePendingTransition(R.anim.right_in, R.anim.left_out);
                                        mDialog.cancel();
                                        threadStop = false;
                                    } else if (1004 == code) {
                                        mDialog.cancel();
                                        ToastUtil.showToast(RegiesPhone.this,
                                                R.string.t_regiest_info_phone,
                                                3000);
                                    } else {
                                        mDialog.cancel();
                                        ToastUtil
                                                .showToast(
                                                        RegiesPhone.this,
                                                        R.string.t_regiest_get_code_err,
                                                        3000);
                                    }
                                } catch (Exception e) {
                                    mDialog.cancel();

                                    e.printStackTrace();
                                    ToastUtil.showToast(RegiesPhone.this,
                                            R.string.t_frag_set_network_err,
                                            3000);
                                }
                            }
                        }, new ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                mDialog.cancel();
                                ToastUtil.showToast(RegiesPhone.this,
                                        R.string.t_frag_set_network_err, 3000);
                            }
                        });
        if (null != mRequestQueue) {
            mRequestQueue.add(jsonRequest);
        }
    }
}
