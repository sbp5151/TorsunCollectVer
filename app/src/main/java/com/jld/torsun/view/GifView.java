package com.jld.torsun.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

import com.jld.torsun.R;

/**
 * Created by lz on 2016/3/8.
 */
public class GifView extends View{

    private long movieStart;
    private Movie movie;

    public GifView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        movie = Movie.decodeStream(getResources().openRawResource(
                R.raw.qrcode));
    }
    public GifView(Context context) {
        super(context);
        movie = Movie.decodeStream(getResources().openRawResource(
                R.raw.qrcode));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        long curTime = android.os.SystemClock.uptimeMillis();
        if (movieStart == 0) {
            movieStart = curTime;
        }
        if (movie != null) {
            int duraction = movie.duration();
            int relTime = (int) ((curTime - movieStart) % duraction);
            movie.setTime(relTime);
            movie.draw(canvas, 0, 0);
            invalidate();
        }
        super.onDraw(canvas);
    }

    @Override
    public void setLayoutParams(LayoutParams params) {
        super.setLayoutParams(params);
    }
}
