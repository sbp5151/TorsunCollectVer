package com.jld.torsun.activity.messageCenter;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonRequest;
import com.google.gson.Gson;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.jld.torsun.MyApplication;
import com.jld.torsun.R;
import com.jld.torsun.activity.BaseActivity;
import com.jld.torsun.http.VolleyJsonUtil;
import com.jld.torsun.modle.CreateReceiveMessageItem;
import com.jld.torsun.util.Constats;
import com.jld.torsun.util.LogUtil;
import com.jld.torsun.util.MD5Util;
import com.jld.torsun.util.TimeUtil;
import com.jld.torsun.util.ToastUtil;
import com.jld.torsun.util.imagecache.MyImageLoader;
import com.jld.torsun.view.RoundImageViewByXfermode;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 显示导游推送信息的二级界面
 */
public class GuiderSecondMessageActivity extends BaseActivity {

    private static final String TAG = "GuiderSecondMessageActivity";

    private View topView;
    private View headView;
    private View footView;
    private ImageButton topBackIV;
    private TextView topContentTV;
    private TextView topRightTV;

    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;

    /*********************
     * headview部分
     ***************************/
    private RoundImageViewByXfermode guiderHeadIcon;
    private TextView messageTitle;
    private TextView guiderName;
    private TextView browseNum;
    private TextView below;
    private String imgUrl = "http://img.torsun.cn/2016-03/1458125192_106610.png";
    /***********************************************************/

    private TextView messageTime;

    private PullToRefreshListView PTRL;
    private ListView listView;
    private List<CreateReceiveMessageItem> mList = new ArrayList<>();
    private GuiderSecondMessageListAdapter adapter;
    private String userId;
    private String newsId;
    private String guideIcon;
    private String teamName;
    private String newsTime;
    private String title;
    private String image;
    private String guideName;
    private String sign;
    private final String save_sp_key = "save_GuiderSecondMessageActivity_content";

    private final String requestReadUrl = Constats.HTTP_URL + Constats.MESSAGE_GET_TUAN_READ_URL;
    public final String LoadreUrl = Constats.HTTP_URL + Constats.MESSAGE_GET_TUAN_ITEM_URL;
    private MyResponse myResponse;
    public static final int SERVICE_RETURN = 1;
    public static final int DIALOG_STOP = 2;

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int what = msg.what;
            switch (what) {
                case SERVICE_RETURN:
                    setData();
                    break;
                case DIALOG_STOP:
//                    if (dialog.isShowing())
//                        dialog.dismiss();
                    break;
            }
        }
    };
    private SharedPreferences sp;

    private void setData() {
        for (CreateReceiveMessageItem cmi : myResponse.getMsgs())
            LogUtil.d(TAG, "newsId：" + cmi.toString());

        browseNum.setText(myResponse.getBrowses());
        below.setText(myResponse.getDesc());
        adapter.setData(myResponse.getMsgs());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guider_second_message);
        mRequestQueue = ((MyApplication) getApplication()).getRequestQueue();
        mImageLoader = MyImageLoader.getInstance(this);
