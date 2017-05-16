package com.jld.torsun.activity.tours;

import android.app.ActionBar;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonRequest;
import com.google.gson.Gson;
import com.jld.torsun.ActivityManageFinish;
import com.jld.torsun.MyApplication;
import com.jld.torsun.R;
import com.jld.torsun.activity.BaseActivity;
import com.jld.torsun.activity.MainFragment;
import com.jld.torsun.adapter.TeamMemberListAdapter;
import com.jld.torsun.db.MemberDao;
import com.jld.torsun.db.TeamDao;
import com.jld.torsun.http.VolleyJsonUtil;
import com.jld.torsun.modle.TeamMember;
import com.jld.torsun.util.AndroidUtil;
import com.jld.torsun.util.Constats;
import com.jld.torsun.util.DensityUtil;
import com.jld.torsun.util.DialogUtil;
import com.jld.torsun.util.LogUtil;
import com.jld.torsun.util.MD5Util;
import com.jld.torsun.util.MyHttpUtil;
import com.jld.torsun.util.ToastUtil;
import com.jld.torsun.util.UserInfo;
import com.lidroid.xutils.bitmap.PauseOnScrollListener;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

//import java.net.InetAddress;
//import java.net.UnknownHostException;
//import com.jld.torsun.db.TeamDao;

/**
 * @author liuzhi
 * @ClassName: TrouTeamMemberManagerActivity
 * @Description: 单个旅游团下面的成员显示界面
 * @date 2015-12-2 上午10:40:47
 */
public class TrouTeamMemberManagerActivity extends BaseActivity {

    private static final String TAG = "TrouTeamMemberManagerActivity";

    public static final String TEAM_name = "team_name";
    public static final String TEAM_Id = "local_id";//本地团队ID
    public static final String TID = "teamid";//服务器团队ID
    public static final String TEAM_NUM = "team_num";
    private View titleView;
    private ListView listv_trouteam_member_list;
    private TeamMemberListAdapter mAdapter;
    private TextView tv_title_title;
    private ArrayList<TeamMember> mList = new ArrayList<>();
    private RequestQueue mRequestQueue;
    private MemberDao mDao;
    private TeamDao teamDao;
    private MulticastClient multicastClient;
    private LinearLayout ll_top_refresh;
    private ProgressBar mProgressBar;
    private TextView mTextView;
    private Context context;
    private String addMemberUrl = Constats.HTTP_URL
            + Constats.ADD_TEAM_MEMBER_FUN;//添加团队成员信息的URl
    private String getMemberUrl = Constats.HTTP_URL
            + Constats.GET_TEAM_MEMBER_FUN;//获取团队成员信息的URl
    private TimeOut timeOut;
    private int num;//记录点进来的listview的postion
    private String name = "";//旅游团名称
    private String localid;//本地ID
    private String teamid;//服务器ID
    private Dialog dialog;
    private Thread emptyThread;
    public final int DELETE_POPUP_WINDOW = 3;
    public static final int ADD_NEW_MEMBER = 2;
    public static final int REFRESH_LISTING = 1;
    public static final int SENDTEAM = 4;
    private PopupWindow popupWindow;
    private Thread delete_pop_thread;
    private long startTime;
    private Boolean isShow = false;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case REFRESH_LISTING:
                    if (isDestroy)
                        return;
                    refreshListView();//刷新list列表
                    break;
                case ADD_NEW_MEMBER://新成员加入提示
                    if (isDestroy)
                        return;
                    String nick = (String) msg.obj;
                    if (isShow)
                        showPopupWindow(nick);
                    LogUtil.d(TAG, "接收：" + nick);
                    refreshListView();//刷新list列表
                    break;
                case DELETE_POPUP_WINDOW://关闭popupWindow
                    mDao.getUserids(sp.getString(UserInfo.LAST_TEAM_ID, ""));
                    mDao.getIcons(sp.getString(UserInfo.LAST_TEAM_ID, ""));
                    if (popupWindow != null && popupWindow.isShowing() && !TrouTeamMemberManagerActivity.this.isFinishing()) {
                        popupWindow.dismiss();
//                        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) listv_trouteam_member_list.getLayoutParams();
                        iv_gong.setVisibility(View.GONE);
                    }
                    break;
                case FragmentTrouManger.SHOWDIALOG:
                    show_update_Dialog();

