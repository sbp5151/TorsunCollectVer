package com.jld.torsun.activity.messageCenter;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.jld.torsun.R;
import com.jld.torsun.activity.BaseActivity;
import com.jld.torsun.activity.MainFragment;
import com.jld.torsun.activity.countryCode.GroupListView;
import com.jld.torsun.adapter.SingleChoiceAdapter;
import com.jld.torsun.db.TeamDao;
import com.jld.torsun.modle.TrouTeam;
import com.jld.torsun.util.LogUtil;
import android.view.View.OnClickListener;

import java.util.Collections;
import java.util.List;

public class SingleChoiceActivity extends BaseActivity {

    private static final String TAG = "SingleChoiceActivity";

    private TeamDao teamDao;
    private List<TrouTeam> mList;

    private View topBack;
    private TextView topTitle;
    private TextView topSure;
    private ListView mListView;
    private SingleChoiceAdapter singleChoiceAdapter;

    private String tuanId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_choice);
        teamDao = TeamDao.getInstance(this);
        mList = teamDao.getTrouTeamName(MainFragment.MYSELFID);
        LogUtil.d(TAG, "mList : " + mList.toString());
        initView();
    }

    private void initView() {
        //init top view.
        View top = findViewById(R.id.single_choice_title);
        topBack = top.findViewById(R.id.iv_title_message_center_back);
        topTitle =(TextView) top.findViewById(R.id.tv_title_message_center_title);
        topSure = (TextView) top.findViewById(R.id.tv_title_message_center_release);

        topBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        topSure.setText(R.string.sure);
        topSure.setOnClickListener(sureListen);
        topSure.setVisibility(View.GONE);
        topTitle.setText(R.string.single_choice_title);

        mListView = (ListView)findViewById(R.id.single_choice_list);
        singleChoiceAdapter = new SingleChoiceAdapter(this,mList);
        mListView.setAdapter(singleChoiceAdapter);
        mListView.setItemChecked(0, true);
        tuanId = mList.get(0).id;
        setFlingView(mListView);
        mListView.setOnItemClickListener(onItemClickListener);
    }

    private OnClickListener sureListen = new OnClickListener() {
        @Override
        public void onClick(View v) {
            final int position = mListView.getCheckedItemPosition();
            tuanId = mList.get(position).id;
            LogUtil.d(TAG,"tuanId : "+tuanId);
            toCreateMSG();
        }
    };

   private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
       @Override
       public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
           tuanId = mList.get(position).id;
           LogUtil.d(TAG,"tuanId : "+tuanId);
           toCreateMSG();
       }
   };

    private void toCreateMSG() {
        LogUtil.d(TAG,"toCreateMSG  tuanId : "+tuanId);
        Intent intent = new Intent(this,CreateNewMessageActivity.class);
        intent.putExtra("tuanId",tuanId);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
    }
}
