package com.jld.torsun.modle;

import java.util.ArrayList;

/**
 * 项目名称：Torsun
 * 晶凌达科技有限公司所有，
 * 受到法律的保护，任何公司或个人，未经授权不得擅自拷贝。
 *
 * @creator 单柏平 <br/>
 * @create-time ${date} ${time}
 */
public class CreateReceiveMessage {

    private ArrayList<CreateReceiveMessageItem> message = new ArrayList<>();
    private String title ="";//标题
    private String desc = "";//第一个描述
    private Boolean isRead;//是否已读
    private String userid="";//用户ID
    private String tuanid="";//团ID
    private String sign;//签名
    private String pic="";//封面

    @Override
    public String toString() {
        return "CreateReceiveMessage{" +
                "message=" + message +
                ", title='" + title + '\'' +
                ", desc='" + desc + '\'' +
                ", isRead=" + isRead +
                ", userid='" + userid + '\'' +
                ", tuanid='" + tuanid + '\'' +
                ", sign='" + sign + '\'' +
                ", pic='" + pic + '\'' +
                ", type=" + type +
                '}';
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    private int type;//0为图文，1为分享

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getTuanid() {
        return tuanid;
    }

    public void setTuanid(String tuanid) {
        this.tuanid = tuanid;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public ArrayList<CreateReceiveMessageItem> getMessage() {
        return message;
    }

    public void setMessage(ArrayList<CreateReceiveMessageItem> message) {
        this.message = message;
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

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }

    public CreateReceiveMessageItem getItem(int position) {
        return message.get(position);
    }

}
