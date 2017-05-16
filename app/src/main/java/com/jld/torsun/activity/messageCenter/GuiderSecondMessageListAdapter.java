package com.jld.torsun.activity.messageCenter;

import android.app.Activity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.jld.torsun.R;
import com.jld.torsun.modle.CreateReceiveMessageItem;
import com.jld.torsun.util.LogUtil;
import com.jld.torsun.util.imagecache.MyImageLoader;

import java.util.List;

/**
 * @author liuzhi
 * @ClassName: TrouTeamListAdapter
 * @Description: 游推送信息的二级界面adapter
 */
public class GuiderSecondMessageListAdapter extends BaseAdapter {

    private List<CreateReceiveMessageItem> mList;
    private Activity mActivity;
    private LayoutInflater mInflater;

    private ImageLoader imageLoader;

    private boolean isFrist = true;

    public GuiderSecondMessageListAdapter(Activity activity, List<CreateReceiveMessageItem> list) {
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
    public CreateReceiveMessageItem getItem(int postion) {
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
        final CreateReceiveMessageItem item = mList.get(postion);
        if (null == contentView) {
            contentView = mInflater.inflate(R.layout.item_guider_second_message_list_layout, null);
            temp = new ViewHold();
            temp.messageContent = (TextView) contentView.findViewById(R.id.tv_item_guider_second_message_list_content);
            temp.listView = (ListView) contentView.findViewById(R.id.lv_item_guider_second_message_list_content);
            contentView.setTag(temp);
        } else {
            temp = (ViewHold) contentView.getTag();
        }
        LogUtil.d("GuiderSecondMessageListAdapter", postion + "---" + item.getUrl());
        GuiderSecondMessageListAdapter_item mAdapter = new GuiderSecondMessageListAdapter_item(mActivity, item.getUrl());
        temp.listView.setAdapter(mAdapter);


        //信息内容
        if (TextUtils.isEmpty(item.getMsg())) {
            temp.messageContent.setVisibility(View.GONE);
        } else {
            temp.messageContent.setText(item.getMsg());
        }

        if (isFrist) {
            isFrist = false;
            temp.listView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    notifyDataSetChanged();
                }
            }, 3000);
        }
        return contentView;
    }

    private void changeViewHeigh(NetworkImageView view) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = (int) ((float) view.getWidth() / view.getDrawable().getMinimumWidth() * view.getDrawable().getMinimumHeight());
        view.setLayoutParams(layoutParams);
        notifyDataSetChanged();
    }

    class ViewHold {
        TextView messageContent;
        ListView listView;
    }

    public void setData(List<CreateReceiveMessageItem> list) {
        mList.clear();
        mList.addAll(list);
        LogUtil.d("GuiderSecondMessageActivity" + mList.size());

        notifyDataSetChanged();
    }
}