                    break;
                case SENDTEAM:
                    mHandler.post(updataToServer);
                    break;
            }
        }
    };
    private ImageView iv_gong;
    private SharedPreferences sp;

    /**
     * 导游权限失效dialog
     */
    private void show_update_Dialog() {

        if (!isShow)
            return;

        // 获取布局
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_update_prompt, null);

        // 设置dialog样式
        final Dialog dialog = new Dialog(this, R.style.CustomDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 设置布局
        dialog.setContentView(view, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT));

        // 获取子控件
        Button confirm = (Button) view
                .findViewById(R.id.bt_update_dialog_confirm);
        ImageView close = (ImageView) view
                .findViewById(R.id.iv_updete_dialog_close);
        TextView update_dialog_message = (TextView) view
                .findViewById(R.id.tv_update_dialog_message);
        update_dialog_message.setText(this.getResources().getString(R.string.team_jurisdiction_cancel_prompt));
        update_dialog_message.setText(this.getResources().getString(
                R.string.t_frag_set_update_cont_1));
        confirm.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                dialog.dismiss();
                Intent intent = new Intent(TrouTeamMemberManagerActivity.this, MainFragment.class);
                startActivity(intent);
            }
        });
        close.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Intent intent = new Intent(TrouTeamMemberManagerActivity.this, MainFragment.class);
                startActivity(intent);
            }
        });
        // dialog.setCanceledOnTouchOutside(false);
        Window window = dialog.getWindow();
        // 设置显示动画
        window.setWindowAnimations(R.style.main_menu_animstyle);
        WindowManager.LayoutParams wl = window.getAttributes();
        wl.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        wl.height = ViewGroup.LayoutParams.WRAP_CONTENT;
//            dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);//将弹出框设置为全局
        dialog.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtil.d(TAG, "onCreate");

        setContentView(R.layout.activity_trouteam_manager);
        ActivityManageFinish.addActivity(this);
        mDao = MemberDao.getInstance(this);
        teamDao = TeamDao.getInstance(this);
        MyApplication ma = (MyApplication) getApplication();
        mRequestQueue = ma.getRequestQueue();
        sp = getSharedPreferences(Constats.SHARE_KEY, Context.MODE_PRIVATE);

        localid = getIntent().getStringExtra(TEAM_Id);//本地团ID
        LogUtil.d(TAG, "----获取到的-localid--" + localid);
        teamid = getIntent().getStringExtra(TID);//团ID
        name = getIntent().getStringExtra(TEAM_name);//团名称
        num = getIntent().getIntExtra(TEAM_NUM, -1);//团队编号
        timeOut = new TimeOut(4500, 1000);
        MyApplication application = (MyApplication) getApplication();
        // 同步数据
        if (!TextUtils.isEmpty(teamid) && !application.trouTeamSyncData) {
//            mHandler.post(updataToServer);
            mHandler.post(updataToDir);

        }
        initView();
        context = TrouTeamMemberManagerActivity.this;
        //自动清空线程
