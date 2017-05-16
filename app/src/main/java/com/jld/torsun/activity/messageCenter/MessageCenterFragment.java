package com.jld.torsun.activity.messageCenter;


import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.google.gson.Gson;
import com.jld.torsun.MyApplication;
import com.jld.torsun.R;
import com.jld.torsun.activity.MainFragment;
import com.jld.torsun.activity.fragment.MenuCallback;
import com.jld.torsun.activity.tours.TrouTeamEdtiActivity;
import com.jld.torsun.db.TeamDao;
import com.jld.torsun.http.VolleyJsonUtil;
import com.jld.torsun.modle.CreateReceiveMessage;
import com.jld.torsun.modle.CreateSendMessage;
import com.jld.torsun.modle.MessageList;
import com.jld.torsun.modle.TrouTeam;
import com.jld.torsun.util.Constats;
import com.jld.torsun.util.DensityUtil;
import com.jld.torsun.util.DialogUtil;
import com.jld.torsun.util.ImageUtil;
import com.jld.torsun.util.LogUtil;
import com.jld.torsun.util.MD5Util;
import com.jld.torsun.util.ToastUtil;
import com.jld.torsun.util.UserInfo;
import com.umeng.analytics.MobclickAgent;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MessageCenterFragment extends Fragment implements OnClickListener {

    private View messageView;

    private RequestQueue mRequestQueue;

    private MenuCallback mCallback;

    private View topTitleView;
    private ImageButton back;
    private TextView topTitleTextView;
    private TextView topTitleSure;
    private ImageView editView;
    private ImageView edit_message;
    private ImageView edit_share;

    private ListView messageListView;
    private List<MessageList> mList = new ArrayList<MessageList>();
    private MessageCenterListAdapter messageAdapter;
    private Context context;
    private String newsId;
    private TeamDao teamDao;
    public static final String TAG = "MessageCenterFragment";
    private final String save_sp_key = "save_MessageCenterFragment_content";
    private String getMessageUrl = Constats.HTTP_URL
            + Constats.MESSAGE_GET_LIST_URL;
    private String deleteMessageUrl = Constats.HTTP_URL
            + Constats.MESSAGE_DELETE_MESSAGE;
    public static final int SHOW_DATA = 1;
    public static final int DIALOG_STOP = 2;
    public static final int DELETE_ITEM = 3;
    private static final int PING_SUCC = 4;
    private static final int PING_ERR = 5;
    private static final int PING_SHOW_DIALOG = 6;
    public static final int SEND_MESSAGE_RESULT = 7;
    private static final int TO_ACTIVITY = 8;
    private static final int ANIMATION_OUT = 9;
    private static final int ANIMATION_IN = 10;
    private String shareMessageUrl = Constats.HTTP_URL
            + Constats.MESSAGE_UPLOAD_ALL;
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int what = msg.what;
            switch (what) {
                case SHOW_DATA:
                    LogUtil.d(TAG, "SHOW_DATA" + SHOW_DATA);
                    mList.clear();
                    count = myRequest.count;
                    mList = myRequest.item;
                    Collections.sort(mList);
                    LogUtil.d(TAG, "count" + count);
                    if (mList.size() > 0) {
                        LogUtil.d(TAG, "mList" + mList.get(0).toString());
                        if (null == messageAdapter) {
                            messageAdapter = new MessageCenterListAdapter(getActivity(), mList);
                            messageListView.setAdapter(messageAdapter);
                            messageListView.setOnItemClickListener(itemClickListener);
                        } else {
                            messageAdapter.setData(mList);
                        }
                    }
                    break;
                case DIALOG_STOP:

                    break;
                case DELETE_ITEM:
                    mList.remove(delete_position);
                    messageAdapter.notifyDataSetChanged();
                    break;
                case PING_SUCC:
                    showPingSucc();
                    break;
                case PING_ERR:
                    ToastUtil.showToast(context, getResources().getString(R.string.t_frag_set_network_err), 4000);
                    editView.setEnabled(true);
                    break;
                case PING_SHOW_DIALOG:
                    showMSGDialog();
                    editView.setEnabled(true);
                    break;
                case SEND_MESSAGE_RESULT://消息发送状况
                    int arg1 = msg.arg1;
                    LogUtil.d(TAG, "arg1:" + arg1);
                    switch (arg1) {
                        case 1://进度
                            int arg2 = msg.arg2;
                            LogUtil.d(TAG, "arg2:" + arg2);
                            topTitleSure.setVisibility(View.VISIBLE);
                            topTitleSure.setText(arg2 + "");
                            break;
                        case 2://完成
                            ToastUtil.showToast(context, getResources().getString(R.string.upload_win), 3000);
                            topTitleSure.setClickable(false);
                            topTitleSure.setVisibility(View.INVISIBLE);
                            //清空保存的数据
                            sp.edit().putString(UserInfo.FAIL_SAVE_CREATE_MESSAGE, "").apply();
                            sp.edit().putString(UserInfo.FAIL_TUAN_ID_MESSAGE, "").apply();
                            sp.edit().putString(UserInfo.FAIL_USERID_MESSAGE, "").apply();
                            mHandler.post(run);
                            break;
                        case 3://网络错误
                            ToastUtil.showToast(context, getResources().getString(R.string.t_frag_set_network_err), 3000);
                            topTitleSure.setVisibility(View.VISIBLE);
                            topTitleSure.setClickable(true);
                            sp.edit().putString(UserInfo.FAIL_USERID_MESSAGE, userId).apply();
                            sp.edit().putString(UserInfo.FAIL_TUAN_ID_MESSAGE, tid).apply();

                            topTitleSure.setText(getResources().getString(R.string.again_send_message));
                            break;
                        case 4://上传失败
                            ToastUtil.showToast(context, getResources().getString(R.string.upload_fail), 3000);
                            topTitleSure.setVisibility(View.VISIBLE);
                            topTitleSure.setClickable(true);
                            //保存团ID 和用户ID
                            sp.edit().putString(UserInfo.FAIL_TUAN_ID_MESSAGE, tid).apply();
                            sp.edit().putString(UserInfo.FAIL_USERID_MESSAGE, userId).apply();
                            topTitleSure.setText(getResources().getString(R.string.again_send_message));
                            break;
                    }
                    break;
                case TO_ACTIVITY:
                    toActivity((String) msg.obj);
                    break;
                case ANIMATION_OUT://展开动画
                    ObjectAnimator translationY_share = ObjectAnimator.ofFloat(edit_share, "translationY", 0, -DensityUtil.dip2px(context, 150));
                    ObjectAnimator rotation_share = ObjectAnimator.ofFloat(edit_share, "rotation", 0f, 360f);
                    ObjectAnimator translationY_message = ObjectAnimator.ofFloat(edit_message, "translationY", 0, -DensityUtil.dip2px(context, 75));
                    ObjectAnimator rotation_message = ObjectAnimator.ofFloat(edit_message, "rotation", 0f, 360f);
                    AnimatorSet animatorSet = new AnimatorSet();
                    animatorSet.playTogether(translationY_share, rotation_share, translationY_message, rotation_message);
                    animatorSet.setInterpolator(new AccelerateInterpolator());//加速插入器
                    animatorSet.setDuration(240);
                    animatorSet.start();
                    break;
                case ANIMATION_IN://聚拢动画
                    translationY_share = ObjectAnimator.ofFloat(edit_share, "translationY", -DensityUtil.dip2px(context, 150), 0);
                    rotation_share = ObjectAnimator.ofFloat(edit_share, "rotation", 360f, 0f);
                    rotation_message = ObjectAnimator.ofFloat(edit_message, "rotation", 360f, 0f);
                    translationY_message = ObjectAnimator.ofFloat(edit_message, "translationY", -DensityUtil.dip2px(context, 75), 0);
                    animatorSet = new AnimatorSet();
                    animatorSet.playTogether(translationY_share, rotation_share, translationY_message, rotation_message);
                    animatorSet.setInterpolator(new AccelerateInterpolator());//加速插入器
                    animatorSet.setDuration(240);
                    animatorSet.start();
                    break;
                default:
                    break;
            }
        }
    };
    private SharedPreferences sp;
    private String userId;
    private String sign;
    private MessageCenterFragment.myRequest myRequest;
    private String count;
    private SwipeRefreshLayout srl_message;
    private String tuanId;
    private Dialog delete_dialog;
    private int delete_position;
    private Handler sendHandler;
    private String createMessage;
    private String tid;
    private CreateReceiveMessage shareContent;
    private EditText share_content;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        LogUtil.d(TAG, "onCreateView");
        context = getActivity();
        teamDao = TeamDao.getInstance(context);
        messageView = inflater.inflate(R.layout.fragment_message_center, container, false);
        mRequestQueue = ((MyApplication) getActivity().getApplication()).getRequestQueue();
        initData();
        initView();
        localUpdateView();
        mHandler.post(run);
        return messageView;
    }
    private boolean isHidden = false;

    @Override
    public void onHiddenChanged(boolean hidden) {
        LogUtil.d(TAG, "onHiddenChanged:" + hidden);
        isHidden = hidden;
        if (hidden) {//隐藏
            hidden();
        } else {//显示
            show();
        }
        super.onHiddenChanged(hidden);
    }

    @Override
    public void onResume() {
        super.onResume();
        LogUtil.d(TAG, "onResume");
        if (!isHidden) {
            show();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        LogUtil.d(TAG, "onPause");
        if (!isHidden) {
            hidden();
        }
    }

    private void hidden() {
        MobclickAgent.onPageEnd("消息中心"); // （仅有Activity的应用中SDK自动调用，不需要单独写）保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息。"SplashScreen"为页面名称，可自定义
        MobclickAgent.onPause(getActivity());
    }

    private boolean isFirst = true;

    public void show() {
        mHandler.post(run);
        MobclickAgent.onPageStart("消息中心"); //统计页面(仅有Activity的应用中SDK自动调用，不需要单独写。"SplashScreen"为页面名称，可自定义)
        MobclickAgent.onResume(getActivity());//统计时长
        if (sp.getBoolean(UserInfo.ISLOAD, false)) {
            editView.setVisibility(View.VISIBLE);
            edit_share.setVisibility(View.VISIBLE);
            edit_message.setVisibility(View.VISIBLE);
        } else {
            editView.setVisibility(View.INVISIBLE);
            edit_share.setVisibility(View.GONE);
            edit_message.setVisibility(View.GONE);
            mHandler.removeMessages(ANIMATION_IN);
        }
    }

    //编辑信息前如果没团队信息时显示该dialog
    private void showMSGDialog() {
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_msg_reminder, null);
        final Dialog dialog = new Dialog(context, R.style.CustomDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(view, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT));
        Button confirm = (Button) view.findViewById(R.id.bt_msg_dialog_confirm);
        ImageView close = (ImageView) view.findViewById(R.id.iv_msg_dialog_close);
        TextView textView = (TextView) view.findViewById(R.id.tv_msg_dialog_message);
        textView.setPadding(0, 10, 0, getResources().getDimensionPixelSize(R.dimen.dialog_message_padding_button));
        textView.setText(R.string.dialog_msg_content);
        confirm.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                dialog.dismiss();
                Intent add_intent = new Intent();
                add_intent.setClass(getActivity(), TrouTeamEdtiActivity.class);
                startActivity(add_intent);
            }
        });
        close.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
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

    @Override
    public void onStart() {
        super.onStart();
        LogUtil.d(TAG, "onStart");
    }

    private void initView() {
        //初始化头部include
        topTitleView = messageView.findViewById(R.id.title_message_center);
        back = (ImageButton) topTitleView.findViewById(R.id.iv_title_message_center_back);
        back.setOnClickListener(this);
        topTitleTextView = (TextView) topTitleView.findViewById(R.id.tv_title_message_center_title);
        topTitleTextView.setText(R.string.message_center_title_content);
        topTitleSure = (TextView) topTitleView.findViewById(R.id.tv_title_message_center_release);
        topTitleSure.setOnClickListener(this);
        if (!TextUtils.isEmpty(createMessage) && userId.equals(sp.getString(UserInfo.FAIL_USERID_MESSAGE, ""))) {
            topTitleSure.setText(getResources().getString(R.string.again_send_message));
            topTitleSure.setVisibility(View.VISIBLE);
            topTitleSure.setClickable(true);
        } else {
            topTitleSure.setVisibility(View.INVISIBLE);
            topTitleSure.setClickable(false);
        }
        editView = (ImageView) messageView.findViewById(R.id.iv_message_center_edit);
        edit_message = (ImageView) messageView.findViewById(R.id.iv_message_center_edit_message);
        edit_share = (ImageView) messageView.findViewById(R.id.iv_message_center_edit_share);
        editView.setOnClickListener(this);
        edit_message.setOnClickListener(this);
        edit_share.setOnClickListener(this);


        //初始化其他控件
        messageListView = (ListView) messageView.findViewById(R.id.message_center_list);
        messageAdapter = new MessageCenterListAdapter(getActivity(), mList);
        messageListView.setAdapter(messageAdapter);
        messageListView.setOnItemClickListener(itemClickListener);
        messageListView.setOnItemLongClickListener(itemLongClickListener);
    }


    private void initData() {
        sp = context.getSharedPreferences(Constats.SHARE_KEY, Context.MODE_PRIVATE);
        userId = sp.getString(UserInfo.USER_ID, "");
        sign = MD5Util.getMD5(Constats.S_KEY + userId);
        createMessage = sp.getString(UserInfo.FAIL_SAVE_CREATE_MESSAGE, "");
        LogUtil.d(TAG, "createMessage:" + createMessage);
        if (isFirst) {
            //发送handler给service，用于查看进度
            Intent intent = new Intent(context, SendMessageService.class);
            context.bindService(intent, connection, Context.BIND_AUTO_CREATE);
        }
        isFirst = false;
        shareContent = new CreateReceiveMessage();
    }

    ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            SendMessageService.MyBind myBind = (SendMessageService.MyBind) service;
            myBind.sendHandler(mHandler);
            sendHandler = myBind.getHandler();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    @Override
    public void onDestroy() {
        context.unbindService(connection);
        super.onDestroy();
    }

    private void localUpdateView() {
        String content = "";
        if (!TextUtils.isEmpty(userId))
            content = sp.getString(userId + save_sp_key, "");
        if (!TextUtils.isEmpty(content)) {
            Gson gson = new Gson();
            myRequest = gson.fromJson(content.toString(), MessageCenterFragment.myRequest.class);
            LogUtil.d(TAG, "myRequest：" + myRequest.toString());
            if (myRequest != null)
                mHandler.sendEmptyMessage(SHOW_DATA);
        }
    }

    //是否有网络连接的标记
    private boolean isHaveNetFlag = false;

    /**
     * 获取消息线程
     */
    Runnable run = new Runnable() {
        @Override
        public void run() {
            HashMap<String, String> params = new HashMap<>();
            params.put("userid", userId);
            params.put("sign", sign);
            LogUtil.d(TAG, "userid：" + userId + "--sign" + sign + "--" + getMessageUrl);
            JsonRequest jsonRequest = VolleyJsonUtil.createJsonObjectRequest(Request.Method.POST, getMessageUrl, params, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject jsonObject) {
                    isHaveNetFlag = true;
                    try {
                        int result = jsonObject.getInt("result");
                        LogUtil.d(TAG, "uploadingGood_Love：" + jsonObject);
                        if (result == 0) {
                            if (!TextUtils.isEmpty(userId))
                                //保存页面数据
                                sp.edit().putString(userId + save_sp_key, jsonObject.toString()).apply();
                            Gson gson = new Gson();
                            myRequest = gson.fromJson(jsonObject.toString(), MessageCenterFragment.myRequest.class);
                            LogUtil.d(TAG, "myRequest：" + myRequest.toString());
                            if (myRequest != null)
                                mHandler.sendEmptyMessage(SHOW_DATA);
                        } else {
                            ToastUtil.showToast(context, getResources().getString(R.string.t_frag_set_network_err), 4000);
                        }
                    } catch (JSONException e) {
                        LogUtil.d(TAG, "e：" + e.toString());
                        e.printStackTrace();
                    }
//                    dialog.dismiss();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    isHaveNetFlag = false;
                    LogUtil.d(TAG, "volleyError：" + volleyError);
                    ToastUtil.showToast(context, getResources().getString(R.string.t_frag_set_network_err), 4000);
//                    dialog.dismiss();
                }
            });
            LogUtil.d(TAG, "mRequestQueue：" + mRequestQueue);
            if (null != mRequestQueue) {
                mRequestQueue.add(jsonRequest);
            }
        }
    };

    /**
     * 消息删除
     */
    Runnable delete_run = new Runnable() {
        @Override
        public void run() {

            HashMap<String, String> params = new HashMap<>();
            String sign = MD5Util.getMD5(Constats.S_KEY + userId + tuanId);
            params.put("userid", userId);
            params.put("tuanid", tuanId);
            params.put("sign", sign);
            LogUtil.d(TAG, "userid:" + userId + "--tuanid:" + tuanId + "--sign:" + sign);
            JsonRequest request = new VolleyJsonUtil().createJsonObjectRequest(Request.Method.POST, deleteMessageUrl, params, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject jsonObject) {
                    LogUtil.d(TAG, "jsonObject:" + jsonObject);

                    String result = null;
                    try {
                        result = jsonObject.getString("result");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    LogUtil.d(TAG, result);
                    if (!TextUtils.isEmpty(result) && result.equals("0")) {
                        mHandler.sendEmptyMessage(DELETE_ITEM);
                        delete_dialog.dismiss();
                        ToastUtil.showToast(context, getResources().getString(R.string.delete_win), 3000);
                        mHandler.post(run);
                    } else {
                        delete_dialog.dismiss();
                        ToastUtil.showToast(context, getResources().getString(R.string.delete_fail), 3000);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    delete_dialog.dismiss();
                    ToastUtil.showToast(context, getResources().getString(R.string.t_frag_set_network_err), 3000);
                }
            });
            if (null != mRequestQueue) {
                mRequestQueue.add(request);
            }
        }
    };

    public class myRequest {
        public String result;
        public String count;
        public ArrayList<MessageList> item;

        @Override
        public String toString() {
            return "myRequest{" +
                    "result='" + result + '\'' +
                    ", count='" + count + '\'' +
                    ", item=" + item +
                    '}';
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mCallback = (MenuCallback) getActivity();
    }

    private boolean isEdit;
    private boolean isShare;

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.iv_title_message_center_back:
                if (null != mCallback) {
                    mCallback.callback();
                }
                break;
            case R.id.iv_message_center_edit://编辑消息
                ImageUtil.togetherRun(editView, 240);
                if (isEdit) {//聚拢
                    mHandler.sendEmptyMessage(ANIMATION_IN);
                } else {//展开
                    mHandler.sendEmptyMessage(ANIMATION_OUT);
                }
                isEdit = !isEdit;
                break;
            case R.id.iv_message_center_edit_message://消息
                if (!sp.getBoolean(UserInfo.ISLOAD, false)) {
                    ToastUtil.showToast(context, getString(R.string.team_jurisdiction_cancel_prompt), 3000);
                } else {
                    isShare = false;
                    new Thread(isPingRunn).start();
                    ImageUtil.togetherRun(edit_message, 240);
                }
                break;
            case R.id.iv_message_center_edit_share://分享
                LogUtil.d(TAG, "iv_message_center_edit_share 分享");

                if (!sp.getBoolean(UserInfo.ISLOAD, false)) {
                    ToastUtil.showToast(context, getString(R.string.team_jurisdiction_cancel_prompt), 3000);
                } else {
                    isShare = true;
                    new Thread(isPingRunn).start();
                    ImageUtil.togetherRun(edit_share, 240);
                }
                break;
            case R.id.tv_title_message_center_release:
                LogUtil.d(TAG, "tv_title_message_center_release点击");
                //重新上传为上传成功的消息
                if (!TextUtils.isEmpty(topTitleSure.getText()) && topTitleSure.getText().equals(getResources().getString(R.string.again_send_message))) {
                    LogUtil.d(TAG, topTitleSure.getText() + "");
                    createMessage = sp.getString(UserInfo.FAIL_SAVE_CREATE_MESSAGE, "");
                    Gson gson = new Gson();
                    CreateSendMessage createSendMessage = gson.fromJson(createMessage, CreateSendMessage.class);
                    Message message = sendHandler.obtainMessage();
                    message.obj = createSendMessage;
                    message.what = CreateNewMessageActivity.UPLOADING;
                    sendHandler.sendMessage(message);
                    topTitleSure.setVisibility(View.VISIBLE);
                }
                break;
            default:
                break;
        }
    }

    //创建信息前根据当前网络进行不同的操作
    private Runnable isPingRunn = new Runnable() {
        @Override
        public void run() {
            try {
                Thread.sleep(240);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
//            if (isHaveNetFlag) {
            if (!teamDao.isHaveData(userId)) {
                mHandler.sendEmptyMessage(PING_SHOW_DIALOG);
                return;
            }
            mHandler.sendEmptyMessage(PING_SUCC);
//            } else {
//                mHandler.sendEmptyMessage(PING_ERR);
//            }
        }
    };

    //有网状态正常跳转编辑信息
    private void showPingSucc() {
//        Intent singleChoice = new Intent(getActivity(), SingleChoiceActivity.class);
//        startActivity(singleChoice);
//        getActivity().overridePendingTransition(R.anim.right_in, R.anim.left_out);
        showChoiceDialog();
        editView.setEnabled(true);
    }

    /**
     * 选择需要发送的团队弹框
     */
    private void showChoiceDialog() {
        final List<TrouTeam> list = teamDao.getTrouTeamName(MainFragment.MYSELFID);
        CustomChoiceDialog.Builder builder = new CustomChoiceDialog.Builder(context);
        builder.setTitle(R.string.single_choice_title);
        builder.setItems(list);
//        builder.setNegativeBtn(R.string.FragmentSet_logout_cancel, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//            }
//        });
        builder.setcanceledOnTouchOutside(true);
        builder.setCancelable(true);
        CustomChoiceDialog dialog = null;
        builder.setMyOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                tid = list.get(position).id;
                LogUtil.d(TAG, "showChoiceDialog  tuanid  : " + tid);
                if (isShare) {
                    shareDialog();
//                    showShare(context);
                } else {
                    Message message = Message.obtain();
                    message.what = TO_ACTIVITY;
                    message.obj = tid;
                    mHandler.sendMessage(message);
                }
            }
        });
        dialog = builder.create();
        dialog.show();
    }

    /**
     * 输入分享内容弹框
     */
    private void shareDialog() {

        final ClipboardManager cm = (ClipboardManager) context.getSystemService(context.CLIPBOARD_SERVICE);
        final CharSequence text = cm.getText();

        View view = ((Activity) context).getLayoutInflater().inflate(R.layout.my_edit_dialog, null);
        final Dialog dialog = new Dialog(context, R.style.CustomDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // 设置布局
        dialog.setContentView(view, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT));
        dialog.setCancelable(false);

        Button cancel = (Button) view
                .findViewById(R.id.bt_select_dialog_cancel);
        ImageView close = (ImageView) view
                .findViewById(R.id.iv_login_dialog_close);
        Button confirm = (Button) view
                .findViewById(R.id.bt_select_dialog_confirm);
        share_content = (EditText) view.findViewById(R.id.et_share_content);

        if (text != null && Patterns.WEB_URL.matcher(text.toString()).matches()) {
            String s2 = text.toString();
            share_content.setText(s2);
        }

        cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        confirm.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (share_content.getText() != null && !TextUtils.isEmpty(share_content.getText()) && Patterns.WEB_URL.matcher(share_content.getText()).matches()) {
                    new Thread(share_run).start();
                } else {
                    ToastUtil.showToast(context, R.string.url_error, 3000);
                }
                dialog.dismiss();
            }
        });
        close.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.setCanceledOnTouchOutside(false);
        Window window = dialog.getWindow();
        // 设置显示动画
        window.setWindowAnimations(R.style.main_menu_animstyle);
        WindowManager.LayoutParams wl = window.getAttributes();
        wl.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        wl.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        dialog.show();
    }

    /**
     * 上传可以分享的URL
     */
    Runnable share_run = new Runnable() {
        public String allSign;

        @Override
        public void run() {
            Editable text = share_content.getText();
            if (TextUtils.isEmpty(userId) || TextUtils.isEmpty(tid)) {
                ToastUtil.showToast(context, R.string.system_abnormal, 3000);
                return;
            }
            LogUtil.d(TAG, "text.toString():" + text.toString());
            String posturl = posturl(text.toString());
            LogUtil.d(TAG, "posturl:" + posturl);
            Document html = Jsoup.parse(posturl);
            String share_title = html.title();
            LogUtil.d(TAG, "share_title:" + share_title);
            allSign = MD5Util.getMD5(Constats.S_KEY + userId + tid);
            shareContent.setUserid(userId);
            shareContent.setTuanid(tid);
            shareContent.setDesc(text.toString());
            if (TextUtils.isEmpty(share_title)) {
                shareContent.setTitle(getResources().getString(R.string.share));
            } else
                shareContent.setTitle(share_title);
            shareContent.setType(2);
            shareContent.setSign(allSign);

            String json = new Gson().toJson(shareContent);
            LogUtil.d(TAG, "json:" + json);
            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(json);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, shareMessageUrl, jsonObject, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject jsonObject) {
                    try {
                        String msg = jsonObject.getString("result");
                        LogUtil.d(TAG, "jsonObject:" + jsonObject);
                        if (msg.equals("0")) {
                            ToastUtil.showToast(context, R.string.upload_win, 3000);
                            mHandler.post(run);
                        } else {
                            ToastUtil.showToast(context, jsonObject.getString("msg"), 3000);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    ToastUtil.showToast(context, R.string.t_frag_set_network_err, 3000);
                }
            });
            if (mRequestQueue != null)
                mRequestQueue.add(jsonRequest);
        }
    };

    /**
     * 获取参数指定的网页代码，将其返回给调用者，由调用者对其解析 返回String
     */
    public String posturl(String url) {
        InputStream is = null;
        String result = "";
        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(url);
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();
            is = entity.getContent();
        } catch (Exception e) {
            return "Fail to establish http connection!" + e.toString();
        }
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    is, "utf-8"));
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();

            result = sb.toString();
        } catch (Exception e) {
            return "Fail to convert net stream!";
        }
        return result;
    }

    private void toActivity(String tid) {
        LogUtil.d(TAG, "toActivity  tuanid  : " + tid);
        Intent intent = new Intent(context, CreateNewMessageActivity.class);
        intent.putExtra("tuanId", tid);
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.right_in, R.anim.left_out);
    }


    private AdapterView.OnItemLongClickListener itemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

            tuanId = mList.get(position).getTuanid();

            delete_position = position;
            // 长按删除的对话框
            // 获取布局
            View view1 = getActivity().getLayoutInflater().inflate(
                    R.layout.dialog_login_select, null);
            // 设置dialog样式
            final Dialog dialog = new Dialog(context, R.style.CustomDialog);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            // 设置布局
            dialog.setContentView(view1, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT));
            // 获取子控件
            Button cancel = (Button) view1
                    .findViewById(R.id.bt_select_dialog_cancel);
            Button confirm = (Button) view1
                    .findViewById(R.id.bt_select_dialog_confirm);
            TextView title = (TextView) view1
                    .findViewById(R.id.tv_select_dialog_title);
            TextView message = (TextView) view1
                    .findViewById(R.id.tv_select_dialog_message);
            ImageView close = (ImageView) view1
                    .findViewById(R.id.iv_login_dialog_close);

            message.setGravity(Gravity.CENTER);
            // 初始化控件

            title.setText(getString(R.string.TrouManger_delete_dialog_title));
            message.setText(getString(R.string.Message_delete_message));
            //!!!cancel和confirm兑换了
            cancel.setText(getString(R.string.TrouManger_delete_dialog_confirm));
            confirm.setText(getString(R.string.TrouManger_delete_dialog_cancel));
            close.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();

                }
            });
            cancel.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    dialog.dismiss();
                    if (tuanId.equals("-1000")) {
                        ToastUtil.showToast(context, getResources().getString(R.string.not_delete_system_message), 3000);
                        return;
                    }
                    mHandler.post(delete_run);
                    delete_dialog = DialogUtil.createLoadingDialog(context, context.getResources().getString(R.string.delete_ing));
                    delete_dialog.setCanceledOnTouchOutside(true);
                    delete_dialog.show();
                }
            });
            confirm.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    dialog.dismiss();
                }
            });
            // dialog.setCanceledOnTouchOutside(false);
            Window window = dialog.getWindow();
            // 设置显示动画
            //window.setWindowAnimations(R.style.main_menu_animstyle);
            WindowManager.LayoutParams wl = window.getAttributes();
            wl.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            wl.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            dialog.show();
            return true;
        }
    };
    //列表单个监听
    private OnItemClickListener itemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            LogUtil.d(TAG, "position:" + position);
            MessageList data = messageAdapter.getData(position);
            newsId = data.getId();
            Intent intent = new Intent(getActivity(), GuiderMessageActivity.class);
            intent.putExtra("newsId", data.getId());
            intent.putExtra("userId", userId);
            intent.putExtra("guideId", data.getGuideid());
            intent.putExtra("tuanId", data.getTuanid());
            startActivity(intent);
        }
    };
}
