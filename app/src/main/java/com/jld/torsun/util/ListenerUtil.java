package com.jld.torsun.util;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton.OnCheckedChangeListener;

/**
 * 设置监听（不用写重复代码，麻烦）
 * 2015.9.8   xiang.he
 * */
public class ListenerUtil {
	
	public static void setListener(OnClickListener listen,View... views){
		for(View view:views){
			view.setOnClickListener(listen);
		}
	}
	
	public static void setListener(OnCheckedChangeListener listen,CheckBox... boxs){
		for(CheckBox box:boxs){
			box.setOnCheckedChangeListener(listen);
		}
	}

}
