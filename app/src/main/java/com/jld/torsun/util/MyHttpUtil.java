package com.jld.torsun.util;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.jld.torsun.MyApplication;
import com.jld.torsun.http.VolleyJsonUtil;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author liuzhi
 * @ClassName: HttpUtil
 * @Description: 网络工具类
 * @date 2015-12-5 上午10:21:33
 */
public class MyHttpUtil {

	// 判断是否连接WiFi
	public static Boolean isWifiConnect = false;
	// 判断是否连接Tucson
	public static Boolean isTucson = false;

	/**
	 * 判断到目标URL地址是否正常连接
	 */
	public static boolean isConnByHttp(String urlString) {
		boolean isConn = false;
		URL url;
		HttpURLConnection conn = null;
		try {
			url = new URL(urlString);
			conn = (HttpURLConnection) url.openConnection();
			// conn.setHeader("Range","bytes="+"");
			conn.setConnectTimeout(1000 * 8);
			if (conn.getResponseCode() == 200) {
				isConn = true;
			}
		} catch (MalformedURLException e) {
		} catch (IOException e) {
		} finally {
			conn.disconnect();
		}
		return isConn;
	}

	/**
	 * 获取WiFi名称
	 * 
	 */
    public static String getRegiesUpWifiName(Activity context){
        String wifiName = "";
        String qianzui = "";
        if (isWifiConn(context)){
            wifiName = getWifiName(context);
            if (isConnTorsun(context)){
                qianzui = "dyb_";
            }
        }
        return qianzui+wifiName;
    }

    public static String getWifiName(Context context) {
        String wifiName = "";
        WifiManager wm = (WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);
        WifiInfo wi = wm.getConnectionInfo();
        wifiName= wi.getSSID();
        LogUtil.d("StartActivity", "wifiName:"+wifiName);
        if (!TextUtils.isEmpty(wifiName)){
            wifiName = wifiName.replace("\"","");
        }
        return wifiName;
    }

	public static String getWifiName1(Context context) {
		String wifiName = "";
		WifiManager wm = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wi = wm.getConnectionInfo();
		 wifiName= wi.getSSID();
		LogUtil.d("StartActivity", "wifiName:"+wifiName);
		return wifiName;
	}

	public static boolean isMovieWifi(Context context) {
		String wifiName = "";
		WifiManager wm = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wi = wm.getConnectionInfo();
		wifiName= wi.getSSID();
		LogUtil.d("StartActivity", "wifiName:"+wifiName);
		return wifiName.startsWith("\"torsun_movie");
	}

	//判断是否连上了设备
	public static  boolean isConnTorsun(Activity activity){
		LogUtil.d("isConnTorsun","activity:"+activity);
		String wifiName = getWifiName1(activity);
		String wifi_name = ((MyApplication)activity.getApplication()).wifiName;
		if (TextUtils.isEmpty(wifiName)){
			LogUtil.d("----没有连wifi-----");
			return false;
		}
		if (TextUtils.isEmpty(wifi_name)){
			LogUtil.d("----没有连上了torsun-----");
			return false;
		}
		if (wifiName.equals(wifi_name)){
			//连上了设备wifi
			LogUtil.d("---------wifi名：" + wifiName);
			return true;
		}
		return false;
	}

	/**
	 * 判断网络是能否访问外网
	 */
	public static final boolean ping() {
		String result = null;
		try {
			String ip = "www.baidu.com";// ping 的地址，可以换成任何一种可靠的外网
			Process p = Runtime.getRuntime().exec("ping -c 2 -w 4 " + ip);// ping网址3次
			// ping的状态
			int status = p.waitFor();
			if (0 == status) {
				return true;
			}
		} catch (IOException e) {
		} catch (InterruptedException e) {
		} finally {
		}
		return false;
	}

