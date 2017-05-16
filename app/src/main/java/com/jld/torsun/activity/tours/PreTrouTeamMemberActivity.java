package com.jld.torsun.activity.tours;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.jld.torsun.ActivityManageFinish;
import com.jld.torsun.MyApplication;
import com.jld.torsun.R;
import com.jld.torsun.activity.BaseActivity;
import com.jld.torsun.activity.MainFragment;
import com.jld.torsun.adapter.TeamMemberListAdapter;
import com.jld.torsun.db.MemberDao;
import com.jld.torsun.db.TeamDao;
import com.jld.torsun.modle.TeamMember;
import com.jld.torsun.util.AndroidUtil;
import com.jld.torsun.util.DensityUtil;
import com.jld.torsun.util.LogUtil;
import com.lidroid.xutils.bitmap.PauseOnScrollListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;



/**
 * @author liuzhi
 * @ClassName: TrouTeamMemberManagerActivity
 * @Description: 旅游团预览成员显示界面
 * @date 2015-12-2 上午10:40:47
 */
public class PreTrouTeamMemberActivity extends BaseActivity {

//	private static final String TAG = "PreTrouTeamMemberActivity";

    private View titleView;
    private ListView listv_trouteam_member_list;
    private TeamMemberListAdapter mAdapter;
    private TextView tv_title_title;
    private List<TeamMember> mList = new ArrayList<TeamMember>();

    private MemberDao mDao;
    private TeamDao teamDao;

    private MulticastClient multicastClient;

    private TextView mTextView;
    private Context context;

    /**旅游团名称*/
    private String name = "";

    private final String TAG = "PreTrouTeamMemberActivity";
    /**本地ID*/
    private String localid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trouteam_manager);
        ActivityManageFinish.addActivity(this);
//        sp = getSharedPreferences(Constats.SHARE_KEY, Context.MODE_PRIVATE);
        teamDao = TeamDao.getInstance(this);
        mDao = MemberDao.getInstance(this);
        MyApplication ma = (MyApplication) getApplication();

        localid=teamDao.selectLastTeamid(MainFragment.MYSELFID);

        name=teamDao.getTeamNameByLocaltid(localid);
        mList = mDao.findMemberByteamid(localid);

        initView();
        context = PreTrouTeamMemberActivity.this;
        MulticastClient.isFirstReceiver = true;

        try {
             multicastClient = MulticastClient.getInstanceMulticastClient(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        multicastClient.clearAllSet();
        new Thread(task).start();
    }

    private boolean isfristFocus = true;

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (isfristFocus && hasFocus){
            isfristFocus=false;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT){
                float height= AndroidUtil.getStatusHeight(this);
                LogUtil.d("--------------状态栏的高度为:" + height);

                int viewHeight = DensityUtil.px2dip(this, (216f - height * 2));
                LogUtil.d("--------------viewHeight的高度:" + viewHeight);
                LinearLayout.LayoutParams params =(LinearLayout.LayoutParams)titleView.getLayoutParams();
                params.height=viewHeight;
                titleView.setLayoutParams(params);
            }
        }
    }
    private void initView() {
        /**
         * title
         */
        titleView = findViewById(R.id.title_trouteam_member_manager);
        ImageView image_title_back = (ImageView) titleView.findViewById(R.id.title_image_title_back);
        image_title_back.setImageResource(R.mipmap.close);
        tv_title_title = (TextView) titleView.findViewById(R.id.tv_title_title);
        tv_title_title.setText(name);
        TextView tv_title_sure = (TextView) titleView.findViewById(R.id.tv_title_sure);
        tv_title_sure.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                overridePendingTransition(R.anim.page_left_in,R.anim.page_right_out);
                finish();
            }
        });
        tv_title_sure.setText(R.string.sure);
        image_title_back.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                overridePendingTransition(R.anim.page_left_in,R.anim.page_right_out);
                finish();
            }
        });
        /**
         * listView
         */
        listv_trouteam_member_list = (ListView) findViewById(R.id.lv_trouteam_member_list);

        mAdapter = new TeamMemberListAdapter(this, mList, -1);
        listv_trouteam_member_list.setAdapter(mAdapter);
        listv_trouteam_member_list.setOnScrollListener(new PauseOnScrollListener(mAdapter.bitmapUtils,true,true));
    }

    private  boolean isRun=true;

    @Override
    protected void onDestroy() {
        LogUtil.d(TAG,"onDestroy");
        isRun=false;
        super.onDestroy();
        isfristFocus=true;
        ActivityManageFinish.removeActivity(this);
    }

    /**
     * 自动刷新在线人数
     */
    private Runnable task = new Runnable() {
        @Override
        public void run() {
            while (isRun) {
                mList = mDao.findMemberByteamid(localid);
                mHandler.sendEmptyMessage(1);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case 1:
                    refreshListView();//刷新list列表
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 成员列表刷新以及其单个点击监听
     */
    private void refreshListView() {
        mList = mDao.findMemberByteamid(localid);
        //null != multicastClient &&
        if (null != mList && mList.size() > 0) {
            tv_title_title.setText(name + "(" + mList.size() + ")");
            mAdapter.setDate(mList);
        }
    }
}
