package com.jld.torsun.activity.messageCenter;

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonRequest;
import com.google.gson.Gson;
import com.jld.torsun.ActivityManageFinish;
import com.jld.torsun.MyApplication;
import com.jld.torsun.R;
import com.jld.torsun.activity.BaseActivity;
import com.jld.torsun.db.MemberDao;
import com.jld.torsun.db.TeamDao;
import com.jld.torsun.http.VolleyJsonUtil;
import com.jld.torsun.modle.CreateSendMessage;
import com.jld.torsun.modle.CreateSendMessageItem;
import com.jld.torsun.util.Constats;
import com.jld.torsun.util.LogUtil;
import com.jld.torsun.util.MD5Util;
import com.jld.torsun.util.ToastUtil;
import com.jld.torsun.util.UserInfo;
import com.jld.torsun.util.imagecache.SDCardUtils;
import com.jld.torsun.view.RoundProgressBar;

import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import zhy.imageloader.MyAdapter;

/**
 * 编辑信息界面
 */
public class CreateNewMessageActivity extends BaseActivity implements OnClickListener {

    private static final String TAG = "CreateNewMessageActivity";
    private View topView;
    private View headView;
    private TextView footView;
    private TextView releaseView;
    private ImageButton backView;
    private ListView mListView;

    private EditText titleHeadView;
    private EditText describeHeadView;

    private CreateNewMessageListAdapter mAdapter;

    public static final int CHOOSEPHOTO_MSG = 0xffff;
    public static final int UPLOADING = 0;
    public static final int SEND_MESSAGE_RESULT = 1;
    public static final int REPEAT_SEND = 2;
    public static final int ADD_IMAGE = 3;
    public static final int NOTIFYCHANGE = 4;
    public static final int IMAGE_DELETE = 5;
    public static final int DESCRIBE_CHANGE = 6;
    private int postion = -1;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case CHOOSEPHOTO_MSG://获取图片
                    postion = msg.arg1;
                    LogUtil.d(TAG, "mHandler--postion:" + postion);
                    showPhotoChooseDialog();
                    break;
                case SEND_MESSAGE_RESULT://service返回结果
                    int arg1 = msg.arg1;
                    LogUtil.d("SendMessageService", "arg1:" + arg1);
                    switch (arg1) {
                        case 1:
                            dialog.dismiss();
                            ToastUtil.showToast(CreateNewMessageActivity.this, getResources().getString(R.string.upload_win), 3000);
                            finish();
                            break;
                        case 2:
                            dialog.dismiss();
                            ToastUtil.showToast(CreateNewMessageActivity.this, getResources().getString(R.string.upload_fail), 5000);
                            break;
                        case 3:
                            dialog.dismiss();
                            ToastUtil.showToast(CreateNewMessageActivity.this, getResources().getString(R.string.t_frag_set_network_err), 5000);
                            break;
                    }
                    break;
                case ADD_IMAGE://添加图片
                    releaseView.setEnabled(true);