	/**
	 * volley框架get方法
	 * 
	 * @param url
	 *            请求链接 用来获取请求队列
	 * @param vwi
	 *            数据返回接口
	 */
	public static final void VolleyGet(String url, RequestQueue mRequestQueue,
			final VolleyInterface vwi) {

		JsonObjectRequest getRequest = new JsonObjectRequest(
				Request.Method.GET, url, null,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						// display response
						vwi.win(response);
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						vwi.error(error);
					}
				}){
			@Override
			protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
				try {
					JSONObject jsonObject = new JSONObject(new String(response.data,"UTF-8"));
					return Response.success(jsonObject, HttpHeaderParser.parseCacheHeaders(response));
				} catch (UnsupportedEncodingException e) {
					return Response.error(new ParseError(e));
				} catch (Exception je) {
					return Response.error(new ParseError(je));
				}
			}
		};

		if (mRequestQueue != null) {
			mRequestQueue.add(getRequest);
		}
	}

	/**
	 * volley框架post请求
	 * 
	 * @param url
	 *            请求链接
	 * @param activity
	 *            用来获取请求队列
	 * @param params
	 *            请求参数
	 * @param vwi
	 *            数据返回接口
	 */
	public static final void VolleyPost(String url, Activity activity,
			Map<String, String> params, final VolleyInterface vwi) {

		JsonRequest<JSONObject> jsonRequest = new VolleyJsonUtil()
				.createJsonObjectRequest(Request.Method.POST, url, params,
						new Response.Listener<JSONObject>() {
							@Override
							public void onResponse(JSONObject response) {
								// TODO Auto-generated method stub
								vwi.win(response);
							}
						}, new Response.ErrorListener() {
							@Override
							public void onErrorResponse(VolleyError error) {
								// TODO Auto-generated method stub
								vwi.error(error);
							}
						});

		MyApplication ma = (MyApplication) activity.getApplication();

		RequestQueue mRequestQueue = ma.getRequestQueue();

		if (mRequestQueue != null) {
			mRequestQueue.add(jsonRequest);
		}
	}

	/**
	 * volley请求返回数据接口
	 * <p/>
	 * 晶凌达科技有限公司所有， 受到法律的保护，任何公司或个人，未经授权不得擅自拷贝。
	 * 
	 * @creator 单柏平 <br/>
	 * @create-time 2015-12-14 下午5:54:03
	 */
	public interface VolleyInterface {
		public void win(JSONObject response);

		public void error(VolleyError error);
	}

	/**
	 * 获取机器WiFi名称
	 * 
	 * @param context
	 */
