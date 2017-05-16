package com.jld.torsun.activity;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.google.gson.Gson;
import com.jld.torsun.MyApplication;
import com.jld.torsun.R;
import com.jld.torsun.activity.loginAndRegies.RegiesUser;
import com.jld.torsun.activity.loginAndRegies.RepeatLoginActivity;
import com.jld.torsun.activity.messageCenter.GuiderMessageActivity;
import com.jld.torsun.http.VolleyJsonUtil;
import com.jld.torsun.modle.Guider;
import com.jld.torsun.util.Constats;
import com.jld.torsun.util.ImageUtil;
import com.jld.torsun.util.LogUtil;
import com.jld.torsun.util.MD5Util;
import com.jld.torsun.util.ToastUtil;
import com.jld.torsun.util.UserInfo;
import com.jld.torsun.util.imagecache.MyImageLoader;
import com.jld.torsun.view.CircleImageViewByNIV;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 导游信息界面2
 */
public class GuiderInfoActivity2 extends BaseActivity implements OnClickListener {

    private static final String TAG = "GuiderInfoActivity2";

    private CircleImageViewByNIV image_guider_head_icon;
    private NetworkImageView bgIMG;

    private ImageView bt_call_guider;
    private ImageView iv_msg;
    private ImageView imagev_guider_info_close;

    private SharedPreferences sp;
    private ImageLoader mImageLoader;
    private ImageLoader fastblurLoader;
    private RequestQueue mRequestQueue;

    private RelativeLayout rl_guider_info_red_top;
    private ScrollView scrollView;
    // 记录首次按下位置
    private float mFirstPosition = 0;
    // 是否正在放大
    private Boolean mScaling = false;
    private DisplayMetrics metric;

    private String image;
    private RelativeLayout mPhoneLayout, mNameLayout, mTaLayout, mAddressLayout;
    private TextView tv_guider_info_nick;

    private static String guider_name = "";//导游名字
    private ImageView iv_good;
    private TextView tv_good_num;
    private ImageView iv_love;
    private TextView tv_love_num;
    private ImageView grade1;
    private ImageView grade2;
    private ImageView grade3;
    private ImageView grade4;
    private ImageView grade5;
    private List<ImageView> grades;
    private AnimationSet animationSet;
    private ScaleAnimation scaleAnimation;
    private Animation animation;
    private int good_num = 0;
    private int love_num = 0;
    private static Guider guider = null;
    private TextView tv_guider_phone;
    private TextView tv_guider_name;
    private TextView tv_ta_name;
    private TextView tv_ta_address;
    //判断该导游是否是消息
    private boolean isHavaMSG = false;
    private static final int HeadICON_MSG = 0x1111;
    private static final int COUNT_MSG = 0x1112;
    private static final int NO_COUNT_MSG = 0x1113;
    Handler handler = new Handler() {
        /**
         * 设置导游头像
         */
        @Override
        public void handleMessage(Message msg) {
            int type = msg.what;
            switch (type) {
                case HeadICON_MSG:
                    if (null != image_guider_head_icon) {
                        if (!isMSG2Here) {
                            image = sp.getString(UserInfo.LOAD_ICON, "");
                        }
                        setTopIMG(image);
                        if (!TextUtils.isEmpty(image)) {
                            handler.removeCallbacks(run);
                            return;
                        }
                        handler.postDelayed(run, 1000);
                    }
                    break;
                case COUNT_MSG:
                    isHavaMSG = true;
//                    iv_msg.setEnabled(true);
                    break;
                case NO_COUNT_MSG:
                    isHavaMSG = false;
//                    iv_msg.setEnabled(false);
                    break;
            }
        }
    };
    Runnable run = new Runnable() {
        @Override
        public void run() {
            Message message = handler.obtainMessage();
            message.what = HeadICON_MSG;
            handler.sendMessage(message);
        }
    };

    private MyNetChangeReceiver myNetChangeReceiver;
    private int grade = 0;

