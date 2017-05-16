package com.jld.torsun.modle;

import java.io.Serializable;

/**
 * Created by lz on 2016/7/8.
 * 电影广告
 */
public class MovieADBean implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     *  {
     "id": "74",
     "name": "做个广告",
     "pic": "http://192.168.1.1:81/movie/15.png",
     "url": "",
     "adtime": "60",
     "starttime": "1467941631",
     "endtime": "1467949631",
     "typename": "advert",
     "typeid": "0",
     "time": "1467941631",
     "nums": "1"
     }
     */
//    private String id;
//    private String name;
    private String pic; //广告图片地址
    private String url; //广告视频地址
    private String adtime;  //广告时间
    private String starttime;   //广告有效时间的开始时间
    private String endtime;     ////广告有效时间的结束时间
//    private String typename;
//    private String typeid;
//    private String time;
//    private String nums;


    public MovieADBean() {
    }

    public MovieADBean(String pic, String url, String adtime) {
        this.pic = pic;
        this.url = url;
        this.adtime = adtime;
    }

    public MovieADBean(String pic, String url, String adtime, String starttime, String endtime) {
        this.pic = pic;
        this.url = url;
        this.adtime = adtime;
        this.starttime = starttime;
        this.endtime = endtime;
    }

    public String getPic() {
        return pic;
    }

    public String getUrl() {
        return url;
    }

    public String getAdtime() {
        return adtime;
    }

    public String getStarttime() {
        return starttime;
    }

    public String getEndtime() {
        return endtime;
    }

    @Override
    public String toString() {
        return "MovieADBean{" +
                "pic='" + pic + '\'' +
                ", url='" + url + '\'' +
                ", adtime='" + adtime + '\'' +
                ", starttime='" + starttime + '\'' +
                ", endtime='" + endtime + '\'' +
                '}';
    }
}
