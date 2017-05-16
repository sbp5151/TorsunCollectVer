package com.jld.torsun.activity.mediaPlayer;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.jld.torsun.MyApplication;
import com.jld.torsun.R;
import com.jld.torsun.activity.fragment.MenuCallback;
import com.jld.torsun.modle.MovieADBean;
import com.jld.torsun.modle.MovieBean;
import com.jld.torsun.modle.MovieConstats;
import com.jld.torsun.modle.MovieTypeBean;
import com.jld.torsun.util.DensityUtil;
import com.jld.torsun.util.LogUtil;
import com.jld.torsun.util.MyHttpUtil;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class MediaPlayerFragment extends Fragment {

    public static final String TAG = "MediaPlayerFragment";

    private String testMoviewUrl = "http://192.168.1.1:81/mv/list.php";

    private GridView listView;

    private MovieGridAdapter adapter;
    private List<MovieBean> mList = new ArrayList<>();
    private List<MovieADBean> mADList = new ArrayList<>();
    private LinkedList<MovieTypeBean> mTypeList = new LinkedList<>();

    private View mediaPlayerView;
    private View topView;
    private View topRightView;
    private TextView topTextView;
    private ImageButton leftBackView;
    private TextView titleRightView;
    private ImageButton topRightIv;

    private ProgressBar loadingBar;
    private TextView loadingText;
    private ImageView loadingIMG;
    private MenuCallback mCallback;

    private static final int LOADINGSUCC = 0xaa0;
    private static final int LOADINGERR = 0xaa1;
    private static final int RELOADING = 0xaa2;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int msgType = msg.what;
            switch (msgType) {
                case LOADINGSUCC:
                    showLoadingSucc();
                    break;
//                case RELOADING:
//                    showLoading();
//                    break;
                case LOADINGERR:
                    showLoadingErr();
                    break;
                default:
                    break;
            }
        }
    };

    private RequestQueue mRequestQueue;

    private PopupWindow popupWindow;
    private ListView typeListView;
    private MovieTypeAdapter typeAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mediaPlayerView = inflater.inflate(R.layout.activity_media_player_grid, container, false);
        mRequestQueue = ((MyApplication) getActivity().getApplication()).getRequestQueue();
        initView();
        return mediaPlayerView;
    }

    private void initPopupWindowView() {
        if (popupWindow == null) {
            View v = LayoutInflater.from(getContext()).inflate(R.layout.list_movie_type_popview,null,false);
            // 创建PopupWindow实例,(View,width,height)
            popupWindow = new PopupWindow(v, DensityUtil.dip2px(getContext(),99), ViewGroup.LayoutParams.WRAP_CONTENT);
            popupWindow.setOutsideTouchable(true);
            popupWindow.setBackgroundDrawable(getResources().getDrawable(R.mipmap.a_alpha));
            popupWindow.setTouchInterceptor(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_OUTSIDE){
                        popupWindow.dismiss();
                        return true;
                    }
                    return false;
                }
            });
            // 自定义view添加触摸事件
//            v.setOnTouchListener(new View.OnTouchListener() {
//                @Override
//                public boolean onTouch(View v, MotionEvent event) {
//                    if (popupWindow != null && popupWindow.isShowing()) {
//                        popupWindow.dismiss();
//                    }
//                    return false;
//                }
//            });
            if (typeAdapter == null) {
                typeAdapter = new MovieTypeAdapter(getActivity(), mTypeList);
            }
            if (typeListView == null) {
                typeListView = (ListView) v.findViewById(R.id.movie_type_list);
                typeListView.setOnItemClickListener(typeItemClickListener);
                typeListView.setAdapter(typeAdapter);
            }
        }
        typeAdapter.setData(mTypeList);
    }

    //执行重新加载的方法
    private void reLoading() {
        showLoading();
        loadingData();
    }

    //加载中
    private void showLoading() {
        loadingBar.setVisibility(View.VISIBLE);
        loadingText.setVisibility(View.GONE);
        loadingIMG.setVisibility(View.GONE);
    }

    //加载成功
    private void showLoadingSucc() {
        listView.setVisibility(View.VISIBLE);
        loadingBar.setVisibility(View.GONE);
        loadingText.setVisibility(View.GONE);
        loadingIMG.setVisibility(View.GONE);
        adapter.setData(mList);
    }

    //加载失败
    private void showLoadingErr() {
        loadingText.setVisibility(View.VISIBLE);
        loadingIMG.setVisibility(View.VISIBLE);
        listView.setVisibility(View.GONE);
        loadingBar.setVisibility(View.GONE);
    }

    private void initView() {
        //初始化头部include
        topView = mediaPlayerView.findViewById(R.id.media_play_grid_top_view);
        topTextView = (TextView) topView.findViewById(R.id.tv_title_message_center_title);
        topTextView.setText(R.string.movie_title_list_tv);
        leftBackView = (ImageButton) topView.findViewById(R.id.iv_title_message_center_back);
        leftBackView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.callback();
            }
        });
        topRightView = topView.findViewById(R.id.ll_right_view);
        titleRightView =(TextView) topView.findViewById(R.id.tv_title_message_center_release);
        titleRightView.setVisibility(View.GONE);