                    LogUtil.d(TAG, "添加图片");
                    int arg13 = msg.arg1;
                    if (arg13 == 0) {//添加封面
                        isCover = true;
//                        if (isTitle) {
//                            releaseView.setEnabled(true);
//                        }
                    }
                    if (arg13 == (mAdapter.getData().size() - 1)) {//其他
                        isBitmap = true;
                        footView.setEnabled(true);
                    }
                    break;
                case IMAGE_DELETE:
                    int arg11 = msg.arg1;
                    int arg2 = msg.arg2;
                    LogUtil.d(TAG, arg11 + "----" + arg2);
                    mAdapter.getData().get(arg11).getPath().remove(arg2);//删除对应图片
                    if (arg11 == 0) {//封面被删除
//                        releaseView.setEnabled(false);
                        isCover = false;
                        createMessage.setCover("");//清空封面
                    }
                    //最后一个的最后一张被删
                    if (arg11 == (mAdapter.getData().size() - 1) && mAdapter.getData().get(arg11).getPath().size() == 0) {
                        isBitmap = false;
                        if (!isDescribe) {
                            footView.setEnabled(false);
                        }
                        //最后一张图片被删,且没有图片描述，删除该item
                    } else if (mAdapter.getData().get(arg11).getPath().size() == 0 && TextUtils.isEmpty(mAdapter.getData().get(arg11).getImageDescribe())) {
                        mAdapter.getData().remove(arg11);
                    }
                    releaseViewEnabled();
                    mAdapter.notifyDataSetChanged();
                    break;
                case NOTIFYCHANGE:
                    mAdapter.notifyDataSetChanged();
                    break;
                case DESCRIBE_CHANGE://图片描述被改变
                    int arg12 = msg.arg1;
                    LogUtil.d(TAG, "图片描述被改变");
                    if (arg12 == 1) {//不为空
                        isDescribe = true;
                        footView.setEnabled(true);
                    } else {//为空
//                        if (!isBitmap)
//                            footView.setEnabled(false);
                        isDescribe = false;
                        releaseViewEnabled();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private void releaseViewEnabled() {
        if (createMessage != null) {
            //内容不为空即可发送
            if (createMessage.contentIsNull()) {
                releaseView.setEnabled(false);
            } else {
                releaseView.setEnabled(true);
            }
            LogUtil.d(TAG, "releaseViewEnabled:" + createMessage.contentIsNull());
            LogUtil.d(TAG, "releaseViewEnabled:" + createMessage);
        }
    }

    private CreateSendMessage createMessage;
    private Handler sendHandler;
    private String pickpath;
    private ServiceConnection connection;
    private RoundProgressBar rpb;
    private Dialog dialog;
    private boolean isTitle;
    private boolean isDescribe;
    private boolean isBitmap;
    private boolean isCover;
    private boolean isEdited;
    private int i = 3;
    private SharedPreferences sp;
    private CreateSendMessage createSendMessage;

    private String tuanId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_message);
        ActivityManageFinish.addActivity(this);
        sp = getSharedPreferences(Constats.SHARE_KEY, MODE_PRIVATE);
        initData();
        initView();
        mHandler.postDelayed(sendTeamRun, 3000);
    }

    private boolean isDestroy = false;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
        LogUtil.d(TAG, "onDestroy");
        isDestroy = true;
        ActivityManageFinish.removeActivity(this);
    }

    private void initData() {
        tuanId = getIntent().getStringExtra("tuanId");
        LogUtil.d(TAG, "tuanId : " + tuanId);
        createMessage = new CreateSendMessage();

        //如果有保存的数据，则恢复数据
        String json = sp.getString(UserInfo.SAVE_CREATE_MESSAGE, "");
        LogUtil.d(TAG, "json:" + json);
        if (!TextUtils.isEmpty(json)) {
            //清空
            sp.edit().putString(UserInfo.SAVE_CREATE_MESSAGE, "").apply();
            createSendMessage = new Gson().fromJson(json, CreateSendMessage.class);
            LogUtil.d(TAG, "createSendMessage:" + createSendMessage);
        }

        Intent intent = new Intent(this, SendMessageService.class);
        intent.putExtra("tuanId", tuanId);
        startService(intent);
        connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                SendMessageService.MyBind myBind = (SendMessageService.MyBind) service;
                sendHandler = myBind.getHandler();
                LogUtil.d("SendMessageService", "onServiceConnected:" + sendHandler);
//                myBind.sendHandler(mHandler);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
            }
        };
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    private void initView() {
        topView = findViewById(R.id.create_message_top);
        headView = LayoutInflater.from(this).inflate(R.layout.item_create_message_center_head_layout, null);
        initHeadView();
        //添加
        footView = (TextView) findViewById(R.id.create_message_foot);
        footView.setOnClickListener(this);
        backView = (ImageButton) topView.findViewById(R.id.iv_title_message_center_back);
        backView.setImageResource(R.mipmap.back_login_or_regies);
        backView.setOnClickListener(this);
        //发布
        releaseView = (TextView) topView.findViewById(R.id.tv_title_message_center_release);
        releaseView.setEnabled(false);
        releaseView.setOnClickListener(this);
        mListView = (ListView) findViewById(R.id.create_message_center_list);
        mListView.addHeaderView(headView);
        mAdapter = new CreateNewMessageListAdapter(this, mHandler);
        mListView.setAdapter(mAdapter);
        //如果有保存数据，则恢复数据
        if (createSendMessage != null) {
            recoverData();
        } else {
            mAdapter.addItem();
            isBitmap = false;
        }
        mListView.setAdapter(mAdapter);
    }

    public void recoverData() {
        //恢复title
        if (!TextUtils.isEmpty(createSendMessage.getTitle())) {
            titleHeadView.setText(createSendMessage.getTitle());
            createMessage.setTitle(createSendMessage.getTitle());
            if (!TextUtils.isEmpty(createMessage.getTitle())) {
                isTitle = true;
                if (isCover || isDescribe) {
                    releaseView.setEnabled(true);
                }
            } else {
                // releaseView.setEnabled(false);
            }
        }
        //恢复第一个描述
        if (!TextUtils.isEmpty(createSendMessage.getDescribe())) {
            describeHeadView.setText(createSendMessage.getDescribe());
            createMessage.setDescribe(createSendMessage.getDescribe());//设置第一个描述
            isDescribe = true;
            if (isTitle) {
                //  releaseView.setEnabled(true);
            }
        }
        //恢复listview
        if (createSendMessage.getList() != null && createSendMessage.getList().size() > 0) {
            mAdapter.addList(createSendMessage.getList());
            ArrayList paths = createSendMessage.getList().get(createSendMessage.getList().size() - 1).getPath();
            LogUtil.d(TAG, paths + "");
            //如果最下面一个item有图片则允许添加图片
            if (paths != null && paths.size() > 0) {
                footView.setEnabled(true);
            }
        }
        //恢复封面
        if (!TextUtils.isEmpty(createSendMessage.getCover())) {
            isCover = true;
            createMessage.setCover(createSendMessage.getCover());
            if (isTitle) {
                //  releaseView.setEnabled(true);
            }
        }
        releaseViewEnabled();
    }

    private void initHeadView() {
        titleHeadView = (EditText) headView.findViewById(R.id.met_create_message_head_title);
        describeHeadView = (EditText) headView.findViewById(R.id.met_create_message_head_describe);
        describeHeadView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                createMessage.setDescribe(s.toString());//设置第一个描述
                if (!TextUtils.isEmpty(createMessage.getDescribe())) {
                    isDescribe = true;
                    if (isTitle) {
                        //   releaseView.setEnabled(true);
                    }
                } else if (!isCover) {
                    // releaseView.setEnabled(false);
                    isDescribe = false;
                } else {
                    isDescribe = false;
                }

                releaseViewEnabled();
            }
        });
        titleHeadView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                createMessage.setTitle(s.toString());//设置标题
                if (!TextUtils.isEmpty(createMessage.getTitle())) {
                    isTitle = true;
                    if (isCover || isDescribe) {
                        releaseView.setEnabled(true);
                    }
                } else {
                    releaseView.setEnabled(false);
                }
                releaseViewEnabled();
            }
        });
    }

    //向list中新增一个item layout
    private void addNewMessageBean() {
        // int index = mList.size()-1;
        //LogUtil.d(TAG,"addNewMessageBean:index:"+index);
        //mList.add(new MessageList());
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.iv_title_message_center_back://返回
                show_Logout_Dialog();
                break;
            case R.id.tv_title_message_center_release://发布
//                if (TextUtils.isEmpty(tuanId)){
//
//                }
//                String tuanId = sp.getString(UserInfo.LAST_SERVICE_TEAM_ID, "");
                if (TextUtils.isEmpty(tuanId)) {
                    ToastUtil.showToast(this, getResources().getString(R.string.create_send_hint), 3000);
                    return;
                }
                createMessage.setList(mAdapter.getData());


                if (TextUtils.isEmpty(createMessage.getTitle())) {
                    String tid = sp.getString(UserInfo.LAST_SERVICE_TEAM_ID, "");
                    LogUtil.d(TAG, "tid:" + tid);
                    LogUtil.d(TAG, "tuanId:" + tuanId);

                    String tname = TeamDao.getInstance(this).tidGetTname(tuanId);
                    if(TextUtils.isEmpty(tname))
                        tname = getResources().getString(R.string.default_tourist_team_name);
                    createMessage.setTitle(tname);
                    LogUtil.d(TAG, "tname:" + tname);
                } else if (TextUtils.isEmpty(createMessage.getDescribe())) {
                    createMessage.setDescribe(createMessage.getTitle());
                }
                //保存发送的数据
                String json = new Gson().toJson(createMessage);
                LogUtil.d(TAG, "json：" + json);
                sp.edit().putString(UserInfo.FAIL_SAVE_CREATE_MESSAGE, json).apply();

                LogUtil.d(TAG, "tuanId : " + tuanId);
                Message message = sendHandler.obtainMessage();
                message.obj = createMessage;
                message.what = UPLOADING;
                sendHandler.sendMessage(message);


                CreateNewMessageActivity.this.finish();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);

