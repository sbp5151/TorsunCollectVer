package com.jld.torsun.activity.messageCenter;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.google.gson.Gson;
import com.jld.torsun.MyApplication;
import com.jld.torsun.modle.CreateReceiveMessage;
import com.jld.torsun.modle.CreateReceiveMessageItem;
import com.jld.torsun.modle.CreateSendMessage;
import com.jld.torsun.modle.CreateSendMessageItem;
import com.jld.torsun.util.Constats;
import com.jld.torsun.util.LogUtil;
import com.jld.torsun.util.MD5Util;
import com.jld.torsun.util.UserInfo;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class SendMessageService extends Service {
    private Handler sendHandler;
    private final int UPLOAD = 10;
    private final int UPLOAD_ALL = 11;
    private final String TAG = "SendMessageService";
    private List<CreateSendMessageItem> createMessageItems;
    private List<String> files = new ArrayList<>();
    private MyBind myBind;
    private String title = "";//标题
    private String describe = "";//第一个描述
    private int Load_num_list = 0;//列数
    private int Load_num_line = 0;//行数
    private ArrayList<ArrayList<File>> hashPaths = new ArrayList<>();//图片地址集合
    private ArrayList<String> describes = new ArrayList<>();//图片描述

    private Boolean isSendOk = false;
    private Boolean sendStop = false;
    private Boolean isCover = false;
    private int imageNum = 0;

    private String current_file;
    private String postImageUrl = Constats.HTTP_URL
            + Constats.MESSAGE_UPLOAD_IMAGE_URL;
    private String sendMessageUrl = Constats.HTTP_URL
            + Constats.MESSAGE_UPLOAD_ALL;
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int what = msg.what;
            switch (what) {
                case CreateNewMessageActivity.UPLOADING:
                    imageNum = 0;
                    sendStop = false;
                    createMessage = (CreateSendMessage) msg.obj;
                    receiveMessage = new CreateReceiveMessage();

                    title = createMessage.getTitle();
                    describe = createMessage.getDescribe();

                    //设置正确的封面
                    if (createMessage.getList().get(0).getPath().size() > 0)
                        cover = createMessage.getList().get(0).getPath().get(0);
//                    createMessage.getList().get(0).getPath().remove(0);
//                    createMessage.getList().get(0).getPath().add(createMessage.getCover());

                    //清除空值
                    for (int i = 0; i < createMessage.getList().size(); i++) {
                        CreateSendMessageItem item = createMessage.getList().get(i);
                        LogUtil.d(TAG, "i:" + i);
                        if ((item.getPath() == null || item.getPath().size() == 0) && TextUtils.isEmpty(item.getImageDescribe())) {
                            LogUtil.d(TAG, "remove:" + item);
                            createMessage.getList().remove(item);
                        } else {
                            //设置图片描述
                            receiveMessage.getMessage().add(new CreateReceiveMessageItem(item.getImageDescribe()));
                        }
                    }

                    for (int i = 0; i < createMessage.getList().size(); i++) {
                        for (int j = 0; j < createMessage.getList().get(i).getPath().size(); j++) {
                            imageNum++;
                        }
                    }

                    if (TextUtils.isEmpty(cover)) {
                        isCover = true;
                        mHandler.sendEmptyMessage(UPLOAD);

                    } else {
                        current_file = cover;
                        isCover = false;
                        mHandler.post(sendImageRunUtil);
                    }
//                    mHandler.post(sendImageRun);

                    receiveMessage.setTitle(title);//设置标题
                    receiveMessage.setDesc(describe);//设置第一个描述
                    LogUtil.d(TAG, "describe:" + describe);
                    LogUtil.d(TAG, "cover:" + cover);
                    LogUtil.d(TAG, "title:" + title);
                    LogUtil.d(TAG, "CreateNewMessageListAdapter.items:" + createMessage.getList());
                    LogUtil.d(TAG, "createMessage.getList().size()" + createMessage.getList().size());
                    break;
                case UPLOAD_ALL:
                    String str = (String) msg.obj;
                    LogUtil.d(TAG, str);
                    break;
                case UPLOAD:
                    loopThread = new Thread(loopRun);
                    loopThread.start();
                    break;
                case CreateNewMessageActivity.REPEAT_SEND:
                    mHandler.sendEmptyMessage(UPLOAD);
                    break;
            }
        }
    };
    private FileOutputStream fos;
    private CreateReceiveMessage receiveMessage;
    private SharedPreferences sp;
    private String sign;
    private List<CreateSendMessageItem> messageItem;
    private String userId;
    private String tuanId;
    private String allSign;
    private RequestQueue mRequestQueue;
    private String cover;
    private Thread loopThread;
    private CreateSendMessage createMessage;
    private File current_file1;

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

    public SendMessageService() {
    }

    /**
     *
     */
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        mRequestQueue = ((MyApplication) getApplication()).getRequestQueue();

        sign = MD5Util.getMD5(Constats.S_KEY);
        sp = getSharedPreferences(Constats.SHARE_KEY, Context.MODE_PRIVATE);

        Log.d(TAG, "allSign:" + allSign + "--userId:" + userId + "--tuanId:" + tuanId);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Load_num_list = 0;
        tuanId = intent.getStringExtra("tuanId");

        Log.e(TAG, "onStartCommand  tuanId  " + tuanId);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.e(TAG, "onBind");
        if (myBind == null)
            myBind = new MyBind();
        return myBind;
    }

    public class MyBind extends Binder {
        public Handler getHandler() {
            Log.e(TAG, "getHandler:" + mHandler);
            return mHandler;
        }

        public void sendHandler(Handler handler) {
            sendHandler = handler;
            Log.e(TAG, "sendHandler" + handler);
        }
    }

