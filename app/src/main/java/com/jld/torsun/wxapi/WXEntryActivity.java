package com.jld.torsun.wxapi;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.jld.torsun.R;
import com.jld.torsun.util.ToastUtil;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;

/**
 * 微信支付回调函数
 */
public class WXEntryActivity extends Activity implements IWXAPIEventHandler {

    Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wxentry);
    }
    @Override
    public void onReq(BaseReq baseReq) {
    }

    /**
     * 支付完成回调函数
     *
     * @param baseResp
     */
    @Override
    public void onResp(BaseResp baseResp) {
        if (baseResp.getType() == 0) {//成功
            ToastUtil.showToast(this, getResources().getString(R.string.pay_win), 3000);
        } else if (baseResp.getType() == -1) {//错误
            ToastUtil.showToast(this, getResources().getString(R.string.pay_fail), 3000);
        } else if (baseResp.getType() == -2) {//用户取消
            ToastUtil.showToast(this, getResources().getString(R.string.pay_cancel), 3000);
        }
    }
}
