package com.jld.torsun.activity.GoogleMap;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.google.android.gms.maps.model.LatLng;
import com.jld.torsun.R;
import com.jld.torsun.util.LogUtil;
import com.jld.torsun.util.distanceUtil;
import com.jld.torsun.view.RoundImageViewByXfermode;

import java.util.ArrayList;

/**
 * Created by boping on 2016/3/12.
 */
public class Google_Map_List_Adapter extends BaseAdapter {

    public ArrayList<MyItem> list;
    public Context context;
    public LatLng latLng;//自己的经纬度
    public ImageLoader imageLoader;
    public Boolean isLoad = false;

    public Google_Map_List_Adapter(ArrayList<MyItem> list, Context context, LatLng latLng, ImageLoader imageLoader, Boolean isLoad) {

        this.list = list;
        this.context = context;
        this.latLng = latLng;
        this.imageLoader = imageLoader;
        this.isLoad = isLoad;
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
        LogUtil.d("GoogleMain", "list.get(position)" + list.get(position).getNick());
        LogUtil.d("GoogleMain", "list.get(position)" + list.get(position).getIsLoad());

        if (list.get(position).getTimeout().equals("0")) {//不在线标记
            holder.iv_mark.setImageResource(R.mipmap.unonline);
            holder.iv_mark.setVisibility(View.VISIBLE);
        } else if ("1".equals(list.get(position).getIsLoad())) {//导游标记
            holder.iv_mark.setImageResource(R.mipmap.daoyou_mark);
            holder.iv_mark.setVisibility(View.VISIBLE);
            holder.call.setVisibility(View.GONE);
        } else {
            holder.iv_mark.setVisibility(View.INVISIBLE);
        }
        if (null != list.get(position).getmPosition() && !"1".equals(list.get(position).getIsLoad()))
            holder.distance.setText(distanceUtil.getDistance_google(latLng, list.get(position).getmPosition()) + "m");
        holder.icon.setDefaultImageResId(R.mipmap.default_hear_ico);//设置默认图片
        if (!TextUtils.isEmpty(list.get(position).getIcon())) {
            holder.icon.setErrorImageResId(R.mipmap.default_hear_ico);
            holder.icon.setImageUrl(list.get(position).getIcon(), imageLoader);
        } else {
            holder.icon.setImageResource(R.mipmap.default_hear_ico);
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