//        titleRightView.setText(R.string.movie_type);
        topRightIv = (ImageButton) topView.findViewById(R.id.iv_title_message_center_edit);
        topRightIv.setBackgroundResource(R.mipmap.ic_movie_more);
        topRightIv.setVisibility(View.VISIBLE);
        topRightView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (popupWindow != null && popupWindow.isShowing()) {
                    popupWindow.dismiss();
                    return;
                } else {
                    initPopupWindowView();
                    popupWindow.showAsDropDown(v, 0, 5);
                }
            }
        });
        //初始化其他控件
        loadingBar = (ProgressBar) mediaPlayerView.findViewById(R.id.pb_movie_grid_loading);
        loadingIMG = (ImageView) mediaPlayerView.findViewById(R.id.iv_movie_grid_loading_err);
        loadingIMG.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                reLoading();
                return false;
            }
        });
        loadingText = (TextView) mediaPlayerView.findViewById(R.id.tv_movie_grid_loading_err);
        loadingText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reLoading();
            }
        });
        listView = (GridView) mediaPlayerView.findViewById(R.id.gv_movie_grid);
        adapter = new MovieGridAdapter(getActivity(), mList);
        listView.setOnItemClickListener(itemClickListener);
        listView.setAdapter(adapter);
        adapter.setData(mList);
    }

    private OnItemClickListener typeItemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            LogUtil.d(TAG,"typeItemClickListener");
            if (popupWindow != null && popupWindow.isShowing()) {
                popupWindow.dismiss();
            }
            movieTypeSele(mTypeList.get(position).getId());
