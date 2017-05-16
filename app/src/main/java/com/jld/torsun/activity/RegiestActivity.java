package com.jld.torsun.activity;

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
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.ImageColumns;
import android.telephony.SmsMessage;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
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
import com.jld.torsun.activity.loginAndRegies.LoginActivity;
import com.jld.torsun.http.UploadUtil.OnUploadProcessListener;
import com.jld.torsun.http.VolleyJsonUtil;
import com.jld.torsun.modle.User;
import com.jld.torsun.util.AndroidUtil;
import com.jld.torsun.util.Constats;
import com.jld.torsun.util.DialogUtil;
import com.jld.torsun.util.LogUtil;
import com.jld.torsun.util.MD5Util;
import com.jld.torsun.util.ToastUtil;
import com.jld.torsun.util.UserInfo;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 注册
 */
public class RegiestActivity extends BaseActivity implements OnClickListener,
        OnUploadProcessListener {
    private static final String TAG = "RegiestActivity";
    private RelativeLayout rl_red_regiest;
    private TextView tv_regiest_login;
    /**
     * 确认按钮
     */
    private Button bt_regiest_sure;
    private ImageView imagev_regiest_head_icon;
    private ImageView imagev_regiest_add_icon;
    private RelativeLayout rl_regiest_add_head;
    /**
     * 获取验证码按钮
     */
    private Button bt_regiest_get_code;
    /**
     * 昵称
     */
    private EditText et_regiest_nike;
    /**
     * 手机号
     */
    private EditText et_regiest_mobile;
    /**
     * 密码
     */
    private EditText et_regiest_password;
    /**
     * 验证码
     */
    private EditText et_regiest_set_security_code;
    /**
     * 真实姓名
     */
    private EditText et_regiest_name;
    private RequestQueue mRequestQueue;
    private ImageView imagev_regiest_name_icon;

    private String passwd;
    private String mobile;
    // private String picPath = "";
    // private Uri photoUri;

    private static String postImageUrl = Constats.HTTP_URL
            + Constats.POST_ICON_FUN;
    // private static final int TIME_OUT = 10 * 1000; // 超时时间
    // private static final String CHARSET = "utf-8"; // 设置编码

    // private static final String IMAGE_FILE_NAME = "headicon";

    /* 使用照相机拍照获取图片 */
    public static final int SELECT_PIC_BY_TACK_PHOTO = 1;
    /* 使用相册中的图片 */
    public static final int SELECT_PIC_BY_PICK_PHOTO = 2;

    public static final int REQUESTCODE_CUTTING = 3;

    private SharedPreferences sp;
    // private String account_name = "";
    private String mcode = "";// 验证码
    private TimeCount time;
    /**
     * 更新确定按钮状态的message.what值
     */
    private static final int UPDATA_SURE_BTN = 0x08;
    private Handler postImgHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATA_SURE_BTN:
                    bt_regiest_sure.setEnabled(true);
                    break;
                case 0:
                    String data = (String) msg.obj;
                    String url = Constats.HTTP_URL + Constats.REGIEST_FUN;
                    LogUtil.d("LoginActivity", "ip2:" + data);
                    params.put("ip", data);
                    httpRegiest(url, params);
                    break;
                default:
                    if (progressdialog.isShowing()) {
                        progressdialog.dismiss();
                        progressdialog = null;
                    }
                    String imgurl = (String) msg.obj;
                    sp.edit().putString(UserInfo.HEAD_ICON_URL, imgurl).commit();
                    Log.d(TAG, "msg.obj = " + imgurl);
                    if (null != imagev_regiest_head_icon) {
                        imagev_regiest_head_icon.setImageBitmap(BitmapFactory
                                .decodeFile(getHeadiconPath()));
                        imagev_regiest_add_icon.setVisibility(View.GONE);
                    }
                    break;
            }
        }
    };

    private final List<String> codeList = new ArrayList<String>();
    private Map<String, String> params;

    private boolean isCode(String code) {
        boolean flag = false;
        if (!TextUtils.isEmpty(code)) {
            flag = codeList.contains(code);
        }
        return flag;
    }


    private Runnable upbtn = new Runnable() {
        @Override
        public void run() {
            while (true) {
                if (!TextUtils.isEmpty(et_regiest_nike.getText().toString().trim())
                        && !TextUtils.isEmpty(et_regiest_password.getText()
                        .toString())
                        && !TextUtils.isEmpty(et_regiest_mobile.getText()
                        .toString())
                        && !TextUtils.isEmpty(et_regiest_set_security_code
                        .getText().toString())) {
                    postImgHandler.sendEmptyMessage(UPDATA_SURE_BTN);
                    break;
                }
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regiest_layout);
        // mRequestQueue = Volley.newRequestQueue(this);
        promsg = getResources().getString(R.string.t_frag_set_dia_pro_date);
        MyApplication ma = (MyApplication) getApplication();
        mRequestQueue = ma.getRequestQueue();
        sp = getSharedPreferences(Constats.SHARE_KEY, Context.MODE_PRIVATE);
        time = new TimeCount(60000, 1000);// 构造CountDownTimer对象
        initView();

        new SmsBroadcastReceiver(this);// 注册短信广播接收中
        LogUtil.d(TAG, "注册广播接受者:");

        new Thread(upbtn).start();
    }

    private void initView() {
        rl_red_regiest = (RelativeLayout) findViewById(R.id.rl_red_regiest);
        rl_red_regiest.getLayoutParams().height = (int) (AndroidUtil
                .getScreenHeight(this) * 0.15);
        TextView protocol = (TextView) findViewById(R.id.tv_regiest_protocol);
        protocol.setOnClickListener(this);
        tv_regiest_login = (TextView) findViewById(R.id.tv_regiest_login);
        tv_regiest_login.setOnClickListener(this);

        rl_regiest_add_head = (RelativeLayout) findViewById(R.id.rl_regiest_add_head);
        LayoutParams lp = (LayoutParams) rl_regiest_add_head.getLayoutParams();
        lp.topMargin = (int) (AndroidUtil.getScreenHeight(this) * 0.15)
                - (int) getResources().getDimension(R.dimen.size_head_icon) / 2;
        rl_regiest_add_head.setLayoutParams(lp);
        rl_regiest_add_head.setOnClickListener(this);

        imagev_regiest_head_icon = (ImageView) findViewById(R.id.imagev_regiest_head_icon);
        imagev_regiest_head_icon.setOnClickListener(this);

        bt_regiest_sure = (Button) findViewById(R.id.bt_regiest_sure);
        bt_regiest_sure.setEnabled(false);
        bt_regiest_sure.setOnClickListener(this);

        imagev_regiest_add_icon = (ImageView) findViewById(R.id.imagev_regiest_add_icon);
        imagev_regiest_add_icon.setOnClickListener(this);

        imagev_regiest_name_icon = (ImageView) findViewById(R.id.imagev_regiest_name_icon);
        imagev_regiest_name_icon.setEnabled(false);

        et_regiest_nike = (EditText) findViewById(R.id.et_regiest_nike);
        et_regiest_mobile = (EditText) findViewById(R.id.et_regiest_mobile);
        et_regiest_mobile.setInputType(InputType.TYPE_CLASS_PHONE);// 只能输入电话号码
        et_regiest_password = (EditText) findViewById(R.id.et_regiest_password);
        et_regiest_set_security_code = (EditText) findViewById(R.id.et_regiest_set_security_code);
        et_regiest_set_security_code.setInputType(InputType.TYPE_CLASS_NUMBER);// 只能输入数字
        et_regiest_name = (EditText) findViewById(R.id.et_regiest_name);
        et_regiest_name.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2,
                                      int arg3) {
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1,
                                          int arg2, int arg3) {
            }

            @Override
            public void afterTextChanged(Editable e) {
                String s = e.toString().trim();
                if (s.length() > 0) {
                    imagev_regiest_name_icon.setEnabled(true);
                } else {
                    imagev_regiest_name_icon.setEnabled(false);
                }
            }
        });

        bt_regiest_get_code = (Button) findViewById(R.id.bt_regiest_get_code);
        bt_regiest_get_code.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            // 登录
            case R.id.tv_regiest_login:
                Intent login_intent = new Intent();
                login_intent.setClass(RegiestActivity.this, LoginActivity.class);
                startActivity(login_intent);
                finish();
                break;

            case R.id.imagev_regiest_add_icon:
                // 选择头像
                show_SetPortrait_Dialog();
                break;
            case R.id.imagev_regiest_head_icon:
                // 选择头像
                show_SetPortrait_Dialog();
                break;
            // 确定注册
            case R.id.bt_regiest_sure:

                String nike = et_regiest_nike.getText().toString().trim();
                mobile = et_regiest_mobile.getText().toString().trim();

                String original_passwd = et_regiest_password.getText().toString()
                        .trim();// 原始密码
                passwd = MD5Util.getMD5(Constats.S_KEY + original_passwd);// 密码加密

                String code = et_regiest_set_security_code.getText().toString()
                        .trim();
                String name = et_regiest_name.getText().toString().trim();
                String image = sp.getString(UserInfo.HEAD_ICON_URL, "");

                String sign = MD5Util.getMD5(Constats.S_KEY + mobile + passwd);// 签名

                String mtype = Constats.ANDROID + "";
                String mno = AndroidUtil.getUniqueId(this);
                String mversion = AndroidUtil.getHandSetInfo();
                String devbrand = AndroidUtil.getVendor();