//	public static void getWifiName(final Context context) {
//
//		RequestQueue mQueue = new Volley().newRequestQueue(context);
//		final SharedPreferences sp = context.getSharedPreferences(
//				Constats.SHARE_KEY, Context.MODE_PRIVATE);
//		StringRequest mStringRequest = new StringRequest(
//				Constats.GET_WIFI_INFO, new Response.Listener<String>() {
//
//					@Override
//					public void onResponse(String response) {
//						// TODO Auto-generated method stub
//						if (response.length() > 160) {// 如果请求被拦截
//							isTucson = true;
//							return;
//						}
//						// 从服务器获取WiFi名称
//						String wifiName = response.split("option ssid")[1]
//								.split("'")[0];
//
//						// 获取已连接WiFi名称
//						WifiManager wm = (WifiManager) context
//								.getSystemService(Context.WIFI_SERVICE);
//						WifiInfo wi = wm.getConnectionInfo();
//						String wifi_name = wi.getSSID();
//						wifiName = "\"" + wifiName + "\"";
//
//						// LogUtil.d("wifiname", "wifiName：" + wifiName +
//						// "		wifi_name:	"
//						// + wifi_name);
//						LogUtil.d("wifiName", "wifiName:" + wifiName
//								+ "	wifi_name" + wifi_name);
//						if (wifiName.equals(wifi_name)) {
//							isTucson = true;
//						} else {
//							isTucson = false;
//						}
//						// SharedPreferences.Editor editor = sp.edit();
//						// editor.putString("WIFINAME", wifiName);
//						// Intent intent = new Intent();
//						// intent.putExtra("", "");
//						// LogUtil.d("wifiname", "获取WiFi改变值：" + wifiName);
//					}
//				}, new Response.ErrorListener() {
//
//					@Override
//					public void onErrorResponse(VolleyError error) {
//						// TODO Auto-generated method stub
//						isTucson = false;
//					}
//				});
//		if (mQueue != null)
//			mQueue.add(mStringRequest);
//	}

	/**
	 * 判断WiFi是否连接
	 * 
	 * @param context
	 * @return 连接返回true 否则false
	 */
	public static void isWifiConnected(Context context) {

		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo wifiInfo = connectivityManager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (wifiInfo.isConnected()) {
			LogUtil.d("AudioPlayService", "WiFi已连接");
			isWifiConnect = true;
		} else {
			LogUtil.d("AudioPlayService", "WiFi未连接");
			isWifiConnect = false;
		}
	}

	/**
	 * 判断WiFi是否连接
	 */
	public static boolean isWifiConn(Context context) {
		boolean flag = false;
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo wifiInfo = connectivityManager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (wifiInfo.isConnected()) {
			flag = true;
		}
		return flag;
	}


	/********************************************************
	 * ******************下载视频****************************
	 *******************************************************/
	public static final int DOWNFILE = 0xfff;
	public static final int DOWN_ADDR_ERR =0xffe;

	private static String moviePath1 = Environment.getExternalStorageDirectory().getAbsolutePath() + "/TorsunMovie/";
	private static String moviePath2 = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)+"/TorsunMovie/";
	private static String moviePath3 = Environment.getExternalStorageDirectory().getPath()+ "/Movies/TorsunMovie/";

	//String url,String movieName,String fileName,Handler handler
	public static void parseStringFromUrl(String url,String movieName,Handler handler) {
		LogUtil.d("VodActivity","parseStringFromUrl");
		int index = url.lastIndexOf("/");
		String listPath = url.substring(0, index + 1);
		String urlString = url.substring(index);

		List<String> resultList = null;

		HttpClient httpClient = new DefaultHttpClient();
		HttpResponse res = null;
		InputStream in = null;
		try {
			res = httpClient.execute(new HttpGet(url));
			if (res != null) {
				resultList = new ArrayList<String>();
				in = res.getEntity().getContent();
				BufferedReader reader = new BufferedReader(new InputStreamReader(in));
				String line = "";
				while ((line = reader.readLine()) != null) {
					if (line.startsWith("#")) {

					} else if (line.length() > 0 && line.endsWith(".ts")) {
						resultList.add(listPath+line);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != in) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		//downFile(movieName,fileName,url,handler,resultList,urlString);
		if (null != resultList && resultList.size() > 0){
			//down2sdCard(movieName, urlString, url);
			String contentStr = getNativeM3u(url, resultList, "");
			write(movieName, urlString,contentStr);
			downFile(movieName,handler,resultList,urlString);
		}else {
			handler.sendEmptyMessage(DOWN_ADDR_ERR);
		}
		//return resultList;
	}

	//final String movieName, final String fileName, final String netUrl,Handler handler, final List<String> pathList,String localUrlName
	private static void downFile(final String movieName,Handler handler, final List<String> pathList,String localUrlName){

		LogUtil.d("VodActivity","downFile");
		int index = 0;
		int playSize;
		if (pathList.size()-1 > 5){
			playSize = 5;
		}else {
			playSize = pathList.size()-1;
		}

		boolean isPlay = true;
		File file1 = new File(moviePath2+movieName+"/");
		if (!file1.exists()){
			file1.mkdirs();
		}
		File file;
		InputStream input = null;
		OutputStream outputStream = null;
		for (String itemUrl: pathList) {
			if (isPlay && index >= playSize){
				isPlay = false;
				Message msg = Message.obtain();
				msg.what = DOWNFILE;
				Bundle data = new Bundle();
				data.putString("url",moviePath2+movieName+localUrlName);
				msg.setData(data);
				handler.sendMessage(msg);
			}
			file = new File(moviePath2+movieName+"/ItanoPart"+index+".ts");
			if (file.exists()){
				index++;
				continue;
			}
			index++;
			try {
				URL url = new URL(itemUrl);
				HttpURLConnection conn = (HttpURLConnection)url.openConnection();
				//取得inputStream，并进行读取
				input = conn.getInputStream();
				outputStream = new FileOutputStream(file);
				byte[] buff = new byte[4*1024];
				int len;
				while ((len = input.read(buff)) != -1){
					outputStream.write(buff,0,len);
				}
				outputStream.flush();
			}catch (Exception e){
				e.printStackTrace();
			}finally {
				if (outputStream != null){
					try {
						outputStream.close();
					}catch (IOException e){
						e.printStackTrace();
					}
				}
				if (input != null){
					try {
						input.close();
					}catch (IOException e){
						e.printStackTrace();
					}
				}
			}
		}
	}

	private static String getNativeM3u(String url,List<String> pathList,String saveFilePath) {
		LogUtil.d("VodActivity","getNativeM3u");
		HttpClient httpClient = new DefaultHttpClient();
		HttpResponse res = null;
		int num=0;
		//需要生成的目标buff
		StringBuffer buf = new StringBuffer();
		try {
			res = httpClient.execute(new HttpGet(url));
			if (res != null) {
				InputStream in = res.getEntity().getContent();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(in));
				String line = "";
				while ((line = reader.readLine()) != null) {
					if (line.length() > 0 && line.endsWith(".ts")) {
						//replce 这行的内容
						//buf.append("file:/"+saveFilePath+num+".ts\r\n");
						buf.append("ItanoPart"+num+".ts\r\n");
						num++;
					}else {
						buf.append(line+"\r\n");
					}
				}
				in.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return buf.toString();
	}

	/** * 将内容回写到文件中 * * @param filePath * @param content */
	private static void write(String movieName,String fileName, String content) {
		LogUtil.d("VodActivity","write");
		File file = new File(moviePath2+movieName);
		if (!file.exists()){
			file.mkdir();
		}
		BufferedWriter bw = null;
		try {
			// 根据文件路径创建缓冲输出流
			bw = new BufferedWriter(new FileWriter(moviePath2+movieName+fileName,false));
			// 将内容写入文件中
			bw.write(content);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// 关闭流
			if (bw != null) {
				try {
					bw.close();
				} catch (IOException e) {
					bw = null;
				}
			}
		}

	}

	private static void down2sdCard(String movieName,String fileName,String urlStr){
		LogUtil.d("VodActivity","down2sdCard:"+fileName);
		File file = new File(moviePath2+movieName);
		if (!file.exists()){
			file.mkdirs();
		}
		File file1 = new File(moviePath2+movieName+fileName);
		if (file1.exists()){
			//file1.delete();
			return;
		}
		try {
			// 构造URL
			URL url = new URL(urlStr);
			// 打开连接
			URLConnection con = url.openConnection();
			//获得文件的长度
			//int contentLength = con.getContentLength();
			//System.out.println("长度 :"+contentLength);
			// 输入流
			InputStream is = con.getInputStream();
			// 1K的数据缓冲
			byte[] bs = new byte[1024];
			// 读取到的数据长度
			int len;
			// 输出的文件流
			OutputStream os = new FileOutputStream(file1);
			// 开始读取
			while ((len = is.read(bs)) != -1) {
				os.write(bs, 0, len);
			}
			// 完毕，关闭所有链接
			os.close();
			is.close();
		} catch (Exception e) {
			LogUtil.d("VodActivity","down2sdCard---Exception"+e.toString());
			e.printStackTrace();
		}
	}

}
