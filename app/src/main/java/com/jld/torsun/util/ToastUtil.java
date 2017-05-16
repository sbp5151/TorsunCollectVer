package com.jld.torsun.util;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.jld.torsun.R;

/**
 * Toast工具 防止toast点击多次显示
 */
public class ToastUtil {
    private static Toast mToast;
    private static Toast mCustomToast;
    private static Handler mHandler = new Handler();
    private static Runnable r = new Runnable() {
        public void run() {
            mToast.cancel();
        }
    };
    private static Runnable runnable = new Runnable() {
        public void run() {
            mCustomToast.cancel();
        }
    };

    public static void showToast(Context mContext, String text, int duration) {

        mHandler.removeCallbacks(r);
        if (mToast != null)
            mToast.setText(text);
        else
            mToast = Toast.makeText(mContext, text, Toast.LENGTH_SHORT);
        mHandler.postDelayed(r, duration);

        mToast.show();
    }

    public static void showToast(Context mContext, int resId, int duration) {
        showToast(mContext, mContext.getResources().getString(resId), duration);
    }

    public static void showCustomToast(Context mContext, String text, int duration) {
        mHandler.removeCallbacks(runnable);
        View layout = LayoutInflater.from(mContext).inflate(R.layout.layout_custom_toast, null);
        TextView textView = (TextView) layout.findViewById(R.id.tv_custom_toast);
        textView.setText(text);
        if (mCustomToast == null) {
            mCustomToast = new Toast(mContext);
            mCustomToast.setDuration(Toast.LENGTH_LONG);
        }
        //mCustomToast.setGravity(Gravity.CENTER_VERTICAL,0,0);
        mCustomToast.setView(layout);
        mHandler.postDelayed(runnable, duration);
        mCustomToast.show();
    }

    public static void showCustomToast(Context mContext, int resId, int duration) {
        showCustomToast(mContext, mContext.getResources().getString(resId), duration);
    }
}
