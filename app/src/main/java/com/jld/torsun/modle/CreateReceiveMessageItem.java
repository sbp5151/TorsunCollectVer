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
public class CreateReceiveMessageItem {
    private String msg = "";//描述
    private ArrayList<String> url = new ArrayList<>();//图片链接

    public CreateReceiveMessageItem(String msg, ArrayList<String> url) {
        this.msg = msg;
        this.url = url;
    }

    public CreateReceiveMessageItem() {
    }

    public CreateReceiveMessageItem(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public ArrayList<String> getUrl() {
        return url;
    }

    public void setUrl(ArrayList<String> url) {
        this.url = url;
    }

    public void setItem(String value) {
        url.add(value);
    }

    public String getItem(int position) {
        return url.get(position);
    }

    @Override
    public String toString() {
        return "CreateReceiveMessageItem{" +
                "msg='" + msg + '\'' +
                ", url=" + url +
                '}';
    }
}
