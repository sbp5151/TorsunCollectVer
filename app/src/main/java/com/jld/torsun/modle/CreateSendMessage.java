package com.jld.torsun.modle;

import android.text.TextUtils;

import com.jld.torsun.util.LogUtil;

import java.util.ArrayList;

/**
 * 项目名称：Torsun
 * 晶凌达科技有限公司所有，
 * 受到法律的保护，任何公司或个人，未经授权不得擅自拷贝。
 *
 * @creator 单柏平 <br/>
 * @create-time ${date} ${time}
 */
public class CreateSendMessage {

    private String title;//标题
    private String describe = "";//描述
    private String coverPath;//封面图片地址
    private ArrayList<CreateSendMessageItem> list = new ArrayList<>();


    @Override
    public String toString() {
        return "CreateSendMessage{" +
                "title='" + title + '\'' +
                ", describe='" + describe + '\'' +
                ", coverPath='" + coverPath + '\'' +
                ", list=" + list +
                '}';
    }

    public ArrayList<CreateSendMessageItem> getList() {
        return list;
    }

    public void setList(ArrayList<CreateSendMessageItem> list) {
        this.list = list;
    }

    public String getTitle() {
        return title;
    }


    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    public String getCover() {
        return coverPath;
    }

    public void setCover(String coverPath) {
        this.coverPath = coverPath;
    }

    //判断内容是否为空
    public boolean contentIsNull() {

        if (TextUtils.isEmpty(title) && TextUtils.isEmpty(describe) && TextUtils.isEmpty(coverPath) && listNull()) {
            LogUtil.d("return true;2");
            return true;
        }
        LogUtil.d("return false;3");
        return false;
    }

    //如果list集合里面有一个item不为空则返回false
    private boolean listNull() {
        for (CreateSendMessageItem item : list) {
            if (!item.contentIsNull()) {
                LogUtil.d("return false;1");
                return false;
            }
        }
        return true;
    }

}
