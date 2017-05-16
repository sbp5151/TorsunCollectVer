package com.jld.torsun.modle;

/**
 * Created by lz on 2016/5/10.
 * 所有广播action的定义集合类
 */
public class ActionConstats {

    /**电池电量变化广播action*/
    public static final String BATTERY_INFO_CHANGE ="com.jld.torsun.service.BatteryInfoReceiver.BATTERY_INFO_CHANGE";
    /**头像修改action*/
    public static final String IMGCHANGE = "com.jld.torsun.activity.fragment.FragmentSet.IMGCHANGE";
    /**用户信息修改改变*/
    public static final String STRCHANGE = "com.jld.torsun.activity.tours.MulticastServer.STRCHANGE";
    /** 修改昵称的 */
    public static final String NICK_CHANGE = "com.jld.torsun.activity.NikChangeActivity.NICK";
    /**导游改变*/
    public static final String GUIDE_INFO_CHANGE = "com.jld.torsun.activity.tours.MulticastClient.GUIDE_INFO_CHANGE";
    /** 修改姓名的 */
    public static final String NAME_CHANGE = "com.jld.torsun.activity.NikChangeActivity.NAME";
    /**扫描成为导游不再发送经纬度*/
    public static final String GET_LOAD_POWER = "get_load_power";
    /**连接网络发生改变*/
    public static final String NET_CONNECT_CHANGE = "android.net.conn.CONNECTIVITY_CHANGE";
}
