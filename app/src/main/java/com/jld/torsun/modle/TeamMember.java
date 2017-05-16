package com.jld.torsun.modle;

import com.jld.torsun.util.LogUtil;

import java.io.Serializable;

/**
 * 团成员 * { "userid":"41", "mobile":"13823133104", "nick":"yaozu",
 * "username":"hh",
 * "img":"http:\/\/img.tucson.net.cn\/2015-11\/1447473975_51953.png" }
 */
public class TeamMember implements Serializable, Comparable {
    /**
     * @Fields serialVersionUID : TODO(用一句话描述这个变量表示什么)
     */
    private static final long serialVersionUID = 1L;
    /**
     * (服务器端)旅游团id
     */
    public String teamid;

    /**
     * 本地端id
     */
    public String localid;
    /**
     * 用户id
     */
    public String userid;
    /**
     * 手机号
     */
    public String mobile;
    /**
     * 用户姓名
     */
    public String username;
    /**
     * 昵称
     */
    public String nick;
    /**
     * 用户头像
     */
    public String img;
    /**
     * 是否是导游的标记
     */
    public String isload;

    /**
     * 获取导游时间
     */
    public long time;

    /**
     * 在线状态改变时间
     */
    public String online_change_time = "0";

    /**
     * 是否在线
     */
    public String online = "0";
    /**
     * 经纬度
     */
    public String jwd;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getTeamid() {
        return teamid;
    }

    public void setTeamid(String teamid) {
        this.teamid = teamid;
    }

    public String getLocalid() {
        return localid;
    }

    public void setLocalid(String localid) {
        this.localid = localid;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getIsload() {
        return isload;
    }

    public void setIsload(String isload) {
        this.isload = isload;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getOnline_change_time() {
        return online_change_time;
    }

    public void setOnline_change_time(String online_change_time) {
        this.online_change_time = online_change_time;
    }

    public String getOnline() {
        return online;
    }

    public void setOnline(String online) {
        this.online = online;
    }

    public String getJwd() {
        return jwd;
    }

    public void setJwd(String jwd) {
        this.jwd = jwd;
    }

    @Override
    public String toString() {
        return "TeamMember{" +
                "teamid='" + teamid + '\'' +
                ", localid='" + localid + '\'' +
                ", userid='" + userid + '\'' +
                ", mobile='" + mobile + '\'' +
                ", username='" + username + '\'' +
                ", nick='" + nick + '\'' +
                ", img='" + img + '\'' +
                ", isload='" + isload + '\'' +
                ", time=" + time +
                ", online_change_time='" + online_change_time + '\'' +
                ", online='" + online + '\'' +
                ", jwd='" + jwd + '\'' +
                '}';
    }

    @Override
    public int compareTo(Object another) {
        TeamMember teamMember = (TeamMember) another;
//        if(Integer.decode(teamMember.getUserid())>Integer.decode(this.getUserid()))
//            return -1;
//        else
//            return 1;

        String online = teamMember.getOnline();
        LogUtil.d("compareTo", "online:" + online);
        LogUtil.d("compareTo", "getOnline():" + getOnline());
        //两个均不在线,则按时间排序
        if (online.equals("0") && this.getOnline().equals("0")) {
            String time = teamMember.getOnline_change_time();
            String myTime = this.getOnline_change_time();
            LogUtil.d("compareTo", "myTime:" + myTime);
            LogUtil.d("compareTo", "time:" + time);

            return Long.decode(myTime) > Long.decode(time) ? -1 : 1;
            //不在线的在上面
        } else if (this.getOnline().equals("0") && online.equals("1")) {
            return -1;
            //不在线的在上面
        } else if (this.getOnline().equals("1") && online.equals("0")) {
            return 1;
            //两个均在线,则按时间排序
        } else if (this.getOnline().equals("1") && online.equals("1")) {
            String time = teamMember.getOnline_change_time();
            String myTime = this.getOnline_change_time();
            return Long.decode(myTime) > Long.decode(time) ? -1 : 1;
        }
        return 0;
    }

//    @Override
//    public int hashCode() {
//        if (TextUtils.isEmpty(userid))
//            return mobile.hashCode() * 29;
//        return userid.hashCode() * 29;
//    }
//
//    @Override
//    public boolean equals(Object o) {
//        if (!(o instanceof TeamMember))
//            return false;
//        if (TextUtils.isEmpty(userid) || TextUtils.isEmpty(((TeamMember) o).getUserid()))
//            return mobile.equals(((TeamMember) o).getMobile());
//        return userid.equals(((TeamMember) o).getUserid());
//    }
}
