package com.jld.torsun.activity.tours;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonRequest;
import com.google.gson.Gson;
import com.jld.torsun.MyApplication;
import com.jld.torsun.R;
import com.jld.torsun.activity.MainFragment;
import com.jld.torsun.activity.fragment.MenuCallback;
import com.jld.torsun.db.MemberDao;
import com.jld.torsun.db.TeamDao;
import com.jld.torsun.http.VolleyJsonUtil;
import com.jld.torsun.modle.ActionConstats;
import com.jld.torsun.modle.TrouTeam;
import com.jld.torsun.util.AndroidUtil;
import com.jld.torsun.util.Constats;
import com.jld.torsun.util.DensityUtil;
import com.jld.torsun.util.ImageUtil;
import com.jld.torsun.util.LogUtil;
import com.jld.torsun.util.MD5Util;
import com.jld.torsun.util.MyHttpUtil;
import com.jld.torsun.util.ToastUtil;
import com.jld.torsun.util.UserInfo;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author liuzhi
 * @ClassName: FragmentTrouManger
 * @Description: 旅游团管理
 * @date 2015-12-4 下午6:09:08
 */
public class FragmentTrouManger extends Fragment implements OnItemClickListener, AdapterView.OnItemLongClickListener, OnClickListener {
    private static final String TAG = "FragmentTrouManger";
    private View titleView;
    private ImageView imagev_add_trouteam;
    private ListView listv_trouteam_list;
    private TextView tv_add_trouteam_prompt;
    private TextView tv_title_sure;
    /**
     * 数据源
     */
    private List<TrouTeam> mList = new ArrayList<TrouTeam>();
    //本地需上传给服务器端的团队集合
    private List<TrouTeam> mNeedUpdataList = new ArrayList<TrouTeam>();
    private ListViewAdapter mAdapter;

    private SharedPreferences sp;
    private RequestQueue mRequestQueue;

    private MenuCallback mCallback;

    private TeamDao teamDao;
    private MemberDao memberDao;
    private Context context;
    private Activity activity;
    // private TextView dialogTextView;
    private Dialog dialog;
    /**
     * 判断是否能连到服务器的标识
     */
    private boolean flag = true;

    private boolean isHave3gNet = false;

    /**
     * 是否是第一次加载
     */
    private boolean isfrist = true;
    private static boolean isFrist = true;

    /**
     * 在当前碎片的生命周期中只执行一次同步从服务器到本地数据
     */
    private static boolean isFristUpDataforServer = true;

    /**
     * 上传本地数据到服务器的次数控制
     */
    private boolean updataTOServer = true;
    /**
     * 是否是编辑状态
     */
    private boolean isEdit;

    private String editString = "";

    /**
     * 获取到的在线人数的集合
     */
    public static Map<String, String> map = new HashMap<String, String>();

    /**
     * 获取旅游团列表URL
     */
    String getUrl = Constats.HTTP_URL + Constats.GET_TEAM_LIST_FUN;

    /**
     * 删除团URL
     */
    private String delUrl = Constats.HTTP_URL + Constats.DELETE_TEAM_LIST;

    /**
     * 创建旅游团URL
     */
    private String addUrl = Constats.HTTP_URL + Constats.ADD_TEAM_FUN;
    /**
     * 获取权限提示
     */
    private RelativeLayout warn_mode;
    /**
     * 有无3g网络的提示
     */
    private RelativeLayout warn_mode_3g_net;

    //网络改变广播
    private NetChanges netChanges;
    private IntentFilter netChangeIntent;
    private PopupWindow popupWindow;

