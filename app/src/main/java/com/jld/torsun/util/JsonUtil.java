package com.jld.torsun.util;

import android.text.TextUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * 项目名称：Torsun
 * 晶凌达科技有限公司所有，
 * 受到法律的保护，任何公司或个人，未经授权不得擅自拷贝。
 *
 * @creator 单柏平 <br/>
 * @create-time ${date} ${time}
 */
public class JsonUtil {

    public static Boolean isJson(String str){
        if(TextUtils.isEmpty(str))
            return false;
        JsonParser jsonParser = new JsonParser();
        JsonElement element = jsonParser.parse(str);
        if(element.isJsonObject())
            return true;
        else
            return false;
    }
}
