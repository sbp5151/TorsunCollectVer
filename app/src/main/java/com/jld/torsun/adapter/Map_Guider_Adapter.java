package com.jld.torsun.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.baidu.mapapi.model.LatLng;
import com.jld.torsun.R;
import com.jld.torsun.activity.baiduMap.Info;
import com.jld.torsun.util.LogUtil;
import com.jld.torsun.util.distanceUtil;
import com.jld.torsun.view.RoundImageViewByXfermode;

import java.util.ArrayList;

/**
 * Created by boping on 2016/2/27.
 */
public class Map_Guider_Adapter extends BaseAdapter {

    public ArrayList<Info> list;
    public Context context;
    public LatLng latLng;
    public ImageLoader imageLoader;

    public Map_Guider_Adapter(ArrayList<Info> list, Context context, LatLng latLng, ImageLoader imageLoader ) {
        this.list = list;
        this.context = context;
        this.latLng = latLng;
        this.imageLoader = imageLoader;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.map_gridview_item, null);
            holder.distance = (TextView) convertView.findViewById(R.id.tv_map_guider_item_distance);
            holder.name = (TextView) convertView.findViewById(R.id.tv_map_guider_item_name);
            holder.icon = (RoundImageViewByXfermode) convertView.findViewById(R.id.iv_map_guider_item_icon);
            holder.iv_mark = (ImageView) convertView.findViewById(R.id.iv_map_guider_item_mark);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.distance.setText(distanceUtil.getDistance(latLng, list.get(position).getLatLng()) + "m");
        holder.icon.setDefaultImageResId(R.mipmap.default_hear_ico);
        if (!TextUtils.isEmpty(list.get(position).getIcon())) {
            holder.icon.setErrorImageResId(R.mipmap.default_hear_ico);
            holder.icon.setImageUrl(list.get(position).getIcon(), imageLoader);
        } else {
            holder.icon.setImageResource(R.mipmap.default_hear_ico);
        }
        holder.name.setText(list.get(position).getNick());
        LogUtil.d("position", "list.get(position).getIsLoad():" + list.get(position).getIsLoad()+"---nick:"+list.get(position).getNick());

        if("1".equals(list.get(position).getIsLoad()))
            holder.iv_mark.setVisibility(View.VISIBLE);
        return convertView;
    }

    public class ViewHolder {

        public TextView distance;
        public TextView name;
        public RoundImageViewByXfermode icon;
        public ImageView iv_mark;
    }

    public double getDistance(LatLng start, LatLng end) {
        double lat1 = (Math.PI / 180) * start.latitude;
        double lat2 = (Math.PI / 180) * end.latitude;

        double lon1 = (Math.PI / 180) * start.longitude;
        double lon2 = (Math.PI / 180) * end.longitude;

        //地球半径
        double R = 6371;

        //两点间距离 km，如果想要米的话，结果*1000就可以了
        double d = Math.acos(Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon2 - lon1)) * R;

        return d * 1000;
    }
}
