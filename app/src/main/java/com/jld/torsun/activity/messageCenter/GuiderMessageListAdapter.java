package com.jld.torsun.activity.messageCenter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.jld.torsun.R;
import com.jld.torsun.modle.TeamMessageItems;
import com.jld.torsun.util.LogUtil;
import com.jld.torsun.util.TimeUtil;
import com.jld.torsun.util.imagecache.MyImageLoader;

import java.util.LinkedList;
import java.util.List;

/**
 * @author liuzhi
 * @ClassName: TrouTeamListAdapter
 * @Description: 游推送信息的一级界面adapter
 */
public class GuiderMessageListAdapter extends BaseAdapter {

    private LinkedList<TeamMessageItems> mList;
    private Activity mActivity;
    private LayoutInflater mInflater;

    private String guideId;
    private ImageLoader imageLoader;

    public GuiderMessageListAdapter(Activity activity, LinkedList<TeamMessageItems> list, String guideId) {
        mActivity = activity;
        mList = list;
        mInflater = LayoutInflater.from(activity);
        imageLoader = MyImageLoader.getInstance(activity);
        this.guideId = guideId;
        LogUtil.d("GuiderMessageListAdapter", "guideId:" + guideId);

    }

    @Override
    public int getCount() {
        if (null != mList && mList.size() > 0) {
//			if (mList.size() > 6){
//				return 6;
//			}else {
            return mList.size();
//			}

        }
        return 0;
    }

    @Override
    public TeamMessageItems getItem(int postion) {
        LogUtil.d("GuiderMessageListAdapter", "postion:" + postion);
        if (null != mList && mList.size() > 0) {
            if (guideId.equals("-1000"))
                return mList.get(postion - 1);
            else
                return mList.get(postion - 2);
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
        if (null == contentView) {
            contentView = mInflater.inflate(R.layout.item_guider_message_list_layout, null);
            temp = new ViewHold();

            temp.messageTime = (TextView) contentView.findViewById(R.id.tv_item_guider_message_list_time);
            temp.messageTitle = (TextView) contentView.findViewById(R.id.tv_item_guider_message_list_title);
            temp.faceImage = (NetworkImageView) contentView.findViewById(R.id.niv_item_guider_message_list_face_img);
            contentView.setTag(temp);
        } else {
            temp = (ViewHold) contentView.getTag();
        }
        final TeamMessageItems item = mList.get(postion);
        temp.faceImage.setDefaultImageResId(R.mipmap.default_image);
        temp.faceImage.setErrorImageResId(R.mipmap.default_image);
        temp.faceImage.setImageUrl(item.getImg(), imageLoader);
//
//		//信息标题
        temp.messageTitle.setText(item.getTitle());
//
//		//信息时间
        temp.messageTime.setText(TimeUtil.timeFormatOther(item.getTime()));
        return contentView;
    }

    class ViewHold {
        TextView messageTime;
        TextView messageTitle;
        NetworkImageView faceImage;
    }

    public List<TeamMessageItems> getList() {
        return mList;
    }

    public void addData(List<TeamMessageItems> list) {
        mList.clear();
        mList.addAll(list);
        notifyDataSetChanged();
    }

}
