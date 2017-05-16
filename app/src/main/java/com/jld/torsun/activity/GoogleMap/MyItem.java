package com.jld.torsun.activity.GoogleMap;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.clustering.ClusterItem;
import com.jld.torsun.util.LogUtil;

/**
 * 项目名称：Torsun
 * 晶凌达科技有限公司所有，
 * 受到法律的保护，任何公司或个人，未经授权不得擅自拷贝。
 *
 * @creator 单柏平 <br/>
 * @create-time ${date} ${time}
 */
public class MyItem implements Parcelable, Comparable, ClusterItem {
    private LatLng mPosition;//经纬度
    public String isLoad;//导游标记
    public String nick;//昵称
    public String icon;//头像
    public String phone;//电话
    public double distance;//距离
    Boolean isCluster;//是否是聚合物
    public String timeout;//是否在线
    public Marker marker;//标记

    protected MyItem(Parcel in) {
        mPosition = in.readParcelable(LatLng.class.getClassLoader());
        isLoad = in.readString();
        nick = in.readString();
        icon = in.readString();
        phone = in.readString();
        distance = in.readDouble();
        timeout = in.readString();
    }

    public static final Creator<MyItem> CREATOR = new Creator<MyItem>() {
        @Override
        public MyItem createFromParcel(Parcel in) {
            return new MyItem(in);
        }

        @Override
        public MyItem[] newArray(int size) {
            return new MyItem[size];
        }
    };

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public MyItem(LatLng mPosition) {
        this.mPosition = mPosition;
    }

    public MyItem(LatLng mPosition, String isLoad, String nick, String icon, String phone, double distance, Boolean isCluster, String timeout) {
        this.mPosition = mPosition;
        this.isLoad = isLoad;
        this.nick = nick;
        this.icon = icon;
        this.phone = phone;
        this.distance = distance;
        this.isCluster = isCluster;
        this.timeout = timeout;
    }

    public MyItem(String nick, String icon, String phone, String isLoad, String timeout, Boolean isCluster) {
        this.timeout = timeout;
        this.isLoad = isLoad;
        this.nick = nick;
        this.icon = icon;
        this.phone = phone;
        this.isCluster = isCluster;
    }

    public LatLng getmPosition() {
        return mPosition;
    }

    public void setmPosition(LatLng mPosition) {
        this.mPosition = mPosition;
    }

    public String getIsLoad() {
        return isLoad;
    }

    public void setIsLoad(String isLoad) {
        this.isLoad = isLoad;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public Boolean getIsCluster() {
        return isCluster;
    }

    public void setIsCluster(Boolean isCluster) {
        this.isCluster = isCluster;
    }

    public String getTimeout() {
        return timeout;
    }

    public void setTimeout(String timeout) {
        this.timeout = timeout;
    }


    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    @Override
    public int compareTo(Object another) {
        MyItem mInfo = (MyItem) another;
        LogUtil.d("MapLocation", mInfo.getPhone() + "return 0" + this.getPhone());
        if(getIsLoad().equals("1"))
            //导游最下面
            return 1;
        else if (mInfo.getPhone() != null && this.getPhone() != null && mInfo.getPhone().equals(this.getPhone())) {
            LogUtil.d("MapLocation", "return 0");
            return 0;
            //距离由远到近
        } else if (("0".equals(mInfo.getTimeout()) && "0".equals(this.getTimeout())) || "1".equals(mInfo.getTimeout()) && "1".equals(this.getTimeout())) {
            return distance > mInfo.getDistance() ? 1 : -1;
        } else {
            //不在线在前
            return "0".equals(mInfo.getTimeout()) ? 1 : -1;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mPosition, flags);
        dest.writeString(isLoad);
        dest.writeString(nick);
        dest.writeString(icon);
        dest.writeString(phone);
        dest.writeDouble(distance);
        dest.writeString(timeout);
    }
}