    public static final int show3gnet = 0x111;
    public static final int hide3gnet = 0x222;
    public static final int ADD_NEW_MEMBER = 2;
    public static final int DELETE_POPUP_WINDOW = 3;
    public static final int EXIT = 4;
    public static final int SHOWDIALOG = 5;
    public static final int CANCEL_WARN_MODE = 6;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (isStop || isHidden)
                return;
            switch (msg.what) {
                case show3gnet://显示3G提示
                    LogUtil.d(TAG, "显示3G网络提示");
                    warn_mode_3g_net.setVisibility(View.VISIBLE);
                    break;
                case hide3gnet://隐藏3G提示
                    warn_mode_3g_net.setVisibility(View.GONE);
                    break;
                case ADD_NEW_MEMBER://新成员加入提示
                    mHandler.sendEmptyMessage(hide3gnet);
                    String nick = (String) msg.obj;
                    showPopupWindow(nick);
                    LogUtil.d(TAG, "接收：" + nick);
                    break;
                case DELETE_POPUP_WINDOW://关闭popupWindow
                    if (popupWindow != null && popupWindow.isShowing()) {
                        if (!isHave3gNet) {
                            mHandler.sendEmptyMessage(show3gnet);
                        }
                        LogUtil.d("FragmentTrouManger", "dismiss");
                        popupWindow.dismiss();
//                        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) listv_trouteam_member_list.getLayoutParams();
                        iv_gong.setVisibility(View.GONE);
                    }
                    break;
                case EXIT:
                    Intent intent = new Intent(getActivity(), MainFragment.class);
                    startActivity(intent);
                    LogUtil.d(TAG, "EXIT");
                    break;
                case SHOWDIALOG:
                    LogUtil.d(TAG, "SHOWDIALOG");
                    break;
                case CANCEL_WARN_MODE:
                    warn_mode.setVisibility(View.GONE);
                    break;
                default:
                    break;
            }
        }
    };

    private ImageView iv_gong;
    //private RelativeLayout title_trouteam_manger;
    private long startTime;

    /**
     * 新成员加入PopupWindow提示
     */
    private void showPopupWindow(String message) {
        if (getUserVisibleHint()) {
            if (popupWindow != null && popupWindow.isShowing()) {
                popupWindow.dismiss();
            }
            View view = LayoutInflater.from(context).inflate(R.layout.member_add_popupwind, null);
            TextView tv_message = (TextView) view.findViewById(R.id.tv_warn_message2);
            ImageView iv_add_warn_delete = (ImageView) view.findViewById(R.id.iv_add_warn_delete);
            iv_add_warn_delete.setOnClickListener(this);
            tv_message.setText(message);
            popupWindow = new PopupWindow(view, ActionBar.LayoutParams.MATCH_PARENT,
                    ActionBar.LayoutParams.WRAP_CONTENT);
            iv_gong.setVisibility(View.VISIBLE);
            warn_mode_3g_net.setVisibility(View.GONE);
            if (titleView != null && FragmentTrouManger.this.isVisible())
                popupWindow.showAsDropDown(titleView);
            Thread delete_pop_thread = new Thread(runnable);
            delete_pop_thread.start();
            startTime = System.currentTimeMillis();
        }
    }

    /**
     * 两秒钟后自动关闭popupWindow
     */
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            long endTime = System.currentTimeMillis();
            if (endTime - startTime >= 3000 && popupWindow != null && popupWindow.isShowing()) {
                LogUtil.d(TAG, "dismiss");
                mHandler.sendEmptyMessage(DELETE_POPUP_WINDOW);
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        LogUtil.i(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.activity_trouteam_add, container,
                false);
        // mRequestQueue = Volley.newRequestQueue(getActivity());
        MyApplication ma = (MyApplication) getActivity().getApplication();
        mRequestQueue = ma.getRequestQueue();
        sp = getActivity().getSharedPreferences(Constats.SHARE_KEY,
                Context.MODE_PRIVATE);
        activity = getActivity();
        context = getActivity();
        teamDao = TeamDao.getInstance(context);
        memberDao = MemberDao.getInstance(context);
        warn_mode_3g_net = (RelativeLayout) view.findViewById(R.id.trou_warn_3g_net);
        if (null == netChanges) {
            warn_mode_3g_net.setVisibility(View.GONE);
            netChanges = new NetChanges();
            netChangeIntent = new IntentFilter(ActionConstats.NET_CONNECT_CHANGE);
            context.registerReceiver(netChanges, netChangeIntent);
        }
        initView(view);
        MulticastClient.sendHandler(mHandler);

        showAddTrouDialog();
        return view;
    }

    private void initView(View view) {

        warn_mode = (RelativeLayout) view.findViewById(R.id.trou_warn);
        ImageView warn_delete = (ImageView) warn_mode.findViewById(R.id.warn_delete);
        warn_delete.setOnClickListener(this);

        iv_gong = (ImageView) view.findViewById(R.id.iv_trouteam_gong);
//        title_trouteam_manger = (RelativeLayout) view.findViewById(R.id.title_trouteam_manger);

        listv_trouteam_list = (ListView) view
                .findViewById(R.id.listv_trouteam_list);
        titleView = view.findViewById(R.id.title_trouteam_add);
        ImageView image_title_back = (ImageView) titleView.findViewById(R.id.title_image_title_back);
        TextView tv_title_title = (TextView) titleView.findViewById(R.id.tv_title_title);

        // ImageView image_title_back = (ImageView) title_trouteam_manger
        //         .findViewById(R.id.title_image_title_back);
        // TextView tv_title_title = (TextView) view
        //         .findViewById(R.id.tv_title_title);
        tv_title_title.setText(R.string.trou_team_manager);
        tv_title_sure = (TextView) titleView.findViewById(R.id.tv_title_sure);
        tv_title_sure.setVisibility(View.INVISIBLE);
        image_title_back.setOnClickListener(this);
        tv_add_trouteam_prompt = (TextView) view
                .findViewById(R.id.tv_add_trouteam_prompt);
        imagev_add_trouteam = (ImageView) view
                .findViewById(R.id.imagev_add_trouteam);
//        imagev_add_trouteam.setOnTouchListener(ivTouchListener);
        imagev_add_trouteam.setOnClickListener(this);
    }

