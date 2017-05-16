package com.jld.torsun.util.imagecache;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.util.LruCache;

import com.android.volley.toolbox.ImageLoader.ImageCache;

@SuppressLint("NewApi")
public class MyBitMapCache implements ImageCache {

	private LruCache<String, Bitmap> lruCache;

	private static MyBitMapCache myBitMapCache;
	
	private MyBitMapCache() {
		int maxSize = 10 * 1024 * 1024;
		lruCache = new LruCache<String, Bitmap>(maxSize) {
			@Override
			protected int sizeOf(String key, Bitmap value) {
				return value.getRowBytes() * value.getHeight();
			}
		};
	}
	
	public static MyBitMapCache getInstance(){
		if (myBitMapCache==null) {
			myBitMapCache=new MyBitMapCache();
		}
		return myBitMapCache;
	}
	
	@Override
	public Bitmap getBitmap(String arg0) {
		return lruCache.get(arg0);
	}

	@Override
	public void putBitmap(String arg0, Bitmap arg1) {
		lruCache.put(arg0, arg1);
	}

}
