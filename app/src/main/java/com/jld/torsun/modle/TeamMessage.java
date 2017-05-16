package com.jld.torsun.modle;

import java.util.LinkedList;

/**
 * 项目名称：Torsun
 * 晶凌达科技有限公司所有，
 * 受到法律的保护，任何公司或个人，未经授权不得擅自拷贝。
 *
 * @creator 单柏平 <br/>
 * @create-time ${date} ${time}
 */
public class TeamMessage {

    private String nick;//昵称
    private String img;//
    private String tuanname;
    private String good;
    private String flower;
    private String start;
    private String count;
    private String checkflower;
    private String checkgood;
    private LinkedList<TeamMessageItems> item;
    @Override
    public String toString() {
        return "TeamMessage{" +
                "nick='" + nick + '\'' +
                ", img='" + img + '\'' +
                ", tuanname='" + tuanname + '\'' +
                ", good='" + good + '\'' +
                ", flower='" + flower + '\'' +
                ", start='" + start + '\'' +
                ", count='" + count + '\'' +
                ", checkflower='" + checkflower + '\'' +
                ", checkgood='" + checkgood + '\'' +
                ", item=" + item +
                '}';
    }

    public String getCheckgood() {
        return checkgood;
    }

    public void setCheckgood(String checkgood) {
        this.checkgood = checkgood;
    }

    public String getCheckflower() {
        return checkflower;
    }

    public void setCheckflower(String checkflower) {
        this.checkflower = checkflower;
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

    public String getTuanname() {
        return tuanname;
    }

    public void setTuanname(String tuanname) {
        this.tuanname = tuanname;
    }

    public String getGood() {
        return good;
    }

    public void setGood(String good) {
        this.good = good;
    }

    public String getFlower() {
        return flower;
    }

    public void setFlower(String flower) {
        this.flower = flower;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public LinkedList<TeamMessageItems> getItem() {
        return item;
    }

    public void setItem(LinkedList<TeamMessageItems> item) {
        this.item = item;
    }
}
