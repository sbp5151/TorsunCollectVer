package com.jld.torsun.util.imagecache;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.util.LruCache;

import com.android.volley.toolbox.ImageLoader.ImageCache;
import com.jld.torsun.util.ImageUtil;

@SuppressLint("NewApi")
public class MyRunAndDiskCache implements ImageCache {

	private DiskUtil diskUtil;
	private MyBitMapCache myBitMapCache;
	private Context context;

	//是否进行模糊处理
	private boolean fastblur = false;

	public MyRunAndDiskCache(Context context,boolean fastblur) {
		diskUtil = DiskUtil.getInstance(context);
		myBitMapCache = MyBitMapCache.getInstance();
		this.context=context;
		this.fastblur=fastblur;
	}

	@Override
	public Bitmap getBitmap(String arg0) {
		if (null == diskUtil || null == myBitMapCache) {
			return null;
		}
		Bitmap bitmap = null;
		bitmap = myBitMapCache.getBitmap(arg0);
		if (null == bitmap) {
			bitmap = diskUtil.get(arg0);
			if (null != bitmap) {// 从本地磁盘中获取
				// 保存到内存缓存中
                myBitMapCache.putBitmap(arg0, bitmap);
			}// 从网络中获取
		}// 从内存缓存中获取
		if (fastblur){//模糊处理
			bitmap = ImageUtil.fastblur(bitmap, 25);
		}
		return bitmap;


	}

	@Override
	public void putBitmap(String url, Bitmap bitmap) {
		if (null == diskUtil || null == myBitMapCache) {
			return;
		}
		// 保存到内存缓存中
		myBitMapCache.putBitmap(url, bitmap);
		// 保存到本地磁盘缓存中
		diskUtil.put(url, bitmap);
	}
}
