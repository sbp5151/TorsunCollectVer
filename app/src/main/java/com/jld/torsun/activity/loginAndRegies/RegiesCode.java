package com.jld.torsun.activity.loginAndRegies;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.util.Log;
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
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.jld.torsun.MyApplication;
import com.jld.torsun.R;
import com.jld.torsun.activity.BaseManageActivity;
import com.jld.torsun.activity.MainFragment;
import com.jld.torsun.http.VolleyJsonUtil;
import com.jld.torsun.modle.ActionConstats;
import com.jld.torsun.modle.User;
import com.jld.torsun.util.AndroidUtil;
import com.jld.torsun.util.Constats;
import com.jld.torsun.util.DialogUtil;
import com.jld.torsun.util.LogUtil;
import com.jld.torsun.util.MD5Util;
import com.jld.torsun.util.MyHttpUtil;
import com.jld.torsun.util.ToastUtil;
import com.jld.torsun.util.UserInfo;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 验证手机页面
 * <p/>
 * 晶凌达科技有限公司所有， 受到法律的保护，任何公司或个人，未经授权不得擅自拷贝。
 *
 * @creator 单柏平 <br/>
 * @create-time 2016-1-4 下午12:31:10
 */
public class RegiesCode extends BaseManageActivity implements OnClickListener {

    /**
     * 验证码
     */
    private EditText et_regies_code;
    /**
     * 读秒倒计时
     */
    private TextView tv_regies_count_down;
    /**
     * 完成注册
     */
    private Button bt_finish;
    /**
     * 国家编码
     */
    private TextView tv_regies_country_code;
    /**
     * 手机号码
     */
    private TextView tv_regies_mobile;
    /**
     * 上一步
     */
    private LinearLayout ll_regies_code_back;
    /**
     * 昵称
     */
    private String nike;
    /**
     * 真实姓名
     */
    private String name;
    /**
     * 手机号码
     */
    private String mobile;
    /**
     * 密码
     */
    private String password;
    /**
     * 国家编码
     */
    private String countryCode;
    /**
     * 验证码
     */
    private String mcode = "";
    /**
     * 是否激活下一步
     */
    private Boolean threadStop = false;
    /**
     * 清空数据线程
     */
    private Boolean deleteThreadStop = false;
    /**
     * volley队列
     */
    private RequestQueue mRequestQueue;
    private TimeCount time;

    private static final String TAG = "RegiesCode";
    private SharedPreferences sp;

    private SmsBroadcastReceiver smsBroadcastReceiver;
    /**
     * 验证码集合
     */
    private List<String> verifiCodes;
    Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String data = (String) msg.obj;
            String url = Constats.HTTP_URL + Constats.REGIEST_FUN;
            LogUtil.d("LoginActivity", "ip2:" + data);
            params.put("ip", data);

