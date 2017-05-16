package com.jld.torsun.util.image;

import android.app.Activity;
import android.content.Intent;
import android.os.Environment;
import android.widget.Toast;

/**
 * 安卓图片工具
 * */
public class ImageUtil {
	
	public static final int  REQUEST_CODE_PICK_IMAGE = 1;
	public static final int  REQUEST_CODE_CAPTURE_CAMEIA = 2;
	
	public static void getImageFromAlbum(Activity activity) {  
	       Intent intent = new Intent(Intent.ACTION_PICK);  
	       intent.setType("image/*");//相片类型  
	       activity.startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);  
	   }  
	
	
	public static void getImageFromCamera(Activity activity) {  
	       String state = Environment.getExternalStorageState();  
	       if (state.equals(Environment.MEDIA_MOUNTED)) {  
	           Intent getImageByCamera = new Intent("android.media.action.IMAGE_CAPTURE");     
	           activity.startActivityForResult(getImageByCamera, REQUEST_CODE_CAPTURE_CAMEIA);  
	       }  
	       else {  
	           Toast.makeText(activity, "请确认已经插入SD卡", Toast.LENGTH_LONG).show();  
	       }  
	   }  
	
	

}
