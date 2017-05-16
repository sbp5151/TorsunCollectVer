package com.jld.torsun.http;

import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * http工具类          2015.9.29       xiang.he
 * */
public class VolleyJsonUtil {

	/** 创建一个普通的JsonRequest */
	public static JsonRequest createJsonObjectRequest(int method, String url,
			Map<String, String> params, Response.Listener<JSONObject> listen,
			Response.ErrorListener errorListener) {
		JSONObject jsonObject = new JSONObject(params);
		JsonRequest<JSONObject> jsonRequest = new JsonObjectRequest(method,
				url, jsonObject, listen, errorListener) {
			@Override
			public Map<String, String> getHeaders() {
				HashMap<String, String> headers = new HashMap<String, String>();
				headers.put("Accept", "application/json");
				headers.put("Content-Type", "application/json; charset=UTF-8");
				return headers;
			}
		};
		return jsonRequest;
	}
	
	/** 创建一个普通的JsonRequest */
	public static JsonRequest createJsonObjectRequest(int method, String url,
			Response.Listener<JSONObject> listen,
			Response.ErrorListener errorListener) {
		JSONObject jsonObject = new JSONObject();
		JsonRequest<JSONObject> jsonRequest = new JsonObjectRequest(method,
				url,jsonObject, listen, errorListener) {
			@Override
			public Map<String, String> getHeaders() {
				HashMap<String, String> headers = new HashMap<String, String>();
				headers.put("Accept", "application/json");
				headers.put("Content-Type", "application/json; charset=UTF-8");
				return headers;
			}
		};
		return jsonRequest;
	}
	
	/** 创建一个普通的JsonArrayRequest */
	public static JsonRequest createJsonArrayRequest(int method, String url,
			Map<String, String> params, Response.Listener<JSONArray> listen,
			Response.ErrorListener errorListener) {
		JSONObject jsonObject = new JSONObject(params);
		JsonRequest<JSONArray> jsonRequest = new MyjsonPostRequest(method,url,jsonObject,listen,errorListener) {
			@Override
			public Map<String, String> getHeaders() {
				HashMap<String, String> headers = new HashMap<String, String>();
				headers.put("Accept", "application/json");
				headers.put("Content-Type", "application/json; charset=UTF-8");
				return headers;
			}
		};
		return jsonRequest;
	}

}
