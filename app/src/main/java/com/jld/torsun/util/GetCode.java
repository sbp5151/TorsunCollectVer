package com.jld.torsun.util;

import android.content.Context;
import android.telephony.TelephonyManager;

import com.jld.torsun.R;

public class GetCode {
	/**
	 * 获取国家编号
	 * 
	 * @return
	 */
//	public static String getCountryZipCode(Context context) {
//		String CountryID = "";
//		String CountryZipCode = "";
//
//		TelephonyManager manager = (TelephonyManager) context
//				.getSystemService(Context.TELEPHONY_SERVICE);
//		// getNetworkCountryIso
//		CountryID = manager.getSimCountryIso().toUpperCase();
//		LogUtil.d("GetCountryZipCode", "id:" + CountryID);
//
//		String[] rl = context.getResources().getStringArray(
//				R.array.CountryCodes);
//		for (int i = 0; i < rl.length; i++) {
//			String[] g = rl[i].split(",");
//			if (g[1].trim().equals(CountryID.trim())) {
//				CountryZipCode = g[0];
//				break;
//			}
//		}
//		return CountryZipCode;
//	}

}
