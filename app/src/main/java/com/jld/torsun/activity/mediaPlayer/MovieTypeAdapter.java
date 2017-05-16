package com.jld.torsun.activity.mediaPlayer;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.jld.torsun.R;
import com.jld.torsun.modle.MovieBean;
import com.jld.torsun.modle.MovieTypeBean;
import com.jld.torsun.util.imagecache.MyImageLoader;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author liuzhi
 */
public class MovieTypeAdapter extends BaseAdapter{
    private static final String TAG = "MovieTypeAdapter";
    private LinkedList<MovieTypeBean> mList = new LinkedList<MovieTypeBean>();
    private Activity mActivity;
    private LayoutInflater mInflater;


    public MovieTypeAdapter(Activity activity, LinkedList<MovieTypeBean> list) {
        mActivity = activity;
        mList = list;
        mInflater = LayoutInflater.from(activity);
    }

    @Override
    public int getCount() {
        if (null != mList && mList.size() > 0) {
            return mList.size();
        }
        return 0;
    }

    @Override
    public MovieTypeBean getItem(int postion) {
        if (null != mList && mList.size() > 0) {
            return mList.get(postion);
        }
        return null;
    }

    @Override
    public long getItemId(int postion) {
        return postion;
    }

    @Override
    public View getView(final int postion, View contentView, ViewGroup arg2) {
        ViewHold viewHold;
        if (contentView == null){
            contentView = mInflater.inflate(R.layout.item_movie_type, null);
            viewHold = new ViewHold();
            viewHold.textView = (TextView) contentView.findViewById(R.id.item_movie_type_tv);
            contentView.setTag(viewHold);
        }else {
            viewHold =(ViewHold) contentView.getTag();
        }
        final MovieTypeBean typeBean = mList.get(postion);
        viewHold.textView.setText(typeBean.getName());
        return contentView;
    }

    class ViewHold {
        TextView textView;
    }

    public void setData(LinkedList<MovieTypeBean> mList) {
        this.mList = mList;
        notifyDataSetChanged();
    }
}
