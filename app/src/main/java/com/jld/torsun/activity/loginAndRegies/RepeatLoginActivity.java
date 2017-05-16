package com.jld.torsun.activity.loginAndRegies;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonRequest;
import com.google.gson.Gson;
import com.jld.torsun.MyApplication;
import com.jld.torsun.R;
import com.jld.torsun.activity.BaseActivity;
import com.jld.torsun.activity.FindBackPassWordActivity;
import com.jld.torsun.activity.MainFragment;
import com.jld.torsun.activity.fragment.FragmentSet;
import com.jld.torsun.activity.tours.MulticastServer;
import com.jld.torsun.http.VolleyJsonUtil;
import com.jld.torsun.http.image.BitmapCache;
import com.jld.torsun.modle.ActionConstats;
import com.jld.torsun.modle.User;
import com.jld.torsun.util.AndroidUtil;
import com.jld.torsun.util.Constats;
import com.jld.torsun.util.DialogUtil;
import com.jld.torsun.util.ListenerUtil;
import com.jld.torsun.util.LogUtil;
import com.jld.torsun.util.MD5Util;
import com.jld.torsun.util.ToastUtil;
import com.jld.torsun.util.UserInfo;
import com.jld.torsun.util.imagecache.MyImageLoader;
import com.jld.torsun.view.RoundImageViewByXfermode;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * 重复登录
 */
