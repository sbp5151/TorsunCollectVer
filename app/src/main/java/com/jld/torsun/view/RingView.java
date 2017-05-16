package com.jld.torsun.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.jld.torsun.R;
import com.jld.torsun.util.DensityUtil;

/**
 * Created by lz on 2016/5/4.
 */
public class RingView extends View {

    private Paint mRingPaint;
    private Paint mPaint;

    private float mBorderRadius;
    private int mBorderWidth;
    public RingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }

    public RingView(Context context) {
        super(context);
    }

    public RingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(getWidth() / 2, getHeight() / 2, getWidth() / 2 - mBorderWidth, mRingPaint);

    }

    private void initView() {
        mRingPaint= new Paint();
        mRingPaint.setAntiAlias(true);
        mRingPaint.setColor(getResources().getColor(R.color.backgroud_ring));
        mRingPaint.setStyle(Paint.Style.STROKE);
        mRingPaint.setStrokeWidth(DensityUtil.dip2px(getContext(), 1));
        mBorderWidth = DensityUtil.dip2px(getContext(), 1);
    }
}
