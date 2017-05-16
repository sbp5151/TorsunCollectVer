package com.jld.torsun.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jld.torsun.ActivityCollector;
import com.jld.torsun.R;
import com.jld.torsun.config.Config;
import com.jld.torsun.util.LanguageUtil;
import com.jld.torsun.util.ListenerUtil;

/**
 * 关于途胜
 * */
public class AboutTucsonActivity extends BaseActivity implements OnClickListener{

	private ImageView imagev_about_tucson_back;
	private RelativeLayout rl_normal_promble, rl_call_us, rl_welcome_page, rl_select_language;
	private TextView tv_aboutView;

	/**
	 * 完整地址为：http://www.torsun.cn/mobile/connect.php?lang=cn
	 * <p/>
	 * 后面中文的话为 cn
	 * 英文的话为 en
	 */
	private String callUsURL = "http://www.torsun.cn/mobile/connect.php?lang=";
	private String prombleURL = "http://www.torsun.cn/mobile/question.php?lang=";
	//中文为 cn  英文为 en
	private String country;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about_tucson);
		ActivityCollector.addActivity(this);
		LinearLayout linearLayout = (LinearLayout) findViewById(R.id.ll_about_layout);
		linearLayout.setLongClickable(true);
		setFlingView(linearLayout);

		initView();
		if (LanguageUtil.isZh(this)) {
			country = "cn";
		} else {
			country = "en";
		}

//		final SharedPreferences sharedPreferences=getSharedPreferences("language_choice", Context.MODE_PRIVATE);
//		final int id = sharedPreferences.getInt("language_id",0);
//		switch (id){
//			case 0://默认，跟随系统语言
//				if (isZh()){
//					country = "cn";
//				}else {
//					country = "en";
//				}
//				break;
//			case 1://中文简体
//				country = "cn";
//				break;
//			case 2://English
//				country = "en";
//				break;
//			default:
//				country = "en";
//				break;
//		}

	}

//	//判断当前系统语言是否为中文
//	private boolean isZh() {
//		Locale locale = getResources().getConfiguration().locale;
//		String language = locale.getLanguage();
//		if (language.endsWith("zh"))
//			return true;
//		else
//			return false;
//	}

	private void initView() {
		imagev_about_tucson_back = (ImageView) findViewById(R.id.imagev_about_tucson_back);
		tv_aboutView = (TextView) findViewById(R.id.tv_title_sure);
		//String app=getResources().getString(R.string.app_name_show);
		//String name=AppUtils.getVersionName(this);
		//tv_aboutView.setText(app+name);
		rl_normal_promble = (RelativeLayout) findViewById(R.id.rl_normal_promble);
		rl_call_us = (RelativeLayout) findViewById(R.id.rl_call_us);
		rl_welcome_page = (RelativeLayout) findViewById(R.id.rl_welcome_page);
		rl_select_language = (RelativeLayout) findViewById(R.id.rl_select_language);
//		imagev_about_tucson_back.setOnClickListener(this);
//		rl_normal_promble.setOnClickListener(this);
//		rl_call_us.setOnClickListener(this);
//		rl_welcome_page.setOnClickListener(this);
		ListenerUtil.setListener(this, imagev_about_tucson_back,
				rl_normal_promble, rl_call_us, rl_welcome_page, rl_select_language);
	}

	@Override
	public void onClick(View view) {
		int id = view.getId();
		switch (id) {
			case R.id.imagev_about_tucson_back:
				onBackPressed();
				break;
			case R.id.rl_normal_promble:
				Intent normal_intent = new Intent();
				normal_intent.setClass(AboutTucsonActivity.this, WebActivity.class);
				normal_intent.putExtra(Config.TYPE, Config.TYPE_NORMAL_PROMBLE);
				normal_intent.putExtra("url", prombleURL + country);
				startActivity(normal_intent);
				overridePendingTransition(R.anim.right_in, R.anim.left_out);
				break;
			case R.id.rl_call_us:
				Intent callus_intent = new Intent();
				callus_intent.setClass(AboutTucsonActivity.this, WebActivity1.class);
				callus_intent.putExtra(Config.TYPE, Config.TYPE_CALL_US);
				callus_intent.putExtra("url", callUsURL + country);
				startActivity(callus_intent);
				overridePendingTransition(R.anim.right_in, R.anim.left_out);
				break;
			case R.id.rl_welcome_page:

				break;
			case R.id.rl_select_language:
				toActivity(SelectLanguageActivity.class);
				break;
		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		ActivityCollector.removeActivity(this);
	}
}
