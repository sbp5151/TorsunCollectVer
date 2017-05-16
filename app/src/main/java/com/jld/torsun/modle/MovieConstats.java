package com.jld.torsun.modle;

/**
 * Created by lz on 2016/6/23.
 */
public class MovieConstats {

    /**电影服务端的ip地址*/
    public static final String MOVIE_IP = "http://192.168.1.1:81";
    /**电影列表*/
    public static final String MOVIE_LIST = "/mv/api.php";
    public static final String MOVIE_LIST_TYPE = "/mv/api.php?typeid="; //typeid是电影类别的id，默认不传或传0值，则为获取所有列表
    /**更新点击数*/
    public static final String UPDATE_NUM = "/mv/nums.php?id="; // mv/nums.php?id=3
}