//    Runnable sendImageRun = new Runnable() {
//        @Override
//        public void run() {
//            HashMap<String, String> params = new HashMap<>();
//            params.put("item", Load_num_list + "");
//            params.put("orders", Load_num_line + "");
//            params.put("sign", sign);
//            params.put("file", "small_" + current_file);
//            current_file1 = new File(SendMessageService.this.current_file);
//            int indexOf = SendMessageService.this.current_file.lastIndexOf("/");
//            String substring = SendMessageService.this.current_file.substring(indexOf + 1);
//            LogUtil.d(TAG, "params:" + params);
//            LogUtil.d(TAG, "Load_num_list:" + Load_num_list);
//            LogUtil.d(TAG, "Load_num_line:" + Load_num_line);
//            LogUtil.d(TAG, "sign:" + sign);
//            LogUtil.d(TAG, "imageName:" + substring);
//
//            OkHttpUtils.post()//
//                    .addFile("file", substring,
//                            current_file1)
//                    .url(postImageUrl)
//                    .params(params)
//                    .build()
//                    .execute(new MyStringCallback());
//
//        }
//    };

    Runnable sendImageRunUtil = new Runnable() {
        @Override
        public void run() {

            HttpUtils httpUtils = new HttpUtils(1000 * 10);
            RequestParams params = new RequestParams();
            params.addBodyParameter("item", Load_num_list + "");
            params.addBodyParameter("orders", Load_num_line + "");
            params.addBodyParameter("sign", sign);
            params.addBodyParameter("file", new File(current_file), current_file);
            httpUtils.send(HttpRequest.HttpMethod.POST, postImageUrl, params, new RequestCallBack<String>() {
                @Override
                public void onSuccess(ResponseInfo<String> responseInfo) {
                    LogUtil.d(TAG, "result:" + responseInfo.result);

                    try {
                        JSONObject jsonObject = new JSONObject(responseInfo.result);
                        String result = jsonObject.getString("result");
                        if ("0".equals(result)) {//上传成功
                            isSendOk = true;
                            String item = jsonObject.getString("item");//获取ID
                            String orders = jsonObject.getString("orders");
                            String msg = jsonObject.getString("msg");//获取返回链接
                            Log.e(TAG, "item:" + item);
                            Log.e(TAG, "orders:" + orders);
                            Log.e(TAG, "msg:" + msg);
                            if (!isCover) {
                                receiveMessage.setPic(msg);
                                isCover = true;
                                mHandler.sendEmptyMessage(UPLOAD);
                                return;
                            }
                            receiveMessage.getMessage().get(Load_num_list).getUrl().add(msg);
                        } else {
                            sendStop = true;
                            if (sendHandler != null) {
                                Message message = sendHandler.obtainMessage();
                                message.arg1 = 4;
                                message.what = MessageCenterFragment.SEND_MESSAGE_RESULT;
                                sendHandler.sendMessage(message);
                            }
                        }
                    } catch (JSONException e) {
                        sendStop = true;
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(HttpException e, String s) {
                    LogUtil.e(TAG, "onError:" + e.toString());
                    if (sendHandler != null) {
                        Message message = sendHandler.obtainMessage();
                        message.arg1 = 3;//3代表网络错误
                        message.what = MessageCenterFragment.SEND_MESSAGE_RESULT;
                        sendHandler.sendMessage(message);
                    }
                    sendStop = true;
                }
            });
        }
    };

    Runnable loopRun = new Runnable() {
        @Override
        public void run() {
            for (int i = 0; i < createMessage.getList().size() && !sendStop; i++) {
//                receiveMessage.getMessage().put(i, new CreateReceiveMessageItem());
                for (int j = 0; j < createMessage.getList().get(i).getPath().size() && !sendStop; j++) {

                    if (sendHandler != null) {
                        Message message = sendHandler.obtainMessage();
                        message.arg1 = 1;//1代表进度
                        message.arg2 = imageNum--;//3代表数量
                        message.what = MessageCenterFragment.SEND_MESSAGE_RESULT;
                        sendHandler.sendMessage(message);
                    }
                    Load_num_line = j;
                    Load_num_list = i;
                    if (i == 0 && j == 0) {
                        current_file = createMessage.getCover();
                    } else {
                        current_file = createMessage.getList().get(i).getPath().get(j);
                    }
                    LogUtil.d(TAG, "Load_num_line:" + Load_num_line);
                    LogUtil.d(TAG, "Load_num_list:" + Load_num_list);
                    LogUtil.d(TAG, "current_file:" + current_file);
                    isSendOk = false;
                    mHandler.post(sendImageRunUtil);
//                    mHandler.post(sendImageRun);
                    while (!isSendOk) {
                        LogUtil.d(TAG, "睡50:");
                        try {
                            loopThread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            LogUtil.d(TAG, "循环完毕:" + sendStop);
            if (!sendStop) {
                mHandler.post(run);
            }
        }
    };

    Runnable run = new Runnable() {
        @Override
        public void run() {

            userId = sp.getString(UserInfo.USER_ID, "");
            if (TextUtils.isEmpty(tuanId))
                tuanId = sp.getString(UserInfo.FAIL_TUAN_ID_MESSAGE, "");
//            tuanId = sp.getString(UserInfo.LAST_SERVICE_TEAM_ID, "");
            allSign = MD5Util.getMD5(Constats.S_KEY + userId + tuanId);

            receiveMessage.setSign(allSign);//设置签名
            receiveMessage.setUserid(userId);//设置useID
            receiveMessage.setTuanid(tuanId);//设置团ID
            LogUtil.d(TAG, "receiveMessage:" + receiveMessage);

            String json = new Gson().toJson(receiveMessage);
            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(json);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            JsonRequest<JSONObject> jsonRequest = new JsonObjectRequest(Request.Method.POST, sendMessageUrl, jsonObject, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject jsonObject) {
                    LogUtil.d(TAG, "onResponse:" + jsonObject);
                    String result = null;
                    try {
                        result = jsonObject.getString("result");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if ("0".equals(result)) {
                        if (sendHandler != null) {
                            Message message = sendHandler.obtainMessage();
                            message.arg1 = 2;//2代表完成
                            message.what = MessageCenterFragment.SEND_MESSAGE_RESULT;
                            sendHandler.sendMessage(message);
                            //上传完成清空压缩的相片
                            for (int i = 0; i < createMessage.getList().size(); i++) {
                                for (int j = 0; j < createMessage.getList().get(i).getPath().size() && !sendStop; j++) {
                                    String str = createMessage.getList().get(i).getPath().get(j);
                                    File file = new File(str);
                                    file.delete();
                                }
                            }
                        }
                        createMessage = null;
                        receiveMessage = null;
                        System.gc();
                        SendMessageService.this.stopSelf();
                    } else {
                        if (sendHandler != null) {
                            Message message = sendHandler.obtainMessage();
                            message.arg1 = 4;//4代表非0错误
                            message.what = MessageCenterFragment.SEND_MESSAGE_RESULT;
                            sendHandler.sendMessage(message);
                        }
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    LogUtil.d(TAG, "onErrorResponse:" + volleyError);
                    if (sendHandler != null) {
                        Message message = sendHandler.obtainMessage();
                        message.arg1 = 3;//3代表网络错误
                        message.what = MessageCenterFragment.SEND_MESSAGE_RESULT;
                        sendHandler.sendMessage(message);
                    }
                }
            });
            if (mRequestQueue != null)
                mRequestQueue.add(jsonRequest);

        }
    };

//    public class myStringCallbackAll extends StringCallback {
//        @Override
//        public void onError(Call call, Exception e) {
//            Log.d(TAG, "onError:" + e.toString());
//            if (sendHandler != null) {
//                Message message = sendHandler.obtainMessage();
//                message.arg1 = 3;//3代表网络错误
//                message.what = CreateNewMessageActivity.SEND_MESSAGE_RESULT;
//                sendHandler.sendMessage(message);
//            }
//        }
//
//        @Override
//        public void onResponse(String response) {
//            LogUtil.d(TAG, "response:" + response);
//            JSONObject jsonObject = null;
//            try {
//                jsonObject = new JSONObject(response);
//                String result = jsonObject.getString("result");
//                Log.d(TAG, "jsonObject:");
//                if ("0".equals(result)) {
//                    if (sendHandler != null) {
//                        Message message = sendHandler.obtainMessage();
//                        message.arg1 = 1;//1代表成功
//                        message.what = CreateNewMessageActivity.SEND_MESSAGE_RESULT;
//                        sendHandler.sendMessage(message);
//                    }
//                    createMessage = null;
//                    receiveMessage = null;
//                    System.gc();
//                    SendMessageService.this.stopSelf();
//                } else {
//                    if (sendHandler != null) {
//                        Message message = sendHandler.obtainMessage();
//                        message.arg1 = 2;//2代表非0错误
//                        message.what = CreateNewMessageActivity.SEND_MESSAGE_RESULT;
//                        sendHandler.sendMessage(message);
//                    }
//                }
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    public class MyStringCallback extends StringCallback {
//        @Override
//        public void onError(Call call, Exception e) {
//            LogUtil.e(TAG, "onError:" + e.toString());
//            LogUtil.e(TAG, "sendHandler:" + sendHandler);
//            if (sendHandler != null) {
//                Message message = sendHandler.obtainMessage();
//                message.arg1 = 3;//3代表网络错误
//                message.what = CreateNewMessageActivity.SEND_MESSAGE_RESULT;
//                sendHandler.sendMessage(message);
//            }
//            sendStop = true;
//        }
//
//        @Override
//        public void onResponse(String response) {
//            LogUtil.d(TAG, "onResponse:" + response);
//            try {
//                JSONObject jsonObject = new JSONObject(response);
//                String result = jsonObject.getString("result");
//                if ("0".equals(result)) {//上传成功
//                    isSendOk = true;
//                    String item = jsonObject.getString("item");//获取ID
//                    String orders = jsonObject.getString("orders");
//                    String msg = jsonObject.getString("msg");//获取返回链接
//                    Log.e(TAG, "item:" + item);
//                    Log.e(TAG, "orders:" + orders);
//                    Log.e(TAG, "msg:" + msg);
//                    if (!isCover) {
//                        receiveMessage.setPic(msg);
//                        isCover = true;
//                        mHandler.sendEmptyMessage(UPLOAD);
//                        return;
//                    }
//                    receiveMessage.getMessage().get(Load_num_list).getUrl().add(msg);
//                } else {
//                    sendStop = true;
//                    if (sendHandler != null) {
//                        Message message = sendHandler.obtainMessage();
//                        message.arg1 = 2;
//                        message.what = CreateNewMessageActivity.SEND_MESSAGE_RESULT;
//                        sendHandler.sendMessage(message);
//                    }
//                }
//            } catch (JSONException e) {
//                sendStop = true;
//                e.printStackTrace();
//            }
//        }
//
//        @Override
//        public void inProgress(float progress) {
//            LogUtil.d(TAG, "inProgress:" + progress);
//        }
//    }

}
