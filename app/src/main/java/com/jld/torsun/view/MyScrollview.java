package com.jld.torsun.view;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.ScrollView;

import com.jld.torsun.util.LogUtil;

/**
 * Created by boping on 2016/1/18.
 */
public class MyScrollview extends ScrollView {
    private View inner;
    private float y;
    private Rect normal = new Rect();
    private TopScrollListener mTopScrollListener;
    private Boolean isTop = true;

    // 根布局视图
    private View mRootView;
    private int mpreY = 0;
    /**
     * 必要的一些初始化
     *
     * @param context
     * @param attrs
     */
    public MyScrollview(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        if (getChildCount() > 0) {
            inner = getChildAt(0);
        }
//        mRootView = getChildAt(0);
//        super.onFinishInflate();
    }
//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        float curY = event.getY();
//        switch (event.getAction()) {
//            case MotionEvent.ACTION_MOVE: {
//                int delta = (int) ((curY - mpreY) * 0.25);
//                if (delta > 0) {
//                    mRootView.layout(mRootView.getLeft(), mRootView.getTop()
//                            + delta, mRootView.getRight(), mRootView.getBottom()
//                            + delta);
//                }
//            }
//            break;
//        }
//        mpreY = (int) curY;
//        return super.onTouchEvent(event);
//    }
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (inner == null) {
            return super.onTouchEvent(ev);
        } else {
            commOnTouchEvent(ev);
        }
        return super.onTouchEvent(ev);
    }

    public void commOnTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                y = ev.getY();

                break;
            case MotionEvent.ACTION_UP:

                if (isNeedAnimation()) {
                    animation();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                final float preY = y;
                float nowY = ev.getY();
                int deltaY = (int) (preY - nowY);
//                if (deltaY > 0) {
//                    if (mTopScrollListener != null) {//如果向上滑动显示title
//                        mTopScrollListener.visibilityTitle();
//                    } else//否则隐藏
//                        mTopScrollListener.inVisibilityTitle();
//                }
                // 滚动
                scrollBy(0, deltaY);
                y = nowY;
                // 当滚动到最上或者最下时就不会再滚动，这时移动布局
                if (isNeedMove()) {
//                    if (normal.isEmpty()) {
//                        // 保存正常的布局位置
//                        normal.set(inner.getLeft(), inner.getTop(), inner
//                                .getRight(), inner.getBottom());
//
//                    }
//                    // 移动布局
//                    inner.layout(inner.getLeft(), inner.getTop() - deltaY, inner
//                            .getRight(), inner.getBottom() - deltaY);
                }
                break;
            default:
                break;
        }
    }

    // 开启动画移动

    public void animation() {
        // 开启移动动画
        TranslateAnimation ta = new TranslateAnimation(0, 0, inner.getTop(),
                normal.top);
        ta.setDuration(200);
        inner.startAnimation(ta);
        // 设置回到正常的布局位置
        inner.layout(normal.left, normal.top, normal.right, normal.bottom);
        normal.setEmpty();
    }

    // 是否需要开启动画
    public boolean isNeedAnimation() {
        return !normal.isEmpty();
    }

    // 是否需要移动布局
    public boolean isNeedMove() {
        int offset = inner.getMeasuredHeight() - getHeight();
        int scrollY = getScrollY();
        if (scrollY == 0) {//顶部
            LogUtil.d("setVisibility", "scrollY == 0");
            mTopScrollListener.visibilityTitle();
            return true;
        } else if (scrollY == offset) {//底部
            LogUtil.d("setVisibility", "scrollY == offset");
            mTopScrollListener.inVisibilityTitle();
            return true;
        }
        LogUtil.d("setVisibility", "else");
        mTopScrollListener.inVisibilityTitle();
        return false;
    }

    public void setTopScrollListener(TopScrollListener mTopScrollListener) {
        this.mTopScrollListener = mTopScrollListener;
    }

    public interface TopScrollListener {
        public void visibilityTitle();

        public void inVisibilityTitle();
    }
}