    private Intent intent;
    private boolean isMSG2Here;
    private String guideId;
    private String tuanId;
    private String userId;
    private String guideNick;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guider_info_2);
        // 获取屏幕宽高
        metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        sp = getSharedPreferences(Constats.SHARE_KEY, Context.MODE_PRIVATE);
        initData();
        mImageLoader = MyImageLoader.getInstance(this);
        fastblurLoader = MyImageLoader.getInstance(this, true);
        mRequestQueue = ((MyApplication) getApplication()).getRequestQueue();
        initView();
        if (isMSG2Here) {
            msgSetguiderInfo();
        } else {
            spSetGuiderInfo();//展示本地导游信息，防止白屏
        }
        //getGuiderInfo();//从网络获取导游信息
        /**注册网络变化广播接收者，只要网络变化就自动加载导游信息*/
        if (null == myNetChangeReceiver) {
            myNetChangeReceiver = new MyNetChangeReceiver();
            IntentFilter netChangeIntent = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
            this.registerReceiver(myNetChangeReceiver, netChangeIntent);
        }
        scrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ViewGroup.LayoutParams lp = rl_guider_info_red_top.getLayoutParams();
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                        // 手指离开后恢复图片
                        mScaling = false;
                        replyImage();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (!mScaling) {
                            if (scrollView.getScrollY() == 0) {
                                mFirstPosition = event.getY();// 滚动到顶部时记录位置，否则正常返回
                            } else {
                                break;
                            }
                        }
                        int distance = (int) ((event.getY() - mFirstPosition) * 1); // 滚动距离乘以一个系数
                        if (distance < 0) { // 当前位置比记录位置要小，正常返回
                            break;
                        }

                        // 处理放大
                        mScaling = true;
                        lp.width = metric.widthPixels + distance;
                        lp.height = (metric.widthPixels + distance) * 966 / 1080;
                        rl_guider_info_red_top.setLayoutParams(lp);
                        //bgImg.setLayoutParams(lp);
                        return true; // 返回true表示已经完成触摸事件，不再处理
                }
                return false;
            }
        });
    }

    private void initData() {
        intent = getIntent();
        isMSG2Here = intent.getBooleanExtra("MSG2That", false);
        userId = sp.getString(UserInfo.USER_ID, "");
        if (isMSG2Here) {
            tuanId = intent.getStringExtra("tuanId");
            guideId = intent.getStringExtra("guideId");
            image = intent.getStringExtra("headImageUrl");
            guideNick = intent.getStringExtra("guideNick");
        } else {
            tuanId = sp.getString(UserInfo.LAST_SERVICE_TEAM_ID, "");
            guideId = sp.getString(UserInfo.GUIDER_ID, "");
        }
        LogUtil.d(TAG, "initData : userId:" + userId + "  guideId:" + guideId + "  tuanId:" + tuanId);
    }

    private void msgSetguiderInfo() {
        tv_guider_info_nick.setText(guideNick);
    }

    // 回弹动画 (使用了属性动画)
    @SuppressLint("NewApi")
    public void replyImage() {
        final ViewGroup.LayoutParams lp = rl_guider_info_red_top
                .getLayoutParams();
        final float w = rl_guider_info_red_top.getLayoutParams().width;// 图片当前宽度
        final float h = rl_guider_info_red_top.getLayoutParams().height;// 图片当前高度
        final float newW = metric.widthPixels;// 图片原宽度
        final float newH = metric.widthPixels * 966 / 1080;// 图片原高度

        // 设置动画
        ValueAnimator anim = ObjectAnimator.ofFloat(0.0F, 1.0F)
                .setDuration(200);

        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float cVal = (Float) animation.getAnimatedValue();
                lp.width = (int) (w - (w - newW) * cVal);
                lp.height = (int) (h - (h - newH) * cVal);
                rl_guider_info_red_top.setLayoutParams(lp);
                // bgImg.setLayoutParams(lp);
            }
        });
        anim.start();

    }

    private void initView() {
        scrollView = (ScrollView) findViewById(R.id.guider_scrollView_2);
        rl_guider_info_red_top = (RelativeLayout) findViewById(R.id.rl_guider_info_red_top_2);
        mPhoneLayout = (RelativeLayout) findViewById(R.id.rl_phone_layout_2);
        mNameLayout = (RelativeLayout) findViewById(R.id.rl_name_layout_2);
        mTaLayout = (RelativeLayout) findViewById(R.id.rl_ta_layout_2);
        mAddressLayout = (RelativeLayout) findViewById(R.id.rl_address_layout_2);

        imagev_guider_info_close = (ImageView) findViewById(R.id.imagev_guider_info_close_2);
        imagev_guider_info_close.setOnClickListener(this);
        image_guider_head_icon = (CircleImageViewByNIV) findViewById(R.id.image_guider_head_icon_2);
        bgIMG = (NetworkImageView) findViewById(R.id.niv_guider_top_bg);
        image_guider_head_icon.setDefaultImageResId(R.mipmap.default_hear_ico);
        bgIMG.setDefaultImageResId(R.mipmap.a_alpha);

        iv_msg = (ImageView) findViewById(R.id.iv_guider_msg);
        bt_call_guider = (ImageView) findViewById(R.id.iv_guider_call);
        iv_good = (ImageView) findViewById(R.id.iv_guider_good_2);
        tv_good_num = (TextView) findViewById(R.id.tv_guider_good_num_2);
        iv_love = (ImageView) findViewById(R.id.iv_guider_love_2);
        tv_love_num = (TextView) findViewById(R.id.tv_guider_love_num_2);

        grade1 = (ImageView) findViewById(R.id.iv_guider_grade_2_1);
        grade2 = (ImageView) findViewById(R.id.iv_guider_grade_2_2);
        grade3 = (ImageView) findViewById(R.id.iv_guider_grade_2_3);
        grade4 = (ImageView) findViewById(R.id.iv_guider_grade_2_4);
        grade5 = (ImageView) findViewById(R.id.iv_guider_grade_2_5);
        grades = new ArrayList<ImageView>();
        grades.add(grade1);
        grades.add(grade2);
        grades.add(grade3);
        grades.add(grade4);
        grades.add(grade5);


        iv_good.setOnClickListener(this);
        iv_love.setOnClickListener(this);
        bt_call_guider.setOnClickListener(this);
        iv_msg.setOnClickListener(this);
//        iv_msg.setEnabled(false);
        tv_guider_info_nick = (TextView) findViewById(R.id.tv_guider_info_name_2);
        tv_guider_phone = (TextView) findViewById(R.id.tv_guider_phone_2);
        tv_guider_phone.setOnClickListener(this);
        tv_guider_name = (TextView) findViewById(R.id.tv_guider_name_2);
        tv_ta_name = (TextView) findViewById(R.id.tv_ta_name_2);
        tv_ta_address = (TextView) findViewById(R.id.tv_ta_address_2);
    }

