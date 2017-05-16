package com.jld.torsun.activity;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.jld.torsun.R;
import com.jld.torsun.util.DensityUtil;
import com.jld.torsun.util.LogUtil;

public class BaseActivity extends Activity implements GestureDetector.OnGestureListener, View.OnTouchListener {
    private static final String TAG = "BaseActivity";
    private GestureDetector mGestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//		//透明状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//		//透明导航栏
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        super.onCreate(savedInstanceState);
        mGestureDetector = new GestureDetector(this);
//		requestWindowFeature(Window.FEATURE_NO_TITLE);
//		LogUtil.d(TAG, "onCreate().excute");
//		if(null != savedInstanceState){
//		LogUtil.d(TAG, "onCreate().savedInstanceState = " + savedInstanceState);
//		}
    }

    /**
     * 右进左出的动画方式跳转到目标activity
     */
    protected void toActivity(Class<?> aClass) {
        Intent intent = new Intent(this, aClass);
        startActivity(intent);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
    }

    /**
     * 右进左出的动画方式跳转到目标activity
     */
    protected void toActivity(Class<?> aClass,String toName) {
        Intent intent = new Intent(this, aClass);
        intent.putExtra("toName",toName);
        startActivity(intent);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
    }
    /**
     * 收起输入框
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (isShouldHideInput(v, ev)) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
            return super.dispatchTouchEvent(ev);
        }
        // 必不可少，否则所有的组件都不会有TouchEvent了
        if (getWindow().superDispatchTouchEvent(ev)) {
            return true;
        }
        return onTouchEvent(ev);
    }

    public boolean isShouldHideInput(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] leftTop = {0, 0};
            //获取输入框当前的location位置
            v.getLocationInWindow(leftTop);
            int left = leftTop[0];
            int top = leftTop[1];
            int bottom = top + v.getHeight();
            int right = left + v.getWidth();
            if (event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom) {
                // 点击的是输入框区域，保留点击EditText的事件
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    /********************************************
     * 滑屏切换
     *****************************************/
    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
    }

    private int verticalMinDistance;
    private int minVelocity = 400;

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        verticalMinDistance = DensityUtil.dip2px(this, 120);
        LogUtil.d(TAG, "verticalMinDistance:" + verticalMinDistance);

        if (e1 == null || e2 == null)
            return false;
        if (e1.getY() - e2.getY() > DensityUtil.dip2px(this, 100)) {
            LogUtil.d(TAG, "向上手势");
        } else if (e2.getY() - e1.getY() > DensityUtil.dip2px(this, 100)) {
            LogUtil.d(TAG, "向下手势");
        } else if (e1.getX() - e2.getX() > verticalMinDistance
                && Math.abs(velocityX) > minVelocity) {
            LogUtil.d(TAG, "向左手势");
        } else if (e2.getX() - e1.getX() > verticalMinDistance
                && Math.abs(velocityX) > minVelocity) {
            LogUtil.d(TAG, "向右手势");
            this.finish();
            overridePendingTransition(R.anim.left_in, R.anim.right_out);
        }
        LogUtil.d(TAG, "e1.getY() - e2.getY():" + (e1.getY() - e2.getY()));
        LogUtil.d(TAG, "e1.getX() - e2.getX()" + (e1.getX() - e2.getX()));

        return false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }

    public void setFlingView(View view) {
        view.setOnTouchListener(this);
    }

    public void setFlingView(ViewGroup linearLayout) {
        LogUtil.d(TAG, "setFlingView:" + linearLayout);

        linearLayout.setOnTouchListener(this);
    }

}
