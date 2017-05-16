package com.jld.torsun.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jld.torsun.R;

/**
 * Created by lz on 2016/1/28.
 */
public class SingleChoiceView extends LinearLayout implements Checkable{

    private CheckBox mCheckBox;
    private ImageView mImageView;
    private TextView mText;
    private TextView mTime;
    private ImageView endImageView;
//    private View bottomView;

    public SingleChoiceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context);
    }

    public SingleChoiceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public SingleChoiceView(Context context) {
        super(context);
        initView(context);
    }

    private void initView(Context context){
        // 填充布局
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.item_single_choice_layout , this, true);
        mCheckBox = (CheckBox) v.findViewById(R.id.item_single_choice_cb);
        mImageView = (ImageView) v.findViewById(R.id.item_single_choice_iv);
        mText = (TextView) v.findViewById(R.id.item_single_choice_name_tv);
        mTime = (TextView) v.findViewById(R.id.item_single_choice_time_tv);
        endImageView = (ImageView) v.findViewById(R.id.item_single_choice_end_iv);
//        bottomView=v.findViewById(R.id.item_language_bottom_view);
    }
    @Override
    public void setChecked(boolean checked) {
        if (checked){
            mImageView.setImageResource(R.mipmap.trou_team_list_able);
            mText.setTextColor(getResources().getColor(R.color.backgroud_red));
            endImageView.setVisibility(VISIBLE);
        }else {
            mImageView.setImageResource(R.mipmap.trou_team_list_enable);
            mText.setTextColor(getResources().getColor(R.color.item_content_black));
            endImageView.setVisibility(INVISIBLE);
        }
        mCheckBox.setChecked(checked);
    }

    @Override
    public boolean isChecked() {
        return mCheckBox.isChecked();
    }

    @Override
    public void toggle() {
        mCheckBox.toggle();
    }

    public void setTitle(String text){
        mText.setText(text);
    }

    public void setTime(String text){
        mTime.setText(text);
    }

//    public void setViewInvisibility(boolean isVisibility){
//        if (isVisibility){
//            bottomView.setVisibility(VISIBLE);
//        }else {
//            bottomView.setVisibility(INVISIBLE);
//        }
//
//    }
}