//    @Override
//    protected void onStart() {
//        super.onStart();
//        handler.postDelayed(run, 1000);
//    }

    private boolean isFristFocus = true;

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (isFristFocus && hasFocus) {
            isFristFocus = false;
            handler.post(run);
        }
    }

    @Override
    protected void onDestroy() {
        if (null != myNetChangeReceiver) {
            this.unregisterReceiver(myNetChangeReceiver);
            myNetChangeReceiver = null;
        }
        super.onDestroy();
    }

    private void setTopIMG(String requestUrl) {
        if (TextUtils.isEmpty(requestUrl)) {
            bgIMG.setVisibility(View.GONE);
        } else {
            bgIMG.setVisibility(View.VISIBLE);
            bgIMG.setErrorImageResId(R.mipmap.a_alpha);
            bgIMG.setImageUrl(requestUrl, fastblurLoader);

            image_guider_head_icon.setErrorImageResId(R.mipmap.default_hear_ico);
            image_guider_head_icon.setImageUrl(requestUrl, mImageLoader);
        }
    }

    private boolean isTvPhone = false;

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.tv_guider_phone_2://电话
                isTvPhone = true;
            case R.id.iv_guider_call:
                if (!isTvPhone)
                    ImageUtil.togetherRun(bt_call_guider);
                isTvPhone = false;
                if (!sp.getBoolean(UserInfo.SIM_START, true)) {// 如果没有SIM卡就不显示设置和扫码认证
                    ToastUtil.showToast(GuiderInfoActivity2.this, "no SIM card", 3000);
                    return;
                }
                String guiderPhone = tv_guider_phone.getText().toString();
                if (TextUtils.isEmpty(guiderPhone)) {
                    ToastUtil.showToast(GuiderInfoActivity2.this, R.string.t_guide_info, 3000);
                    return;
                }
                show_Call_Dialog(guiderPhone);