            httpRegiest(url, params);
        }
    };
    private Map<String, String> params;
    private Dialog regiesDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regies_code);

        // 获取volley队列
        MyApplication ma = (MyApplication) getApplication();
        mRequestQueue = ma.getRequestQueue();

        sp = getSharedPreferences(Constats.SHARE_KEY, Context.MODE_PRIVATE);

        MyApplication mApplication = (MyApplication) getApplication();
        verifiCodes = mApplication.getverifiCodes();

        smsBroadcastReceiver = new SmsBroadcastReceiver(this);// 注册短信广播接收者
        time = new TimeCount(60000, 1000);// 构造CountDownTimer对象
        time.start();
        // 获取传递过来的值
        getBundle();
        // 初始化界面
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        threadStop = false;
        deleteThreadStop = false;
        MobclickAgent.onPageStart("注册界面"); // （仅有Activity的应用中SDK自动调用，不需要单独写）保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息。"SplashScreen"为页面名称，可自定义
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("注册界面"); // （仅有Activity的应用中SDK自动调用，不需要单独写）保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息。"SplashScreen"为页面名称，可自定义
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != smsBroadcastReceiver) {
            unregisterReceiver(smsBroadcastReceiver);
        }
    }

    /**
     * 初始化页面
     */
    public void initView() {
        tv_regies_country_code = (TextView) findViewById(R.id.tv_regies_code_country_code);// 国家编码
        tv_regies_count_down = (TextView) findViewById(R.id.tv_regies_count_down);// 读秒倒计时
        tv_regies_mobile = (TextView) findViewById(R.id.tv_regies_code_phone);// 电话
        bt_finish = (Button) findViewById(R.id.bt_regiest_finish);// 完成注册
        et_regies_code = (EditText) findViewById(R.id.et_regies_set_code);// 验证码
        ll_regies_code_back = (LinearLayout) findViewById(R.id.ll_register_code_back);// 上一步

        tv_regies_country_code.setText("+" + countryCode);
        tv_regies_mobile.setVisibility(View.VISIBLE);
        tv_regies_mobile.setText(mobile);
        tv_regies_count_down.setOnClickListener(this);
        ll_regies_code_back.setOnClickListener(this);
        bt_finish.setOnClickListener(this);
        new Thread(mRunnable).start();// 开启检查完成注册线程
    }

    @Override
    public void onClick(View v) {

        int id = v.getId();
        switch (id) {
            case R.id.tv_regies_count_down:// 倒计时
                tv_regies_count_down.setClickable(false);
                getCode();
                break;

            case R.id.ll_register_code_back:// 上一步
                finish();
                overridePendingTransition(R.anim.page_left_in,
                        R.anim.page_right_out);
                break;
            case R.id.bt_regiest_finish:// 完成注册
                regiesDialog = DialogUtil.createLoadingDialog(RegiesCode.this, getResources().getString(R.string.web_pro_text));
                regiesDialog.show();
                regies();
                break;

        }
    }

    /**
     * 电话和密码不为空下一步才激活
     */
    Runnable mRunnable = new Runnable() {

        @Override
        public void run() {
            while (!threadStop) {
                if (!TextUtils.isEmpty(et_regies_code.getText().toString()
                        .trim())
                        && !bt_finish.isEnabled()) {// 有数据非激活状态
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            bt_finish.setEnabled(true);// 设置激活状态
                        }
                    });
                } else if (TextUtils.isEmpty(et_regies_code.getText()
                        .toString().trim())
                        && bt_finish.isEnabled()) {// 没有数据激活状态
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            bt_finish.setEnabled(false);// 设置非激活状态
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
     * 获取传递过来的值
     */
    public void getBundle() {
        Intent mIntent = getIntent();
        Bundle mBundle = mIntent.getExtras();
        nike = mBundle.getString("nike");// 获取昵称
        name = mBundle.getString("name");// 获取姓名
        mobile = mBundle.getString("mobile");// 获取手机号
        password = mBundle.getString("password");// 获取密码
        countryCode = mBundle.getString("code");// 获取国家编码
        mcode = mBundle.getString("mcode");// 验证码
        verifiCodes.add(mcode);
        for (String str : verifiCodes) {
            LogUtil.d(TAG, str);
        }

        // LogUtil.d(TAG, "nike：" + nike + "\n" + "name" + name + "\n" +
        // "mobile"
        // + mobile + "\n" + "password" + password + "\n" + "countryCode"
        // + countryCode + "\n" + "mcode:" + mcode);
    }

    /**
     * 获取验证码
     */
    public void getCode() {

        String getsign = MD5Util.getMD5(Constats.S_KEY + mobile);
        String geturl = Constats.HTTP_URL + Constats.SMS_FUN;
        Map<String, String> sparams = new HashMap<String, String>();
        sparams.put("mobile", mobile);// 电话
        sparams.put("da", countryCode);// 国家编码
        sparams.put("sign", getsign);// 签名
        getSecurityCode(geturl, sparams);
    }

    /**
     * 注册
     */
    public void regies() {
        String passwd = MD5Util.getMD5(Constats.S_KEY + password);// 密码加密

        String code = et_regies_code.getText().toString().trim();// 验证码

        String sign = MD5Util.getMD5(Constats.S_KEY + mobile + passwd);// 签名

        String mtype = Constats.ANDROID + "";
        String mno = AndroidUtil.getUniqueId(RegiesCode.this);// 手机唯一标识码
        String mversion = AndroidUtil.getHandSetInfo();
        String devbrand = AndroidUtil.getVendor();
        String dyid = sp.getString(UserInfo.GUIDER_ID, "0");
        String wifiName = sp.getString(UserInfo.download_wifi, "");
        String daoyoubao = TextUtils.isEmpty(wifiName) ? MyHttpUtil.getRegiesUpWifiName(this) : wifiName;
        for (String str : verifiCodes) {
            LogUtil.d(TAG, str);
        }
        LogUtil.d(TAG, "是否包含：" + verifiCodes.contains(code) + "	-code" + code);

        if (!TextUtils.isEmpty(nike) && !TextUtils.isEmpty(mobile)
                && !TextUtils.isEmpty(code) && !TextUtils.isEmpty(passwd)) {
            if (TextUtils.isEmpty(mcode)) {
                ToastUtil.showToast(RegiesCode.this, R.string.t_regiest_code_1,
                        3000);
                return;
            } else if (!verifiCodes.contains(code)) {
                ToastUtil.showToast(RegiesCode.this, R.string.t_regiest_code_2,
                        3000);
                return;
            }
            // IntentFilter intentFilter;
            params = new HashMap<String, String>();
            params.put("mobile", mobile);
            params.put("passwd", passwd);
            if (!TextUtils.isEmpty(name)) {
                params.put("realname", name);
            } else {
                params.put("realname", "");
            }
            params.put("nick", nike);
            // params.put("img", image);
            params.put("sign", sign);
            params.put("code", countryCode);
            params.put("mtype", mtype);
            params.put("mno", mno);
            params.put("mversion", mversion);
            params.put("devbrand", devbrand);
            params.put("daoyoubao", daoyoubao);
            params.put("dyid", dyid);
            regiesDialog = DialogUtil.createLoadingDialog(RegiesCode.this, getResources().getString(R.string.web_pro_text));
            regiesDialog.show();
            AndroidUtil.getLocalHostIp(handler);
        } else {
            ToastUtil.showToast(RegiesCode.this, R.string.t_login_info, 3000);
        }
    }

    /**
     * 计时器
     */
    class TimeCount extends CountDownTimer {
        public TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);// 参数依次为总时长,和计时的时间间隔
        }

        @Override
        public void onFinish() {// 计时完毕时触发
            tv_regies_count_down.setText(R.string.t_find_back_pwd_re_code_info);
            tv_regies_count_down.setClickable(true);
        }

        @Override
        public void onTick(long millisUntilFinished) {// 计时过程显示
            tv_regies_count_down.setClickable(false);
            tv_regies_count_down.setText(millisUntilFinished / 1000 + "");
        }
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
                                    try {
                                        result = response.getInt("result");

                                    } catch (Exception e) {// 网络可能被拦截
                                        ToastUtil
                                                .showToast(
                                                        RegiesCode.this,
                                                        R.string.t_frag_set_network_approve,
                                                        4000);
                                        return;
                                    }
                                    int code = response.getInt("code");
                                    if (0 == result) {// 获取成功设置mcode,启动计时
                                        mcode = code + "";
                                        verifiCodes.add(mcode);
                                        // et_regiest_set_security_code.setText(mcode);
                                        // ToastUtil.showToast(
                                        // RegiestActivity.this, mcode,
                                        // 2000);
                                        // tv_regies_count_down
                                        // .setVisibility(View.VISIBLE);
                                        time.start();
                                    } else if (1004 == code) {
                                        ToastUtil.showToast(RegiesCode.this,
                                                R.string.t_regiest_info_phone,
                                                3000);
                                    } else {
                                        ToastUtil
                                                .showToast(
                                                        RegiesCode.this,
                                                        R.string.t_regiest_get_code_err,
                                                        3000);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    ToastUtil.showToast(RegiesCode.this,
                                            R.string.t_frag_set_network_err,
                                            3000);
                                }
                            }
                        }, new ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                ToastUtil.showToast(RegiesCode.this,
                                        R.string.t_frag_set_network_err, 3000);
                            }
                        });
        if (null != mRequestQueue) {
            mRequestQueue.add(jsonRequest);
        }
    }

    /**
     * 注册(服务器)
     */
    @SuppressWarnings("unchecked")
    private void httpRegiest(String url, Map<String, String> params) {
        JsonRequest<JSONObject> jsonRequest = VolleyJsonUtil
                .createJsonObjectRequest(Method.POST, url, params,
                        new Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    int result = response.getInt("result");
                                    String message = response.getString("msg");
                                    if (0 == result) {
                                        JSONObject item = response
                                                .getJSONObject("item");
                                        Log.d(TAG, "item = " + item);
                                        Gson gson = new Gson();
                                        User user = gson.fromJson(
                                                item.toString(), User.class);
                                        ToastUtil.showToast(RegiesCode.this,
                                                R.string.t_regiest_info_sure,
                                                3000);

                                        sp.edit().putString(UserInfo.JSONSTR, item.toString()).commit();

                                        sp.edit().putBoolean(UserInfo.LOGINING, true).commit();

                                        sp.edit().putString(UserInfo.USER_ID, user.userid).commit();

                                        sp.edit().putString(UserInfo.NIK, user.nick).commit();

                                        sp.edit().putString(UserInfo.USER_NAME, user.username).commit();

                                        sp.edit().putString(UserInfo.HEAD_ICON_URL, user.img).commit();

                                        sp.edit().putString(UserInfo.LOGIN_ACCOUT, user.mobile).commit();
                                        sp.edit().putString(UserInfo.PASS_WORD, password).commit();// 保存密码
                                        sp.edit().putString(UserInfo.LOGIN_ACCOUT, mobile).commit();// 保存电话号码


                                        Intent intent2 = new Intent(ActionConstats.STRCHANGE);
                                        sendBroadcast(intent2);
                                        Intent imgIntent = new Intent(ActionConstats.IMGCHANGE);
                                        sendBroadcast(imgIntent);

                                        Intent loginIntent = new Intent();
                                        loginIntent.setClass(RegiesCode.this, MainFragment.class);
                                        startActivity(loginIntent);
                                        overridePendingTransition(R.anim.right_in, R.anim.left_out);
                                        //友盟统计
                                        MobclickAgent.onProfileSignIn(user.userid);
                                        threadStop = true;
                                    } else {
                                        ToastUtil.showToast(RegiesCode.this,
                                                message, 3000);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    ToastUtil.showToast(RegiesCode.this,
                                            R.string.t_frag_set_network_err,
                                            3000);
                                }
                                regiesDialog.dismiss();
                            }
                        }, new ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                regiesDialog.dismiss();
                                ToastUtil.showToast(RegiesCode.this,
                                        R.string.t_frag_set_network_err, 3000);
                            }
                        });
        if (null != mRequestQueue) {
            mRequestQueue.add(jsonRequest);
        }
    }

    /**
     * 动态短信广播接收者 ,自动输入短信验证码
     * <p/>
     * 晶凌达科技有限公司所有， 受到法律的保护，任何公司或个人，未经授权不得擅自拷贝。
     *
     * @creator 单柏平 <br/>
     * @create-time 2015-12-29 下午5:15:14
     */
    public class SmsBroadcastReceiver extends BroadcastReceiver {

        public SmsBroadcastReceiver(Context context) {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.provider.Telephony.SMS_RECEIVED");
            context.registerReceiver(this, filter);
        }

        @Override
        public void onReceive(Context context, Intent intent) {

            Object[] pduses = (Object[]) intent.getExtras().get("pdus");
            for (Object pdus : pduses) {
                byte[] pdusmessage = (byte[]) pdus;
                SmsMessage sms = SmsMessage.createFromPdu(pdusmessage);
                String content = sms.getMessageBody(); // 短信内容
                if (content.contains("途胜旅行") && content.contains("验证码")) {

                    et_regies_code.setText(mcode);
                }

                // String mobile = sms.getOriginatingAddress();// 发送短信的手机号码
                // Date date = new Date(sms.getTimestampMillis());
                // SimpleDateFormat format = new SimpleDateFormat(
                // "yyyy-MM-dd HH:mm:ss");
                // String time = format.format(date); // 得到发送时间

            }
        }

    }

}
