package com.jld.torsun.adapter;

import android.app.Activity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.jld.torsun.R;
import com.jld.torsun.modle.TeamMember;
import com.jld.torsun.util.LogUtil;
import com.jld.torsun.util.imagecache.MyImageLoader;
import com.lidroid.xutils.BitmapUtils;
import com.squareup.picasso.Picasso;

import java.util.Collections;
import java.util.List;

//import com.android.volley.RequestQueue;

/**
 * @author liuzhi
 * @ClassName: TeamMemberListAdapter
 * @Description: 团队下的成员列表显示的适配器
 * @date 2015-12-2 下午4:10:37
 */
public class TeamMemberListAdapter extends BaseAdapter {
    private List<TeamMember> mList;
    private Activity mActivity;
    //	private LayoutInflater mInflater;
    public static final String TAG = "TeamMemberListAdapter";
    private ImageLoader imageLoader;
    private boolean isTeam;
    public BitmapUtils bitmapUtils;

    private boolean isPreView = false;

    public TeamMemberListAdapter(Activity activity, List<TeamMember> list,
                                 int postNum) {
        mActivity = activity;
        mList = list;
//		mInflater = LayoutInflater.from(activity);
        // BitmapCache imageCache=new BitmapCache();
        // queue=Volley.newRequestQueue(mActivity);
        imageLoader = MyImageLoader.getInstance(activity);
        isTeam = (0 == postNum);
        isPreView = postNum < 0;//判断是否为预览界面的list
        bitmapUtils = new BitmapUtils(activity);
        bitmapUtils.configDefaultLoadingImage(R.mipmap.default_hear_ico);//默认背景图片
        bitmapUtils.configDefaultLoadFailedImage(R.mipmap.default_hear_ico);//加载失败图片
    }

    @Override
    public int getCount() {
        if (null != mList && mList.size() > 0) {
            return mList.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int postion) {
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

        TeamMember item = mList.get(postion);
        ViewHold temp_ViewHold = null;
        if (null == contentView) {
            contentView = LayoutInflater.from(mActivity).inflate(R.layout.item_team_member_list,
                    null);
            temp_ViewHold = new ViewHold();

            temp_ViewHold.tv_team_member_item_name = (TextView) contentView
                    .findViewById(R.id.tv_team_member_item_name);
            temp_ViewHold.tv_team_member_item_name_2 = (TextView) contentView
                    .findViewById(R.id.tv_team_member_item_name_2);
            temp_ViewHold.tv_team_member_item_number = (TextView) contentView
                    .findViewById(R.id.tv_team_member_item_number);
            temp_ViewHold.imagev_team_member_list_icon = (ImageView) contentView
                    .findViewById(R.id.iv_team_member_list_icon);
            temp_ViewHold.imagev_team_member_call = (ImageView) contentView
                    .findViewById(R.id.imagev_team_member_call);
            temp_ViewHold.isOnlineView = (ImageView) contentView
                    .findViewById(R.id.image_team_member_isonline);
            contentView.setTag(temp_ViewHold);
        } else {
            temp_ViewHold = (ViewHold) contentView.getTag();
        }
        temp_ViewHold.tv_team_member_item_name.setText(item.nick);
        if (!TextUtils.isEmpty(item.username)) {
            temp_ViewHold.tv_team_member_item_name_2
                    .setVisibility(View.VISIBLE);
            temp_ViewHold.tv_team_member_item_name_2.setText("/"
                    + item.username);
        } else {
            temp_ViewHold.tv_team_member_item_name_2.setVisibility(View.GONE);
        }

        temp_ViewHold.tv_team_member_item_number.setText(item.mobile);

        if (isTeam) {
            temp_ViewHold.isOnlineView.setVisibility(View.VISIBLE);
        } else {
            temp_ViewHold.isOnlineView.setVisibility(View.GONE);
        }

//        temp_ViewHold.imagev_team_member_list_icon
//                .setDefaultImageResId(R.mipmap.default_hear_ico_re);
//        if (item.isOnline) {
//            temp_ViewHold.isOnlineView.setImageResource(R.drawable.online);
        if (!TextUtils.isEmpty(item.img)) {//判断是否有头像
//            temp_ViewHold.imagev_team_member_list_icon
//                    .setErrorImageResId(R.mipmap.default_hear_ico_re);
//            temp_ViewHold.imagev_team_member_list_icon.setImageUrl(
//                    item.img, imageLoader);
            Picasso.with(mActivity).load(item.img).error(R.mipmap.default_hear_ico_re).placeholder(R.mipmap.default_hear_ico_re).into(temp_ViewHold.imagev_team_member_list_icon);

            LogUtil.d(TAG, item.nick + "ioc:" + item.img);
        } else {
            LogUtil.d(TAG, item.nick + "ioc为空");
            temp_ViewHold.imagev_team_member_list_icon.setImageResource(R.mipmap.default_hear_ico_re);
        }

        if (item.online.equals("1")) {//判断是否在线
            temp_ViewHold.isOnlineView.setImageResource(R.mipmap.online);
        } else {
            temp_ViewHold.isOnlineView.setImageResource(R.mipmap.unonline);
        }

        //判断是否是预览界面
        if (isPreView) {
            temp_ViewHold.imagev_team_member_call.setVisibility(View.INVISIBLE);
        } else {
            temp_ViewHold.imagev_team_member_call.setVisibility(View.VISIBLE);
        }
        return contentView;
    }

    class ViewHold {
        TextView tv_team_member_item_name, tv_team_member_item_name_2,
                tv_team_member_item_number;
        ImageView imagev_team_member_call;
        ImageView isOnlineView;
        ImageView imagev_team_member_list_icon;
    }

    public void setDate(List<TeamMember>list) {
        Collections.sort(list);
        mList.clear();
        mList = list;
        notifyDataSetChanged();
    }

}
