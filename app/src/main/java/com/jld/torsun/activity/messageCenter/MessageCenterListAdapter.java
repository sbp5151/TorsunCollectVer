package com.jld.torsun.activity.messageCenter;

import android.app.Activity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.jld.torsun.R;
import com.jld.torsun.modle.MessageList;
import com.jld.torsun.util.LogUtil;
import com.jld.torsun.util.TimeUtil;
import com.jld.torsun.util.imagecache.MyImageLoader;
import com.jld.torsun.view.RoundImageViewByXfermode;

import java.util.List;

/**
 * @author liuzhi
 * @ClassName: TrouTeamListAdapter
 * @Description: 信息中心的显示信息adapter
 */
public class MessageCenterListAdapter extends BaseAdapter {

    private List<MessageList> mList;
    private Activity mActivity;
    private LayoutInflater mInflater;

    private ImageLoader imageLoader;
    private final String TAG = "MessageCenterListAdapter";

    public MessageCenterListAdapter(Activity activity, List<MessageList> list) {
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
    public MessageList getItem(int postion) {
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
        MessageList item = mList.get(postion);
        LogUtil.d(TAG,item.toString());
        if (null == contentView) {
            contentView = mInflater.inflate(R.layout.item_message_center_layout, null);
            temp = new ViewHold();
            temp.messageTime = (TextView) contentView.findViewById(R.id.tv_item_message_time);
            temp.messageTitle = (TextView) contentView.findViewById(R.id.tv_item_message_title);
            temp.messageDescribe = (TextView) contentView.findViewById(R.id.tv_item_message_content);
            temp.messageNum = (TextView) contentView.findViewById(R.id.tv_item_message_num);
            temp.messageGuiderIv = (RoundImageViewByXfermode) contentView.findViewById(R.id.iv_item_message_icon);
            temp.messageTuanName = (TextView) contentView.findViewById(R.id.tv_item_message_tuan_name);
            temp.messageView = contentView.findViewById(R.id.view_item_message_flag);
            contentView.setTag(temp);
        } else {
            temp = (ViewHold) contentView.getTag();
        }
        if (!TextUtils.isEmpty(item.getGuideid()) && item.getGuideid().equals("-1000")) {
            temp.messageGuiderIv.setDefaultImageResId(R.mipmap.torsun_msg_ico);
            temp.messageGuiderIv.setErrorImageResId(R.mipmap.torsun_msg_ico);
            temp.messageGuiderIv.setImageUrl("", imageLoader);
            temp.messageTuanName.setText(mActivity.getResources().getString(R.string.msg_system_information));
            temp.messageDescribe.setText(item.getTitle());
            temp.messageView.setVisibility(View.GONE);
            temp.messageTitle.setVisibility(View.GONE);
        } else {
            //导游头像设置
            temp.messageGuiderIv.setDefaultImageResId(R.mipmap.default_hear_ico);
            temp.messageGuiderIv.setErrorImageResId(R.mipmap.default_hear_ico);
            if (!TextUtils.isEmpty(item.getTitle())) {
                temp.messageGuiderIv.setImageUrl(item.getImg(), imageLoader);
                //信息团名称
                temp.messageTuanName.setText(item.getTuanname());
                //信息标题
                temp.messageTitle.setText(item.getTitle());
                //信息内容
                temp.messageDescribe.setText(item.getDesc());
            }
        }

        //信息个数
        if (TextUtils.isEmpty(item.getNums()) || "0".equals(item.getNums())) {
            temp.messageNum.setVisibility(View.GONE);
        } else {
            temp.messageNum.setVisibility(View.VISIBLE);
            temp.messageNum.setText(item.getNums());
        }

        //信息时间
        temp.messageTime.setText(TimeUtil.timeFormatOther(item.getTime()));
        return contentView;
    }

    public void addItem(MessageList item) {
        mList.add(item);
        notifyDataSetChanged();
    }

    class ViewHold {
        TextView messageTitle;
        TextView messageDescribe;
        TextView messageNum;
        TextView messageTime;
        TextView messageTuanName;
        View messageView;
        RoundImageViewByXfermode messageGuiderIv;
    }

    public void setData(List<MessageList> list) {
        mList = list;
        notifyDataSetChanged();
    }

    public MessageList getData(int position) {
        return mList.get(position);
    }
}