//                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"
//                        + guiderPhone));
//                GuiderInfoActivity2.this.startActivity(intent);
                break;
            case R.id.iv_guider_msg://消息
                if (!inspectLoginOrRegies()) {
                    return;
                }
                ImageUtil.togetherRun(iv_msg);
                if (isMSG2Here) {
                    finish();
                    overridePendingTransition(R.anim.no_change, R.anim.suoxiao_out);
                    return;
                }
                if (!isHavaMSG) {
                    ToastUtil.showToast(GuiderInfoActivity2.this, R.string.t_guide_msg, 3000);
                    return;
                }

                LogUtil.d(TAG, "userId:" + userId + "  guideId:" + guideId + "  tuanId:" + tuanId);
                if (TextUtils.isEmpty(tuanId) || TextUtils.isEmpty(userId) || TextUtils.isEmpty(guideId)) {
                    return;
                }
                Intent toMSGIntent = new Intent(this, GuiderMessageActivity.class);
                toMSGIntent.putExtra("userId", userId);
                toMSGIntent.putExtra("guideId", guideId);
                toMSGIntent.putExtra("tuanId", tuanId);
                startActivity(toMSGIntent);
                break;
            case R.id.imagev_guider_info_close_2:
                if (!isMSG2Here) {
                    Intent toIntent = new Intent();
                    toIntent.setClass(this, MainFragment.class);
                    startActivity(toIntent);
                }
                finish();
                overridePendingTransition(R.anim.no_change, R.anim.suoxiao_out);
                break;
            case R.id.iv_guider_good_2:
                if (!inspectLoginOrRegies()) {
                    return;
                }
                iv_good.setImageResource(R.mipmap.ic_good_on);
                animation = AnimationUtils.loadAnimation(this, R.anim.guide_like_anim);
                iv_good.startAnimation(animation);
                good_num = Integer.parseInt(tv_good_num.getText().toString());
                tv_good_num.setText((++good_num) + "");
                iv_good.setClickable(false);
                uploadingGood_Love(0 + "");
                break;
            case R.id.iv_guider_love_2:

                if (!inspectLoginOrRegies()) {
                    return;
                }
                iv_love.setImageResource(R.mipmap.ic_love_on);
                animation = AnimationUtils.loadAnimation(this, R.anim.guide_like_anim);
                iv_love.startAnimation(animation);
                love_num = Integer.parseInt(tv_love_num.getText().toString());
                tv_love_num.setText((++love_num) + "");
                iv_love.setClickable(false);
                uploadingGood_Love(1 + "");
                break;
            default:
                break;
        }
    }

    /**
     * 拨打电话dialog
     */
    private void show_Call_Dialog(final String mobileNumber) {
        // 退出登录的对话框

        // 获取布局
        View view = this.getLayoutInflater().inflate(
                R.layout.dialog_login_select, null);

        // 设置dialog样式
        final Dialog dialog = new Dialog(this, R.style.CustomDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 设置布局
        dialog.setContentView(view, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT));

        // 获取子控件
        Button cancel = (Button) view
                .findViewById(R.id.bt_select_dialog_cancel);
        Button confirm = (Button) view
                .findViewById(R.id.bt_select_dialog_confirm);

        TextView title = (TextView) view
                .findViewById(R.id.tv_select_dialog_title);
        TextView message = (TextView) view
                .findViewById(R.id.tv_select_dialog_message);
        message.setPadding(0, 0, 0, getResources().getDimensionPixelSize(R.dimen.dialog_message_padding_button));
        ImageView close = (ImageView) view
                .findViewById(R.id.iv_login_dialog_close);

        cancel.setText(getResources().getString(R.string.FragmentSet_cache_cancel));
        message.setText(getResources().getString(R.string.TrouTeam_call_message));//设置类容
        confirm.setText(R.string.TrouTeam_call_confirm);
        title.setText(getResources().getString(R.string.t_frag_set_dia_title));

        cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        close.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        confirm.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                dialog.dismiss();
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + mobileNumber));
                startActivity(intent);
            }
        });
        dialog.show();
    }

    private boolean inspectLoginOrRegies() {
        boolean login = sp.getBoolean(UserInfo.LOGINING, false);
        if (TextUtils.isEmpty(userId)) {//没有注册，跳转到注册界面
            Intent intent = new Intent(this, RegiesUser.class);
            startActivity(intent);
            return false;
        } else if (!login) {//没有登陆，跳转到登陆界面
            Intent intent = new Intent(this, RepeatLoginActivity.class);
            startActivity(intent);
            return false;
        }
        return true;
    }

    /**
     * 从服务器端获取当前的消息个数
     */
    private Runnable getMSGCount = new Runnable() {
        @Override
        public void run() {

            LogUtil.d(TAG, "userId:" + userId + "  guideId:" + guideId + "  tuanId:" + tuanId);
            if (TextUtils.isEmpty(tuanId) || TextUtils.isEmpty(userId) || TextUtils.isEmpty(guideId)) {
                return;
            }
            final String sign = MD5Util.getMD5(Constats.S_KEY + userId + tuanId + guideId);
            final String requestUrl = Constats.HTTP_URL + Constats.MESSAGE_GET_TUAN_URL;
            Map<String, String> params = new HashMap<String, String>();
            params.put("guideid", guideId);
            params.put("userid", userId);
            params.put("tuanid", tuanId);
            params.put("limits", "1");
            params.put("sign", sign);
            JsonRequest<JSONObject> jsonRequest = VolleyJsonUtil
                    .createJsonObjectRequest(Request.Method.POST, requestUrl, params,
                            new com.android.volley.Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    try {
                                        int result = response.getInt("result");
                                        if (0 == result) {
                                            int count = response.getInt("count");
                                            LogUtil.d(TAG, "count：" + count);
                                            if (count > 0) {
                                                Message message = handler.obtainMessage();
                                                message.what = COUNT_MSG;
                                                handler.sendMessage(message);
                                                return;
                                            }
                                        }
                                        LogUtil.d(TAG, "result：" + result);
                                        Message message = handler.obtainMessage();
                                        message.what = NO_COUNT_MSG;
                                        handler.sendMessage(message);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }, new com.android.volley.Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Message message = handler.obtainMessage();
                                    message.what = NO_COUNT_MSG;
                                    handler.sendMessage(message);
                                }
                            });
            if (null != mRequestQueue) {
                mRequestQueue.add(jsonRequest);
            }
        }
    };

    /**
     * 访问服务器，获取导游信息
     */
    @SuppressWarnings("unchecked")
    protected Runnable getGuiderInfo = new Runnable() {
        @Override
        public void run() {
            final String phone = sp.getString(UserInfo.GUIDER_PHONE, "");
            LogUtil.d(TAG, "：" + "getGuiderInfo");
            if (TextUtils.isEmpty(phone)) {
                return;
            }
            final String url = Constats.HTTP_URL + Constats.GET_GUIDER_INFO;
            final String sign = MD5Util.getMD5(Constats.S_KEY + guideId);
            Map<String, String> params = new HashMap<String, String>();
            params.put("guideid", guideId);
            params.put("userid", userId);
            params.put("sign", sign);
            LogUtil.d(TAG, "：userId" + userId + "：guideid" + guideId);
            JsonRequest<JSONObject> jsonRequest = VolleyJsonUtil
                    .createJsonObjectRequest(Request.Method.POST, url, params,
                            new com.android.volley.Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    try {
                                        int result = response.getInt("result");
                                        LogUtil.d(TAG, "response：" + response);
                                        if (0 == result) {
                                            String resulrString = response.getString("item");
                                            Gson gson = new Gson();
                                            Guider res = gson.fromJson(
                                                    resulrString,
                                                    Guider.class);
                                            guider = res;
                                            setGuiderInfo();
                                        } else if (1001 == result) {//导游未认证
                                            setGrade(response);
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }, new com.android.volley.Response.ErrorListener() {// 如果无法访问网络则从本地获取
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    ToastUtil.showToast(GuiderInfoActivity2.this, getResources().getString(R.string.get_guider_info_error), 3000);
                                    spSetGuiderInfo();
                                }
                            });
            if (null != mRequestQueue) {
                mRequestQueue.add(jsonRequest);
            }
        }
    };

    /**
     * 上传点赞或点心
     *
     * @param type 0为点赞，1为点心
     */
    private void uploadingGood_Love(String type) {

        String url = Constats.HTTP_URL + Constats.UPLOAD_GOOD_LOVE;

        String sign = MD5Util.getMD5(Constats.S_KEY + guideId + userId);
        HashMap<String, String> params = new HashMap<>();
        params.put("guideid", guideId);
        params.put("userid", userId);
        params.put("type", type);
        params.put("sign", sign);
        LogUtil.d(TAG, "uploadingGood_Love：url：" + url);

        JsonRequest jsonRequest = VolleyJsonUtil.createJsonObjectRequest(Request.Method.POST, url, params, new com.android.volley.Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                try {
                    int result = jsonObject.getInt("result");
                    LogUtil.d(TAG, "uploadingGood_Love：" + jsonObject);

                    if (result == 0) {
                        //点赞成功
                    } else {
                        ToastUtil.showToast(GuiderInfoActivity2.this, getResources().getString(R.string.to_server_failed), 3000);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                ToastUtil.showToast(GuiderInfoActivity2.this, getResources().getString(R.string.t_frag_set_network_err), 3000);
            }
        });
        if (null != mRequestQueue) {
            mRequestQueue.add(jsonRequest);
        }
    }

    /**
     * 设置导游信息(服务器获取的数据)
     */
    public void setGuiderInfo() {
        LogUtil.d(TAG, "setGuiderInfo");

        if (guider != null) {
            LogUtil.d(TAG, "setGuiderInfo");
            //设置电话
            if (!TextUtils.isEmpty(guider.mobile)) {
                LogUtil.d(TAG, "setGuiderInfo  mobile  " + guider.mobile);
                mPhoneLayout.setVisibility(View.VISIBLE);
                tv_guider_phone.setText(guider.mobile);
            } else {
                mPhoneLayout.setVisibility(View.INVISIBLE);
            }
            //设置真实名字
            if (!TextUtils.isEmpty(guider.realname)) {
                LogUtil.d(TAG, "setGuiderInfo  realname  " + guider.realname);
                mNameLayout.setVisibility(View.VISIBLE);
                tv_guider_name.setText(guider.realname);
            } else {
                mNameLayout.setVisibility(View.GONE);
            }
            //设置旅游社
            if (!TextUtils.isEmpty(guider.company)) {
                LogUtil.d(TAG, "setGuiderInfo  compnay  " + guider.company);
                mTaLayout.setVisibility(View.VISIBLE);
                tv_ta_name.setText(guider.company);
                //保存导游旅游社名称
                sp.edit().putString(UserInfo.GUIDER_COMPNAY, guider.company).apply();
            } else {
                mTaLayout.setVisibility(View.GONE);
            }
            //设置地址
            if (!TextUtils.isEmpty(guider.address)) {
                LogUtil.d(TAG, "setGuiderInfo  address  " + guider.address);
                mAddressLayout.setVisibility(View.VISIBLE);
                tv_ta_address.setText(guider.address);
                //保存导游地址
                sp.edit().putString(UserInfo.GUIDER_ADDRESS, guider.address).apply();
            } else {
                mAddressLayout.setVisibility(View.GONE);
            }
            //设置点赞数
            if (!TextUtils.isEmpty(guider.good)) {
                LogUtil.d(TAG, "setGuiderInfo  good  " + guider.good);
                tv_good_num.setVisibility(View.VISIBLE);
                tv_good_num.setText(guider.good);
                sp.edit().putString(UserInfo.GUIDER_GOOD, guider.good).apply();
            }
            //设置点心数
            if (!TextUtils.isEmpty(guider.flower)) {
                LogUtil.d(TAG, "setGuiderInfo  flower  " + guider.flower);
                tv_love_num.setVisibility(View.VISIBLE);
                tv_love_num.setText(guider.flower);
                sp.edit().putString(UserInfo.GUIDER_LOVE, guider.flower).apply();
            }
            //设置等级
            if (!TextUtils.isEmpty(guider.start)) {
                LogUtil.d(TAG, "setGuiderInfo  start  " + guider.start);
                setGuiderGrade(Integer.parseInt(guider.start));
                sp.edit().putString(UserInfo.GUIDER_GRADE, guider.start).apply();
                /**保存导游等级时导游的电话，防等级和姓名错乱*/
                sp.edit().putString(UserInfo.IS_MOBILE, guider.mobile).apply();
            }
            //判断是否点过赞
            if (!TextUtils.isEmpty(guider.checkgood) && "yes".equals(guider.checkgood)) {
                iv_good.setImageResource(R.mipmap.ic_good_on);
                iv_good.setClickable(false);
            }
            //判断是否点过心
            if (!TextUtils.isEmpty(guider.checkflower) && "yes".equals(guider.checkflower)) {
                iv_love.setImageResource(R.mipmap.ic_love_on);
                iv_love.setClickable(false);
            }
            guider_name = guider.realname;
            sp.edit().putString(UserInfo.LOAD_ICON, guider.img).apply();
            LogUtil.d(TAG, "guider.img：" + guider.img);

            sp.edit()
                    .putString(UserInfo.GUIDER_PHONE_NETWORK, guider.mobile)
                    .apply();
            handler.postDelayed(run, 1000);
        } else {
            spSetGuiderInfo();
        }
    }


    /**
     * 本地设置导游信息(UDP传过来的数据)
     */
    public void spSetGuiderInfo() {
        LogUtil.d(TAG, "本地SetGuiderInfo");
        tv_guider_info_nick.setText(getResources().getString(R.string.tour_guide) + sp.getString(UserInfo.GUIDER_NICK, ""));

        String phone = sp.getString(UserInfo.GUIDER_PHONE, "");
        String name = sp.getString(UserInfo.GUIDER_NAME, "");
        String good = sp.getString(UserInfo.GUIDER_GOOD, "");
        String love = sp.getString(UserInfo.GUIDER_LOVE, "");
        String grade = sp.getString(UserInfo.GUIDER_GRADE, "");
        if (!TextUtils.isEmpty(phone)) {
            mPhoneLayout.setVisibility(View.VISIBLE);
            tv_guider_phone.setText(phone);
        } else {
            mPhoneLayout.setVisibility(View.INVISIBLE);
        }
        if (!TextUtils.isEmpty(name)) {
            mNameLayout.setVisibility(View.VISIBLE);
            tv_guider_name.setText(name);
        } else {
            mNameLayout.setVisibility(View.INVISIBLE);
        }
        /**如果保存等级时导游电话号码和当前导游电话号码一致，则认为该等级是该导游的*/
        if (sp.getString(UserInfo.IS_MOBILE, "").equals(sp.getString(UserInfo.GUIDER_PHONE, "-"))) {
            /**设置点赞数量*/
            if (!TextUtils.isEmpty(good)) {
                tv_good_num.setVisibility(View.VISIBLE);
                tv_good_num.setText(good);
            } else {
                tv_good_num.setVisibility(View.INVISIBLE);
            }
            /**设置点心数量*/
            if (!TextUtils.isEmpty(love)) {
                tv_love_num.setVisibility(View.VISIBLE);
                tv_love_num.setText(love);
            } else {
                tv_love_num.setVisibility(View.INVISIBLE);
            }
            /**设置等级*/
            if (!TextUtils.isEmpty(grade)) {
                setGuiderGrade(Integer.parseInt(grade));
            } else {
                setGuiderGrade(0);
            }
        } else {
            tv_good_num.setVisibility(View.INVISIBLE);
            tv_love_num.setVisibility(View.INVISIBLE);
        }
        /**如果不能访问网络，只显示UDP传过来的名称和电话*/
        mTaLayout.setVisibility(View.INVISIBLE);
        mAddressLayout.setVisibility(View.INVISIBLE);
        guider_name = sp.getString(UserInfo.GUIDER_NAME, "");
        handler.postDelayed(run, 1000);
    }

    /**
     * 如果导游没有认证只设置的等级和点赞心
     *
     * @param json
     */
    private void setGrade(JSONObject json) {
        try {
            JSONObject jsonObject = json.getJSONObject("item");
            LogUtil.d(TAG, "jsonObject：" + jsonObject);

            /**设置点赞数量*/
            if (!TextUtils.isEmpty(jsonObject.getString("good"))) {
                tv_good_num.setVisibility(View.VISIBLE);
                tv_good_num.setText(jsonObject.getString("good"));
            } else {
                tv_good_num.setVisibility(View.INVISIBLE);
            }
            /**设置点心数量*/
            if (!TextUtils.isEmpty(jsonObject.getString("flower"))) {
                tv_love_num.setVisibility(View.VISIBLE);
                tv_love_num.setText(jsonObject.getString("flower"));
            } else {
                tv_love_num.setVisibility(View.INVISIBLE);
            }
            /**设置等级*/
            if (!TextUtils.isEmpty(jsonObject.getString("start"))) {
                setGuiderGrade(Integer.parseInt(jsonObject.getString("start")));
            } else {
                setGuiderGrade(0);
            }
            /**设置是否点过赞*/
            if (!TextUtils.isEmpty(jsonObject.getString("checkgood")) && "yes".equals(jsonObject.getString("checkgood"))) {
                iv_good.setImageResource(R.mipmap.ic_good_on);
                iv_good.setClickable(false);
            }
            /**设置是否点过心*/
            if (!TextUtils.isEmpty(jsonObject.getString("checkflower")) && "yes".equals(jsonObject.getString("checkflower"))) {
                iv_love.setImageResource(R.mipmap.ic_love_on);
                iv_love.setClickable(false);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 通过导游的游客数来判定导游的等级
     *
     * @param grade
     */
    public void setGuiderGrade(int grade) {

        LogUtil.d(TAG, "grade:" + grade);
        if (grade >= 0 && grade <= 5) {
            for (int i = 0; i < grade; i++) {
                ImageView imageView = grades.get(i);
                imageView.setImageResource(R.mipmap.grade_primary_on);
            }
            for (int i = grade; i < 5; i++) {
                ImageView imageView = grades.get(i);
                imageView.setImageResource(R.mipmap.grade_primary_off);
            }
        } else if (grade >= 6 && grade <= 10) {
            int off_on = grade - 5;
            for (int i = 0; i < off_on; i++) {
                ImageView imageView = grades.get(i);
                imageView.setImageResource(R.mipmap.grade_intermediate_on);
            }
            for (int i = off_on; i < 5; i++) {
                ImageView imageView = grades.get(i);
                imageView.setImageResource(R.mipmap.grade_intermediate_off);
            }

        } else if (grade >= 11) {
            if (grade > 15)
                grade = 15;
            int off_on = grade - 10;
            for (int i = 0; i < off_on; i++) {
                ImageView imageView = grades.get(i);
                imageView.setImageResource(R.mipmap.grade_advanced_on);
            }
            for (int i = off_on; i < 5; i++) {
                ImageView imageView = grades.get(i);
                imageView.setImageResource(R.mipmap.grade_advanced_off);
            }
        }
    }

    public class MyNetChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            LogUtil.d(TAG, "：MyNetChangeReceiver:" + intent.getAction());
            new Thread(getGuiderInfo).start();
            new Thread(getMSGCount).start();
        }
    }

    /**
     * 保存从服务器获取的gson数据
     */
    class Response {
        public int result;
        public String msg;
        /**
         * 从服务器查询的返回的用户信息列表
         */
        public List<Guider> item;
    }
}
