package com.jld.torsun.activity.GoogleMap;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.google.android.gms.maps.model.LatLng;
import com.jld.torsun.ActivityManageFinish;
import com.jld.torsun.R;
import com.jld.torsun.activity.BaseActivity;
import com.jld.torsun.util.Constats;
import com.jld.torsun.util.LogUtil;
import com.jld.torsun.util.ToastUtil;
import com.jld.torsun.util.UserInfo;
import com.jld.torsun.util.imagecache.MyImageLoader;

import java.util.ArrayList;
import java.util.Collections;

/**
 * 百度地图“距离详情”页面
 */
public class GoogleMapMemberList extends BaseActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private RelativeLayout warn_mode;
    private ImageView iv_gong;
    private ListView listv_trouteam_list;
    private View titleView;
    private TextView tv_title_sure;
    private TextView tv_add_trouteam_prompt;
    private ImageView imagev_add_trouteam;
    private ArrayList<MyItem> markers;
    private double mCurrentLantitude;
    private double mCurrentLongitude;
    private LatLng latLng;
    private ImageLoader imageLoader;
    private SharedPreferences sp;
    private boolean isLoad;
    private int onlineNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trouteam_manager);
        ActivityManageFinish.addActivity(this);
        Intent intent = getIntent();
        if (intent != null) {
            markers = intent.getParcelableArrayListExtra("online_infos");
            mCurrentLantitude = intent.getDoubleExtra("mCurrentLantitude", 0);
            mCurrentLongitude = intent.getDoubleExtra("mCurrentLongitude", 0);
            onlineNum = intent.getIntExtra("onlineNum", 0);
            latLng = new LatLng(mCurrentLantitude, mCurrentLongitude);
            Collections.sort(markers);
        }
        imageLoader = MyImageLoader.getInstance(this);
        sp = getSharedPreferences(Constats.SHARE_KEY, MODE_PRIVATE);
        isLoad = sp.getBoolean(UserInfo.ISLOAD, false);
        initView();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityManageFinish.removeActivity(this);
    }

    private void initView() {

        listv_trouteam_list = (ListView) this
                .findViewById(R.id.lv_trouteam_member_list);
        listv_trouteam_list.setAdapter(new Google_Map_List_Adapter(markers, this, latLng, imageLoader, isLoad));
        listv_trouteam_list.setOnItemClickListener(this);
        titleView = this.findViewById(R.id.title_trouteam_member_manager);

        ImageView image_title_back = (ImageView) titleView.findViewById(R.id.title_image_title_back);
        image_title_back.setOnClickListener(this);
        image_title_back.setImageResource(R.mipmap.close);
        TextView tv_title_title = (TextView) titleView.findViewById(R.id.tv_title_title);
        tv_title_title.setText(getResources().getString(R.string.map_nearby_distance) + "(" + markers.size() + ")");

        tv_title_sure = (TextView) titleView.findViewById(R.id.tv_title_sure);
        tv_title_sure.setVisibility(View.INVISIBLE);

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.title_image_title_back:
                finish();
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        LogUtil.d("onItemClick",markers.get(position).getIsLoad());
        if (markers.get(position).getIsLoad().equals("0")) {
            show_Call_Dialog(position);
        }
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
        final Dialog dialog = new Dialog(this, R.style.CustomDialog);

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

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                String mobileNumber = markers.get(position).getPhone();
                if (!"无SIM卡".equals(mobileNumber)) {
                    dialog.dismiss();
                    Intent intent = new Intent(Intent.ACTION_CALL, Uri
                            .parse("tel:" + mobileNumber));
                    GoogleMapMemberList.this.startActivity(intent);
                } else {
                    dialog.dismiss();
                    ToastUtil.showToast(GoogleMapMemberList.this, R.string.team_no_sim, 2000);

                }
            }
        });
        dialog.show();
    }

}
