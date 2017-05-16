package com.jld.torsun.util.imagecache;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.jld.torsun.util.LogUtil;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;

@SuppressLint("NewApi")
public class DiskUtil {

	private DiskLruCache mDiskLruCache;

	private static DiskUtil diskUtil;

	private DiskUtil(Context context) {
		try {
			mDiskLruCache = DiskLruCache.open(
					getDiskCacheDir(context, "bitmap"),
					AppUtils.getAppVersionCode(context), 1, 50* 1024 * 1024);
			LogUtil.i("------mDiskLruCache:"+mDiskLruCache);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static DiskUtil getInstance(Context context) {
		if (diskUtil == null) {
			diskUtil = new DiskUtil(context);
		}
		return diskUtil;
	}

	/** 存 */
	public boolean put(String url, Bitmap bitmap) {
		if (null==mDiskLruCache) {
			return false;
		}

		String key = hashKeyForDisk(url);
		OutputStream outputStream = null;
		DiskLruCache.Editor editor = null;
		try {
			editor = mDiskLruCache.edit(key);
			outputStream = editor.newOutputStream(0);
			outputStream.write(myBitmapUtils.compressBitmap(bitmap));
			editor.commit();
			mDiskLruCache.flush();
			outputStream.flush();
		} catch (IOException e) {
			try {
				editor.abort();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		} finally {
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return false;
	}

	/** 取*/
	public Bitmap get(String url) {
		if (null==mDiskLruCache) {
			return null;
		}

		String key = hashKeyForDisk(url);
		InputStream inputStream = null;
		Bitmap bitmap = null;
		try {
			DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
			if (snapshot != null) {
				inputStream = snapshot.getInputStream(0);
				bitmap = myBitmapUtils.getBitmap(inputStream);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return bitmap;
	}

	/**获取缓存地址*/
	public File getDiskCacheDir(Context context, String uniqueName) {
		String cachePath;
		if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())
				|| !Environment.isExternalStorageRemovable()) {
			cachePath = context.getExternalCacheDir().getPath();
		} else {
			cachePath = context.getCacheDir().getPath();
		}
		return new File(cachePath + File.separator + uniqueName);
	}

	/** MD5编码 编码后的字符串是唯一的 */
	public String hashKeyForDisk(String key) {
		String cacheKey;
		try {
			final MessageDigest mDigest = MessageDigest.getInstance("MD5");
			mDigest.update(key.getBytes());
			cacheKey = bytesToHexString(mDigest.digest());
		} catch (NoSuchAlgorithmException e) {
			cacheKey = String.valueOf(key.hashCode());
		}
		return cacheKey;
	}

	private String bytesToHexString(byte[] bytes) {
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < bytes.length; i++) {
			String hex = Integer.toHexString(0xFF & bytes[i]);
			if (hex.length() == 1) {
				sb.append('0');
			}
			sb.append(hex);
		}
		return sb.toString();
	}

}
