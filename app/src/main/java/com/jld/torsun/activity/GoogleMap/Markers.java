package com.jld.torsun.activity.GoogleMap;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;
import com.jld.torsun.util.LogUtil;

/**
 * Created by Sujit on 29/08/15.
 */
public class Markers implements Parcelable, Comparable {
    double latitude;
    double longitude;
    Bitmap bitmap;
    String nick;
    double distance;
    LatLng latLng;
    String isLoad;//是否为导游
    String icon;
    String phone;
    String timeOut;

    protected Markers(Parcel in) {
        latitude = in.readDouble();
        longitude = in.readDouble();
        bitmap = in.readParcelable(Bitmap.class.getClassLoader());
        nick = in.readString();
        distance = in.readDouble();
        latLng = in.readParcelable(LatLng.class.getClassLoader());
        isLoad = in.readString();
        icon = in.readString();
        phone = in.readString();
        timeOut = in.readString();
    }

    public static final Creator<Markers> CREATOR = new Creator<Markers>() {
        @Override
        public Markers createFromParcel(Parcel in) {
            return new Markers(in);
        }

        @Override
        public Markers[] newArray(int size) {
            return new Markers[size];
        }
    };

    public String getLoad() {
        return isLoad;
    }

    public void setLoad(String load) {
        isLoad = load;
    }

    public String getImg() {
        return icon;
    }

    public void setImg(String img) {
        this.icon = img;
    }

    public String getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(String timeOut) {
        this.timeOut = timeOut;
    }

    public Markers(LatLng latLng, String nick,String icon, String isLoad, String phone, String timeOut) {
        this.latLng = latLng;
        this.nick = nick;
        this.isLoad = isLoad;
        this.phone = phone;
        this.timeOut = timeOut;
        this.icon = icon;
    }
    public Markers(String nick, String icon, String phone, String isLoad, LatLng latLng, double distance,String timeOut) {
        this.nick = nick;
        this.icon = icon;
        this.latLng = latLng;
        this.phone = phone;
        this.isLoad = isLoad;
        this.distance = distance;
        this.timeOut = timeOut;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public LatLng getLatLng() {
        if (latLng == null)
            latLng = new LatLng(latitude, longitude);
        return latLng;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public String getIsLoad() {
        return isLoad;
    }

    public void setIsLoad(String load) {
        isLoad = load;
    }


    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public Bitmap getBitmapDescriptor() {
        return bitmap;
    }

    public void setBitmapDescriptor(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public String getName() {
        return nick;
    }

    public void setName(String name) {
        this.nick = name;
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


    @Override
    public int compareTo(Object another) {

        Markers markers = (Markers) another;
        if (markers.getPhone().equals(this.getPhone())) {
            LogUtil.d("MapLocation", "return 0");
            return 0;
        } else if (("0".equals(markers.getTimeOut()) && "0".equals(this.getTimeOut())) || "1".equals(markers.getTimeOut()) && "1".equals(this.getTimeOut())) {
            return distance > markers.getDistance() ? -1 : 1;
        } else {
            return "0".equals(markers.getTimeOut()) ? 1 : -1;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeParcelable(bitmap, flags);
        dest.writeString(nick);
        dest.writeDouble(distance);
        dest.writeParcelable(latLng, flags);
        dest.writeString(isLoad);
        dest.writeString(icon);
        dest.writeString(phone);
        dest.writeString(timeOut);
    }
}

