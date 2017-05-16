package com.jld.torsun.activity;

import java.io.File;

import com.jld.torsun.R;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout.LayoutParams;

/**
 * 
* @ClassName: ChosePhoneActivity 
* @Description: 设置用户头像 
* @author liuzhi
* @date 2015-12-1 下午2:43:57
 */
public class ChosePhoneActivity extends BaseActivity{
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_phote_chose);
		show_SetPortrait_Dialog();
	}
	
	private Dialog dialog;
	private void show_SetPortrait_Dialog() {
		// 点击头像图片的点击事件，弹出更改头像的对话框,设置头像
		View view = getLayoutInflater().inflate(R.layout.dialog_photo_choose,
				null);
		dialog = new Dialog(this,R.style.CustomDialog);
		dialog.setContentView(view, new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
		Window window = dialog.getWindow();
		// 设置显示动画
		window.setWindowAnimations(R.style.main_menu_animstyle);
		WindowManager.LayoutParams wl = window.getAttributes();

		wl.width = ViewGroup.LayoutParams.MATCH_PARENT;
		wl.height = ViewGroup.LayoutParams.MATCH_PARENT;

		// 设置显示位置
		dialog.onWindowAttributesChanged(wl);
		// 设置点击外围解散
		dialog.setCanceledOnTouchOutside(true);
		dialog.show();
	}
	
	
	//从图库选取头像
		public void myset_portrait_picture(View v){
			Intent intent1 = new Intent(Intent.ACTION_PICK, null);
			intent1.setDataAndType(
					MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
			startActivityForResult(intent1, 1);
			dialog.dismiss();
		}
		//拍照更改头像
		public void myset_portrait_camera(View v){
			Intent intent2 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			intent2.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(
					Environment.getExternalStorageDirectory(), "head.jpg")));
			startActivityForResult(intent2, 2);// 采用ForResult打开
			dialog.dismiss();
		}

}