public class RepeatLoginActivity extends BaseActivity implements
        OnClickListener ,OnTouchListener,OnGestureListener {
    private static final String TAG = "RepeatLoginActivity";
    private TextView tv_login_regiest_button;
    private RoundImageViewByXfermode imageview_login_head_icon;
    private EditText et_relogin_number, et_relogin_password;
    private Button bt_login_login;
    private TextView tv_login_forget_password, tv_change_acuuout;
    private SharedPreferences sp;
    private RequestQueue mRequestQueue;
    private BitmapCache mBitmapCache;
    private Dialog mDialog;
    private ImageView backImg;

    private boolean isToRegies = false;

    Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String data = (String) msg.obj;
            String url = Constats.HTTP_URL + Constats.LOGIN_FUN;
            LogUtil.d(TAG, "ip2:" + data);
            params.put("ip", data);
            login(url, params);
        }
    };
    private Map<String, String> params;

    private GestureDetector mGestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repeat_login_layout);
        sp = getSharedPreferences(Constats.SHARE_KEY, Context.MODE_PRIVATE);
        // mRequestQueue = Volley.newRequestQueue(this);
        MyApplication ma = (MyApplication) getApplication();
        mRequestQueue = ma.getRequestQueue();
        mBitmapCache = new BitmapCache();
        mGestureDetector = new GestureDetector((OnGestureListener) this);
        RelativeLayout relativeLayout =(RelativeLayout)findViewById(R.id.repeat_login_layout);
        relativeLayout.setLongClickable(true);
        relativeLayout.setOnTouchListener(this);

        initView();
    }

    private void initView() {
        backImg=(ImageView)findViewById(R.id.repeat_login_back);
        String userid = sp.getString(UserInfo.USER_ID,"");
        if (!TextUtils.isEmpty(userid)){
            isToRegies = false;
            backImg.setVisibility(View.VISIBLE);
        }else {
            isToRegies = true;
        }
        tv_login_regiest_button = (TextView) findViewById(R.id.tv_login_regiest_button);
        imageview_login_head_icon = (RoundImageViewByXfermode) findViewById(R.id.imageview_login_head_icon);
        imageview_login_head_icon.setDefaultImageResId(R.mipmap.default_hear_ico_re);
        et_relogin_number = (EditText) findViewById(R.id.et_relogin_number);
        et_relogin_number.setText(sp.getString(UserInfo.LOGIN_ACCOUT, ""));
        et_relogin_password = (EditText) findViewById(R.id.et_relogin_password);
        bt_login_login = (Button) findViewById(R.id.bt_repeat_login_login);
        tv_login_forget_password = (TextView) findViewById(R.id.tv_login_forget_password);
        tv_change_acuuout = (TextView) findViewById(R.id.tv_change_acuuout);

        ListenerUtil.setListener(this, tv_login_regiest_button, backImg,
                imageview_login_head_icon, bt_login_login,
                tv_login_forget_password, tv_change_acuuout);

        String requestUrl = sp.getString(UserInfo.HEAD_ICON_URL, "");
        if (!TextUtils.isEmpty(requestUrl)) {
            ImageLoader imageLoader = MyImageLoader.getInstance(this);
            imageview_login_head_icon.setErrorImageResId(R.mipmap.default_hear_ico_re);
            imageview_login_head_icon.setImageUrl(requestUrl, imageLoader);
//			ImageLoader imageLoader = new ImageLoader(mRequestQueue,
//					mBitmapCache);
//			ImageListener listener = ImageLoader.getImageListener(
//					imageview_login_head_icon, R.drawable.about_tucson,
//					R.drawable.about_tucson);
//			imageLoader.get(requestUrl, listener);
        }

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.tv_login_regiest_button:
                Intent regist = new Intent();
                regist.setClass(RepeatLoginActivity.this, RegiesUser.class);
                startActivity(regist);
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
                finish();
                break;
            case R.id.imageview_login_head_icon:

                break;
            case R.id.bt_repeat_login_login:

                mDialog = DialogUtil.createLoadingDialog(this, getResources().getString(R.string.t_re_login_string));
                mDialog.show();
                String mobile = et_relogin_number.getText().toString().trim();

                String original_passwd = et_relogin_password.getText().toString()
                        .trim();// 原始密码
                String passwd = MD5Util.getMD5(Constats.S_KEY + original_passwd);// 密码加密

                String sign = MD5Util.getMD5(Constats.S_KEY + mobile + passwd);

                String mtype = Constats.ANDROID + "";
                String mno = AndroidUtil.getUniqueId(this);
                String mversion = AndroidUtil.getHandSetInfo();
                String devbrand = AndroidUtil.getVendor();
                String ip = "";
//                String ip = AndroidUtil.getLocalHostIp();
                LogUtil.d("againGetParams", "登录mobile：" + mobile + "	passwd："
                        + passwd + "sign：" + sign + "--mno" + mno);

                if (!TextUtils.isEmpty(mobile) && !TextUtils.isEmpty(passwd)) {
                    params = new HashMap<String, String>();
                    params.put("mobile", mobile);
                    params.put("passwd", passwd);
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
                Intent forget_password = new Intent();
                forget_password.setClass(RepeatLoginActivity.this,
                        FindBackPassWordActivity.class);
                startActivity(forget_password);
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
                break;
            case R.id.tv_change_acuuout:
                Intent login = new Intent();
                login.setClass(RepeatLoginActivity.this, LoginActivity.class);
                startActivity(login);
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
                finish();
                break;
            case R.id.repeat_login_back:
                Intent login_intent = new Intent();
                login_intent.setClass(RepeatLoginActivity.this, MainFragment.class);
                startActivity(login_intent);
                overridePendingTransition(R.anim.page_left_in, R.anim.page_right_out);
                this.finish();
                break;
            default:
                break;
        }
    }

    /**
     * 登录 {"msg":"登陆成功","result":0,"item":
     * {"img":"http:\/\/img.tucson.net.cn\/2015-10\/1445022971_34247.jpg",
     * "userid":"41","username":"hh","nick":"yaozu","mobile":"13823133104"}}
     */
    public void login(String url, final Map<String, String> params) {
        @SuppressWarnings("unchecked")
        JsonRequest<JSONObject> jsonRequest = VolleyJsonUtil
                .createJsonObjectRequest(Method.POST, url, params,
                        new Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    LogUtil.d(TAG, "loginresponse = "
                                            + response.toString());
                                    int result = response.getInt("result");
                                    String message = response.getString("msg");
                                    if (0 == result) {
                                        ToastUtil.showToast(RepeatLoginActivity.this, R.string.t_login_suc, 3000);
                                        JSONObject item = response.getJSONObject("item");
                                        // Log.d(TAG, "item = " + item);
                                        Gson gson = new Gson();
                                        User user = gson.fromJson(item.toString(), User.class);

                                        sp.edit().putString(UserInfo.JSONSTR, item.toString()).commit();


                                        /**登录状态*/
                                        sp.edit().putBoolean(UserInfo.LOGINING, true).commit();

                                        /**用户ID*/
                                        sp.edit().putString(UserInfo.USER_ID, user.userid).commit();
                                        /**用户昵称*/
                                        sp.edit().putString(UserInfo.NIK, user.nick).commit();

                                        sp.edit().putString(UserInfo.USER_NAME, user.username).commit();

                                        sp.edit().putString(UserInfo.HEAD_ICON_URL, user.img).commit();

                                        sp.edit().putString(UserInfo.LOGIN_ACCOUT, user.mobile).commit();
                                        sp.edit().putString(UserInfo.PASS_WORD, params.get("passwd")).commit();
                                        sp.edit().putString(UserInfo.LOGIN_ACCOUT, params.get("mobile")).commit();

                                        Intent intent2 = new Intent(ActionConstats.STRCHANGE);
                                        sendBroadcast(intent2);
                                        Intent login_intent = new Intent();
                                        login_intent.setClass(RepeatLoginActivity.this, MainFragment.class);
                                        login_intent.putExtra("type","login");
                                        startActivity(login_intent);
                                        overridePendingTransition(R.anim.right_in, R.anim.left_out);
                                        Intent imgIntent = new Intent(ActionConstats.IMGCHANGE);
                                        sendBroadcast(imgIntent);
                                        //友盟统计
                                        MobclickAgent.onProfileSignIn(user.userid);
                                        if (mDialog != null){
                                            mDialog.dismiss();
                                        }
                                        finish();
                                    } else {
                                        LogUtil.i(TAG,"---------login-----message---------:" + message);
                                        ToastUtil.showToast(
                                                RepeatLoginActivity.this,
                                                message, 3000);
                                        if (mDialog != null){
                                            mDialog.dismiss();
                                        }
                                    }
                                } catch (Exception e) {

                                    e.printStackTrace();
                                }
                            }
                        }, new ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                LogUtil.i(TAG,"----------login-------网络错误？？---");
                                ToastUtil.showToast(RepeatLoginActivity.this,
                                        R.string.t_frag_set_network_err, 3000);
                                if (mDialog != null){
                                    mDialog.dismiss();
                                }
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
            if (isToRegies){
                Intent regiest = new Intent();
                regiest.setClass(RepeatLoginActivity.this, RegiesUser.class);
                startActivity(regiest);
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
                finish();
            }else {
                Intent login_intent = new Intent();
                login_intent.setClass(RepeatLoginActivity.this, MainFragment.class);
                startActivity(login_intent);
                finish();
                //onBackPressed();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
                //Toast.makeText(this, "向右手势", Toast.LENGTH_SHORT).show();
            }

        }
        return false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }

}