//			String ip = AndroidUtil.getLocalHostIp();
                String ip = "";

                if (!TextUtils.isEmpty(nike) && !TextUtils.isEmpty(mobile)
                        && !TextUtils.isEmpty(code) && !TextUtils.isEmpty(passwd)) {
                    if (TextUtils.isEmpty(mcode)) {
                        ToastUtil.showToast(this, R.string.t_regiest_code_1, 3000);
                        break;
                    } else if (!code.equals(mcode)) {
                        ToastUtil.showToast(this, R.string.t_regiest_code_2, 3000);
                        break;
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
                    params.put("img", image);
                    params.put("sign", sign);
                    params.put("mtype", mtype);
                    params.put("mno", mno);
                    params.put("mversion", mversion);
                    params.put("devbrand", devbrand);
                    AndroidUtil.getLocalHostIp(postImgHandler);
                } else {
                    ToastUtil.showToast(this, R.string.t_login_info, 3000);
                }
                break;

            case R.id.bt_regiest_get_code:// 获取验证码
                String smobile = et_regiest_mobile.getText().toString().trim();
                if (TextUtils.isEmpty(smobile)) {
                    ToastUtil.showToast(this, R.string.t_find_back_pwd_phone, 3000);
                    break;
                }
                String da = "";
                String getsign = MD5Util.getMD5(Constats.S_KEY + smobile);
                String geturl = Constats.HTTP_URL + Constats.SMS_FUN;
                Map<String, String> sparams = new HashMap<String, String>();
                sparams.put("mobile", smobile);
                sparams.put("da", da);
                sparams.put("sign", getsign);
                getSecurityCode(geturl, sparams);
                break;

            case R.id.rl_regiest_add_head:
                // show_SetPortrait_Dialog();
                break;

            case R.id.tv_regiest_protocol: // 用户协议
                if (isZh()){
                    Intent intent3 = new Intent(this, WebActivity.class);
                    intent3.putExtra(com.jld.torsun.config.Config.TYPE, com.jld.torsun.config.Config.TYPE_SET_PROTOCOL);
                    intent3.putExtra("url", "file:///android_asset/userProtocol_cn.html");
                    startActivity(intent3);
                }else {
                    Intent intent3 = new Intent(this, WebActivity.class);
                    intent3.putExtra(com.jld.torsun.config.Config.TYPE, com.jld.torsun.config.Config.TYPE_SET_PROTOCOL);
                    intent3.putExtra("url", "file:///android_asset/userProtocol_en.html");
                    startActivity(intent3);
                }
                break;

            default:
                break;
        }
    }

    //判断当前系统语言是否为中文
    private boolean isZh() {
        Locale locale = getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        if (language.endsWith("zh"))
            return true;
        else
            return false;
    }

    /**
     * 注册
     */
    @SuppressWarnings("unchecked")
    private void httpRegiest(String url, Map<String, String> params) {
        LogUtil.d(TAG, "url =  " + url);
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
                                        ToastUtil.showToast(
                                                RegiestActivity.this,
                                                R.string.t_regiest_info_sure,
                                                3000);
                                        sp.edit()
                                                .putBoolean(UserInfo.LOGINING,
                                                        true).commit();

                                        sp.edit()
                                                .putString(UserInfo.USER_ID,
                                                        user.userid).commit();

                                        sp.edit()
                                                .putString(UserInfo.NIK,
                                                        user.nick).commit();

                                        sp.edit()
                                                .putString(UserInfo.USER_NAME,
                                                        user.username).commit();

                                        sp.edit()
                                                .putString(
                                                        UserInfo.HEAD_ICON_URL,
                                                        user.img).commit();

                                        sp.edit()
                                                .putString(
                                                        UserInfo.LOGIN_ACCOUT,
                                                        user.mobile).commit();
                                        sp.edit()
                                                .putString(UserInfo.PASS_WORD,
                                                        passwd).commit();// 保存密码
                                        sp.edit()
                                                .putString(
                                                        UserInfo.LOGIN_ACCOUT,
                                                        mobile).commit();// 保存电话号码

                                        Intent loginIntent = new Intent();
                                        loginIntent.setClass(
                                                RegiestActivity.this,
                                                MainFragment.class);
                                        startActivity(loginIntent);
                                    } else {
                                        ToastUtil.showToast(
                                                RegiestActivity.this, message,
                                                3000);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    ToastUtil.showToast(RegiestActivity.this,
                                            R.string.t_frag_set_network_err,
                                            3000);
                                }
                            }
                        }, new ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                ToastUtil.showToast(RegiestActivity.this,
                                        R.string.t_frag_set_network_err, 3000);
                            }
                        });
        if (null != mRequestQueue) {
            mRequestQueue.add(jsonRequest);
        }
    }

    /**
     * 获取验证码
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

                                    if (element.isJsonObject()) {// 如果返回不是json格式可能被拦截
                                        ToastUtil
                                                .showToast(
                                                        RegiestActivity.this,
                                                        R.string.t_frag_set_network_approve,
                                                        4000);
                                        return;
                                    }

                                    int result = response.getInt("result");
                                    int code = response.getInt("code");
                                    LogUtil.i("", "---------result----:"
                                            + result);
                                    LogUtil.i("", "---------code----:" + code);
                                    if (0 == result) {
                                        mcode = code + "";
                                        // et_regiest_set_security_code.setText(mcode);
                                        // ToastUtil.showToast(
                                        // RegiestActivity.this, mcode,
                                        // 2000);
                                        time.start();
                                    } else if (1004 == code) {
                                        ToastUtil.showToast(
                                                RegiestActivity.this,
                                                R.string.t_regiest_info_phone,
                                                3000);
                                    } else {
                                        ToastUtil
                                                .showToast(
                                                        RegiestActivity.this,
                                                        R.string.t_regiest_get_code_err,
                                                        3000);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    ToastUtil.showToast(RegiestActivity.this,
                                            R.string.t_frag_set_network_err,
                                            3000);
                                }
                            }
                        }, new ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                ToastUtil.showToast(RegiestActivity.this,
                                        R.string.t_frag_set_network_err, 3000);
                            }
                        });
        if (null != mRequestQueue) {
            mRequestQueue.add(jsonRequest);
        }
    }

    private Dialog dialog;

    private void show_SetPortrait_Dialog() {
        // 点击头像图片的点击事件，弹出更改头像的对话框,设置头像
        View view = getLayoutInflater().inflate(R.layout.dialog_photo_choose,
                null);
        dialog = new Dialog(this, R.style.CustomDialog);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);

        dialog.setContentView(view, new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
        Button capture_picture = (Button) view
                .findViewById(R.id.capture_picture);
        capture_picture.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                takePhoto();
                if (null != dialog) {
                    dialog.dismiss();
                    dialog = null;
                }
            }
        });
        Button phohe_picture = (Button) view.findViewById(R.id.phohe_picture);
        phohe_picture.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                pickPhoto();
                if (null != dialog) {
                    dialog.dismiss();
                    dialog = null;
                }
            }
        });
        Window window = dialog.getWindow();
        // 设置显示动画
        // window.setWindowAnimations(R.style.main_menu_animstyle);
        WindowManager.LayoutParams wl = window.getAttributes();
        wl.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        wl.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        //dialog.setCanceledOnTouchOutside(true);
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
    private void takePhoto() {
        Intent getImageByCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(getImageByCamera, SELECT_PIC_BY_TACK_PHOTO);
    }

    Dialog progressdialog;
    private String promsg;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (null != data) {
            switch (requestCode) {
                case SELECT_PIC_BY_PICK_PHOTO:// 直接从相册获取
                    try {
                        Uri pickuri = data.getData();
                        String pickpath = getRealFilePath(this, pickuri);
                        Bitmap pickphoto = compressImageFromFile(pickpath);
                        saveHead(pickphoto);
                        // 新线程后台上传服务端
                        new Thread() {
                            public void run() {
                                uploadFile(postImageUrl);
                            }

                            ;
                        }.start();
                        if (null != progressdialog && progressdialog.isShowing()) {
                            progressdialog.dismiss();
                        }
                        progressdialog = DialogUtil.createLoadingDialog(this, promsg);
                        progressdialog.show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case SELECT_PIC_BY_TACK_PHOTO:// 调用相机拍照
                    Uri tackuri = data.getData();
                    if (tackuri == null) {
                        Bundle bundle = data.getExtras();
                        if (bundle != null) {
                            Bitmap photo = (Bitmap) bundle.get("data");
                            saveHead(photo);
                            // 新线程后台上传服务端
                            new Thread() {
                                public void run() {
                                    uploadFile(postImageUrl);
                                }

                                ;
                            }.start();
                            if (null != progressdialog && progressdialog.isShowing()) {
                                progressdialog.dismiss();
                            }
                            progressdialog = DialogUtil.createLoadingDialog(this,
                                    promsg);
                            progressdialog.show();
                        }
                    } else {
                        String tackpath = getRealFilePath(this, tackuri);
                        try {
                            Bitmap pickphoto = compressImageFromFile(tackpath);
                            saveHead(pickphoto);
                            // 新线程后台上传服务端
                            new Thread() {
                                public void run() {
                                    uploadFile(postImageUrl);
                                }

                                ;
                            }.start();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (null != progressdialog && progressdialog.isShowing()) {
                            progressdialog.dismiss();
                        }
                        progressdialog = DialogUtil.createLoadingDialog(this, promsg);
                        progressdialog.show();
                    }
                    break;
            }
        }

    }

    public static final String HEAD_NAME = "headicon.png";

    public void saveHead(Bitmap photo) {
        try {
            OutputStream outputStream = openFileOutput(HEAD_NAME,
                    Activity.MODE_PRIVATE);
            photo.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUploadDone(int responseCode, String message) {

    }

    @Override
    public void onUploadProcess(int uploadSize) {

    }

    @Override
    public void initUpload(int fileSize) {

    }

    /**
     * android上传文件到服务器
     * <p/>
     * 请求的rul
     *
     * @return 返回响应的内容
     */
    private void uploadFile(String uploadUrl) {
        String end = "\r\n";
        String twoHyphens = "--";
        String boundary = "******";
        // String ss = getHeadiconPath();
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
                msg.sendToTarget();
            }
        } catch (Exception e) {
            e.printStackTrace();
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
            bt_regiest_get_code.setText(R.string.t_find_back_pwd_re_code_info);
            bt_regiest_get_code.setClickable(true);
        }

        @Override
        public void onTick(long millisUntilFinished) {// 计时过程显示
            bt_regiest_get_code.setClickable(false);
            bt_regiest_get_code.setText(millisUntilFinished / 1000 + "s");
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
        String file = getFilesDir().getAbsolutePath() + "/" + HEAD_NAME;
        return file;
    }

    /**
     * 注册短信接收广播接收者
     *
     * @param context
     */
    // public void registerSmsBroadcastReceiver(Context context) {
    //
    // SmsBroadcastReceiver mReceiver = new SmsBroadcastReceiver();
    // IntentFilter filter = new IntentFilter();
    // filter.addAction("android.provider.Telephony.SMS_RECEIVED");
    // context.registerReceiver(mReceiver, filter);
    // }

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

            LogUtil.d(TAG, "收到短信验证码:");

            Object[] pduses = (Object[]) intent.getExtras().get("pdus");
            for (Object pdus : pduses) {
                byte[] pdusmessage = (byte[]) pdus;
                SmsMessage sms = SmsMessage.createFromPdu(pdusmessage);
                String content = sms.getMessageBody(); // 短信内容
                LogUtil.d(TAG, "content:" + content);
                if (content.contains("途胜旅行") && content.contains("验证码")) {
                    LogUtil.d(TAG, "content_ok");

                    et_regiest_set_security_code.setText(mcode);
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
