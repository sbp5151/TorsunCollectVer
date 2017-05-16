package com.jld.torsun.util.imagecache;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class MyRequestQueue {

	private static RequestQueue queue;
	
	public static RequestQueue getInstance(Context context){
		if (queue==null) {
			queue=Volley.newRequestQueue(context);
		}
		return queue;
	}
}
