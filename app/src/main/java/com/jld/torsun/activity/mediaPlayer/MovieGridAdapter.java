package com.jld.torsun.activity.mediaPlayer;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.jld.torsun.R;
import com.jld.torsun.modle.MovieBean;
import com.jld.torsun.util.imagecache.MyImageLoader;

import java.util.ArrayList;
import java.util.List;

import com.android.volley.RequestQueue;

/**
 * @author liuzhi
 * @ClassName: CreateNewMessageListAdapter
 * @Description: 创建新信息界面的listAdapter
 * @date 2016-3-17
 */
public class MovieGridAdapter extends BaseAdapter{
    private static final String TAG = "MovieGridAdapter";
    private List<MovieBean> mList = new ArrayList<MovieBean>();
    private Activity mActivity;
    private LayoutInflater mInflater;
    private ImageLoader imageLoader;

    //public BitmapUtils bitmapUtils;

    public MovieGridAdapter(Activity activity, List<MovieBean> list) {
        mActivity = activity;
        mList = list;
        mInflater = LayoutInflater.from(activity);
        imageLoader = MyImageLoader.getInstance(activity);
    }

    @Override
    public int getCount() {
        if (null != mList && mList.size() > 0) {
            return mList.size();
        }
        return 0;
    }

    @Override
    public MovieBean getItem(int postion) {
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
            contentView = mInflater.inflate(R.layout.item_movie_grid_layout, null);
            viewHold = new ViewHold();
            viewHold.textView = (TextView) contentView.findViewById(R.id.tv_item_grid_movie_name);
            viewHold.moviePlayNum = (TextView) contentView.findViewById(R.id.tv_item_grid_movie_play_num);
            viewHold.imageView = (NetworkImageView)contentView.findViewById(R.id.niv_item_grid_movie_bg);
            viewHold.imageView.setDefaultImageResId(R.mipmap.default_hear_ico);
            contentView.setTag(viewHold);
        }else {
            viewHold =(ViewHold) contentView.getTag();
        }
        final MovieBean movieBean = mList.get(postion);
        viewHold.textView.setText(movieBean.getName());
        viewHold.moviePlayNum.setText("Play："+movieBean.getNums());
        viewHold.imageView.setErrorImageResId(R.mipmap.default_hear_ico);
        viewHold.imageView.setImageUrl(movieBean.getPic(),imageLoader);
        return contentView;
    }

    class ViewHold {
        NetworkImageView imageView;
        TextView textView;
        TextView moviePlayNum;
    }

    public void setData(List<MovieBean> mList) {
        this.mList = mList;
        notifyDataSetChanged();
    }
}
