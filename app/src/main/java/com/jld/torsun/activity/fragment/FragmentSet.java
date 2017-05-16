package com.jld.torsun.activity.fragment;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.ImageColumns;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.jld.torsun.MyApplication;
import com.jld.torsun.R;
import com.jld.torsun.activity.AboutTucsonActivity;
import com.jld.torsun.activity.FeedBackActivity;
import com.jld.torsun.activity.MainFragment;
import com.jld.torsun.activity.NikChangeActivity;
import com.jld.torsun.activity.PasswordChangeActivity;
import com.jld.torsun.activity.WebActivity;
import com.jld.torsun.activity.loginAndRegies.RegiesUser;
import com.jld.torsun.activity.loginAndRegies.RepeatLoginActivity;
import com.jld.torsun.config.Config;
import com.jld.torsun.http.VolleyJsonUtil;
import com.jld.torsun.modle.ActionConstats;
import com.jld.torsun.service.AudioPlayService;
import com.jld.torsun.util.Constats;
import com.jld.torsun.util.DialogUtil;
import com.jld.torsun.util.ImageUtil;
import com.jld.torsun.util.LanguageUtil;
import com.jld.torsun.util.ListenerUtil;
import com.jld.torsun.util.LogUtil;
import com.jld.torsun.util.MD5Util;
import com.jld.torsun.util.ToastUtil;
import com.jld.torsun.util.UserInfo;
import com.jld.torsun.util.imagecache.AppUtils;
import com.jld.torsun.util.imagecache.MyImageLoader;
import com.jld.torsun.util.imagecache.SDCardUtils;
import com.jld.torsun.view.RoundImageViewByXfermode;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class FragmentSet extends Fragment implements OnClickListener {

    private static final String TAG = "FragmentSet";

    private RelativeLayout rl_set_red_top;

    private ImageView iv_set_back, iv_set_back2, iv_check_update_ico;
    private RoundImageViewByXfermode image_set_head_icon;
    private NetworkImageView top_img_bg;
    public TextView tv_set_version_name, tv_set_nik_change, tv_set_name_change;
    private LinearLayout ll_set_nik_change, ll_set_password_change,
            ll_set_name_change, ll_set_about_tucson, ll_set_cache_clear,
            ll_set_check_update, ll_set_feed_back, ll_set_flow;
    private Button bt_set_logout;

    public Context context;
    private SharedPreferences sp;
    private ImageLoader imageLoader;
    private ImageLoader fastblurLoader;
    private RequestQueue mRequestQueue;
    private TextView update_dialog_message;

    private ScrollView sv_set;

    private static final String IMAGE_FILE_NAME = "headicon";
    private static String postImageUrl = Constats.HTTP_URL
            + Constats.POST_ICON_FUN;
    /* 使用照相机拍照获取图片 */
    private static final int SELECT_PIC_BY_TACK_PHOTO = 1;
    /* 使用相册中的图片 */
    private static final int SELECT_PIC_BY_PICK_PHOTO = 2;

    private static final int REQUESTCODE_CUTTING = 3;

    private static final String PHOTO_FILE_NAME = "photo";

    private String imgurl;
    private final int ICO_CHANGE_URL = 1;
    private final int ICO_CHANGE = 2;
    private String mIp;
    private Handler postImgHandler = new Handler() {
        public void handleMessage(Message msg) {

            int what = msg.what;
            // 修改用户头像

            switch (what) {
                case ICO_CHANGE_URL:
                    if (progressdialog.isShowing()) {
                        progressdialog.dismiss();
                        progressdialog = null;
                    }
                    String url = Constats.HTTP_URL + Constats.CHANGE_HEAD_ICON;
                    imgurl = (String) msg.obj;
                    String userid = sp.getString(UserInfo.USER_ID, "");
                    String sign = MD5Util.getMD5(Constats.S_KEY + userid);

                    Map<String, String> params = new HashMap<String, String>();
                    params.put("userid", userid);
                    params.put("img", imgurl);
                    LogUtil.d("imgurl", imgurl);
                    params.put("sign", sign);
                    changeHeadicon(url, params);
                    break;
                case ICO_CHANGE:
                    imgurl = (String) msg.obj;
                    if (!TextUtils.isEmpty(imgurl)) {
                        top_img_bg.setImageBitmap(ImageUtil.fastblur(BitmapFactory.decodeFile(getHeadiconPath()), fastblurValues));
                        image_set_head_icon.setErrorImageResId(R.mipmap.default_hear_ico);
                        image_set_head_icon.setImageUrl(imgurl, imageLoader);

                    } else {
                        top_img_bg.setVisibility(View.GONE);
                    }
                    break;
            }

        }
    };

    private MenuCallback mCallback;

    private SetChangeBroadcast setChangeBroadcast;
    private IntentFilter intentFilter;
    private IntentFilter intentFilter2;
    private boolean isRegisterSetBroadcast = false;

    // 记录首次按下位置
    private float mFirstPosition = 0;
    // 是否正在放大
    private Boolean mScaling = false;

    private DisplayMetrics metric;

    //图片模糊度值，1--25
    private final int fastblurValues = 25;
    private Bitmap bitmap;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mCallback = (MenuCallback) getActivity();
        isRegisterSetBroadcast = false;
        if (null == setChangeBroadcast) {
            setChangeBroadcast = new SetChangeBroadcast();
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_set_activity, container,
                false);
        proString = getResources().getString(R.string.t_frag_set_dia_pro_date);
        networkerr = getResources().getString(R.string.t_frag_set_network_err);
        // 获取屏幕宽高
        metric = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metric);

        imageLoader = MyImageLoader.getInstance(context);
        fastblurLoader = MyImageLoader.getInstance(context, true);

        MyApplication ma = (MyApplication) getActivity().getApplication();
        mRequestQueue = ma.getRequestQueue();
        sp = getActivity().getSharedPreferences(Constats.SHARE_KEY,
                Context.MODE_PRIVATE);
        initView(view);
        sv_set.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ViewGroup.LayoutParams lp = rl_set_red_top.getLayoutParams();
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                        // 手指离开后恢复图片
                        mScaling = false;
                        replyImage();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (!mScaling) {
                            if (sv_set.getScrollY() == 0) {
                                mFirstPosition = event.getY();// 滚动到顶部时记录位置，否则正常返回
                            } else {
                                break;
                            }
                        }
                        int distance = (int) ((event.getY() - mFirstPosition) * 2); // 滚动距离乘以一个系数
                        if (distance < 0) { // 当前位置比记录位置要小，正常返回
                            break;
                        }

                        // 处理放大
                        mScaling = true;
                        lp.width = metric.widthPixels + distance;
                        lp.height = (metric.widthPixels + distance) * 73 / 108;
                        rl_set_red_top.setLayoutParams(lp);
                        //bgImg.setLayoutParams(lp);
                        return true; // 返回true表示已经完成触摸事件，不再处理
                }
                return false;
            }
        });

        //setTopImg();
        tv_set_nik_change.setText(sp.getString(UserInfo.NIK, ""));
        tv_set_name_change.setText(sp.getString(UserInfo.USER_NAME, ""));
        return view;
    }

    // 回弹动画 (使用了属性动画)
    @SuppressLint("NewApi")
    public void replyImage() {
        final ViewGroup.LayoutParams lp = rl_set_red_top
                .getLayoutParams();
        final float w = rl_set_red_top.getLayoutParams().width;// 图片当前宽度
        final float h = rl_set_red_top.getLayoutParams().height;// 图片当前高度
        final float newW = metric.widthPixels;// 图片原宽度
        final float newH = metric.widthPixels * 73 / 108;// 图片原高度

        // 设置动画
        ValueAnimator anim = ObjectAnimator.ofFloat(0.0F, 1.0F)
                .setDuration(200);

        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float cVal = (Float) animation.getAnimatedValue();
                lp.width = (int) (w - (w - newW) * cVal);
                lp.height = (int) (h - (h - newH) * cVal);
                rl_set_red_top.setLayoutParams(lp);
            }
        });
        anim.start();

    }

    private void initView(View view) {
        sv_set = (ScrollView) view.findViewById(R.id.sv_set);

        rl_set_red_top = (RelativeLayout) view.findViewById(R.id.rl_set_red_top);

        iv_set_back = (ImageView) view.findViewById(R.id.iv_set_back);
        iv_set_back2 = (ImageView) view.findViewById(R.id.iv_set_back2);

        top_img_bg = (NetworkImageView) view.findViewById(R.id.set_top_img_bg);
        top_img_bg.setDefaultImageResId(R.mipmap.a_alpha);

        image_set_head_icon = (RoundImageViewByXfermode) view.findViewById(R.id.image_set_head_icon);
        image_set_head_icon.setDefaultImageResId(R.mipmap.default_hear_ico);

        tv_set_version_name = (TextView) view.findViewById(R.id.tv_set_version_name);
        tv_set_version_name.setText(AppUtils.getVersionName(context));
        tv_set_nik_change = (TextView) view.findViewById(R.id.tv_set_nik_change);
        tv_set_name_change = (TextView) view.findViewById(R.id.tv_set_name_change);

        ll_set_nik_change = (LinearLayout) view
                .findViewById(R.id.ll_set_nik_change);
        ll_set_password_change = (LinearLayout) view
                .findViewById(R.id.ll_set_password_change);
        ll_set_name_change = (LinearLayout) view
                .findViewById(R.id.ll_set_name_change);
        ll_set_about_tucson = (LinearLayout) view
                .findViewById(R.id.ll_set_about_tucson);
        ll_set_cache_clear = (LinearLayout) view
                .findViewById(R.id.ll_set_cache_clear);
        ll_set_check_update = (LinearLayout) view
                .findViewById(R.id.ll_set_check_update);
        ll_set_flow = (LinearLayout) view
                .findViewById(R.id.ll_set_flow);
        ll_set_feed_back = (LinearLayout) view
                .findViewById(R.id.ll_set_feed_back);
        iv_check_update_ico = (ImageView) view
                .findViewById(R.id.iv_check_update_ico);
        bt_set_logout = (Button) view.findViewById(R.id.bt_set_logout);

        ListenerUtil.setListener(this, iv_set_back, iv_set_back2, image_set_head_icon,
                ll_set_nik_change, ll_set_password_change, ll_set_name_change, ll_set_cache_clear, ll_set_check_update,
                ll_set_feed_back, ll_set_about_tucson, bt_set_logout, ll_set_flow);

        TextView tv_set_protocol = (TextView) view
                .findViewById(R.id.tv_set_protocol);
        tv_set_protocol.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(context, WebActivity.class);
                intent.putExtra(Config.TYPE, Config.TYPE_SET_PROTOCOL);
                if (LanguageUtil.isZh(getActivity())) {
                    intent.putExtra("url", "file:///android_asset/userProtocol_cn.html");
                } else {
                    intent.putExtra("url", "file:///android_asset/userProtocol_en.html");
                }
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.right_in, R.anim.left_out);
            }
        });
        //setTopImg();
    }

    private boolean isHidden = false;

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        isHidden = hidden;
        if (hidden) {

        } else {
            show();
        }
    }

    private void setTopImg() {
        final String requestUrl = sp.getString(UserInfo.HEAD_ICON_URL, "").trim();
        LogUtil.d(TAG, "setTopImg--requestUrl:" + requestUrl);
        if (!TextUtils.isEmpty(requestUrl)) {

            top_img_bg.setErrorImageResId(R.mipmap.a_alpha);
            top_img_bg.setImageUrl(requestUrl, fastblurLoader);

            image_set_head_icon.setErrorImageResId(R.mipmap.default_hear_ico);
            image_set_head_icon.setImageUrl(requestUrl, imageLoader);

        } else {
            top_img_bg.setVisibility(View.GONE);
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        if (!isHidden) {
            show();
        }
    }

    private void show() {
        setTopImg();
        tv_set_nik_change.setText(sp.getString(UserInfo.NIK, ""));
        tv_set_name_change.setText(sp.getString(UserInfo.USER_NAME, ""));
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private boolean inspectLoginOrRegies() {
        boolean login = sp.getBoolean(UserInfo.LOGINING, false);
        String userid = sp.getString(UserInfo.USER_ID, "");
        if (TextUtils.isEmpty(userid)) {//没有注册，跳转到注册界面
            Intent intent = new Intent(getActivity(), RegiesUser.class);
            startActivity(intent);
            return false;
        } else if (!login) {//没有登陆，跳转到登陆界面
            Intent intent = new Intent(getActivity(), RepeatLoginActivity.class);
            startActivity(intent);
            return false;
        }
        return true;
    }


    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.iv_set_back://返回
                if (null != mCallback) {
                    mCallback.callback();
                }
                break;
            case R.id.iv_set_back2://返回
                if (null != mCallback) {
                    mCallback.callback();
                }
                break;
            case R.id.image_set_head_icon:// 选择头像
                if (!inspectLoginOrRegies()) {
                    return;
                }
                show_SetPortrait_Dialog();
                break;
            case R.id.ll_set_nik_change://昵称修改
                if (!inspectLoginOrRegies()) {
                    return;
                }
                intentFilter = new IntentFilter(ActionConstats.NICK_CHANGE);
                //intentFilter.addAction(NikChangeActivity.NICKCHANGE);
                context.registerReceiver(setChangeBroadcast, intentFilter);
                isRegisterSetBroadcast = true;
                Intent nick_intent = new Intent();
                nick_intent.setClass(getActivity(), NikChangeActivity.class);
                nick_intent.putExtra(NikChangeActivity.FROM,
                        NikChangeActivity.FROM_NIKE);
                startActivity(nick_intent);
                getActivity().overridePendingTransition(R.anim.right_in, R.anim.left_out);
                break;
            case R.id.ll_set_password_change://密码修改
                if (!inspectLoginOrRegies()) {
                    return;
                }
                Intent password_intent = new Intent();
                password_intent.setClass(getActivity(),
                        PasswordChangeActivity.class);
                startActivity(password_intent);
                getActivity().overridePendingTransition(R.anim.right_in, R.anim.left_out);
                break;
            case R.id.ll_set_name_change://修改名字
                if (!inspectLoginOrRegies()) {
                    return;
                }
                intentFilter2 = new IntentFilter(ActionConstats.NAME_CHANGE);
                //intentFilter2.addAction(NikChangeActivity.NAMECHANGE);
                context.registerReceiver(setChangeBroadcast, intentFilter2);
                isRegisterSetBroadcast = true;
                Intent name_intent = new Intent();
                name_intent.setClass(getActivity(), NikChangeActivity.class);
                name_intent.putExtra(NikChangeActivity.FROM,
                        NikChangeActivity.FROM_NAME);
                startActivity(name_intent);
                getActivity().overridePendingTransition(R.anim.right_in, R.anim.left_out);
                break;
            case R.id.ll_set_about_tucson://关于途胜
                Intent about_intent = new Intent();
                about_intent.setClass(getActivity(), AboutTucsonActivity.class);
                startActivity(about_intent);
                getActivity().overridePendingTransition(R.anim.right_in, R.anim.left_out);
                break;
            case R.id.ll_set_cache_clear:// 清除缓存
                show_Cache_Dialog();
                break;
            case R.id.ll_set_check_update://检查更新
                show_update_Dialog();
                break;
            case R.id.ll_set_feed_back://意见反馈
                if (!inspectLoginOrRegies()) {
                    return;
                }
                Intent feed_intent = new Intent();
                feed_intent.setClass(getActivity(), FeedBackActivity.class);
                startActivity(feed_intent);
                getActivity().overridePendingTransition(R.anim.right_in, R.anim.left_out);
                break;
            case R.id.bt_set_logout:// 退出登录
                show_Logout_Dialog();
                break;
            case R.id.ll_set_flow:
                ToastUtil.showToast(context, "流量购买", 3000);
                break;
        }
    }



    /**
     * 删除指定文件
     *
     * @param file
     */
    public void deleteFile(File file) {
        if (file != null && file.exists() && file.isDirectory()) {
            for (File item : file.listFiles()) {
                if (item.isDirectory()) {
                    deleteFile(item);

                } else {
                    item.delete();
                }
            }
        }
    }

    /**
     * 清除缓存dialog
     */
    private void show_Cache_Dialog() {

        // 获取布局
        View view = getActivity().getLayoutInflater().inflate(
                R.layout.dialog_login_select, null);

        // 设置dialog样式
        final Dialog dialog = new Dialog(context, R.style.CustomDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 设置布局
        dialog.setContentView(view, new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));

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

        message.setGravity(Gravity.CENTER);
        // 初始化控件

        title.setText(getString(R.string.FragmentSet_logout_title));
        message.setText(getString(R.string.FragmentSet_cache_message));
        confirm.setText(getString(R.string.FragmentSet_logout_confirm));
        cancel.setText(getString(R.string.FragmentSet_logout_cancel));

        confirm.setOnClickListener(new OnClickListener() {// 清除缓存
            @Override
            public void onClick(View arg0) {
                dialog.dismiss();
                final Dialog mDialog = DialogUtil.createLoadingDialog(context,
                        getResources().getString(R.string.t_set_clean_string));
                mDialog.show();// 显示清除提示弹框
                deleteFile(context.getCacheDir());// 清除完成
                final Dialog cache_dialog_finish = new Dialog(context,
                        R.style.CustomDialog);
                cache_dialog_finish
                        .setContentView(R.layout.dialog_cache_finish);
                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        mDialog.cancel();// 取消清除提示弹框
                        cache_dialog_finish.show();// 显示清除完成弹框
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                cache_dialog_finish.cancel();// 提示弹框显示1秒
                            }
                        }, 800);
                    }
                }, 1000);

            }
        });

        cancel.setOnClickListener(new OnClickListener() {// 取消
            @Override
            public void onClick(View arg0) {
                dialog.dismiss();
            }
        });
        close.setOnClickListener(new OnClickListener() {// 关闭
            @Override
            public void onClick(View arg0) {
                dialog.dismiss();
            }
        });

        Window window = dialog.getWindow();
        // 设置显示动画
        window.setWindowAnimations(R.style.main_menu_animstyle);
        WindowManager.LayoutParams wl = window.getAttributes();
        wl.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        wl.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        dialog.show();
    }

    /**
     * 退出登录dialog
     */
    private void show_Logout_Dialog() {
        // 退出登录的对话框
        // 获取布局
        View view = getActivity().getLayoutInflater().inflate(
                R.layout.dialog_login_select, null);
        // 设置dialog样式
        final Dialog dialog = new Dialog(context, R.style.CustomDialog);
        // 设置布局
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(view, new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
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

        message.setGravity(Gravity.CENTER);
        // 初始化控件

        title.setText(getString(R.string.FragmentSet_logout_title));
        message.setText(getString(R.string.FragmentSet_logout_message));
        cancel.setText(getString(R.string.FragmentSet_logout_confirm));
        confirm.setText(getString(R.string.FragmentSet_logout_cancel));

        cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                dialog.dismiss();
                //旅游团添加成员初始化
                MyApplication.isfristSavaLastTeamID = true;
                sp.edit().putString(UserInfo.LAST_TEAM_ID, "").apply();
                sp.edit().putString(UserInfo.LAST_SERVICE_TEAM_ID, "").apply();

                sp.edit().putString(UserInfo.NIK, "").apply();
                sp.edit().putString(UserInfo.USER_NAME, "").apply();
                // sp.edit().putString(UserInfo.HEAD_ICON_URL,"").commit();
                sp.edit().putString(UserInfo.JSONSTR, "").apply();
                sp.edit().putBoolean(UserInfo.LOGINING, false).apply();
                sp.edit().putBoolean(UserInfo.ISLOAD, false).apply();
                sp.edit().putString(UserInfo.SAVE_CREATE_MESSAGE, "").apply();
                sp.edit().putString(UserInfo.FAIL_USERID_MESSAGE, "").apply();
                sp.edit().putString(UserInfo.FAIL_SAVE_CREATE_MESSAGE, "").apply();
                sp.edit().putString(UserInfo.FAIL_TUAN_ID_MESSAGE, "").apply();
                LogUtil.d(TAG, "bt_set_logout");
                MainFragment.isSetAlias = false;
                MainFragment activity = (MainFragment) getActivity();
                AudioPlayService.MyBinder binder = activity.getBinder();

                Intent intent = new Intent();
                intent.setClass(getActivity(), RepeatLoginActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        });

        confirm.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                dialog.dismiss();
            }
        });
        close.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                dialog.dismiss();
            }
        });
        // dialog.setCanceledOnTouchOutside(false);

        Window window = dialog.getWindow();
        // 设置显示动画
        window.setWindowAnimations(R.style.main_menu_animstyle);
        WindowManager.LayoutParams wl = window.getAttributes();
        wl.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        wl.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        dialog.show();
    }

    /**
     * 检查更新dialog
     */
    private void show_update_Dialog() {

        // 获取布局
        View view = getActivity().getLayoutInflater().inflate(
                R.layout.dialog_update_prompt, null);

        // 设置dialog样式
        final Dialog dialog = new Dialog(context, R.style.CustomDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 设置布局
        dialog.setContentView(view, new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT));

        // 获取子控件
        Button confirm = (Button) view
                .findViewById(R.id.bt_update_dialog_confirm);
        ImageView close = (ImageView) view
                .findViewById(R.id.iv_updete_dialog_close);
        update_dialog_message = (TextView) view
                .findViewById(R.id.tv_update_dialog_message);
        update_dialog_message.setPadding(0, 0, 0, getResources().getDimensionPixelSize(R.dimen.dialog_message_padding_button));

        MyApplication ma = (MyApplication) getActivity().getApplication();
        if (ma.isVersionUpdate) {
            update_dialog_message.setText(getResources().getString(
                    R.string.t_frag_set_update_cont_1));
        } else {
            update_dialog_message.setText(getResources().getString(
                    R.string.t_frag_set_update_cont_2));
        }
        confirm.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                dialog.dismiss();
            }
        });
        close.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        // dialog.setCanceledOnTouchOutside(false);
        Window window = dialog.getWindow();
        // 设置显示动画
        window.setWindowAnimations(R.style.main_menu_animstyle);
        WindowManager.LayoutParams wl = window.getAttributes();
        wl.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        wl.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        dialog.show();
    }


    /**
     * 获取头像dialog
     */
    private void show_SetPortrait_Dialog() {
        // 点击头像图片的点击事件，弹出更改头像的对话框,设置头像
        View view = getActivity().getLayoutInflater().inflate(
                R.layout.dialog_photo_choose, null);
        final Dialog dialog = new Dialog(getActivity(), R.style.CustomDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(view, new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
        ImageView closeView = (ImageView) view
                .findViewById(R.id.dialog_close_iv);
        closeView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (null != dialog) {
                    dialog.dismiss();
                }
            }
        });
        Button capture_picture = (Button) view
                .findViewById(R.id.capture_picture);
        capture_picture.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                takePhoto();//拍摄头像
                if (null != dialog) {
                    dialog.dismiss();
                }
            }
        });
        Button phohe_picture = (Button) view.findViewById(R.id.phohe_picture);
        phohe_picture.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                pickPhoto();//图库获取头像
                if (null != dialog) {
                    dialog.dismiss();
                }
            }
        });
        Window window = dialog.getWindow();
        // 设置显示动画

        window.setWindowAnimations(R.style.main_menu_animstyle);
        WindowManager.LayoutParams wl = window.getAttributes();
        wl.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        wl.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        dialog.show();
    }

    /***
     * 从相册中取图片
     */
    private void pickPhoto() {
        Intent pickIntent = new Intent(Intent.ACTION_PICK, null);
        pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                "image/*");
        startActivityForResult(pickIntent, SELECT_PIC_BY_PICK_PHOTO);
    }

    /**
     * 拍照获取图片
     */

    private File tempFile;

    private void takePhoto() {
        if (SDCardUtils.isSDCardEnable()) {
            Intent getImageByCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            tempFile = new File(Environment.getExternalStorageDirectory(),
                    PHOTO_FILE_NAME);
            Uri uri = Uri.fromFile(tempFile);
            getImageByCamera.putExtra(MediaStore.EXTRA_OUTPUT, uri);

            startActivityForResult(getImageByCamera, SELECT_PIC_BY_TACK_PHOTO);
        } else {
            ToastUtil.showToast(context, R.string.t_set_photo_no_sdcard, 3000);
        }

    }

    Dialog progressdialog;
    private String proString;
    private String networkerr;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SELECT_PIC_BY_PICK_PHOTO) {// 直接从相册获取
            if (null != data) {
                Uri pickuri = data.getData();
                LogUtil.d(TAG,"pickuri:"+pickuri.getPath());
                crop(pickuri);
            }
        } else if (requestCode == SELECT_PIC_BY_TACK_PHOTO) {// 调用相机拍照
            if (SDCardUtils.isSDCardEnable()) {
                if (null != tempFile) {
                    crop(Uri.fromFile(tempFile));
                }
            }
        } else if (requestCode == REQUESTCODE_CUTTING) {//剪切后返回的数据
            if (null != data) {
                Uri pickuri = data.getData();
                LogUtil.d(TAG,"pickuri:"+pickuri.getPath());
                bitmap = data.getParcelableExtra("data");
                if (null != bitmap) {
                    upIMGToSerive(bitmap);
                }
            }
            try {
                if (null != tempFile) {
                    tempFile.delete();
                }
            } catch (Exception e) {

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /*
     * 剪切图片
     */
    private void crop(Uri uri) {
        // 裁剪图片意图
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        // 裁剪框的比例，1：1
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // 裁剪后输出图片的尺寸大小
        intent.putExtra("outputX", 250);
        intent.putExtra("outputY", 250);
        intent.putExtra("outputFormat", "JPEG");// 图片格式
        intent.putExtra("noFaceDetection", true);// 取消人脸识别
        intent.putExtra("return-data", true);
        // 开启一个带有返回值的Activity，请求码为REQUESTCODE_CUTTING
        startActivityForResult(intent, REQUESTCODE_CUTTING);
    }

    public void upIMGToSerive(Bitmap photoBitmap) {
        try {
            // Bitmap pickphoto = compressImageFromFile(tackpath);
            saveHead(photoBitmap);
            // 新线程后台上传服务端
            new Thread() {
                public void run() {
                    uploadFile(postImageUrl);
                }
            }.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (null != progressdialog && progressdialog.isShowing()) {
            progressdialog.dismiss();
        }
        progressdialog = DialogUtil.createLoadingDialog(
                getActivity(), proString);
        progressdialog.show();
    }

    /**
     * 修改用户头像
     */
    public void changeHeadicon(String url, Map<String, String> params) {
        @SuppressWarnings("unchecked")
        JsonRequest<JSONObject> jsonRequest = VolleyJsonUtil
                .createJsonObjectRequest(Method.POST, url, params,
                        new Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    LogUtil.d(TAG, "loginresponse = " + response.toString());
                                    int result = response.getInt("result");
                                    String message = response.getString("msg");
                                    if (0 == result) {
                                        JSONObject item = response.getJSONObject("item");
                                        sp.edit().putString(UserInfo.JSONSTR, item.toString()).commit();

                                        sp.edit().putString(UserInfo.HEAD_ICON_URL, imgurl).commit();
                                        LogUtil.d(TAG, item.toString());

                                        Intent intent = new Intent(ActionConstats.IMGCHANGE);
                                        context.sendBroadcast(intent);

                                        Intent i = new Intent(ActionConstats.STRCHANGE);
                                        getActivity().sendBroadcast(i);
                                        //context.sendBroadcast(i);


                                        if (null != image_set_head_icon) {

                                            Message message1 = postImgHandler.obtainMessage();
                                            message1.what = ICO_CHANGE;
                                            message1.obj = imgurl;
                                            postImgHandler.sendMessage(message1);

//                                            LogUtil.d(TAG, getHeadiconPath());
//                                            image_set_head_icon.setImageBitmap(BitmapFactory.decodeFile(getHeadiconPath()));

//                                            top_img_bg.setImageBitmap(ImageUtil.fastblur(context, BitmapFactory.decodeFile(getHeadiconPath()), fastblurValues));
                                        }

                                    } else {
                                        ToastUtil.showToast(getActivity(),
                                                message, 3000);
                                    }

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                ToastUtil.showToast(getActivity(), networkerr,
                                        3000);
                            }
                        });
        if (null != mRequestQueue) {
            mRequestQueue.add(jsonRequest);
        }
    }

    /**
     * android上传文件到服务器
     *
     * @return 返回响应的内容
     */
    private void uploadFile(String uploadUrl) {
        String end = "\r\n";
        String twoHyphens = "--";
        String boundary = "******";
        String ss = getHeadiconPath();
        try {
            URL url = new URL(uploadUrl);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url
                    .openConnection();// http连接
            // 设置每次传输的流大小，可以有效防止手机因为内存不足崩溃
            // 此方法用于在预先不知道内容长度时启用没有进行内部缓冲的 HTTP 请求正文的流。
            httpURLConnection.setChunkedStreamingMode(128 * 1024);// 128K
            // 允许输入输出流
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setUseCaches(false);
            // 使用POST方法
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Connection", "Keep-Alive");// 保持一直连接
            httpURLConnection.setRequestProperty("Charset", "UTF-8");// 编码
            httpURLConnection.setRequestProperty("Content-Type",
                    "multipart/form-data;boundary=" + boundary);// POST传递过去的编码

            DataOutputStream dos = new DataOutputStream(
                    httpURLConnection.getOutputStream());// 输出流

            dos.writeBytes(twoHyphens + boundary + end);

            dos.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\"; filename=\""
                    + getHeadiconPath().substring(
                    getHeadiconPath().lastIndexOf("/") + 1)
                    + "\""
                    + end);

            dos.writeBytes(end);
            FileInputStream fis = new FileInputStream(getHeadiconPath());// 文件输入流，写入到内存中
            LogUtil.d(TAG, "水水水水picPath = " + getHeadiconPath());
            byte[] buffer = new byte[8192]; // 8k
            int count = 0;
            // 读取文件
            while ((count = fis.read(buffer)) != -1) {
                dos.write(buffer, 0, count);
            }
            fis.close();

            dos.writeBytes(end);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + end);
            dos.flush();

            InputStream is = httpURLConnection.getInputStream();// http输入，即得到返回的结果
            InputStreamReader isr = new InputStreamReader(is, "utf-8");
            BufferedReader br = new BufferedReader(isr);
            String result = br.readLine();

            LogUtil.d(TAG, "水水水水result = " + result);
            dos.close();
            is.close();
            if (!TextUtils.isEmpty(result)) {
                JSONObject json = new JSONObject(result);
                String iamgurl = json.getString("msg");
                Message msg = postImgHandler.obtainMessage(0);
                msg.obj = iamgurl;
                msg.what = ICO_CHANGE_URL;
                postImgHandler.sendMessage(msg);
            } else {
                if (null != progressdialog && progressdialog.isShowing()) {
                    progressdialog.dismiss();
                    progressdialog = null;
                }

            }
        } catch (Exception e) {
            if (null != progressdialog && progressdialog.isShowing()) {
                progressdialog.dismiss();
                progressdialog = null;
            }
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                        ToastUtil.showToast(getActivity(), getString(R.string.toast_photo_error), 3000);
                }
            });
            e.printStackTrace();
        }
    }

    /**
     * 压缩图片
     */
    private Bitmap compressImageFromFile(String srcPath) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        newOpts.inJustDecodeBounds = true;// 只读边,不读内容
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        float hh = 800f;//
        float ww = 480f;//
        int be = 1;
        if (w > h && w > ww) {
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;// 设置采样率
        newOpts.inPurgeable = true;// 同时设置才会有效
        newOpts.inInputShareable = true;// 。当系统内存不够时候图片自动被回收
        bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
        return bitmap;
    }

    /**
     * 从uri中获取文件路径
     */
    public static String getRealFilePath(final Context context, final Uri uri) {
        if (null == uri)
            return null;
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null)
            data = uri.getPath();
        else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = context.getContentResolver().query(uri,
                    new String[]{ImageColumns.DATA}, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(ImageColumns.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return data;
    }

    private String getHeadiconPath() {
        String file = getActivity().getFilesDir().getAbsolutePath() + "/"
                + HEAD_NAME;
        return file;
    }


    public static final String HEAD_NAME = "headicon.png";

    public void saveHead(Bitmap photo) {
        try {
            OutputStream outputStream = getActivity().openFileOutput(HEAD_NAME,
                    Activity.MODE_PRIVATE);
            photo.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class SetChangeBroadcast extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ActionConstats.NICK_CHANGE)) {
                tv_set_nik_change.setText(sp.getString(UserInfo.NIK, ""));
            }
            if (action.equals(ActionConstats.NAME_CHANGE)) {
                tv_set_name_change.setText(sp.getString(UserInfo.USER_NAME, ""));
            }
            //isRegisterSetBroadcast = false;
        }
    }

    @Override
    public void onDestroy() {
        if (isRegisterSetBroadcast && null != setChangeBroadcast) {
            context.unregisterReceiver(setChangeBroadcast);
            setChangeBroadcast = null;
        }
        super.onDestroy();
    }


}
