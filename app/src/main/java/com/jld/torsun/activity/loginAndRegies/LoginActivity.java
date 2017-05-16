package com.jld.torsun.activity.loginAndRegies;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.jld.torsun.MyApplication;
import com.jld.torsun.R;
import com.jld.torsun.activity.BaseActivity;
import com.jld.torsun.activity.FindBackPassWordActivity;
import com.jld.torsun.activity.MainFragment;
import com.jld.torsun.activity.countryCode.CountryPageActivity;
import com.jld.torsun.modle.User;
import com.jld.torsun.util.AndroidUtil;
import com.jld.torsun.util.Constats;
import com.jld.torsun.util.DialogUtil;
import com.jld.torsun.util.ListenerUtil;
import com.jld.torsun.util.LogUtil;
import com.jld.torsun.util.MD5Util;
import com.jld.torsun.util.MyHttpUtil;
import com.jld.torsun.util.MyHttpUtil.VolleyInterface;
import com.jld.torsun.util.ToastUtil;
import com.jld.torsun.util.UserInfo;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * 登录界面
 */
public class LoginActivity extends BaseActivity implements OnClickListener, OnTouchListener, OnGestureListener {
    private static final String TAG = "LoginActivity";
    private TextView tv_login_back;
    private ImageView iv_login_back;
    private EditText et_login_number, et_login_password;
    private Button bt_login_login;
    private TextView tv_login_forget_password;
    private TextView tv_login_country_code;
    private RequestQueue mRequestQueue;
    private SharedPreferences sp;
    private String account_name = "";
    private String code = "";// 验证码
    private String mobile;
    private String passwd;
    private Dialog mDialog;
    /**
     * 国家编码
     */
    private String CountryCode = "86";
    /**
     * 国家名字
     */
    private String CountryName = "中国";

    Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String data = (String) msg.obj;
            String url = Constats.HTTP_URL + Constats.LOGIN_FUN;
            LogUtil.d("LoginActivity", "ip2:" + data);
            params.put("ip", data);
            login(url, params);
        }
    };
    private Map<String, String> params;

    private GestureDetector mGestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_layout);
        sp = getSharedPreferences(Constats.SHARE_KEY, Context.MODE_PRIVATE);
        // mRequestQueue = Volley.newRequestQueue(this);
        MyApplication ma = (MyApplication) getApplication();
        mRequestQueue = ma.getRequestQueue();

        mGestureDetector = new GestureDetector((OnGestureListener) this);
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.login_layout_activity);
        relativeLayout.setLongClickable(true);
        relativeLayout.setOnTouchListener(this);

        tv_login_country_code = (TextView) findViewById(R.id.tv_login_country_code);
        tv_login_back = (TextView) findViewById(R.id.tv_login_back);
        iv_login_back = (ImageView) findViewById(R.id.iv_login_back);

        et_login_number = (EditText) findViewById(R.id.et_login_number);
        et_login_number.setInputType(InputType.TYPE_CLASS_PHONE);
        et_login_password = (EditText) findViewById(R.id.et_login_password);
        tv_login_forget_password = (TextView) findViewById(R.id.tv_login_forget_password);
        bt_login_login = (Button) findViewById(R.id.bt_login_login);
        ListenerUtil.setListener(this, tv_login_forget_password,
                tv_login_country_code, tv_login_back, iv_login_back,
                bt_login_login);
        if (!TextUtils.isEmpty(sp.getString(UserInfo.LOGIN_ACCOUT, ""))) {
            et_login_number.setText(sp.getString(UserInfo.LOGIN_ACCOUT, ""));
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        switch (id) {
            case R.id.iv_login_back:
                Intent iBack = new Intent();
                iBack.setClass(this, RepeatLoginActivity.class);
                startActivity(iBack);
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
                finish();
                break;
            case R.id.tv_login_back:
                Intent intentBack = new Intent();
                intentBack.setClass(this, RepeatLoginActivity.class);
                startActivity(intentBack);
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
                finish();
                break;
            case R.id.bt_login_login:
                mDialog = DialogUtil.createLoadingDialog(this, "正在登陆...");
                mDialog.show();

                mobile = et_login_number.getText().toString().trim();
                String original_passwd = et_login_password.getText().toString()
                        .trim();// 原始密码
                passwd = MD5Util.getMD5(Constats.S_KEY + original_passwd);// 密码加密
                String sign = MD5Util.getMD5(Constats.S_KEY + mobile + passwd);

                account_name = mobile;
                String mtype = Constats.ANDROID + "";
                String mno = AndroidUtil.getUniqueId(this);// 获取手机唯一标识

                String mversion = AndroidUtil.getHandSetInfo();// 手机型号
                String devbrand = AndroidUtil.getVendor();// 手机品牌
                LogUtil.d("againGetParams", "登录mobile：" + mobile + "	passwd："
                        + passwd + "sign：" + sign + "--mno" + mno);

                if (!TextUtils.isEmpty(mobile) && !TextUtils.isEmpty(passwd)) {
                    params = new HashMap<String, String>();
                    params.put("mobile", mobile);
                    params.put("passwd", passwd);
                    params.put("code", CountryCode);
                    params.put("sign", sign);
                    params.put("mtype", mtype);
                    params.put("mno", mno);
                    params.put("mversion", mversion);
                    params.put("devbrand", devbrand);
                    AndroidUtil.getLocalHostIp(handler);
                } else {
                    ToastUtil.showToast(this, R.string.t_login_info, 3000);
                }
                break;
            case R.id.tv_login_forget_password:
                Intent forget_intent = new Intent();
                forget_intent.setClass(this, FindBackPassWordActivity.class);
                startActivity(forget_intent);
                break;
            case R.id.tv_login_country_code:// 获取国家编码
                Intent intent = new Intent(LoginActivity.this,
                        CountryPageActivity.class);
                startActivityForResult(intent, 888);
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
                break;
            default:
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
                tv_login_country_code.setText(CountryName + "(+" + CountryCode
                        + ")");
            }
        }

    }

    /**
     * 登录 * {"msg":"登陆成功","result":0,"item":
     * {"img":"http:\/\/img.tucson.net.cn\/2015-10\/1445022971_34247.jpg",
     * "userid":"41","username":"hh","nick":"yaozu","mobile":"13823133104"}}
     */
    public void login(String url, Map<String, String> params) {
        MyHttpUtil.VolleyPost(url, this, params, new VolleyInterface() {
            @Override
            public void win(JSONObject response) {
                // TODO Auto-generated method stub
                try {
                    if (response.toString().contains("doctype html")) {
                        ToastUtil.showToast(LoginActivity.this,
                                R.string.t_frag_set_network_approve, 3000);
                    }
                    LogUtil.d(TAG, "loginresponse = " + response.toString());
                    int result = response.getInt("result");
                    String message = response.getString("msg");
                    // LogUtil.d(TAG, "result = " + result);
                    if (0 == result) {
                        ToastUtil.showToast(LoginActivity.this,
                                R.string.t_login_suc, 3000);
                        JSONObject item = response.getJSONObject("item");
                        // Log.d(TAG, "item = " + item);
                        Gson gson = new Gson();
                        User user = gson.fromJson(item.toString(), User.class);
                        sp.edit().putString(UserInfo.JSONSTR, item.toString())
                                .commit();
                        sp.edit().putBoolean(UserInfo.LOGINING, true).commit();
                        sp.edit().putString(UserInfo.USER_ID, user.userid)
                                .commit();
                        sp.edit().putString(UserInfo.NIK, user.nick).commit();
                        sp.edit().putString(UserInfo.USER_NAME, user.username)
                                .commit();
                        sp.edit().putString(UserInfo.HEAD_ICON_URL, user.img)
                                .commit();
                        sp.edit().putString(UserInfo.LOGIN_ACCOUT, user.mobile)
                                .commit();

                        sp.edit().putString(UserInfo.PASS_WORD, passwd)
                                .commit();// 保存密码
                        sp.edit().putString(UserInfo.LOGIN_ACCOUT, mobile)
                                .commit();// 保存电话号码

                        Intent login_intent = new Intent();
                        login_intent.setClass(LoginActivity.this,
                                MainFragment.class);
                        login_intent.putExtra("type", "login");
                        startActivity(login_intent);
                        //友盟统计
                        MobclickAgent.onProfileSignIn(user.userid);
                        if (mDialog != null) {
                            mDialog.dismiss();
                        }
                        finish();
                    } else {

                        ToastUtil.showToast(LoginActivity.this, message + "",
                                3000);
                        if (mDialog != null) {
                            mDialog.dismiss();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void error(VolleyError error) {
                // TODO Auto-generated method stub

                ToastUtil.showToast(LoginActivity.this,
                        R.string.t_frag_set_network_err, 3000);
                if (mDialog != null) {
                    mDialog.dismiss();
                }
            }

        });

    }

    /********************************************
     * 滑屏切换
     *****************************************/

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    private int verticalMinDistance = 100;
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
            Intent regiest = new Intent();
            regiest.setClass(this, RepeatLoginActivity.class);
            startActivity(regiest);
            overridePendingTransition(R.anim.left_in, R.anim.right_out);
            finish();

        }
        return false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }

}