//                dialog = DialogUtil.createLoadingDialog(this, getResources().getString(R.string.t_frag_set_dia_pro_date));
//                dialog.setCanceledOnTouchOutside(false);
//                dialog.show();
                break;
            case R.id.create_message_foot://底部添加图片
                if (mAdapter.getCount() > 5) {
                    ToastUtil.showToast(this, getResources().getString(R.string.message_add_item_hint), 3000);
                } else {
                    mAdapter.addItem();
                    footView.setEnabled(false);
                    mListView.setSelection(mListView.getBottom());
                }
                break;
        }
    }

    public void showPhotoChooseDialog() {
        View view = this.getLayoutInflater().inflate(
                R.layout.dialog_photo_choose, null);
        final Dialog dialog = new Dialog(this, R.style.CustomDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(view, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT));
        ImageView closeView = (ImageView) view
                .findViewById(R.id.dialog_close_iv);
        closeView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (null != dialog) {
                    dialog.dismiss();
                }
            }
        });
        Button capture_picture = (Button) view
                .findViewById(R.id.capture_picture);
        capture_picture.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                takePhoto();
                if (null != dialog) {
                    dialog.dismiss();
                }
            }
        });
        Button phohe_picture = (Button) view.findViewById(R.id.phohe_picture);
        phohe_picture.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                pickPhoto();
                if (null != dialog) {
                    dialog.dismiss();
                }
            }
        });
        Window window = dialog.getWindow();
        // 设置显示动画

        window.setWindowAnimations(R.style.main_menu_animstyle);
        WindowManager.LayoutParams wl = window.getAttributes();
        wl.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        wl.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        dialog.show();
    }

    /* 使用照相机拍照获取封面 */
    public static final int SELECT_PIC_BY_TACK_COVER_PHOTO = 1;
    /* 使用照相机拍照获取图片 */
    public static final int SELECT_PIC_BY_TACK_PHOTO = 2;
    /* 使用相册获取封面 */
    public static final int SELECT_PIC_BY_PICK_COVER_PHOTO = 3;
    /* 使用相册获取图片 */
    public static final int SELECT_PIC_BY_PICK_PHOTO = 150;
    /*剪切图片*/
    private static final int REQUESTCODE_CUTTING = 5;

    private static final String PHOTO_FILE_NAME = "photo";

    /***
     * 从相册中取图片
     */
    private void pickPhoto() {
        if (postion == 0) {//进入相册选择封面
            Intent pickIntent = new Intent(Intent.ACTION_PICK, null);
            pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    "image/*");
            startActivityForResult(pickIntent, SELECT_PIC_BY_PICK_COVER_PHOTO);
        } else {//进入自定义相册选择多张图片
            Intent pickIntent = new Intent(this, zhy.imageloader.MainActivity.class);
            startActivityForResult(pickIntent, SELECT_PIC_BY_PICK_PHOTO);
        }
    }

    /**
     * 拍照获取图片
     */

    private File tempFile;

    private void takePhoto() {
        if (SDCardUtils.isSDCardEnable()) {
            if (postion == 0) {//相册获取封面
                Intent getImageByCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                tempFile = new File(Environment.getExternalStorageDirectory(), PHOTO_FILE_NAME);
                Uri uri = Uri.fromFile(tempFile);
                getImageByCamera.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                startActivityForResult(getImageByCamera, SELECT_PIC_BY_TACK_COVER_PHOTO);
            } else {//相册获取图片
                Intent getImageByCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                tempFile = new File(Environment.getExternalStorageDirectory(), PHOTO_FILE_NAME);
                Uri uri = Uri.fromFile(tempFile);
                getImageByCamera.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                startActivityForResult(getImageByCamera, SELECT_PIC_BY_TACK_PHOTO);
            }
        } else {
            ToastUtil.showToast(this, R.string.t_set_photo_no_sdcard, 3000);
        }
    }

    private Bitmap bitmap;
    private Bitmap faceBitmap;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        LogUtil.d(TAG, "resultCode" + resultCode + "--requestCode" + requestCode + "--data" + data);

        switch (requestCode) {
            case SELECT_PIC_BY_TACK_COVER_PHOTO://相机获取封面
                if (resultCode == 0) {
                    return;
                }
                if (SDCardUtils.isSDCardEnable()) {
                    if (null != tempFile) {
                        pickpath = getRealFilePath(this, Uri.fromFile(tempFile));
                        File file = compressFile(pickpath);
                        if (file == null)
                            return;
                        pickpath = file.toString();
                        bitmap = compressImageFromFile(pickpath);
                        crop(Uri.fromFile(tempFile));
                    }
                }
                break;
            case SELECT_PIC_BY_TACK_PHOTO://相机获取图片
                if (SDCardUtils.isSDCardEnable()) {
                    if (null != tempFile) {
                        //crop(Uri.fromFile(tempFile));
                        String pickpath = getRealFilePath(this, Uri.fromFile(tempFile));
                        File file = compressFile(pickpath);
                        if (file == null)
                            return;
                        pickpath = file.toString();
                        bitmap = compressImageFromFile(pickpath);
                        //刷新界面
                        if (null != bitmap) {
                            if (postion >= 0) {
                                //更新界面
                                mAdapter.addImage(pickpath, postion);
                            }
                            postion = -1;
                            bitmap = null;
                        }
                        tempFile.delete();
                    }
                }
                break;
            case SELECT_PIC_BY_PICK_COVER_PHOTO://相册获取封面
                if (null != data) {
                    Uri pick_uri = data.getData();
                    pickpath = getRealFilePath(this, pick_uri);//从URL中获取图片路径
                    bitmap = compressImageFromFile(pickpath);//从路径中获取bitmap
                    crop(pick_uri);

                }
                break;
            case SELECT_PIC_BY_PICK_PHOTO://相册获取图片
                LogUtil.d(TAG, "data" + data);

                if (null != data) {
                    int size = data.getIntExtra("size", 0);
                    if (size == 0) {
                        return;
                    }
                    LogUtil.d(TAG, "size" + size);

                    ArrayList<String> paths = new ArrayList<>();
                    for (String str : MyAdapter.mSelectedImage) {
                        LogUtil.d(TAG, "str" + str);
                        String path = compressFile(str).toString();
                        LogUtil.d(TAG, "path" + path);
                        paths.add(path);
                    }
                    //刷新界面
                    if (paths.size() > 0) {
                        if (postion >= 0) {
                            mAdapter.addImages(paths, postion);
                        }
                        postion = -1;
                        bitmap = null;
                    }
                    MyAdapter.mSelectedImage.clear();
                    paths.clear();
                    System.gc();
                }
                break;
            case REQUESTCODE_CUTTING://图片剪切后获得封面
                if (null != data) {
                    faceBitmap = data.getParcelableExtra("data");
                    if (null != faceBitmap && null != bitmap) {
                        LogUtil.d(TAG, "onActivityResult--postion:" + postion);
                        if (postion == 0) {
                            saveHead(faceBitmap);//保存封面到本地
                            File file = compressFile(pickpath);
                            createMessage.setCover(file.toString());//保存封面图片
                            mAdapter.addImage(getHeadiconPath(), postion);
                        }
                        postion = -1;
                        bitmap = null;
                    }
                    try {
                        if (null != tempFile) {
                            tempFile.delete();
                        }
                    } catch (Exception e) {
                    }
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);

    }

    public File compressFile(String path) {
        //获取图片父目录，用于保存缩小版图片
        int indexOf = path.lastIndexOf("/");
        String substring = path.substring(0, indexOf);

        LogUtil.d(TAG, "substring:" + substring);
        //图片压缩并将压缩的路径保存于集合

        File file1 = new File(path);
        Bitmap bm = getSmallBitmap(path);
        if (bm == null)
            return null;
        File smallFile = null;
        FileOutputStream fos = null;
        try {
            smallFile = new File(
                    substring, "small_" + System.currentTimeMillis() + file1.getName());
            fos = new FileOutputStream(smallFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        bm.compress(Bitmap.CompressFormat.JPEG, 40, fos);
        return smallFile;
    }


    /**
     * 根据路径获得突破并压缩返回bitmap用于显示
     *
     * @return
     */
    public static Bitmap getSmallBitmap(String filePath) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, 480, 800);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(filePath, options);
    }

    /**
     * 计算图片的缩放值
     *
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            // Calculate ratios of height and width to requested height and
            // width
            final int heightRatio = Math.round((float) height
                    / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will
            // guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }

    /**
     * 把byte数据解析成图片
     */
    private Bitmap decodeBitmap(String path, byte[] data, Context context, Uri uri, BitmapFactory.Options options) {
        Bitmap result = null;
        if (path != null) {
            result = BitmapFactory.decodeFile(path, options);
        } else if (data != null) {
            result = BitmapFactory.decodeByteArray(data, 0, data.length, options);
        } else if (uri != null) {
            ContentResolver cr = context.getContentResolver();
            InputStream inputStream = null;
            try {
                inputStream = cr.openInputStream(uri);
                result = BitmapFactory.decodeStream(inputStream, null, options);
                inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /*
     * 剪切图片
     */
    private void crop(Uri uri) {
        if (uri == null) {
            return;
        }
        // 裁剪图片意图
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        // 裁剪框的比例，1：1
        intent.putExtra("aspectX", 45);
        intent.putExtra("aspectY", 23);
        // 裁剪后输出图片的尺寸大小
        intent.putExtra("outputX", 450);
        intent.putExtra("outputY", 230);
        intent.putExtra("outputFormat", "JPEG");// 图片格式
        intent.putExtra("noFaceDetection", true);// 取消人脸识别
        intent.putExtra("return-data", true);
        // 开启一个带有返回值的Activity，请求码为REQUESTCODE_CUTTING
        startActivityForResult(intent, REQUESTCODE_CUTTING);
    }


    private Bitmap compressImageFromFile(String srcPath) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        newOpts.inJustDecodeBounds = true;// 只读边,不读内容
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        float hh = 800f;//
        float ww = 480f;//
        int be = 1;
        if (w > h && w > ww) {
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;// 设置采样率
        newOpts.inPurgeable = true;// 同时设置才会有效
        newOpts.inInputShareable = true;// 。当系统内存不够时候图片自动被回收
        bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
        return bitmap;
    }

    /**
     * 从uri中获取文件路径
     */
    public static String getRealFilePath(final Context context, final Uri uri) {
        if (null == uri)
            return null;
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null)
            data = uri.getPath();
        else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = context.getContentResolver().query(uri,
                    new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return data;
    }

    public static final String COVER_IAMGE_NAME = "covericon.png";

    public void saveHead(Bitmap photo) {
        try {
            OutputStream outputStream = this.openFileOutput(COVER_IAMGE_NAME,
                    Activity.MODE_PRIVATE);
            photo.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getHeadiconPath() {
        String file = getFilesDir().getAbsolutePath() + "/"
                + COVER_IAMGE_NAME;
        return file;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {//按返回键
            show_Logout_Dialog();
        }
        return true;
    }

    /**
     * 退出保存dialog
     */
    private void show_Logout_Dialog() {

        if (TextUtils.isEmpty(createMessage.getCover()) && TextUtils.isEmpty(createMessage.getTitle()) && TextUtils.isEmpty(createMessage.getDescribe())) {
            finish();
            return;
        }

        // 退出登录的对话框
        // 获取布局
        View view = this.getLayoutInflater().inflate(
                R.layout.dialog_login_select, null);
        // 设置dialog样式
        final Dialog dialog = new Dialog(this, R.style.CustomDialog);
        // 设置布局
        dialog.setContentView(view, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT));
        // 获取子控件
        Button cancel = (Button) view
                .findViewById(R.id.bt_select_dialog_cancel);
        Button confirm = (Button) view
                .findViewById(R.id.bt_select_dialog_confirm);
        TextView title = (TextView) view
                .findViewById(R.id.tv_select_dialog_title);
        TextView message = (TextView) view
                .findViewById(R.id.tv_select_dialog_message);

        message.setPadding(0, 0, 0, getResources().getDimensionPixelSize(R.dimen.dialog_message_padding_button));
        ImageView close = (ImageView) view
                .findViewById(R.id.iv_login_dialog_close);

        message.setGravity(Gravity.CENTER);
        // 初始化控件

        title.setText(getString(R.string.FragmentSet_logout_title));
        message.setText(getString(R.string.message_create_quit_hint1));
        confirm.setText(getString(R.string.message_create_quit_hint2));
        cancel.setText(getString(R.string.message_create_quit_hint3));

        //不保存
        cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                dialog.dismiss();
                CreateNewMessageActivity.this.finish();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
                ArrayList<CreateSendMessageItem> data = mAdapter.getData();
                //上传完成清空压缩的相片
                for (int i = 0; i < data.size(); i++) {
                    for (int j = 0; j < data.get(i).getPath().size(); j++) {
                        String str = data.get(i).getPath().get(j);
                        File file = new File(str);
                        file.delete();
                    }
                }
            }
        });
        //保存
        confirm.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                dialog.dismiss();
                createMessage.setList(mAdapter.getData());
                String json = new Gson().toJson(createMessage);
                LogUtil.d(TAG, "json：" + json);
                sp.edit().putString(UserInfo.SAVE_CREATE_MESSAGE, json).apply();
                CreateNewMessageActivity.this.finish();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
            }
        });
        close.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                dialog.dismiss();
            }
        });
        // dialog.setCanceledOnTouchOutside(false);

        Window window = dialog.getWindow();
        // 设置显示动画
        window.setWindowAnimations(R.style.main_menu_animstyle);
        WindowManager.LayoutParams wl = window.getAttributes();
        wl.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        wl.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        dialog.show();
    }


    Runnable sendTeamRun = new Runnable() {
        @Override
        public void run() {
            addUserForDirToServer();
        }
    };
    private String addMemberUrl = Constats.HTTP_URL
            + Constats.ADD_TEAM_MEMBER_FUN;//添加团队成员信息的URl

    /**
     * 将本地的成员同步到服务器
     */
    @SuppressWarnings("unchecked")
    public void addUserForDirToServer() {
        MemberDao mDao = MemberDao.getInstance(this);
        String localid = sp.getString(UserInfo.LAST_TEAM_ID, "");
        String teamid = sp.getString(UserInfo.LAST_SERVICE_TEAM_ID, "");
        MyApplication ma = (MyApplication) getApplication();
        RequestQueue mRequestQueue = ma.getRequestQueue();
        LogUtil.d(TAG, "localid:" + localid);
        List<String> mList = mDao.selectAllUidByTid(localid);
        LogUtil.d(TAG, mList.size() + ":size:" + mHandler);

        if (null != mList && mList.size() > 0 && !isDestroy) {
            String sign = MD5Util.getMD5(Constats.S_KEY + teamid);
            Map<String, String> params = new HashMap<String, String>();
            params.put("tuanid", teamid);
            //params.put("userid", listToString(mList.toString()));
            params.put("userid", listToString(mList.toString()));
            LogUtil.d(TAG, "====上传的团成员的id=listToString(mList.toString())==:" + listToString(mList.toString()));
            params.put("sign", sign);
            JsonRequest<JSONObject> jsonRequest = VolleyJsonUtil
                    .createJsonObjectRequest(Request.Method.POST, addMemberUrl, params,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    try {
                                        int result = response.getInt("result");
                                        LogUtil.d(TAG, "上传的团成员信息成功--result--:" + result);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    LogUtil.d(TAG, "上传的团成员信息失败");
                                }
                            });
            if (null != mRequestQueue) {
                mRequestQueue.add(jsonRequest);
            }
        } else {
            LogUtil.d(TAG, "没有新数据,不需上传的团成员信息");
        }
    }

    public String listToString(String str) {
        //去掉字符串中间的空格
        str = str.replace(" ", "");
        int end = str.length() - 1;
        return str.substring(1, end);
    }


}
