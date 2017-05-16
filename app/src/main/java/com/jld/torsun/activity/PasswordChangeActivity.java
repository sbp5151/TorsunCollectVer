package com.jld.torsun.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
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
import com.jld.torsun.http.VolleyJsonUtil;
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
 * 修改密码
 */
public class PasswordChangeActivity extends BaseActivity implements OnClickListener{
    // private static final String TAG = "PasswordChangeActivity";
    private View title_change_password;
    private EditText et_old_password, et_new_password, et_sure_new_password;
    private Button bt_change_password_complete;
    // private String number = "";
    // private String code = "";

    private RequestQueue mRequestQueue;
    private SharedPreferences sp;
    private String newpassword;

    Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String data = (String) msg.obj;
            String url = Constats.HTTP_URL + Constats.CHANGE_PASSWORD_FUN;
            LogUtil.d("LoginActivity", "ip2:" + data);
            params.put("ip", data);
            changPassword(url, params);
        }
    };
    private Map<String, String> params;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password_layout);

        LinearLayout linearLayout=(LinearLayout)findViewById(R.id.ll_change_password_layout);
        linearLayout.setLongClickable(true);
        setFlingView(linearLayout);

        // mRequestQueue = Volley.newRequestQueue(this);
        MyApplication ma = (MyApplication) getApplication();
        mRequestQueue = ma.getRequestQueue();
        sp = getSharedPreferences(Constats.SHARE_KEY, Context.MODE_PRIVATE);

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
                LinearLayout.LayoutParams params =(LinearLayout.LayoutParams)title_change_password.getLayoutParams();
                params.height=viewHeight;
                title_change_password.setLayoutParams(params);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isfristFocus=true;
    }

    private void initView() {
        title_change_password = findViewById(R.id.title_change_password);
        TextView title = (TextView) title_change_password
                .findViewById(R.id.tv_view_title_message);
        title.setText(R.string.change_password);
        ImageView image_title_back = (ImageView) title_change_password
                .findViewById(R.id.iv_view_title_back);
        TextView tv_sure = (TextView) title_change_password.findViewById(R.id.tv_set_title_sure);
        tv_sure.setVisibility(View.GONE);

        image_title_back.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                finish();
            }
        });
        et_old_password = (EditText) findViewById(R.id.et_old_password);
        et_old_password.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2,
                                      int arg3) {

            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1,
                                          int arg2, int arg3) {

            }

            @Override
            public void afterTextChanged(Editable text) {
                if (text.length() > 0) {

                }
            }
        });
        et_new_password = (EditText) findViewById(R.id.et_new_password);
        et_sure_new_password = (EditText) findViewById(R.id.et_sure_new_password);
        bt_change_password_complete = (Button) findViewById(R.id.bt_change_password_complete);
        bt_change_password_complete.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {

            case R.id.bt_change_password_complete:

                String original_oldpassword = et_old_password.getText().toString()
                        .trim();// 旧密码
                String original_newpassword = et_new_password.getText().toString()
                        .trim();// 新密码1
                String original_newpasswordSure = et_sure_new_password.getText()
                        .toString().trim();// 新密码2
                // 加密
                if (TextUtils.isEmpty(original_newpassword)||TextUtils.isEmpty(original_newpasswordSure)){
                    ToastUtil.showToast(this,R.string.t_reset_pwd_new_pwd,3000);
                    break;
                }
                String oldpassword = MD5Util.getMD5(Constats.S_KEY
                        + original_oldpassword);
                newpassword = MD5Util.getMD5(Constats.S_KEY + original_newpassword);
                String newpasswordSure = MD5Util.getMD5(Constats.S_KEY
                        + original_newpasswordSure);

                String mobile = sp.getString(UserInfo.LOGIN_ACCOUT, "");
                String sign = MD5Util.getMD5(Constats.S_KEY + mobile + oldpassword
                        + newpassword);

                String mtype = Constats.ANDROID + "";
                String mno = AndroidUtil.getUniqueId(this);
                String mversion = AndroidUtil.getHandSetInfo();
                String devbrand = AndroidUtil.getVendor();
//			String ip = AndroidUtil.getLocalHostIp();
                String ip = "";
                if (TextUtils.isEmpty(oldpassword)
                        || TextUtils.isEmpty(newpassword)
                        || TextUtils.isEmpty(newpasswordSure)) {
                    ToastUtil.showToast(this, R.string.t_login_info, 3000);
                    break;
                }

                if (!TextUtils.isEmpty(newpassword)
                        && !newpassword.equals(newpasswordSure)) {
                    ToastUtil.showToast(this, R.string.t_pwd_change_info_2, 3000);
                    break;
                }

                params = new HashMap<String, String>();
                params.put("userid", sp.getString(UserInfo.USER_ID, ""));
                params.put("mobile", sp.getString(UserInfo.LOGIN_ACCOUT, ""));

                params.put("passwd", oldpassword);
                params.put("newpasswd", newpassword);
                params.put("newpasswd2", newpasswordSure);
                params.put("sign", sign);
                params.put("mtype", mtype);
                params.put("mno", mno);
                params.put("mversion", mversion);
                params.put("devbrand", devbrand);
                AndroidUtil.getLocalHostIp(handler);
                break;
        }
    }

    /**
     * 修改密码
     */
    @SuppressWarnings("unchecked")
    public void changPassword(String url, Map<String, String> params) {
        JsonRequest<JSONObject> jsonRequest = VolleyJsonUtil
                .createJsonObjectRequest(Method.POST, url, params,
                        new Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    int result = response.getInt("result");
                                    String message = response.getString("msg");
                                    if (0 == result) {
                                        ToastUtil
                                                .showToast(
                                                        PasswordChangeActivity.this,
                                                        R.string.t_pwd_change_info_sure,
                                                        3000);
                                        sp.edit()
                                                .putString(UserInfo.PASS_WORD,
                                                        newpassword).commit();// 保存密码
                                        finish();
                                    } else {
                                        ToastUtil.showToast(
                                                PasswordChangeActivity.this,
                                                message, 3000);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                ToastUtil.showToast(
                                        PasswordChangeActivity.this,
                                        R.string.t_frag_set_network_err, 3000);
                            }
                        });
        if (null != mRequestQueue) {
            mRequestQueue.add(jsonRequest);
        }
    }

}
