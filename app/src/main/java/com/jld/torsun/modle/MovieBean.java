package com.jld.torsun.modle;

import java.io.Serializable;

/**
 * Created by lz on 2016/4/1.
 */
public class MovieBean implements Serializable {

    private static final long serialVersionUID = 2L;

    /**
     *  "id": "56",
     "name": "功夫熊猫2",
     "pic": "http://192.168.1.1:81/movie/1.png",
     "url": "http://192.168.1.1:81/movie/1.flv",
     "adtime": 0,
     "starttime": "NULL",
     "endtime": "NULL",
     "typename": "movie",
     "typeid": "1",
     "time": "1467941631",
     "nums": "7"
     */
    private String id;          //ID
    private String name;        //电影名称
    private String pic;         //电影封面图片
    private String url;         //电影播放地址
    private String ad_pic;      //广告图片
    private String adpic_time;  //广告图片显示时间
    private String ad_url;      //广告视频
    private String adurl_time;  //广告视频显示时间
    private String time;        //创建时间
    private String nums;        //点击次数

    public MovieBean() {
    }

    public MovieBean(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public MovieBean(String id, String name, String pic, String url) {
        this.id = id;
        this.name = name;
        this.pic = pic;
        this.url = url;
    }

    public MovieBean(String id, String name, String pic, String url, String ad_pic, String adpic_time, String ad_url, String adurl_time, String time, String nums) {
        this.id = id;
        this.name = name;
        this.pic = pic;
        this.url = url;
        this.ad_pic = ad_pic;
        this.adpic_time = adpic_time;
        this.ad_url = ad_url;
        this.adurl_time = adurl_time;
        this.time = time;
        this.nums = nums;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPic() {
        return pic;
    }

    public String getUrl() {
        return url;
    }

    public String getAd_pic() {
        return ad_pic;
    }

    public String getAdpic_time() {
        return adpic_time;
    }

    public String getAd_url() {
        return ad_url;
    }

    public String getAdurl_time() {
        return adurl_time;
    }

    public String getTime() {
        return time;
    }

    public String getNums() {
        return nums;
    }

    @Override
    public String toString() {
        return "MovieBean{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", pic='" + pic + '\'' +
                ", url='" + url + '\'' +
                ", ad_pic='" + ad_pic + '\'' +
                ", adpic_time='" + adpic_time + '\'' +
                ", ad_url='" + ad_url + '\'' +
                ", adurl_time='" + adurl_time + '\'' +
                ", time='" + time + '\'' +
                ", nums='" + nums + '\'' +
                '}';
    }
}
