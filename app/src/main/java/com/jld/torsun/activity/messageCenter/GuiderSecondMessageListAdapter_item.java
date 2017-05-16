package com.jld.torsun.activity.messageCenter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.android.volley.toolbox.ImageLoader;
import com.jld.torsun.R;
import com.jld.torsun.util.LogUtil;
import com.jld.torsun.util.imagecache.MyImageLoader;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * @author liuzhi
 * @ClassName: TrouTeamListAdapter
 * @Description: 游推送信息的二级界面adapter
 */
public class GuiderSecondMessageListAdapter_item extends BaseAdapter {

    private List<String> mList;
    private Activity mActivity;
    private LayoutInflater mInflater;

    private ImageLoader imageLoader;

    private boolean isFrist = true;

    public GuiderSecondMessageListAdapter_item(Activity activity, List<String> list) {
        mActivity = activity;
        mList = list;
        mInflater = LayoutInflater.from(activity);
        imageLoader = MyImageLoader.getInstance(activity);
        LogUtil.d("GuiderSecondMessageListAdapter", "--item--" + list);

    }

    @Override
    public int getCount() {
        int ret = 0;
        if (null != mList) {
            ret = mList.size();
        }
        LogUtil.d("GuiderSecondMessageListAdapter", "ret--item--" + ret);

        return ret;
    }

    @Override
    public String getItem(int postion) {
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
    public View getView(int postion, View contentView, ViewGroup arg2) {
        ViewHold temp = null;
        String url = mList.get(postion);
        if (null == contentView) {
            contentView = mInflater.inflate(R.layout.item_guider_second_message_list_layout_item, null);
            temp = new ViewHold();
            temp.imageView = (ImageView) contentView.findViewById(R.id.iv_item_guider_second_message_list_img);
            contentView.setTag(temp);
        } else {
            temp = (ViewHold) contentView.getTag();
        }
        LogUtil.d("GuiderSecondMessageListAdapter", postion + "--item--" + url);
        Picasso.with(mActivity).load(url).error(R.mipmap.default_image).placeholder(R.mipmap.default_image).into(temp.imageView);
        return contentView;
    }

    class ViewHold {
        ImageView imageView;
    }

    public void addData(List<String> list) {
        mList.addAll(list);
        notifyDataSetChanged();
    }
}