//        new Thread(task).start();
        multicastClient.sendHandler(mHandler);
        mHandler.sendEmptyMessage(REFRESH_LISTING);
        isDestroy = false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        LogUtil.d(TAG, "onStart");
        isShow = true;
    }

    private boolean isfristFocus = true;

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (isfristFocus && hasFocus) {
            isfristFocus = false;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                float height = AndroidUtil.getStatusHeight(this);
                LogUtil.d(TAG, "--------------状态栏的高度为:" + height);
                int viewHeight = DensityUtil.px2dip(this, (216f - height * 2));
                LogUtil.d(TAG, "--------------viewHeight的高度:" + viewHeight);
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) titleView.getLayoutParams();
                params.height = viewHeight;
                titleView.setLayoutParams(params);
            }
        }
    }

    private boolean isDestroy = false;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtil.d(TAG, "onDestroy");
        isfristFocus = true;
        isDestroy = true;
        ActivityManageFinish.removeActivity(this);
    }

    private void initView() {
        /**
         * warn
         */
//        rl_warn = (RelativeLayout) findViewById(R.id.trou_item_warn);
//        warn_messamge = (TextView) rl_warn.findViewById(R.id.warn_message2);
        iv_gong = (ImageView) findViewById(R.id.iv_troumanager_gong);
        /**
         * progressBar
         */
        mProgressBar = (ProgressBar) findViewById(R.id.pb_top_refresh);
        mTextView = (TextView) findViewById(R.id.tv_top_refresh);
        ll_top_refresh = (LinearLayout) findViewById(R.id.ll_top_refresh);

        /**
         * title
         */
        titleView = findViewById(R.id.title_trouteam_member_manager);
        ImageView image_title_back = (ImageView) titleView.findViewById(R.id.title_image_title_back);
        tv_title_title = (TextView) titleView.findViewById(R.id.tv_title_title);
        tv_title_title.setText(name);
        TextView tv_title_sure = (TextView) titleView.findViewById(R.id.tv_title_sure);
        if (num != 0) {

            tv_title_sure.setVisibility(View.INVISIBLE);
            tv_title_sure.setClickable(false);
        } else {
            tv_title_sure.setClickable(true);
            tv_title_sure.setText(R.string.refush);
        }
        image_title_back.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                finish();
            }
        });
        tv_title_sure.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {

//                if (MyApplication.isfristSavaLastTeamID && 0 == num) {
//                    MyApplication.isfristSavaLastTeamID = false;
//                    String last_Team_id = teamDao.selectLastTeamid();
//                    if (!TextUtils.isEmpty(last_Team_id)) {
//                        sp.edit().putString(UserInfo.LAST_TEAM_ID, last_Team_id).apply();
//                        LogUtil.i(TAG, "-----FragmentTrouManger--当前本地团id:" + last_Team_id);
//                    }
//                }
                dialog = DialogUtil.createLoadingDialog(TrouTeamMemberManagerActivity.this, "正在刷新...");
                dialog.setCanceledOnTouchOutside(false);// 设置点击屏幕Dialog不消失
                if (dialog.isShowing())
                    dialog.dismiss();
                dialog.show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        dialog.dismiss();
                    }
                }).start();
            }
        });
        // 刷新监听

        /**
         * listView
         */
        listv_trouteam_member_list = (ListView) findViewById(R.id.lv_trouteam_member_list);
        mList = (ArrayList<TeamMember>) mDao.findMemberByteamid(localid);
        listv_trouteam_member_list.setOnItemClickListener(onItemClickListener);
        mAdapter = new TeamMemberListAdapter(this, mList, num);
        listv_trouteam_member_list.setAdapter(mAdapter);
        listv_trouteam_member_list.setOnScrollListener(new PauseOnScrollListener(mAdapter.bitmapUtils, true, true));
        setFlingView(listv_trouteam_member_list);
        mHandler.sendEmptyMessage(REFRESH_LISTING);
    }
    private Runnable updataToServer = new Runnable() {
        @Override
        public void run() {
            String localId = sp.getString(UserInfo.LAST_TEAM_ID, "");
            if (!TextUtils.isEmpty(localId) && !TextUtils.isEmpty(teamid) && !("0".equals(teamid))) {
                addUserForDirToServer();//将本地数据同步到服务器
            }
        }
    };

    public String listToString(String str) {
        //去掉字符串中间的空格
        str = str.replace(" ", "");
        int end = str.length() - 1;
        return str.substring(1, end);
    }

    /**
     * 将本地的成员同步到服务器
     */
    @SuppressWarnings("unchecked")
    public void addUserForDirToServer() {
        List<String> mList = mDao.selectAllUidByTid(localid);
        if (null != mList && mList.size() > 0) {
            String sign = MD5Util.getMD5(Constats.S_KEY + teamid);
            Map<String, String> params = new HashMap<String, String>();
            params.put("tuanid", teamid);
            //params.put("userid", listToString(mList.toString()));
            params.put("userid", listToString(mList.toString()));
            LogUtil.d(TAG, "====上传的团成员的id=listToString(mList.toString())==:" + listToString(mList.toString()));
            params.put("sign", sign);
            JsonRequest<JSONObject> jsonRequest = VolleyJsonUtil
                    .createJsonObjectRequest(Method.POST, addMemberUrl, params,
                            new Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    try {
                                        int result = response.getInt("result");
                                        LogUtil.d(TAG, "上传的团成员信息成功--result--:" + result);
//                                        mHandler.post(updataToDir);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                }
                            }, new ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    LogUtil.d(TAG, "上传的团成员信息失败");
//                                    mHandler.post(updataToDir);
                                }
                            });
            if (null != mRequestQueue) {
                mRequestQueue.add(jsonRequest);
            }
        } else {
            LogUtil.d(TAG, "没有新数据,不需上传的团成员信息");
//            mHandler.post(updataToDir);
        }
    }

    private String url = Constats.HTTP_URL + Constats.ADD_TEAM_FUN;

    /**
     * 访问服务器提交数据 创建一个旅游团
     * {"result":0,"msg":"操作成功","item":{"id":"33","name":"大南山",
     * "userid":"41","createtime":1446370012}}
     */
    @SuppressWarnings("unchecked")
    private void createTeam(Map<String, String> params, final String localid) {
        JsonRequest<JSONObject> jsonRequest = VolleyJsonUtil
                .createJsonObjectRequest(Method.POST, url, params,
                        new Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    int result = response.getInt("result");
//									String message = response.getString("msg");
                                    if (0 == result) {
                                        if (response.has("item")) {
                                            JSONObject jsonObject = response
                                                    .getJSONObject("item");
                                            String teamid = jsonObject
                                                    .getString("id");
                                            LogUtil.d("BaiduMapService", "服务器最新团队ID：" + teamid);
                                            sp.edit().putString(UserInfo.LAST_SERVICE_TEAM_ID, teamid).apply();//最新一个旅游团 ID
                                            teamDao.updateTeam(teamid, localid);
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                toActivity(PreTrouTeamMemberActivity.class);
                                finish();
                            }
                        }, new ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                toActivity(PreTrouTeamMemberActivity.class);
                                finish();
                            }
                        });
        if (null != mRequestQueue) {
            mRequestQueue.add(jsonRequest);
        }
    }

    /**
     * 新成员加入PopupWindow提示
     */
    private void showPopupWindow(String message) {

        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        }
        View view = LayoutInflater.from(this).inflate(R.layout.member_add_popupwind, null);
        TextView tv_message = (TextView) view.findViewById(R.id.tv_warn_message2);
        ImageView iv_add_delete = (ImageView) view.findViewById(R.id.iv_add_warn_delete);
        iv_add_delete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mHandler.sendEmptyMessage(DELETE_POPUP_WINDOW);
            }
        });
        tv_message.setText(message);
        popupWindow = new PopupWindow(view, ActionBar.LayoutParams.MATCH_PARENT,
                ActionBar.LayoutParams.WRAP_CONTENT);
        iv_gong.setVisibility(View.VISIBLE);
        if (titleView != null && !TrouTeamMemberManagerActivity.this.isFinishing())
            popupWindow.showAsDropDown(titleView);
        delete_pop_thread = new Thread(runnable);
        delete_pop_thread.start();
        startTime = System.currentTimeMillis();
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

    /**
     * 从服务器获取成员信息，然后同步到本地
     */
    private Runnable updataToDir = new Runnable() {
        @Override
        public void run() {
//            getTrouMemberInfoToServer();
            Map<String, String> mParams = new HashMap<String, String>();
            String trouid = teamid;
            String sign = MD5Util.getMD5(Constats.S_KEY + trouid);
            LogUtil.d(TAG, "===============trouid===============:" + trouid);
            mParams.put("tuanid", trouid);
            mParams.put("sign", sign);
            getTrouMemberListHttp(getMemberUrl, mParams);
        }
    };
    /**
     * 获取团成员列表
     */
    @SuppressWarnings("unchecked")
    private void getTrouMemberListHttp(String url, Map<String, String> params) {
        JsonRequest<JSONObject> jsonRequest = VolleyJsonUtil
                .createJsonObjectRequest(Method.POST, url, params,
                        new Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    int result = response.getInt("result");
                                    //String msg = response.getString("msg");
                                    if (0 == result) {
                                        LogUtil.d(TAG, "=========response.toString()===============" + response.toString());
                                        Gson gson = new Gson();
                                        Response res = gson.fromJson(response.toString(), Response.class);
                                        LogUtil.d(TAG, "=========getTrouMemberListHttp======mList===============" + res.item.toString());
                                        mList = res.item;
                                        LogUtil.d(TAG, "=========mList===============" + mList.toString());
                                        addTeamMemberToDir();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                            }
                        });
        if (null != mRequestQueue) {
            mRequestQueue.add(jsonRequest);
        }
    }

    /**
     * 将服务器上的数据同步到本地
     */
    protected void addTeamMemberToDir() {
        if (TextUtils.isEmpty(localid)) {
            return;
        }
        if (null != mList && mList.size() > 0) {
            int size = mList.size();
            for (int i = 0; i < size; i++) {
                if (TextUtils.isEmpty(mList.get(i).userid)) {
                    continue;
                }
                mList.get(i).localid = localid;//设置团队ID
                LogUtil.d(TAG, "---同步到本地时的-localid--" + localid);
                if (mDao.findIdExist(mList.get(i))) {
                    mDao.updateUser(mList.get(i));
                } else {
                    mDao.insertMember(mList.get(i));
                }
            }
        }
        mHandler.sendEmptyMessage(REFRESH_LISTING);
    }


    @Override
    protected void onResume() {
        super.onResume();
        ll_top_refresh.setVisibility(View.GONE);
        topRefreshInit();
//        mHandler.post(task);
        if (MyApplication.isfristSavaLastTeamID && 0 == num) {
            showAddMemberDialog();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        LogUtil.d(TAG, "onStop");

        isShow = false;
        mHandler.sendEmptyMessage(DELETE_POPUP_WINDOW);
        if (!TextUtils.isEmpty(teamid)) {
            mHandler.post(updataToServer);//本地数据同步服务器
        }
    }

    private static String locaTuanId = "";
    /**
     * 用来保存在线人数的集合
     */
    private Set<String> uid = new HashSet<String>();
    private boolean isfrist = true;

    /**
     * 成员列表刷新以及其单个点击监听
     */
    private void refreshListView() {
        if (null == multicastClient && MyHttpUtil.isWifiConn(this)) {
            try {
                multicastClient = MulticastClient
                        .getInstanceMulticastClient(this);
                isfrist = false;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mList = (ArrayList<TeamMember>) mDao.findMemberByteamid(localid);
        /**
         * 重新创建团队清空集合
         */
//        if (!locaTuanId.equals(sp.getString(UserInfo.LAST_TEAM_ID, ""))) {
//            multicastClient.clearAllSet();
//        }
//        locaTuanId = sp.getString(UserInfo.LAST_TEAM_ID, "");

        if (null != multicastClient && null != mList) {
            if (0 == num) {
                if (MulticastClient.isClear && multicastClient.getUidSet2().size() > 0)
                    uid = multicastClient.getUidSet2();//获取在线人数
                else
                    uid = multicastClient.getUidSet();//获取在线人数

                LogUtil.d(TAG, "mList:" + mList.size());
                LogUtil.d(TAG, "uid:" + uid.size());

                int size = mList.size();
                for (int i = 0; i < size; i++) {
                    if (uid.contains(mList.get(i).userid)) {
                        //不在线转为在线，并更新在线时间
                        if (mList.get(i).online.equals("0")) {
                            mList.get(i).online = "1";
                            mList.get(i).online_change_time = System.currentTimeMillis() + "";
                            //更新数据库
                            mDao.updateOnline(mList.get(i).getUserid(), mList.get(i).online);
                            mDao.updateOnlineTime(mList.get(i).getUserid(), mList.get(i).online_change_time);
                        }
                    } else {
                        //不在线转为在线，并更新不在线时间
                        if (mList.get(i).online.equals("1")) {
                            mList.get(i).online = "0";
                            mList.get(i).online_change_time = System.currentTimeMillis() + "";

                            //更新数据库
                            mDao.updateOnline(mList.get(i).getUserid(), mList.get(i).online);
                            mDao.updateOnlineTime(mList.get(i).getUserid(), mList.get(i).online_change_time);
//                            TeamMember teamMember = mList.get(i);
//                            mList.remove(i);
//                            mList.addFirst(teamMember);
                        }
                    }
                }
                tv_title_title.setText(name + "(" + uid.size() + "/"
                        + mList.size() + ")");
            } else {
                tv_title_title.setText(name + "(" + mList.size() + ")");
            }
            mAdapter.setDate(mList);
            ll_top_refresh.setVisibility(View.GONE);
        }
    }

//    /**
//     * 刷新成员列表
//     */
//    private Runnable isOnline = new Runnable() {
//        @Override
//        public void run() {
//            while (!clearThreadStop) {
//                if (isfrist && MyHttpUtil.isWifiConn(TrouTeamMemberManagerActivity.this)) {
//                    try {
//                        multicastClient = MulticastClient
//                                .getInstanceMulticastClient(TrouTeamMemberManagerActivity.this);
//                        isfrist = false;
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//                if (multicastClient != null) {
//                    uid = multicastClient.getUidSet();//获取在线人数
//                    isClear = true;
//                    uid.clear();
//                    LogUtil.d("uidSet", "清空1：" + uid.size());
//                    try {
//                        emptyThread.sleep(6000);
//                        isClear = false;
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                        LogUtil.d("uidSet", "清空异常：" + uid.size());
//                    }
//                    mHandler.sendEmptyMessage(1);//刷新列表
//                    LogUtil.d("uidSet", "清空2：" + uid.size());
//                }
//                try {
//                    emptyThread.sleep(1000 * 5);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    };
    /**
     * item点击监听
     */
    private OnItemClickListener onItemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int position,
                                long id) {
            show_Call_Dialog(position);
            //    ToastUtil.showToast(context, "" + position, 3000);
        }
    };

    /**
     * 刷新提示dialog
     */
    private void showAddMemberDialog() {
        // 退出登录的对话框
        // 获取布局
        View view = this.getLayoutInflater().inflate(
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
        message.setPadding(getResources().getDimensionPixelSize(R.dimen.dialog_message_padding_left_right), 0, getResources().getDimensionPixelSize(R.dimen.dialog_message_padding_left_right), getResources().getDimensionPixelSize(R.dimen.dialog_message_padding_button));
        ImageView close = (ImageView) view
                .findViewById(R.id.iv_login_dialog_close);

        cancel.setText(getResources().getString(R.string.FragmentSet_cache_cancel));
        message.setText(getResources().getString(R.string.dialog_member_msg_content));//设置类容
        confirm.setText(R.string.FragmentSet_cache_confirm);
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
                MyApplication.isfristSavaLastTeamID = false;
                String last_Team_id = teamDao.selectLastTeamid(MainFragment.MYSELFID);
                if (!TextUtils.isEmpty(last_Team_id)) {
                    sp.edit().putString(UserInfo.LAST_TEAM_ID, last_Team_id).apply();
                    LogUtil.i(TAG, "-----FragmentTrouManger--当前本地团id:" + last_Team_id);
                }
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
     * 拨打电话dialog
     */
    private void show_Call_Dialog(final int position) {
        // 退出登录的对话框

        // 获取布局
        View view = this.getLayoutInflater().inflate(
                R.layout.dialog_login_select, null);

        // 设置dialog样式
        final Dialog dialog = new Dialog(context, R.style.CustomDialog);
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
                String mobileNumber = mList.get(position).mobile;
                if (!"无SIM卡".equals(mobileNumber)) {
                    dialog.dismiss();
                    Intent intent = new Intent(Intent.ACTION_CALL, Uri
                            .parse("tel:" + mobileNumber));
                    TrouTeamMemberManagerActivity.this.startActivity(intent);
                } else {
                    dialog.dismiss();
                    ToastUtil.showToast(TrouTeamMemberManagerActivity.this, R.string.team_no_sim, 2000);

                }
            }
        });
        dialog.show();
    }

    /**
     * 初始化刷新控件的状态
     */
    private void topRefreshInit() {
        mProgressBar.setVisibility(View.VISIBLE);
        mTextView.setText(R.string.loaddata);
    }


    /**
     * 计时器
     */
    class TimeOut extends CountDownTimer {

        public TimeOut(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {// 计时完毕时触发
            // refreshListView();
            mProgressBar.setVisibility(View.GONE);
            mTextView.setText(R.string.loaddata_un);
            // ll_top_refresh.setVisibility(View.GONE);
        }

        @Override
        public void onTick(long arg0) {// 计时过程显示
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
        public ArrayList<TeamMember> item;
    }
}
