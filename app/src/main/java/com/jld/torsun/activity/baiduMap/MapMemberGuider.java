package com.jld.torsun.activity.baiduMap;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.baidu.mapapi.model.LatLng;
import com.jld.torsun.ActivityManageFinish;
import com.jld.torsun.R;
import com.jld.torsun.activity.BaseActivity;
import com.jld.torsun.adapter.Map_Guider_Adapter;
import com.jld.torsun.util.imagecache.MyImageLoader;

import java.util.ArrayList;
import java.util.Collections;

/**
 * 百度地图“附近的人”页面
 */
public class MapMemberGuider extends BaseActivity implements View.OnClickListener {

    private TextView tv_title_message;
    private ImageView iv_title_left;
    private ImageView iv_title_right;
    private ArrayList<Info> infos;
    private ArrayList<Info> infoAll;
    private Double mCurrentLantitude;
    private Double mCurrentLongitude;
    private ImageLoader imageLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_member_guider);
        ActivityManageFinish.addActivity(this);
        Intent intent = getIntent();
        infos = intent.getParcelableArrayListExtra("online_infos");
        infoAll = intent.getParcelableArrayListExtra("infoAll");
        mCurrentLantitude = intent.getDoubleExtra("mCurrentLantitude", 0);
        mCurrentLongitude = intent.getDoubleExtra("mCurrentLongitude", 0);
        imageLoader = MyImageLoader.getInstance(this);
        Collections.sort(infos);
        initView();
    }

    private void initView() {
        RelativeLayout guider_title = (RelativeLayout) this.findViewById(R.id.map_guider_title);
        tv_title_message = (TextView) guider_title.findViewById(R.id.tv_map_nearby_title_message);
        tv_title_message.setText(getResources().getString(R.string.map_nearby_num) + "(" + infos.size() + ")");
        iv_title_left = (ImageView) guider_title.findViewById(R.id.iv_map_nearby_title_left);
        iv_title_left.setOnClickListener(this);
        iv_title_right = (ImageView) guider_title.findViewById(R.id.iv_map_nearby_title_right);
        iv_title_right.setOnClickListener(this);
        GridView gv_context = (GridView) findViewById(R.id.gv_map_member);
        LatLng latLng = new LatLng(mCurrentLantitude, mCurrentLongitude);
        Map_Guider_Adapter adapter = new Map_Guider_Adapter(infos, this, latLng, imageLoader);
        gv_context.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.iv_map_nearby_title_left:
                finish();
                break;
            case R.id.iv_map_nearby_title_right:
                Intent intent = new Intent(MapMemberGuider.this, MapMemberList.class);
                intent.putParcelableArrayListExtra("online_infos", (ArrayList<? extends Parcelable>) infoAll);
                intent.putExtra("mCurrentLantitude", mCurrentLantitude);
                intent.putExtra("mCurrentLongitude", mCurrentLongitude);
                startActivity(intent);
                finish();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityManageFinish.removeActivity(this);
    }
}
