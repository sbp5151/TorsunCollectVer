package com.jld.torsun.activity.messageCenter;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonRequest;
import com.google.gson.Gson;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.jld.torsun.MyApplication;
import com.jld.torsun.R;
import com.jld.torsun.activity.BaseActivity;
import com.jld.torsun.activity.GuiderInfoActivity2;
import com.jld.torsun.http.VolleyJsonUtil;
import com.jld.torsun.modle.TeamMessage;
import com.jld.torsun.modle.TeamMessageItems;
import com.jld.torsun.util.Constats;
import com.jld.torsun.util.ImageUtil;
import com.jld.torsun.util.LogUtil;
import com.jld.torsun.util.MD5Util;
import com.jld.torsun.util.ToastUtil;
import com.jld.torsun.util.imagecache.MyImageLoader;
import com.jld.torsun.view.RoundImageViewByXfermode;
import com.jld.torsun.view.RoundProgressBar;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * 显示导游推送信息的一级界面
 */
public class GuiderMessageActivity extends BaseActivity {

    private static final String TAG = "GuiderMessageActivity";

    private View topView;
    private View headView;
    private ImageButton topBackIV;
    private TextView topContentTV;
    private TextView topRightTV;
    private RoundProgressBar rpb;

    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;

    /*********************
     * headview部分
     ***************************/
    private RoundImageViewByXfermode guiderHeadIcon;
    private TextView guiderName;

    private ImageView iv_good;
    private TextView tv_good_num;
    private ImageView iv_love;
    private TextView tv_love_num;
    private ImageView grade1;
    private ImageView grade2;
    private ImageView grade3;
    private ImageView grade4;
    private ImageView grade5;
    /***********************************************************/

    private ListView listView;
    private PullToRefreshListView PTRL;
    private GuiderMessageListAdapter adapter;
    private LinkedList<TeamMessageItems> mList = new LinkedList<>();
    private SharedPreferences sp;
    private String guidId;
    private String tuanId;
    private String sign;
    private final String requestUrl = Constats.HTTP_URL + Constats.MESSAGE_GET_TUAN_URL;
    private final String requestReadUrl = Constats.HTTP_URL + Constats.MESSAGE_GET_TUAN_READ_URL;
    private String userId;
    private String newsId;
    //    private String newsId;
    private TeamMessage teamMessage;
    private List<ImageView> grades;
    private int requestNum = 1;

    private final String save_sp_key = "save_GuiderMessageActivity_content";

    public static final int SERVICE_RETURN = 1;
    public static final int PTRL_STOP = 2;
    public static final int DIALOG_STOP = 3;
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int what = msg.what;
            switch (what) {
                case SERVICE_RETURN:
                    if (teamMessage != null)
                        setData();
                    break;
                case PTRL_STOP:
                    if (PTRL.isRefreshing())
                        PTRL.onRefreshComplete();
                    break;
                case DIALOG_STOP:
//                    if (dialog != null && dialog.isShowing()) {
//                        dialog.dismiss();
//                    }
                    break;
            }
        }
    };
    private Animation animation;
    private String save_content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guider_message);
        mRequestQueue = ((MyApplication) getApplication()).getRequestQueue();
        mImageLoader = MyImageLoader.getInstance(this);
