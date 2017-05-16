package com.jld.torsun.activity;

import android.content.Context;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;

import android.view.View;
import android.view.ViewGroup;

import android.widget.BaseAdapter;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import android.view.View.OnClickListener;

import com.jld.torsun.ActivityCollector;
import com.jld.torsun.R;
import com.jld.torsun.util.AndroidUtil;
import com.jld.torsun.util.DensityUtil;
import com.jld.torsun.util.LogUtil;

import com.jld.torsun.view.SingleView;

import java.util.Locale;

public class SelectLanguageActivity extends BaseActivity{

    private TextView languageTitle,languageTitleSure;
    private ImageView back;
    private ListView listView;
    private String[] mData;

    private Context mContext;

    private SharedPreferences sp;

    private View titleView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_language);
        mContext=this;
        sp=mContext.getSharedPreferences("language_choice", Context.MODE_PRIVATE);
        initView();
        listener();
    }

    private boolean isfristFocus = true;

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (isfristFocus && hasFocus){
            isfristFocus=false;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT){
                float height= AndroidUtil.getStatusHeight(this);
                LogUtil.d("--------------状态栏的高度为:" + height);

                int viewHeight = DensityUtil.px2dip(this, (216f - height * 2));
                LogUtil.d("--------------viewHeight的高度:" + viewHeight);
                LinearLayout.LayoutParams params =(LinearLayout.LayoutParams)titleView.getLayoutParams();
                params.height=viewHeight;
                titleView.setLayoutParams(params);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isfristFocus=true;
    }

    private void initView() {
        final int id =sp.getInt("language_id", 0);
        titleView=findViewById(R.id.select_language_title);
        back=(ImageView)titleView.findViewById(R.id.title_image_title_back);
        languageTitleSure=(TextView)titleView.findViewById(R.id.tv_title_sure);
        languageTitle=(TextView)titleView.findViewById(R.id.tv_title_title);
        languageTitle.setText(R.string.select_language);
        listView=(ListView)findViewById(R.id.select_language_list);
        listView.setDividerHeight(0);
        mData=getResources().getStringArray(R.array.language);
        MyLanguageAdapter adapter=new MyLanguageAdapter();
        listView.setAdapter(adapter);
        listView.setItemChecked(id, true);
        setFlingView(listView);
    }

    private void listener() {
        back.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        languageTitleSure.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = listView.getCheckedItemPosition();

                if (ListView.INVALID_POSITION != position) {
                    //ToastUtil.showToast(mContext,"qweq:"+position,3000);
                    selectLanguage(position);
                }

            }
        });
    }

    private int languageId;

    private void selectLanguage(int position) {

        final int id =sp.getInt("language_id",0);
        switch (position){
            case 0://默认，跟随系统语言
                languageId=0;
                break;
            case 1://中文简体
                languageId=1;
                break;
            case 2://English
                languageId=2;
                break;
            default:
                languageId=0;
                break;
        }
        sp.edit().putInt("language_id",languageId).commit();
        updataLocale();
    }

    public void updataLocale(){
        int id = sp.getInt("language_id",0);
        Resources resources = getResources();//获得res资源对象
        DisplayMetrics dm = resources.getDisplayMetrics();//获得屏幕参数：主要是分辨率，像素等。
        Configuration config = resources.getConfiguration();//获得设置对象
        switch (id){
            case 0://默认，跟随系统语言
                config.locale=Locale.getDefault();
                break;
            case 1://中文简体
                config.locale = Locale.SIMPLIFIED_CHINESE;
                break;
            case 2://English
                config.locale=Locale.ENGLISH;
                break;
            default:
                config.locale=Locale.getDefault();
                break;
        }
        resources.updateConfiguration(config, dm);
        ActivityCollector.finishAll();
        this.finish();
        toActivity(MainFragment.class);
//        Intent intent=new Intent();
//        intent.setClass(this,MainFragment.class);

    }

    class MyLanguageAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return mData.length;
        }

        @Override
        public Object getItem(int position) {
            return mData[position];
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            SingleView singleView;
            if (convertView == null){
                singleView = new SingleView(mContext);
                convertView = singleView;
            }else {
                singleView =(SingleView) convertView;
            }
            singleView.setTitle(mData[position]);

            singleView.setViewInvisibility(position < mData.length-1);
            return convertView;
        }


    }

}