//        dialog = DialogUtil.createLoadingDialog(this, getResources().getString(R.string.team_member_pro_text));
//        dialog.setCanceledOnTouchOutside(false);
//        dialog.show();
        sp = getSharedPreferences(Constats.SHARE_KEY, MODE_PRIVATE);
        initData();
        initView();
        localUpdateView();
        new Thread(uploadRun).start();
        new Thread(readRun).start();
    }

    private void initData() {
        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");
        newsId = intent.getStringExtra("newsId");
        guideName = intent.getStringExtra("guideName");
        guideIcon = intent.getStringExtra("guideIcon");
        teamName = intent.getStringExtra("teamName");
        newsTime = intent.getStringExtra("newsTime");
        title = intent.getStringExtra("title");
        image = intent.getStringExtra("image");
        LogUtil.d(TAG, "image:" + image + "userId:" + userId + "--newsId:" + newsId + "--guideName:" + guideName + "--guideIcon:" + "--teamName:" + teamName + "--newsTime:" + newsTime + "--title:" + title);
        sign = MD5Util.getMD5(Constats.S_KEY + userId + newsId);
    }

    private void initView() {
        //初始化headview
        headView = LayoutInflater.from(this).inflate(R.layout.item_guider_second_message_head_layout, null);
        initHeadView();
        footView = LayoutInflater.from(this).inflate(R.layout.item_guider_second_message_foot_layout, null);
        initFootView();
        topView = findViewById(R.id.guider_second_message_top_view);
        topBackIV = (ImageButton) topView.findViewById(R.id.iv_title_message_center_back);
        topBackIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        topContentTV = (TextView) topView.findViewById(R.id.tv_title_message_center_title);
        topContentTV.setTextColor(getResources().getColor(R.color.backgroud_red));
        topContentTV.setText(teamName);

        topRightTV = (TextView) topView.findViewById(R.id.tv_title_message_center_release);
        topRightTV.setVisibility(View.GONE);

        //初始化listview
        listView = (ListView) findViewById(R.id.guider_second_message_list_view);
        listView.addHeaderView(headView, null, false);
        listView.addFooterView(footView, null, false);
        adapter = new GuiderSecondMessageListAdapter(this, mList);
        listView.setAdapter(adapter);
        //设置滑动手势view
        setFlingView(listView);
    }

    private void localUpdateView() {
        String content = "";
        if (!TextUtils.isEmpty(newsId) && !TextUtils.isEmpty(userId)) {
            LogUtil.d(TAG, "本地数据newsId：" + newsId);
            content = sp.getString(userId + newsId + save_sp_key, "");
        }
        if (!TextUtils.isEmpty(content)) {
            LogUtil.d(TAG, "展示本地数据newsId：" + newsId);

            myResponse = new Gson().fromJson(content.toString(), MyResponse.class);
            LogUtil.d(TAG, "myResponse：" + myResponse);
            if (myResponse != null)
                mHandler.sendEmptyMessage(SERVICE_RETURN);
        }
    }

    private void initFootView() {
        messageTime = (TextView) footView.findViewById(R.id.tv_item_guider_second_message_foot_time);
        messageTime.setText(TimeUtil.timeFormatOther(newsTime));
    }

    private void initHeadView() {
        guiderHeadIcon = (RoundImageViewByXfermode) headView.findViewById(R.id.rivbxf_item_guider_second_message_head_icon);
        guiderHeadIcon.setDefaultImageResId(R.mipmap.ic_launcher);
        guiderHeadIcon.setErrorImageResId(R.mipmap.ic_launcher);
        guiderHeadIcon.setImageUrl("", mImageLoader);

        messageTitle = (TextView) headView.findViewById(R.id.tv_item_guider_second_message_head_title);
        messageTitle.setText(title);
        guiderName = (TextView) headView.findViewById(R.id.tv_item_guider_second_message_head_name);
        guiderName.setText(guideName);
        browseNum = (TextView) headView.findViewById(R.id.tv_item_guider_second_message_head_browser_num);
        below = (TextView) headView.findViewById(R.id.tv_item_guider_second_message_below);
        guiderHeadIcon.setDefaultImageResId(R.mipmap.default_hear_ico);
        guiderHeadIcon.setErrorImageResId(R.mipmap.default_hear_ico);
        guiderHeadIcon.setImageUrl(guideIcon, mImageLoader);
    }

    /**
     * 获取数据
     */
    Runnable uploadRun = new Runnable() {
        @Override
        public void run() {
            HashMap<String, String> params = new HashMap<>();
            params.put("id", newsId);
            params.put("userid", userId);
            params.put("sign", sign);
            LogUtil.d(TAG, "newsId：" + newsId + "--userid" + userId + "--" + LoadreUrl + "--sign:" + sign);
            JsonRequest jsonRequest = VolleyJsonUtil.createJsonObjectRequest(Request.Method.POST, LoadreUrl, params, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject jsonObject) {
                    LogUtil.d(TAG, "jsonObject：" + jsonObject);
                    try {
                        int result = jsonObject.getInt("result");
                        LogUtil.d(TAG, "result：" + result);

                        if (result == 0) {

                            if (!TextUtils.isEmpty(newsId) && !TextUtils.isEmpty(userId)) {
                                LogUtil.d(TAG, "缓存本地数据newsId：" + newsId);
                                //保存页面数据
                                sp.edit().putString(userId + newsId + save_sp_key, jsonObject.toString()).apply();
                            }

                            myResponse = new Gson().fromJson(jsonObject.toString(), MyResponse.class);
                            LogUtil.d(TAG, "myResponse：" + myResponse);

                            if (myResponse != null)
                                mHandler.sendEmptyMessage(SERVICE_RETURN);
                        } else {
                            ToastUtil.showToast(GuiderSecondMessageActivity.this, getResources().getString(R.string.to_server_failed), 4000);
                        }
                    } catch (JSONException e) {
                        LogUtil.d(TAG, "e：" + e.toString());
                        e.printStackTrace();
                    }
                    mHandler.sendEmptyMessage(DIALOG_STOP);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    ToastUtil.showToast(GuiderSecondMessageActivity.this, getResources().getString(R.string.t_frag_set_network_err), 4000);
                    mHandler.sendEmptyMessage(DIALOG_STOP);
                }
            });
            LogUtil.d(TAG, "mRequestQueue：" + mRequestQueue);
            if (null != mRequestQueue) {
                mRequestQueue.add(jsonRequest);
            }
        }
    };
    /**
     * 朕已阅
     */
    Runnable readRun = new Runnable() {
        @Override
        public void run() {
            HashMap<String, String> params = new HashMap<>();
            params.put("userid", userId);
            params.put("nid", newsId);
            params.put("sign", MD5Util.getMD5(Constats.S_KEY + userId + newsId));
            LogUtil.d(TAG, "userId：" + userId + "--newsId" + newsId + "--" + requestReadUrl);
            JsonRequest jsonRequest = VolleyJsonUtil.createJsonObjectRequest(Request.Method.POST, requestReadUrl, params, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject jsonObject) {
                    try {
                        int result = jsonObject.getInt("result");
                        LogUtil.d(TAG, "jsonObject：" + jsonObject);
                        if (result == 0) {
                        } else {
                            ToastUtil.showToast(GuiderSecondMessageActivity.this, getResources().getString(R.string.to_server_failed), 4000);
                        }
                    } catch (JSONException e) {
                        LogUtil.d(TAG, "e：" + e.toString());
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    ToastUtil.showToast(GuiderSecondMessageActivity.this, getResources().getString(R.string.t_frag_set_network_err), 4000);
                }
            });
            LogUtil.d(TAG, "mRequestQueue：" + mRequestQueue);
            if (null != mRequestQueue) {
                mRequestQueue.add(jsonRequest);
            }
        }
    };

    public class MyResponse {
        public String browses;
        public String desc;
        public ArrayList<CreateReceiveMessageItem> msgs;

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public ArrayList<CreateReceiveMessageItem> getMsgs() {
            return msgs;
        }

        public void setMsgs(ArrayList<CreateReceiveMessageItem> msgs) {
            this.msgs = msgs;
        }

        public String getBrowses() {

            return browses;
        }

        public void setBrowses(String browses) {
            this.browses = browses;
        }

        @Override
        public String toString() {
            return "MyResponse{" +
                    "browses='" + browses + '\'' +
                    ", desc='" + desc + '\'' +
                    ", msgs=" + msgs +
                    '}';
        }
    }
}