//        dialog = DialogUtil.createLoadingDialog(this, getResources().getString(R.string.loaddata));
//        dialog.setCanceledOnTouchOutside(false);
//        dialog.show();
        initData();
        initView();
        localUpdateView();
        new Thread(run).start();
    }

    private void initData() {
        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");
        newsId = intent.getStringExtra("newsId");
        guidId = intent.getStringExtra("guideId");
        tuanId = intent.getStringExtra("tuanId");
        LogUtil.d(TAG, "newsId:" + newsId);
        sign = MD5Util.getMD5(Constats.S_KEY + userId + tuanId + guidId);
        sp = getSharedPreferences(Constats.SHARE_KEY, MODE_PRIVATE);
    }

    private void initView() {

        //初始化headview
        headView = LayoutInflater.from(this).inflate(R.layout.item_guider_message_head_layout, null);
        initHeadView();
        //初始化top控件
        topView = findViewById(R.id.guider_message_top_view);
        topBackIV = (ImageButton) topView.findViewById(R.id.iv_title_message_center_back);
        topBackIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        topContentTV = (TextView) topView.findViewById(R.id.tv_title_message_center_title);
        topContentTV.setText(R.string.app_name);
        topRightTV = (TextView) topView.findViewById(R.id.tv_title_message_center_release);
        topRightTV.setVisibility(View.GONE);

        //初始化listview
        PTRL = (PullToRefreshListView) findViewById(R.id.guider_message_list_view);
        PTRL.setMode(PullToRefreshBase.Mode.PULL_UP_TO_REFRESH);//只能上拉加载

        PTRL.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                new Thread(run).start();
            }
        });

        listView = PTRL.getRefreshableView();
        if (!guidId.equals("-1000")) {
            listView.addHeaderView(headView, null, false);
        }
        adapter = new GuiderMessageListAdapter(this, mList, guidId);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(itemClickListener);
        //设置滑动手势view
        setFlingView(listView);
    }

    public void localUpdateView() {

        if (!TextUtils.isEmpty(newsId) && !TextUtils.isEmpty(userId)) {
            LogUtil.d(TAG, "newsId + save_sp_key:" + newsId + save_sp_key);
            save_content = sp.getString(userId + newsId + save_sp_key, "");
        }

        if (!TextUtils.isEmpty(save_content)) {
            LogUtil.d(TAG, "本地数据");

            teamMessage = new Gson().fromJson(save_content.toString(), TeamMessage.class);
            if (teamMessage.getItem() == null || teamMessage.getItem().size() == 0) {
                ToastUtil.showToast(GuiderMessageActivity.this, getResources().getString(R.string.message_pull_hint), 3000);
            } else if (teamMessage != null)
                mHandler.sendEmptyMessage(SERVICE_RETURN);
        }
    }

    public void setData() {

        LogUtil.d(TAG, "teamMessage：" + teamMessage);

        //设置导游头像
        if (!TextUtils.isEmpty(teamMessage.getImg())) {
            guiderHeadIcon.setDefaultImageResId(R.mipmap.default_hear_ico);
            guiderHeadIcon.setErrorImageResId(R.mipmap.default_hear_ico);
            guiderHeadIcon.setImageUrl(teamMessage.getImg(), mImageLoader);
        }
        //设置团名
        if (!TextUtils.isEmpty(teamMessage.getTuanname()))
            topContentTV.setText(teamMessage.getTuanname());
        //设置导游昵称
        if (!TextUtils.isEmpty(teamMessage.getNick()))
            guiderName.setText(getResources().getString(R.string.tour_guide) + teamMessage.getNick());
        //设置点赞数
        if (!TextUtils.isEmpty(teamMessage.getGood()))
            tv_good_num.setText(teamMessage.getGood());
        //设置点心数
        if (!TextUtils.isEmpty(teamMessage.getFlower()))
            tv_love_num.setText(teamMessage.getFlower());
        //设置等级
        if (!TextUtils.isEmpty(teamMessage.getStart())) {
            setGuiderGrade(Integer.parseInt(teamMessage.getStart()));
        }
        //设置是否点过赞
        if ("yes".equals(teamMessage.getCheckflower())) {
            iv_love.setImageResource(R.mipmap.love_on);
            iv_love.setEnabled(false);
        }
        if ("yes".equals(teamMessage.getCheckgood())) {
            iv_good.setImageResource(R.mipmap.good_on);
            iv_good.setEnabled(false);
        }
        //设置列表数据
        if (teamMessage.getItem() != null) {
            adapter.addData(teamMessage.getItem());
            requestNum++;
        }
    }

    /**
     * 通过导游的游客数来判定导游的等级
     *
     * @param grade
     */
    public void setGuiderGrade(int grade) {

        if (grade > 0 && grade < 1200) {
            int on_num = grade / 200;
            int off_num = 5 - on_num;
            for (int i = 0; i < on_num; i++) {
                ImageView imageView = grades.get(i);
                imageView.setImageResource(R.mipmap.grade_primary_on);
            }
            for (int i = on_num; i <= 4; i++) {
                ImageView imageView = grades.get(i);
                imageView.setImageResource(R.mipmap.grade_primary_off);
            }
        } else if (grade >= 1200 && grade < 7200) {
            int on_num = grade / 1200;
            int off_num = 5 - on_num;
            for (int i = 0; i < on_num; i++) {
                ImageView imageView = grades.get(i);
                imageView.setImageResource(R.mipmap.grade_intermediate_on);
            }
            for (int i = on_num; i <= 4; i++) {
                ImageView imageView = grades.get(i);
                imageView.setImageResource(R.mipmap.grade_intermediate_off);
            }

        } else if (grade >= 7200 && grade < 43200) {
            int on_num = grade / 7200;
            int off_num = 5 - on_num;
            for (int i = 0; i < on_num; i++) {
                ImageView imageView = grades.get(i);
                imageView.setImageResource(R.mipmap.grade_advanced_on);
            }
            for (int i = on_num; i <= 4; i++) {
                ImageView imageView = grades.get(i);
                imageView.setImageResource(R.mipmap.grade_advanced_off);
            }
        }
    }

    private void initHeadView() {
        guiderHeadIcon = (RoundImageViewByXfermode) headView.findViewById(R.id.rivbxf_item_guider_message_head_icon);
        guiderName = (TextView) headView.findViewById(R.id.tv_item_guider_message_head_name);
        guiderName.setText("Guide");
        iv_good = (ImageView) headView.findViewById(R.id.iv_item_guider_message_head_good);
        tv_good_num = (TextView) headView.findViewById(R.id.tv_item_guider_message_head_good_num);
        tv_good_num.setText("0");
        iv_love = (ImageView) headView.findViewById(R.id.iv_item_guider_message_head_love);
        tv_love_num = (TextView) headView.findViewById(R.id.tv_item_guider_message_head_love_num);
        tv_love_num.setText("0");
        grade1 = (ImageView) headView.findViewById(R.id.iv_item_guider_message_head_grade_1);
        grade2 = (ImageView) headView.findViewById(R.id.iv_item_guider_message_head_grade_2);
        grade3 = (ImageView) headView.findViewById(R.id.iv_item_guider_message_head_grade_3);
        grade4 = (ImageView) headView.findViewById(R.id.iv_item_guider_message_head_grade_4);
        grade5 = (ImageView) headView.findViewById(R.id.iv_item_guider_message_head_grade_5);

        grades = new ArrayList<>();
        grades.add(grade1);
        grades.add(grade2);
        grades.add(grade3);
        grades.add(grade4);
        grades.add(grade5);

        iv_good.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iv_good.setImageResource(R.mipmap.good_on);
                animation = AnimationUtils.loadAnimation(GuiderMessageActivity.this, R.anim.guide_like_anim);
                iv_good.startAnimation(animation);
                int good_num = Integer.parseInt(tv_good_num.getText().toString());
                tv_good_num.setText((++good_num) + "");
                iv_good.setClickable(false);
                uploadingGood_Love(0 + "");
            }
        });
        iv_love.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iv_love.setImageResource(R.mipmap.love_on);
                animation = AnimationUtils.loadAnimation(GuiderMessageActivity.this, R.anim.guide_like_anim);
                iv_love.startAnimation(animation);
                int love_num = Integer.parseInt(tv_love_num.getText().toString());
                tv_love_num.setText((++love_num) + "");
                iv_love.setClickable(false);
                uploadingGood_Love(1 + "");
            }
        });
        guiderHeadIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                toGuiderInfo();
            }
        });

    }

    private void toGuiderInfo() {
        if (null != teamMessage) {
            guiderHeadIcon.setEnabled(false);
            ImageUtil.togetherRun(guiderHeadIcon);
            guiderHeadIcon.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(GuiderMessageActivity.this, GuiderInfoActivity2.class);
                    intent.putExtra("MSG2That", true);
                    intent.putExtra("guideId", guidId);
                    intent.putExtra("tuanId", tuanId);
                    intent.putExtra("headImageUrl", teamMessage.getImg());
                    intent.putExtra("guideNick", teamMessage.getNick());
                    startActivity(intent);
                    overridePendingTransition(R.anim.fangda_in, R.anim.no_change);
                    guiderHeadIcon.setEnabled(true);
                }
            }, 400);

        }
    }

    private OnItemClickListener itemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            LogUtil.d(TAG, "guidId:" + guidId);
            LogUtil.d(TAG, "position:" + position);
            LogUtil.d(TAG, "adapter.getList():" + adapter.getList().size());
            String type = adapter.getItem(position).getType();

            if ("-1000".equals(guidId)) {//系统消息
                TeamMessageItems item = adapter.getItem(position);
                Intent intent = new Intent(GuiderMessageActivity.this, SystemMessageWebActivity.class);
                //intent.putExtra("url","http://www.torsun.cn/mobile/connect_sysmsg.php");
                intent.putExtra("userId", userId);
                intent.putExtra("newsId", item.getId());
                GuiderMessageActivity.this.startActivity(intent);
            } else if ("0".equals(type)) {//普通图文
                TeamMessageItems item = adapter.getItem(position);
                Intent intent = new Intent(GuiderMessageActivity.this, GuiderSecondMessageActivity.class);
                intent.putExtra("newsId", item.getId());
                intent.putExtra("userId", userId);
                intent.putExtra("guideName", teamMessage.getNick());
                intent.putExtra("guideIcon", teamMessage.getImg());
                intent.putExtra("teamName", teamMessage.getTuanname());
                intent.putExtra("newsTime", item.getTime());
                intent.putExtra("title", item.getTitle());
                intent.putExtra("image", item.getImg());
                GuiderMessageActivity.this.startActivity(intent);
            } else if ("2".equals(type)) {//分享的内容
                TeamMessageItems item = adapter.getItem(position);

                String share_url = item.getDesc();
                String share_title = item.getTitle();
                String share_id = item.getId();
                LogUtil.d(TAG, "share_url:" + share_url);
                if (Patterns.WEB_URL.matcher(share_url).matches()) {
                    LogUtil.d(TAG, "share_url:" + share_url);
                    Intent intent = new Intent(GuiderMessageActivity.this, Share_Web.class);
                    intent.putExtra("url", share_url);
                    intent.putExtra("title", share_title);
                    intent.putExtra("newsId", share_id);
                    GuiderMessageActivity.this.startActivity(intent);
                } else {
                    ToastUtil.showToast(GuiderMessageActivity.this, R.string.url_error, 3000);
                }
            }
        }
    };
    Runnable run = new Runnable() {
        @Override
        public void run() {
            HashMap<String, String> params = new HashMap<>();
            params.put("guideid", guidId);
            params.put("userid", userId);
            params.put("tuanid", tuanId);
            params.put("limits", requestNum + "");
            params.put("sign", sign);
            LogUtil.d(TAG, "tuanId：" + tuanId + "--guidId:" + guidId + "--userId:" + userId + "--sign:" + sign + "--requestNum" + requestNum + "--" + requestUrl);
            JsonRequest jsonRequest = VolleyJsonUtil.createJsonObjectRequest(Request.Method.POST, requestUrl, params, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject jsonObject) {
                    try {
                        int result = jsonObject.getInt("result");
                        LogUtil.d(TAG, "uploadingGood_Love：" + jsonObject);
                        if (result == 0) {
                            if (!TextUtils.isEmpty(newsId) && !TextUtils.isEmpty(userId)) {
                                LogUtil.d(TAG, "缓存本地数据");
                                //保存数据信息
                                sp.edit().putString(userId + newsId + save_sp_key, jsonObject.toString()).apply();
                            }
                            teamMessage = new Gson().fromJson(jsonObject.toString(), TeamMessage.class);
                            if (teamMessage.getItem() == null || teamMessage.getItem().size() == 0) {
                                ToastUtil.showToast(GuiderMessageActivity.this, getResources().getString(R.string.message_pull_hint), 3000);
                            } else if (teamMessage != null)
                                mHandler.sendEmptyMessage(SERVICE_RETURN);
                        } else if (1003 == result) {
                            ToastUtil.showToast(GuiderMessageActivity.this, getResources().getString(R.string.message_pull_hint), 3000);
                        } else {
                            ToastUtil.showToast(GuiderMessageActivity.this, getResources().getString(R.string.to_server_failed), 3000);
                        }
                    } catch (JSONException e) {
                        LogUtil.d(TAG, "e：" + e.toString());
                        e.printStackTrace();
                    }
                    mHandler.sendEmptyMessage(DIALOG_STOP);
                    mHandler.sendEmptyMessage(PTRL_STOP);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    ToastUtil.showToast(GuiderMessageActivity.this, getResources().getString(R.string.t_frag_set_network_err), 4000);
                    mHandler.sendEmptyMessage(PTRL_STOP);
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
     * 上传点赞或点心
     *
     * @param type 0为点赞，1为点心
     */
    private void uploadingGood_Love(String type) {

        String url = Constats.HTTP_URL + Constats.UPLOAD_GOOD_LOVE;
        //String guideId = sp.getString(UserInfo.GUIDER_ID, "");
        //String userId = sp.getString(UserInfo.USER_ID, "");
        String sign = MD5Util.getMD5(Constats.S_KEY + guidId + userId);
        HashMap<String, String> params = new HashMap<>();
        params.put("guideid", guidId);
        params.put("userid", userId);
        params.put("type", type);
        params.put("sign", sign);
        LogUtil.d(TAG, "uploadingGood_Love：url：" + url);
        LogUtil.d(TAG, "params:" + params.toString());
        JsonRequest jsonRequest = VolleyJsonUtil.createJsonObjectRequest(Request.Method.POST, url, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                try {
                    int result = jsonObject.getInt("result");
                    LogUtil.d(TAG, "uploadingGood_Love：" + jsonObject);
                    if (result == 0) {
                        //点赞成功
                    } else {
                        ToastUtil.showToast(GuiderMessageActivity.this, getResources().getString(R.string.to_server_failed), 3000);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                ToastUtil.showToast(GuiderMessageActivity.this, getResources().getString(R.string.t_frag_set_network_err), 3000);
            }
        });
        if (null != mRequestQueue) {
            mRequestQueue.add(jsonRequest);
        }
    }

}
