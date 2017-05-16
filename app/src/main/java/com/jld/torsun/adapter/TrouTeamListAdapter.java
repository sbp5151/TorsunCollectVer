package com.jld.torsun.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.jld.torsun.R;
import com.jld.torsun.modle.TrouTeam;

import java.util.List;
/**
 * 
* @ClassName: TrouTeamListAdapter 
* @Description: 旅游团历史记录列表适配
* @author liuzhi
* @date 2015-12-2 上午9:43:06
 */
public class TrouTeamListAdapter extends BaseAdapter{

	private List<TrouTeam> mList;
	private Activity mActivity;
	private LayoutInflater mInflater;
	
	public TrouTeamListAdapter(Activity activity, List<TrouTeam> list) {
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
	public TrouTeam getItem(int postion) {
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
		ViewHold temp_ViewHold = null;
		if (null == contentView) {
			contentView = mInflater.inflate(R.layout.item_trou_team_layout, null);
			temp_ViewHold = new ViewHold();
			
			temp_ViewHold.tv_trouteam_item_name = (TextView) contentView
					.findViewById(R.id.tv_trouteam_item_name);
			temp_ViewHold.tv_trouteam_item_time = (TextView) contentView
					.findViewById(R.id.tv_trouteam_item_time);
			temp_ViewHold.tv_trouteam_item_member=(TextView) contentView.findViewById(R.id.tv_trouteam_item_member);
			temp_ViewHold.imagev_trouteam_list_icon = (ImageView) contentView.findViewById(R.id.imagev_trouteam_list_icon);
			temp_ViewHold.deleteButton=(Button)contentView.findViewById(R.id.btn_trouteam_item_delete);
			contentView.setTag(temp_ViewHold);
		} else {
			temp_ViewHold = (ViewHold) contentView.getTag();
		}
		final TrouTeam item = mList.get(postion);
		if (postion==index) {
			if (temp_ViewHold.deleteButton.getVisibility()==View.VISIBLE) {
				temp_ViewHold.deleteButton.setVisibility(View.GONE);
				
			}
		}
		temp_ViewHold.tv_trouteam_item_name.setText(item.name);
		temp_ViewHold.tv_trouteam_item_time.setText(item.createtime);
		temp_ViewHold.tv_trouteam_item_member.setText(item.show);
		if (0==postion){
			temp_ViewHold.imagev_trouteam_list_icon.setImageResource(R.mipmap.trou_team_list_able);
		}else {
			temp_ViewHold.imagev_trouteam_list_icon.setImageResource(R.mipmap.trou_team_list_enable);
		}
		return contentView;
	}

	class ViewHold {
		TextView tv_trouteam_item_name,tv_trouteam_item_time,tv_trouteam_item_member;
		ImageView imagev_trouteam_list_icon;
		Button deleteButton;
	}
	
	private int index=-1;
	
	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public void setData(List<TrouTeam> list){
		mList=list;
		notifyDataSetChanged();
	}
}