//            MovieTypeBean movieTypeBean = mTypeList.get(position);
//            if ("-1".equals(movieTypeBean.getId())){
//                LogUtil.d(TAG,"-1 : " + movieTypeBean.getName());
//            }else {
//                LogUtil.d(TAG,movieTypeBean.getId() + " : " + movieTypeBean.getName());
//            }
        }
    };

    //列表单个监听
    private OnItemClickListener itemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            gotoActivity(adapter.getItem(position));
        }
    };

    //跳转到播放视频界面
    private void gotoActivity(MovieBean item) {
        if (item == null || TextUtils.isEmpty(item.getUrl()))
            return;

        upDatePlayNum(item.getId());
        Intent intent = new Intent();
        Random r = new Random();
        if (item.getUrl().endsWith(".mp4")) {
            intent.setClass(getActivity(), VodActivity.class);
        } else {
            intent.setClass(getActivity(), PlayerActivity.class);
        }
        LogUtil.d(TAG, "MovieBean : " + item.toString());
        intent.putExtra("url", item.getUrl());
        intent.putExtra("movieName", item.getName());
        if (mADList.size() > 0) {
            int index = r.nextInt(mADList.size());
            LogUtil.d(TAG, "index : " + index + "  mADList.size() :" + mADList.size());
            final MovieADBean adBean = mADList.get(index);
            intent.putExtra("ad_url", adBean.getUrl());
            intent.putExtra("ad_pic", adBean.getPic());
            intent.putExtra("ad_time", adBean.getAdtime());
        }
        startActivity(intent);
    }

    private void upDatePlayNum(String movieID) {
        if (TextUtils.isEmpty(movieID)) {
            return;
        }
        MyHttpUtil.VolleyGet(MovieConstats.MOVIE_IP + MovieConstats.UPDATE_NUM + movieID, mRequestQueue, new MyHttpUtil.VolleyInterface() {
            @Override
            public void win(JSONObject response) {
                LogUtil.d(TAG, "upDatePlayNum   win : " + response.toString());
            }

            @Override
            public void error(VolleyError error) {
                LogUtil.d(TAG, "upDatePlayNum  error ");
            }
        });
    }

    private void movieTypeSele(String movieTypeID){
        if (TextUtils.isEmpty(movieTypeID)) {
            return;
        }
        MyHttpUtil.VolleyGet(MovieConstats.MOVIE_IP + MovieConstats.MOVIE_LIST_TYPE + movieTypeID, mRequestQueue, new MyHttpUtil.VolleyInterface() {
            @Override
            public void win(JSONObject response) {
                LogUtil.d(TAG, "movieTypeSele   win : " + response.toString());
                try {
                    int result = response.getInt("result");
                    LogUtil.d(TAG, "result " + result);
                    if (result == 1) {
                        Gson gson = new Gson();
                        Respon respon = gson.fromJson(response.toString(), Respon.class);
                        mList = respon.item;
                        mADList = respon.advert;
                        mTypeList = respon.movietype;
                        mTypeList.addFirst(new MovieTypeBean("0","所有"));
                        mHandler.sendEmptyMessage(LOADINGSUCC);
                        final long time = (System.currentTimeMillis()) / 1000;
                        for (int i = 0; i < mADList.size(); i++) {
                            final long start = Long.parseLong(mADList.get(i).getStarttime());
                            final long end = Long.parseLong(mADList.get(i).getEndtime());
                            if (time < start || time > end) {
                                mADList.remove(i);
                                i--;
                            }
                        }
                    }else {
                        LogUtil.d(TAG, "movieTypeSele  result != 1");
                        mHandler.sendEmptyMessage(LOADINGERR);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void error(VolleyError error) {
                LogUtil.d(TAG, "movieTypeSele  error");
                mHandler.sendEmptyMessage(LOADINGERR);
            }
        });
    }

    private void loadingData() {
        showLoading();
        MyHttpUtil.VolleyGet(MovieConstats.MOVIE_IP + MovieConstats.MOVIE_LIST, mRequestQueue, new MyHttpUtil.VolleyInterface() {
            @Override
            public void win(JSONObject response) {
                LogUtil.d(TAG, "loadingData  win");
                try {
                    int result = response.getInt("result");
                    LogUtil.d(TAG, "result " + result);
                    if (result == 1) {
                        Gson gson = new Gson();
                        LogUtil.d(TAG, "response.toString() : " + response.toString());
                        Respon respon = gson.fromJson(response.toString(), Respon.class);
                        mList = respon.item;
                        mADList = respon.advert;
                        mTypeList = respon.movietype;
                        mTypeList.addFirst(new MovieTypeBean("0","所有"));
                        mHandler.sendEmptyMessage(LOADINGSUCC);
                        final long time = (System.currentTimeMillis()) / 1000;
                        for (int i = 0; i < mADList.size(); i++) {
                            final long start = Long.parseLong(mADList.get(i).getStarttime());
                            final long end = Long.parseLong(mADList.get(i).getEndtime());
                            if (time < start || time > end) {
                                mADList.remove(i);
                                i--;
                            }
                        }
                    }else {
                        LogUtil.d(TAG, "loadingData  result != 1");
                        mHandler.sendEmptyMessage(LOADINGERR);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void error(VolleyError error) {
                LogUtil.d(TAG, "loadingData  error");
                mHandler.sendEmptyMessage(LOADINGERR);
//                mHandler.sendEmptyMessage(LOADINGSUCC);
            }
        });
    }

    private class Respon {
        public int result;
        public List<MovieBean> item;
        public List<MovieADBean> advert;
        public LinkedList<MovieTypeBean> movietype;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mCallback = (MenuCallback) getActivity();
    }

    private boolean isHidden = false;

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        isHidden = hidden;
        if (hidden) {
            hidden();
        } else {
            show();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!isHidden) {
            hidden();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isHidden) {
            show();
        }
    }

    private void show() {
        MobclickAgent.onPageStart("视频点播"); //统计页面(仅有Activity的应用中SDK自动调用，不需要单独写。"SplashScreen"为页面名称，可自定义)
        MobclickAgent.onResume(getActivity());//统计时长
        loadingData();
    }

    private void hidden() {
        MobclickAgent.onPageEnd("视频点播"); // （仅有Activity的应用中SDK自动调用，不需要单独写）保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息。"SplashScreen"为页面名称，可自定义
        MobclickAgent.onPause(getActivity());
    }
}
