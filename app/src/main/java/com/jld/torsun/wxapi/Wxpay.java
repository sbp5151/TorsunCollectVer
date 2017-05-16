package com.jld.torsun.wxapi;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import java.util.Random;

/**
 * 项目名称：branches
 * 晶凌达科技有限公司所有，
 * 受到法律的保护，任何公司或个人，未经授权不得擅自拷贝。
 *
 * @creator boping
 * @create-time 2016/8/25 10:00
 */
public class Wxpay extends Activity {
    /**
     一、支付流程：
     1、首先是把该app注册到微信，
     2、发起一个线程生成支付订单。这里要注意金额的单位为分，只能为整数。
     3、把支付订单post到后台生成一个预支付订单，返回prepay_id（预支付回话标识）
     4、将参数再次签名传输给app发起支付，
     5、支付结果会在WXPayEntryActivity类里onResp函数中查看，当errCode为：0表示成功 -1表示错误 -2表示用户取消。
     */
    public static final String APP_ID = "wx974115dc2397166d";
    private IWXAPI api;
    private Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * 把APP注册到微信
     */
    private void initWx() {
        api = WXAPIFactory.createWXAPI(context,APP_ID,true);
        api.registerApp(APP_ID);
    }
    /**
     * 随机字符串生成
     *
     * @return
     */
    public static String getRandomString() {
        String base = "abcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < 32; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }
}
