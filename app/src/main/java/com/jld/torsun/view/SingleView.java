package com.jld.torsun.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jld.torsun.R;

/**
 * Created by lz on 2016/1/28.
 */
public class SingleView extends LinearLayout implements Checkable{

    private TextView mText;
    private CheckBox mCheckBox;
    private View bottomView;
    public SingleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context);
    }

    public SingleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public SingleView (Context context) {
        super(context);
        initView(context);
    }

    private void initView(Context context){
        // 填充布局
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.item_select_language_layout , this, true);
        mText = (TextView) v.findViewById(R.id.item_language_text);
        mCheckBox = (CheckBox) v.findViewById(R.id.item_language_cb);
        bottomView=v.findViewById(R.id.item_language_bottom_view);
    }
    @Override
    public void setChecked(boolean checked) {
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

    public void setViewInvisibility(boolean isVisibility){
        if (isVisibility){
            bottomView.setVisibility(VISIBLE);
        }else {
            bottomView.setVisibility(INVISIBLE);
        }

    }
}
