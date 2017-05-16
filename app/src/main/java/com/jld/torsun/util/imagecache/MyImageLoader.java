package com.jld.torsun.util.imagecache;

import android.content.Context;

import com.android.volley.toolbox.ImageLoader;

public class MyImageLoader {

	private static ImageLoader imageLoader1;
	private static ImageLoader imageLoader2;

	public static ImageLoader getInstance(Context context) {
		if (imageLoader1 == null) {
			imageLoader1 = new ImageLoader(MyRequestQueue.getInstance(context),
					new MyRunAndDiskCache(context,false));
		}
		return imageLoader1;
	}

	public static ImageLoader getInstance(Context context ,boolean fastblur) {
		if (imageLoader2 == null) {
			imageLoader2 = new ImageLoader(MyRequestQueue.getInstance(context),
					new MyRunAndDiskCache(context,fastblur));
		}
		return imageLoader2;
	}
}
