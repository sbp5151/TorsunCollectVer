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
 * Created by boping on 2016/3/12.
 */
public class Map_List_Adapter extends BaseAdapter {

    public ArrayList<Info> list;
    public Context context;
    public LatLng latLng;//自己的经纬度
    public ImageLoader imageLoader;
    public Boolean isLoad = false;

    public Map_List_Adapter(ArrayList<Info> list, Context context, LatLng latLng, ImageLoader imageLoader, Boolean isLoad) {

        this.list = list;
        this.context = context;
        this.latLng = latLng;
        this.imageLoader = imageLoader;
        this.isLoad = isLoad;

        LogUtil.d("Map_List_Adapter", list.toString());
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
            convertView = LayoutInflater.from(context).inflate(R.layout.map_list_item, null);
            holder.distance = (TextView) convertView.findViewById(R.id.tv_map_list_item_distance);
            holder.nick = (TextView) convertView.findViewById(R.id.tv_map_list_item_nick);
            holder.icon = (RoundImageViewByXfermode) convertView.findViewById(R.id.iv_map_list_item_icon);
            holder.iv_mark = (ImageView) convertView.findViewById(R.id.iv_map_list_item_mark);
            holder.call = (TextView) convertView.findViewById(R.id.tv_map_list_item_call);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (isLoad) {//呼叫功能
            holder.call.setVisibility(View.VISIBLE);
        }
        LogUtil.d("Map_List_Adapter", "getNick:" + list.get(position).getNick());
        LogUtil.d("Map_List_Adapter", "getTimeout:" + list.get(position).getTimeout());
        LogUtil.d("Map_List_Adapter", "list.get(position).getIsLoad():" + list.get(position).getIsLoad());
        if (list.get(position).getTimeout().equals("0")) {//不在线标记
            holder.iv_mark.setImageResource(R.mipmap.unonline);
            holder.iv_mark.setVisibility(View.VISIBLE);
        } else if ("1".equals(list.get(position).getIsLoad())) {//导游标记
            holder.iv_mark.setImageResource(R.mipmap.daoyou_mark);
            holder.iv_mark.setVisibility(View.VISIBLE);
            holder.distance.setVisibility(View.INVISIBLE);
            holder.call.setVisibility(View.INVISIBLE);
        } else {
            holder.iv_mark.setVisibility(View.INVISIBLE);
        }
        if (null != list.get(position).getLatLng() && !"1".equals(list.get(position).getIsLoad()))
            holder.distance.setText(distanceUtil.getDistance(latLng, list.get(position).getLatLng()) + "m");
        holder.icon.setDefaultImageResId(R.mipmap.default_hear_ico_re);//设置默认图片
        if (!TextUtils.isEmpty(list.get(position).getIcon())) {
            holder.icon.setErrorImageResId(R.mipmap.default_hear_ico_re);
            holder.icon.setImageUrl(list.get(position).getIcon(), imageLoader);
        } else {
            holder.icon.setImageResource(R.mipmap.default_hear_ico_re);
        }
        LogUtil.d("position", "list_list.get(position).getIsLoad():" + list.get(position).getIsLoad() + "---nick:" + list.get(position).getNick());

        holder.nick.setText(list.get(position).getNick());
        return convertView;
    }
    public class ViewHolder {
        public TextView distance;
        public TextView nick;
        public RoundImageViewByXfermode icon;
        public TextView call;
        public ImageView iv_mark;
    }
}
