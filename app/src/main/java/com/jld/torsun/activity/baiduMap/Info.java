package com.jld.torsun.activity.baiduMap;

import android.os.Parcel;
import android.os.Parcelable;

import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.model.LatLng;
import com.jld.torsun.util.LogUtil;

import clusterutil.clustering.ClusterItem;

/**
 * Created by boping on 2016/2/27.
 */
public class Info implements Parcelable, Comparable, ClusterItem {

    public String isLoad;
    public String nick;
    public String icon;
    public String phone;
    public double distance;
    public LatLng latLng;
    Boolean isCluster;
    public String timeout;

    public Info(Boolean isCluster) {
        this.isCluster = isCluster;
    }


    protected Info(Parcel in) {
        isLoad = in.readString();
        nick = in.readString();
        icon = in.readString();
        phone = in.readString();
        timeout = in.readString();
        distance = in.readDouble();
        latLng = in.readParcelable(LatLng.class.getClassLoader());
    }


    public static final Creator<Info> CREATOR = new Creator<Info>() {
        @Override
        public Info createFromParcel(Parcel in) {
            return new Info(in);
        }

        @Override
        public Info[] newArray(int size) {
            return new Info[size];
        }
    };

    public String getIsLoad() {
        return isLoad;
    }


    public Boolean getIsCluster() {

        return isCluster;
    }

    public void setIsCluster(Boolean isCluster) {
        this.isCluster = isCluster;
    }

    public Info(String nick, String icon, String phone, String isLoad, Boolean isCluster, String timeout) {
        this.nick = nick;
        this.icon = icon;
        this.latLng = latLng;
        this.isCluster = isCluster;
        this.phone = phone;
        this.isLoad = isLoad;
        this.timeout = timeout;
    }

    public String getNick() {
        return nick;
    }

    public String getPhone() {
        return phone;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getIcon() {
        return icon;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setTimeout(String timeout) {
        this.timeout = timeout;
    }

    @Override
    public int compareTo(Object another) {
        Info mInfo = (Info) another;
        LogUtil.d("MapLocation", mInfo.getNick() + "return 0" + this.getNick());

        if (mInfo.getPhone() != null && this.getPhone() != null && mInfo.getPhone().equals(this.getPhone())) {
            LogUtil.d("MapLocation", "return 0");
            return 0;
        } else if (getIsLoad().equals("1"))//导游在最下面
            return 1;
        else if (mInfo.getIsLoad().equals("1"))
            return -1;
        else if (("0".equals(mInfo.getTimeout()) && "0".equals(this.getTimeout())) || "1".equals(mInfo.getTimeout()) && "1".equals(this.getTimeout())) {
            return distance > mInfo.getDistance() ? 1 : -1;//距离由近到远
        } else {
            return "0".equals(mInfo.getTimeout()) ? 1 : -1;//不在线在上面
        }
    }


    public double getDistance() {
        return distance;
    }

    public String getTimeout() {
        return timeout;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(isLoad);
        dest.writeString(nick);
        dest.writeString(icon);
        dest.writeString(phone);
        dest.writeString(timeout);
        dest.writeDouble(distance);
        dest.writeParcelable(latLng, flags);

    }

    @Override
    public String toString() {
        return "Info{" +
                "isLoad='" + isLoad + '\'' +
                ", nick='" + nick + '\'' +
                ", icon='" + icon + '\'' +
                ", phone='" + phone + '\'' +
                ", distance=" + distance +
                ", latLng=" + latLng +
                ", isCluster=" + isCluster +
                ", timeout='" + timeout + '\'' +
                '}';
    }

    public void setIsLoad(String isLoad) {
        this.isLoad = isLoad;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    @Override
    public LatLng getPosition() {
        return null;
    }

    @Override
    public BitmapDescriptor getBitmapDescriptor() {
        return null;
    }

    @Override
    public BitmapDescriptor getDefaultBitmapDescriptor() {
        return null;
    }

    @Override
    public Info getMarkerItem() {
        return null;
    }
}
