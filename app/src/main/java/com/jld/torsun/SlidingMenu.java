package com.jld.torsun;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import com.jld.torsun.activity.MainFragment;
import com.jld.torsun.util.AndroidUtil;
import com.jld.torsun.util.LogUtil;
import com.nineoldandroids.view.ViewHelper;

public class SlidingMenu extends HorizontalScrollView {

    private static final String TAG = "SlidingMenu";
    /**
     * 屏幕宽度
     */
    private int mScreenWidth;
    /**
     * dp
     */
    private int mMenuRightPadding;
    /**
     * 菜单的宽度
     */
    private int mMenuWidth;
    private int mHalfMenuWidth;
    private MenuOpenClose mMenuOpenClose;

    private boolean isOpen;

    private boolean once;

    private ViewGroup mMenu;
    private ViewGroup mContent;
    private int lastNum = 1;

    public SlidingMenu(Context context, AttributeSet attrs) {
        this(context, attrs, 0);

    }

    public SlidingMenu(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        LogUtil.d(TAG, "SlidingMenu");

        mScreenWidth = AndroidUtil.getScreenWidth(context);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.SlidingMenu, defStyle, 0);
        int n = a.getIndexCount();
        for (int i = 0; i < n; i++) {
            int attr = a.getIndex(i);
            switch (attr) {
                case R.styleable.SlidingMenu_rightPadding:
                    // 默认50
                    mMenuRightPadding = a.getDimensionPixelSize(attr,
                            (int) TypedValue.applyDimension(
                                    TypedValue.COMPLEX_UNIT_DIP, 66,
                                    getResources().getDisplayMetrics()));// 默认为10DP
                    break;
            }
        }
        a.recycle();
    }

    public SlidingMenu(Context context) {
        this(context, null, 0);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        /**
         * 显示的设置一个宽度
         */
        if (!once) {
            LinearLayout wrapper = (LinearLayout) getChildAt(0);
            mMenu = (ViewGroup) wrapper.getChildAt(0);
            mContent = (ViewGroup) wrapper.getChildAt(1);
            mMenuWidth = mScreenWidth - (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 67,
                    getResources().getDisplayMetrics());
            //	mMenuWidth = mScreenWidth - mMenuRightPadding;
            mHalfMenuWidth = mMenuWidth / 2;
            mMenu.getLayoutParams().width = mMenuWidth;
            mContent.getLayoutParams().width = mScreenWidth;

        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (changed) {
            // 将菜单隐藏
            this.scrollTo(mMenuWidth, 0);
            once = true;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        switch (action) {
            // Up时，进行判断，如果显示区域大于菜单宽度一半则完全显示，否则隐藏
            case MotionEvent.ACTION_UP:
                int scrollX = getScrollX();
                if (scrollX > mHalfMenuWidth) {
                    this.smoothScrollTo(mMenuWidth, 0);
                    isOpen = false;
                    MainFragment.fragmentNum = lastNum;
                    if (mMenuOpenClose != null) {
                        mMenuOpenClose.isClose();
                    }
                } else {
                    this.smoothScrollTo(0, 0);
                    isOpen = true;
                    lastNum = MainFragment.fragmentNum;
                    MainFragment.fragmentNum = 0;
                    if (mMenuOpenClose != null) {
                        mMenuOpenClose.isOpen();
                    }
                }
                return true;
        }
        return super.onTouchEvent(ev);
    }

    /**
     * 打开菜单
     */
    public void openMenu() {
        LogUtil.d(TAG, "openMenu");
        if (isOpen)
            return;
        this.smoothScrollTo(0, 0);
        if (mMenu != null)
            mMenu.setFocusable(true);
        isOpen = true;
        lastNum = MainFragment.fragmentNum;
        MainFragment.fragmentNum = 0;
        if(mMenuOpenClose!=null){
            mMenuOpenClose.isOpen();
//            setChildViewEnabled(mMenu,false);
        }
    }

    /**
     * 关闭菜单
     */
    public void closeMenu() {
        LogUtil.d(TAG, "closeMenu");
        if (isOpen) {
            this.smoothScrollTo(mMenuWidth, 0);
            mMenu.setEnabled(false);
            isOpen = false;
            if(mMenuOpenClose!=null){
                mMenuOpenClose.isClose();
//                setChildViewEnabled(mMenu,true);

            }
        }
    }

    public void setChildViewEnabled(ViewGroup viewGroup, boolean isEnabled) {
        LogUtil.d(TAG, "setChildViewEnabled");
        int size = viewGroup.getChildCount();
        int index = 0;
        for (; index < size; index++) {
            viewGroup.getChildAt(index).setEnabled(isEnabled);
        }
    }

    /**
     * 切换菜单状态
     */
    public void toggle() {
        LogUtil.d(TAG, "toggle");
        if (isOpen) {
            closeMenu();
        } else {
            openMenu();
        }
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        float scale = l * 1.0f / mMenuWidth;
        float leftScale = 1 - 0.3f * scale;
        float rightScale = 0.8f + scale * 0.2f;

        ViewHelper.setScaleX(mMenu, leftScale);
        ViewHelper.setScaleY(mMenu, leftScale);
        ViewHelper.setAlpha(mMenu, 0.6f + 0.4f * (1 - scale));
        ViewHelper.setTranslationX(mMenu, mMenuWidth * scale * 0.7f);

        ViewHelper.setPivotX(mContent, 0);
        ViewHelper.setPivotY(mContent, mContent.getHeight() / 2);
        ViewHelper.setScaleX(mContent, rightScale);
        ViewHelper.setScaleY(mContent, rightScale);

    }

    public void setInterface(MenuOpenClose menuOpenClose){
        LogUtil.d(TAG, "setInterface");
        mMenuOpenClose = menuOpenClose;
    }
   public interface MenuOpenClose{
        void isOpen();
        void isClose();
    }
}