//    private void togetherRun(ImageView imageView) {
//        ObjectAnimator anim1 = ObjectAnimator.ofFloat(imageView, "scaleX",
//                1.0f, 0.8f, 1.1f, 1.0f);
//        ObjectAnimator anim2 = ObjectAnimator.ofFloat(imageView, "scaleY",
//                1.0f, 0.8f, 1.1f, 1.0f);
//        AnimatorSet animSet = new AnimatorSet();
//        animSet.setDuration(240);
//        animSet.setInterpolator(new LinearInterpolator());
//        //两个动画同时执行
//        animSet.playTogether(anim1, anim2);
//        animSet.start();
//    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.imagev_add_trouteam:
                boolean aBoolean = sp.getBoolean(UserInfo.ISLOAD, false);
                if (!aBoolean){
                    ToastUtil.showToast(context, getResources().getString(R.string.tour_guide_power_lose), 3000);
                    return;
                }
                imagev_add_trouteam.setEnabled(false);
//                togetherRun(imagev_add_trouteam);
                ImageUtil.togetherRun(imagev_add_trouteam, 240);
                if (isEdit) {
                    tv_title_sure.setText(R.string.edit);
                    isEdit = false;
                    mAdapter.notifyDataSetChanged();
                    editString = "";
                }
                updataTOServer = true;
                imagev_add_trouteam.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent add_intent = new Intent();
                        add_intent.setClass(getActivity(), TrouTeamEdtiActivity.class);
                        startActivity(add_intent);
                        imagev_add_trouteam.setEnabled(true);
                    }
                }, 400);

                break;
            case R.id.title_image_title_back:
                if (null != mCallback) {
                    if (isEdit) {
                        tv_title_sure.setText(R.string.edit);
                        isEdit = false;
                        mAdapter.notifyDataSetChanged();
                        editString = "";
                    }
                    mCallback.callback();
                }
                break;
            case R.id.warn_delete:
