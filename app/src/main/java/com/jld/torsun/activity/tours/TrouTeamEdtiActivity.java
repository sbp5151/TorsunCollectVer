package com.jld.torsun.activity.tours;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonRequest;
import com.jld.torsun.ActivityManageFinish;
import com.jld.torsun.MyApplication;
import com.jld.torsun.R;
import com.jld.torsun.activity.BaseActivity;
import com.jld.torsun.activity.MainFragment;
import com.jld.torsun.db.TeamDao;
import com.jld.torsun.http.VolleyJsonUtil;
import com.jld.torsun.modle.TrouTeam;
import com.jld.torsun.util.AndroidUtil;
import com.jld.torsun.util.Constats;
import com.jld.torsun.util.DensityUtil;
import com.jld.torsun.util.DialogUtil;
import com.jld.torsun.util.LogUtil;
import com.jld.torsun.util.MD5Util;
import com.jld.torsun.util.UserInfo;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * @author liuzhi
 * @ClassName: TrouTeamEdtiActivity
 * @Description: 新建旅游团
 * @date 2015-12-2 上午9:52:40
 */
public class TrouTeamEdtiActivity extends BaseActivity implements
        OnClickListener {
    private static final String TAG = "TrouTeamEdtiActivity";
    private View titleView;
    private EditText edit_trouteam_name;
    /**
     * 确定按钮
     */
    private Button bt_edit_trouteam_sure;
    // private TrouTeam mTrouteam;
    private SharedPreferences sp;
    private RequestQueue mRequestQueue;

    private TeamDao teamDao;
    private Dialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trouteam_edite);
        ActivityManageFinish.addActivity(this);
        teamDao = TeamDao.getInstance(this);
        // mTrouteam = new TrouTeam();
        sp = getSharedPreferences(Constats.SHARE_KEY, Context.MODE_PRIVATE);
        // mRequestQueue = Volley.newRequestQueue(this);
        MyApplication ma = (MyApplication) getApplication();
        mRequestQueue = ma.getRequestQueue();
        initView();
    }

    private boolean isfristFocus = true;

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (isfristFocus && hasFocus) {
            isfristFocus = false;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                float height = AndroidUtil.getStatusHeight(this);
                LogUtil.d(TAG,"--------------状态栏的高度为:" + height);

                int viewHeight = DensityUtil.px2dip(this, (216f - height * 2));
                LogUtil.d(TAG,"--------------viewHeight的高度:" + viewHeight);
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) titleView.getLayoutParams();
                params.height = viewHeight;
                titleView.setLayoutParams(params);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isfristFocus = true;
        ActivityManageFinish.removeActivity(this);
    }

    private void initView() {
        titleView = findViewById(R.id.title_trouteam_edit);
        ImageView image_title_back = (ImageView) titleView.findViewById(R.id.title_image_title_back);
        image_title_back.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                finish();
            }
        });
        TextView tv_title_title = (TextView) titleView.findViewById(R.id.tv_title_title);
        tv_title_title.setText(R.string.create_trouteam);
        TextView tv_title_sure = (TextView) titleView.findViewById(R.id.tv_title_sure);
        tv_title_sure.setVisibility(View.INVISIBLE);
        edit_trouteam_name = (EditText) findViewById(R.id.edit_trouteam_name);
        bt_edit_trouteam_sure = (Button) findViewById(R.id.bt_edit_trouteam_sure);
        bt_edit_trouteam_sure.setEnabled(true);
        bt_edit_trouteam_sure.setOnClickListener(this);
        LinearLayout viewById = (LinearLayout) findViewById(R.id.ll_create_team);
        setFlingView(viewById);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.bt_edit_trouteam_sure:
                creatTrouteam();
                break;
        }
    }

    /**
     * 创建一个旅游团
     */
    private void creatTrouteam() {
        bt_edit_trouteam_sure.setEnabled(false);
        String name = edit_trouteam_name.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            // 当团队名输入为空时,就采用默认团队名字
            name = this.getResources().getString(
                    R.string.default_tourist_team_name);
        }
        // 保存在本地数据库
        teamDao.insertTeam(MainFragment.MYSELFID, name);
        String newId = teamDao.selectLastTeamid(MainFragment.MYSELFID);
        sp.edit().putString(UserInfo.LAST_TEAM_ID, newId).apply();
		if (MyApplication.isfristSavaLastTeamID){
			MyApplication.isfristSavaLastTeamID = false;
		}
        LogUtil.i(TAG, "---------newId-------" + newId);
        // 提交服务器
        TrouTeam team = teamDao.selectInfoByLocaltid(newId);
        if (TextUtils.isEmpty(team.id)) {
            // String userid = sp.getString(UserInfo.USER_ID,"");
            // String mobile = sp.getString(UserInfo.LOGIN_ACCOUT, "");
            String sign = MD5Util
                    .getMD5(Constats.S_KEY + MainFragment.MYSELFID);
            Map<String, String> params = new HashMap<String, String>();
            params.put("userid", MainFragment.MYSELFID);
            params.put("localid", team.createtime);
            params.put("name", team.name);
            params.put("time", team.createtime);
            params.put("sign", sign);
            LogUtil.i(TAG, "---localid--createtime-----" + team.createtime);
            // mTrouteam.name = name;
            createTeam(params, newId);
            loadingDialog = DialogUtil.createLoadingDialog(this, getResources().getString(R.string.team_member_pro_text));
            loadingDialog.show();
        } else {
            toActivity(PreTrouTeamMemberActivity.class);
            finish();
        }
    }

    /**
     * 创建旅游团URL
     */
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
                                    LogUtil.d(TAG, "response ：" + response.toString());
                                    int result = response.getInt("result");
//									String message = response.getString("msg");
                                    if (0 == result) {
                                        if (response.has("item")) {
                                            JSONObject jsonObject = response
                                                    .getJSONObject("item");
                                            String teamid = jsonObject
                                                    .getString("id");
                                            LogUtil.d(TAG, "服务器最新团队ID：" + teamid);
                                            sp.edit().putString(UserInfo.LAST_SERVICE_TEAM_ID, teamid).apply();//最新一个旅游团 ID
                                            teamDao.updateTeam(teamid, localid);
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                toActivity(PreTrouTeamMemberActivity.class);
                                loadingDialog.dismiss();
                                finish();
                            }
                        }, new ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                toActivity(PreTrouTeamMemberActivity.class);
                                loadingDialog.dismiss();
                                finish();
                            }
                        });
        if (null != mRequestQueue) {
            mRequestQueue.add(jsonRequest);
        }
    }
}
