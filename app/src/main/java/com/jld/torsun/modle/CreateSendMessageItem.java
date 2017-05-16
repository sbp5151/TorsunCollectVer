package com.jld.torsun.modle;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import java.util.ArrayList;

/**
 * 项目名称：Torsun
 * 晶凌达科技有限公司所有，
 * 受到法律的保护，任何公司或个人，未经授权不得擅自拷贝。
 *
 * @creator 单柏平 <br/>
 * @create-time ${date} ${time}
 * <p/>
 * 导游创建消息item类
 */
public class CreateSendMessageItem implements Parcelable {

    private String imageDescribe = "";//图片描述
    private ArrayList<String> path = new ArrayList<>();//图片路径

    public CreateSendMessageItem() {
    }

    protected CreateSendMessageItem(Parcel in) {
        imageDescribe = in.readString();
        path = in.createStringArrayList();
    }

    public static final Creator<CreateSendMessageItem> CREATOR = new Creator<CreateSendMessageItem>() {
        @Override
        public CreateSendMessageItem createFromParcel(Parcel in) {
            return new CreateSendMessageItem(in);
        }

        @Override
        public CreateSendMessageItem[] newArray(int size) {
            return new CreateSendMessageItem[size];
        }
    };

    @Override
    public String toString() {
        return "CreateSendMessageItem{" +
                "imageDescribe='" + imageDescribe + '\'' +
                ", path=" + path +
                '}';
    }

    public ArrayList<String> getPath() {
        return path;
    }

    public void setPath(ArrayList<String> path) {
        this.path = path;
    }

    public String getImageDescribe() {
        return imageDescribe;
    }

    public void setImageDescribe(String imageDescribe) {
        this.imageDescribe = imageDescribe;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(imageDescribe);
        dest.writeStringList(path);
    }

    //判断内容是否围殴康
    public boolean contentIsNull() {
        if (TextUtils.isEmpty(imageDescribe) && path.size() < 2)
            return true;
        else return false;
    }
}