//                Animation animation = AnimationUtils.loadAnimation(context, R.anim.alpha_warn);
//                warn_mode.startAnimation(animation);
                warn_mode.setVisibility(View.GONE);
                break;
            case R.id.iv_add_warn_delete://取消添加成员提示
                mHandler.sendEmptyMessage(DELETE_POPUP_WINDOW);
                break;
        }
    }

    private OnTouchListener ivTouchListener = new OnTouchListener() {
        int[] temp = new int[]{0, 0};
        float y1, y2;

        public boolean onTouch(View v, MotionEvent event) {

            int eventaction = event.getAction();

            int x = (int) event.getRawX();
            int y = (int) event.getRawY();

            switch (eventaction) {

                case MotionEvent.ACTION_DOWN: // touch down so check if the
                    y1 = event.getRawY();
                    temp[0] = (int) event.getX();
                    temp[1] = y - v.getTop();
                    break;

                case MotionEvent.ACTION_MOVE: // touch drag with the ball
                    //v.layout(x - temp[0], y - temp[1], x + v.getWidth() - temp[0], y - temp[1] + v.getHeight());
                    v.layout(v.getLeft(), y - temp[1], v.getRight(), y - temp[1] + v.getHeight());
//                  v.postInvalidate();

                    break;

                case MotionEvent.ACTION_UP:
                    y2 = event.getRawY();
                    if (Math.abs(y1 - y2) > 60) {
                        return true;
                    } else {
                        break;
                    }
            }

            return false;
        }
    };

    private void showAddTrouDialog() {
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_add_trou_reminder, null);
        final Dialog dialog = new Dialog(context, R.style.CustomDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(view, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT));
        Button confirm = (Button) view.findViewById(R.id.bt_add_trou_dialog_confirm);
        ImageView close = (ImageView) view.findViewById(R.id.iv_add_trou_dialog_close);
        TextView textView = (TextView) view.findViewById(R.id.tv_add_trou_dialog_message);
        textView.setPadding(0, 10, 0, getResources().getDimensionPixelSize(R.dimen.dialog_message_padding_button));
        textView.setText(R.string.custom_toast);
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

    /**
     * 团队Item长按响应
     */
    private void show_Logout_Dialog(final TrouTeam item) {
        // 长按删除的对话框
        // 获取布局
        View view = getActivity().getLayoutInflater().inflate(
                R.layout.dialog_login_select, null);
        // 设置dialog样式
        final Dialog dialog = new Dialog(context, R.style.CustomDialog);
        // 设置布局
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
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
        ImageView close = (ImageView) view
                .findViewById(R.id.iv_login_dialog_close);

        message.setGravity(Gravity.CENTER);
        // 初始化控件

        title.setText(getString(R.string.TrouManger_delete_dialog_title));
        message.setText(getString(R.string.TrouManger_delete_dialog_message));
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
                deleteTeam(item.id, item.localID);
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
    }

    private boolean isfristFocus = true;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mCallback = (MenuCallback) getActivity();
        mList = teamDao.selectAllTeamToDesc(MainFragment.MYSELFID);
        mAdapter = new ListViewAdapter();
        listv_trouteam_list.setAdapter(mAdapter);
        listv_trouteam_list.setOnItemClickListener(this);
        listv_trouteam_list.setOnItemLongClickListener(this);
        if (isfristFocus) {
            isfristFocus = false;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                float height = AndroidUtil.getStatusHeight(context);
                LogUtil.d(TAG, "--------------状态栏的高度为:" + height);

                int viewHeight = DensityUtil.px2dip(context, (216f - height * 2));
                LogUtil.d(TAG, "--------------viewHeight的高度:" + viewHeight);
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) titleView.getLayoutParams();
                params.height = viewHeight;
                titleView.setLayoutParams(params);
            }
        }
    }

    /**
     * 将本地团队数据上传给服务器
     */
    private void uploadDataToServer() {
        LogUtil.d(TAG, "-------将本地团队数据上传给服务器--------");
        mNeedUpdataList = teamDao.getNeedUpdataTrouTeam(MainFragment.MYSELFID);
        if (null != mNeedUpdataList && mNeedUpdataList.size() > 0) {
            LogUtil.d(TAG, "-------需上传的团队个数:" + mNeedUpdataList.size());
            String sign = MD5Util
                    .getMD5(Constats.S_KEY + MainFragment.MYSELFID);
            for (TrouTeam team : mNeedUpdataList) {
                Map<String, String> params = new HashMap<String, String>();
                params.put("userid", MainFragment.MYSELFID);
                params.put("localid", team.createtime);
                params.put("name", team.name);
                params.put("time", team.createtime);
                params.put("sign", sign);
                synchronized (params) {
                    LogUtil.d(TAG, "-------localid---createtime-:" + team.createtime);
                    createTeam(params, team.localID);
                }
            }
        }
        if (isFristUpDataforServer) {
            LogUtil.d(TAG, "-------从服务器端更新数据到终端-------");
            isFristUpDataforServer = false;
            getTrouListHttp();
        }
    }

    // 提交数据到服务器
    @SuppressWarnings("unchecked")
    private void createTeam(Map<String, String> params, final String localID) {
        JsonRequest<JSONObject> jsonRequest = VolleyJsonUtil
                .createJsonObjectRequest(Method.POST, addUrl, params,
                        new Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    int result = response.getInt("result");
                                    String message = response.getString("msg");
                                    if (0 == result) {
                                        if (response.has("item")) {
                                            JSONObject jsonObject = response
                                                    .getJSONObject("item");
                                            String teamid = jsonObject
                                                    .getString("id");
                                            // LogUtil.i("",
                                            // "------teamid---localid-"+teamid+"----"+localid);
                                            teamDao.updateTeam(teamid, localID);
                                            sp.edit().putString(UserInfo.LAST_SERVICE_TEAM_ID, teamid).apply();//最新一个旅游团 ID
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                updataTOServer = true;
                            }
                        });
        if (null != mRequestQueue) {
            mRequestQueue.add(jsonRequest);
        }

    }

    /**
     * 从服务器获取旅游团列表
     */
    @SuppressWarnings("unchecked")
    private void getTrouListHttp() {
        String sign = MD5Util.getMD5(Constats.S_KEY + MainFragment.MYSELFID);
        Map<String, String> params = new HashMap<String, String>();
        params.put("userid", MainFragment.MYSELFID);
        params.put("sign", sign);
        // getTrouListHttp(params);
        JsonRequest<JSONObject> jsonRequest = VolleyJsonUtil
                .createJsonObjectRequest(Method.POST, getUrl, params,
                        new Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    int result = response.getInt("result");
                                    if (0 == result) {
                                        Gson gson = new Gson();
                                        Respon respon = gson.fromJson(response.toString(), Respon.class);
                                        mList = respon.item;
                                        LogUtil.d(TAG, "--------getTrouListHttp返回值:" + mList.toString());
                                        updateTeaminfo();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                flag = false;
                                isFristUpDataforServer = true;
                                mList = teamDao
                                        .selectAllTeamToDesc(MainFragment.MYSELFID);
                                // refreshListView();
                            }
                        });
        if (null != mRequestQueue) {
            mRequestQueue.add(jsonRequest);
        }
    }

    /**
     * 将服务器团队信息同步到本地
     */
    private void updateTeaminfo() {

        Collections.reverse(mList);
        // for (TrouTeam team : mList) {
        // // LogUtil.i("", "-----------team:" + team.toString());
        // teamDao.insertTeamToDir(MainFragment.MYSELFID, team);
        // }
        int size = mList.size();
        for (int i = 0; i < size; i++) {
            teamDao.insertTeamToDir(MainFragment.MYSELFID, mList.get(i));
        }
        Collections.reverse(mList);
        refreshListView();
    }

    /**
     * 刷新与显示团队信息列表
     */
    private void refreshListView() {
        mList = teamDao.selectAllTeamToDesc(MainFragment.MYSELFID);
        if (mList.size() > 0) {
            sp.edit().putString(UserInfo.LAST_SERVICE_TEAM_ID, mList.get(0).id).apply();
            LogUtil.d(TAG, "从服务器获取最新ID：" + mList.get(0).id);
        }
        if (null != mList && mList.size() > 0) {
            listv_trouteam_list.setVisibility(View.VISIBLE);
            tv_add_trouteam_prompt.setVisibility(View.GONE);
            mAdapter.notifyDataSetChanged();
        } else {
            if (isEdit) {
                tv_title_sure.setText(R.string.edit);
                isEdit = false;
            }
            listv_trouteam_list.setVisibility(View.GONE);
            tv_add_trouteam_prompt.setVisibility(View.VISIBLE);
        }
    }

    /**
     * listview单击监听
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        boolean aBoolean = sp.getBoolean(UserInfo.ISLOAD, false);
        if (!aBoolean){
            ToastUtil.showToast(context, getResources().getString(R.string.tour_guide_power_lose), 3000);
            return;
        }
        String localid = mList.get(position).localID;
        String tid = mList.get(position).id;
        String trouName = mList.get(position).name;
        Intent intent = new Intent();
        intent.setClass(getActivity(), TrouTeamMemberManagerActivity.class);
        intent.putExtra(TrouTeamMemberManagerActivity.TEAM_Id, localid);
        intent.putExtra(TrouTeamMemberManagerActivity.TID, tid);
        intent.putExtra(TrouTeamMemberManagerActivity.TEAM_NUM, position);
        intent.putExtra(TrouTeamMemberManagerActivity.TEAM_name, trouName);
        startActivity(intent);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        TrouTeam trouTeam = mList.get(position);
        show_Logout_Dialog(trouTeam);
        return true;
    }

    /**
     * 删除团
     */
    @SuppressWarnings("unchecked")
    protected void deleteTeam(String teamid, final String local_tid) {
        String sign = MD5Util.getMD5(Constats.S_KEY + MainFragment.MYSELFID
                + teamid);
        Map<String, String> params = new HashMap<String, String>();
        params.put("userid", MainFragment.MYSELFID);
        params.put("tuanid", teamid);
        params.put("sign", sign);
        // final String teamidString = teamid;
        JsonRequest<JSONObject> jsonRequest = VolleyJsonUtil
                .createJsonObjectRequest(Method.POST, delUrl, params,
                        new Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    int result = response.getInt("result");
                                    if (0 == result) {
                                        teamDao.deleteTeam(local_tid);
                                        memberDao.deleteMember2Team(local_tid);
                                        if (!MyApplication.isfristSavaLastTeamID) {
                                            sp.edit().putString(UserInfo.LAST_TEAM_ID, teamDao.selectLastTeamid(MainFragment.MYSELFID)).apply();
                                            sp.edit().putString(UserInfo.LAST_SERVICE_TEAM_ID, teamDao.selectServiceLastTeamid(MainFragment.MYSELFID)).apply();
                                        }

                                        mList = teamDao.selectAllTeamToDesc(MainFragment.MYSELFID);
                                        refreshListView();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                        }, new ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                ToastUtil.showToast(context, R.string.t_frag_trou_net_err_re, 2000);
                            }
                        });
        if (null != mRequestQueue) {
            mRequestQueue.add(jsonRequest);
        }
    }

    class Respon {
        public int result;
        public String msg;
        public List<TrouTeam> item;
    }

    class ListViewAdapter extends BaseAdapter {

        public ListViewAdapter() {
            showAnimation.setDuration(1000);
            hideAnimation.setDuration(1000);
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public TrouTeam getItem(int arg0) {
            return mList.get(arg0);
        }

        @Override
        public long getItemId(int arg0) {
            return arg0;
        }

        private Animation showAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
        private Animation hideAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                0.0f);

        @Override
        public View getView(final int position, View contentView, ViewGroup arg2) {
            ViewHold temp_ViewHold = null;
            if (null == contentView) {
                contentView = LayoutInflater.from(activity).inflate(
                        R.layout.item_trou_team_layout, null);
                temp_ViewHold = new ViewHold();
                temp_ViewHold.tv_trouteam_item_name = (TextView) contentView
                        .findViewById(R.id.tv_trouteam_item_name);
                temp_ViewHold.tv_trouteam_item_time = (TextView) contentView
                        .findViewById(R.id.tv_trouteam_item_time);
                temp_ViewHold.tv_trouteam_item_member = (TextView) contentView
                        .findViewById(R.id.tv_trouteam_item_member);
                temp_ViewHold.imagev_trouteam_list_icon = (ImageView) contentView
                        .findViewById(R.id.imagev_trouteam_list_icon);
                temp_ViewHold.deleteButton = (Button) contentView
                        .findViewById(R.id.bt_trouteam_item_delete);
                contentView.setTag(temp_ViewHold);
            } else {
                temp_ViewHold = (ViewHold) contentView.getTag();
            }
            final TrouTeam item = mList.get(position);
            if (isEdit) {
                temp_ViewHold.deleteButton.setAnimation(showAnimation);
                temp_ViewHold.deleteButton.setVisibility(View.VISIBLE);
                temp_ViewHold.deleteButton
                        .setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View arg0) {
                                // 删除团队
                                deleteTeam(item.id, item.localID);
                            }
                        });
            } else if (!TextUtils.isEmpty(editString)) {
                temp_ViewHold.deleteButton.setAnimation(hideAnimation);
                temp_ViewHold.deleteButton.setVisibility(View.GONE);
                editString = "";
            } else {
                temp_ViewHold.deleteButton.setVisibility(View.GONE);
            }
            String createtime = item.createtime;
            String[] createtimes = createtime.split(" ");
            createtime = createtimes[0].substring(2);
            temp_ViewHold.tv_trouteam_item_name.setText(item.name);
            temp_ViewHold.tv_trouteam_item_time.setText(createtime);
            temp_ViewHold.tv_trouteam_item_member.setText(item.show);
            if (0 == position) {
                temp_ViewHold.imagev_trouteam_list_icon
                        .setImageResource(R.mipmap.trou_team_list_able);
            } else {
                temp_ViewHold.imagev_trouteam_list_icon
                        .setImageResource(R.mipmap.trou_team_list_enable);
            }
            return contentView;
        }

        class ViewHold {
            TextView tv_trouteam_item_name, tv_trouteam_item_time,
                    tv_trouteam_item_member;
            ImageView imagev_trouteam_list_icon;
            Button deleteButton;
        }
    }


    class NetChanges extends BroadcastReceiver {
        public NetChanges() {
            select3gnet();
        }

        @Override
        public void onReceive(Context mContext, Intent intent) {
            final String action = intent.getAction();
            if (ActionConstats.NET_CONNECT_CHANGE.equals(action)) {
                select3gnet();
            }
        }
    }

    private void select3gnet() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                if (MyHttpUtil.ping()) {
                    isHave3gNet = true;
                } else {
                    isHave3gNet = false;
                }
                if (null != warn_mode_3g_net) {
                    if (isHave3gNet) {
                        mHandler.sendEmptyMessage(hide3gnet);
                        //有网同步本地团
                        uploadDataToServer();
                    } else {
                        mHandler.sendEmptyMessage(show3gnet);
                    }
                }
            }
        }.start();
    }

    @Override
    public void onDestroy() {
        if (null != netChanges) {
            context.unregisterReceiver(netChanges);
            netChanges = null;
        }
        isfristFocus = true;
        super.onDestroy();
    }

    private boolean isHidden = false;

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        LogUtil.d(TAG, "-------onHiddenChanged:" + hidden);
        isHidden = hidden;
        if (hidden) {
            hidden();
        } else {
            show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        LogUtil.d(TAG, "-------onResume:" + isHidden);
        if (!isHidden) {
            show();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!isHidden) {
            hidden();
        }
    }

    public void hidden() {
        MobclickAgent.onPageEnd("团队管理"); // （仅有Activity的应用中SDK自动调用，不需要单独写）保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息。"SplashScreen"为页面名称，可自定义
        MobclickAgent.onPause(getActivity());
    }

    private boolean isStop = false;

    @Override
    public void onStop() {
        super.onStop();
        isStop = true;
    }


    @Override
    public void onStart() {
        super.onStart();
        isStop = false;
    }

    public void show() {
        MobclickAgent.onPageStart("团队管理"); //统计页面(仅有Activity的应用中SDK自动调用，不需要单独写。"SplashScreen"为页面名称，可自定义)
        MobclickAgent.onResume(getActivity());//统计时长
        if (!isFrist) {
            warn_mode.setVisibility(View.GONE);
        } else {
            isFrist = false;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    mHandler.sendEmptyMessage(CANCEL_WARN_MODE);
                }
            }).start();
        }

        if (updataTOServer) {
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    // 上传本地数据
                    uploadDataToServer();
                    // 同步服务器数据
                    //getTrouListHttp();
                }
            }.start();
            updataTOServer = false;
        }
        // 获取最新的本地团ID
        if (!MyApplication.isfristSavaLastTeamID) {
            String lastTeamid = teamDao.selectLastTeamid(MainFragment.MYSELFID);
            if (!TextUtils.isEmpty(lastTeamid)) {
                sp.edit().putString(UserInfo.LAST_TEAM_ID, lastTeamid).apply();
                LogUtil.i(TAG, "-----FragmentTrouManger--当前本地团id:" + lastTeamid);
            }
        }
        refreshListView();
        tv_title_sure.setText(R.string.edit);
        tv_title_sure.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (!flag) {
                    ToastUtil.showToast(context, R.string.t_frag_set_network_err, 2000);
                    return;
                } else if (mList.size() == 0) {
                    ToastUtil.showToast(context, R.string.t_frag_trou_text, 2000);
                    return;
                } else if (!isEdit) {
                    tv_title_sure.setText(R.string.complete);
                    isEdit = true;
                    editString = "edit";
                    mAdapter.notifyDataSetChanged();
                } else {
                    tv_title_sure.setText(R.string.edit);
                    isEdit = false;
                    mAdapter.notifyDataSetChanged();
                    //editString="";
                }
            }
        });
        flag = true;
    }
}
