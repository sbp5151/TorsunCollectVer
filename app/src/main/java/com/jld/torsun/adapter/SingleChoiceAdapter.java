package com.jld.torsun.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.jld.torsun.modle.TrouTeam;
import com.jld.torsun.util.TimeUtil;
import com.jld.torsun.view.SingleChoiceView;

import java.util.List;

/**
 * Created by lz on 2016/5/23.
 */
public class SingleChoiceAdapter extends BaseAdapter {

    private Context mContext;
    private List<TrouTeam> mList;

    public SingleChoiceAdapter (Context context,List<TrouTeam> list){
        mContext = context;
        mList = list;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SingleChoiceView singleView;
        if (null == convertView){
            singleView = new SingleChoiceView(mContext);
            convertView = singleView;
        }else {
            singleView = (SingleChoiceView) convertView;
        }
        final TrouTeam trouTeam = mList.get(position);
        singleView.setTitle(trouTeam.name);
        singleView.setTime(TimeUtil.timeFormatOther(trouTeam.createtime));
        return convertView;
    }
}
