package com.jld.torsun.modle;

import java.io.Serializable;

/**
 * Created by lz on 2016/3/17.
 */
public class MessageList implements Serializable, Comparable {

    private static final long serialVersionUID = 1L;

    private String id;//消息ID
    private String guideid;//导游ID
    private String tuanid;//团ID
    private String title;//标题
    private String desc;//简介
    private String time;//时间
    private String nums;//未读数
    private String img;//头像链接
    private String tuanname;//团名称
    private boolean isPick;//是否被选中

    public boolean isPick() {
        return isPick;
    }

    public void setPick(boolean pick) {
        isPick = pick;
    }

    public MessageList(String title, String desc, String time, String nums, String img) {
        this.title = title;
        this.desc = desc;
        this.time = time;
        this.nums = nums;
        this.img = img;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGuideid() {
        return guideid;
    }

    public void setGuideid(String guideid) {
        this.guideid = guideid;
    }

    public String getTuanid() {
        return tuanid;
    }

    public void setTuanid(String tuanid) {
        this.tuanid = tuanid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getNums() {
        return nums;
    }

    public void setNums(String nums) {
        this.nums = nums;
    }

    public String getTuanname(){
        return tuanname;
    }

    public void setTuanname(String tuanname){
        this.tuanname = tuanname;
    }

    @Override
    public String toString() {
        return "MessageList{" +
                "id='" + id + '\'' +
                ", guideid='" + guideid + '\'' +
                ", tuanid='" + tuanid + '\'' +
                ", title='" + title + '\'' +
                ", desc='" + desc + '\'' +
                ", time='" + time + '\'' +
                ", nums='" + nums + '\'' +
                ", img='" + img + '\'' +
                ", tuanname='" + tuanname + '\'' +
                '}';
    }

    @Override
    public int compareTo(Object another) {
        MessageList item = (MessageList) another;
        if (Integer.parseInt(item.getTime()) - Integer.parseInt(this.getTime()) > 0) {
            return -1;
        } else if (Integer.parseInt(item.getTime()) - Integer.parseInt(this.getTime()) < 0) {
            return 1;
        } else
            return 0;
    }
}
