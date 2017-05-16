package com.jld.torsun.activity.tours;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.jld.torsun.activity.baiduMap.SendLocationService;
import com.jld.torsun.activity.fragment.FragmentMainVoice;
import com.jld.torsun.db.MemberDao;
import com.jld.torsun.db.TeamDao;
import com.jld.torsun.modle.ActionConstats;
import com.jld.torsun.modle.TeamMember;
import com.jld.torsun.util.Constats;
import com.jld.torsun.util.LogUtil;
import com.jld.torsun.util.UserInfo;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.HashSet;
import java.util.Set;

/**
 * @author liuzhi
 * @ClassName: MulticastClient
 * @Description: 组团功能中接收广播
 * @date 2015-12-2 下午4:57:36
 */
@SuppressLint("HandlerLeak")
public class MulticastClient implements Runnable {

    private MulticastSocket mMultiSocket;
    private InetAddress mAddress;

    private boolean mScan = false;

    private Thread mThread = null;

    private byte[] mBuffer = new byte[512];

    private DatagramPacket mDatagram = null;

    private static final String TAG = "MulticastClient";
    public static boolean isFirstReceiver = true;

    private Context mContext = null;
    private SharedPreferences sp;
    Thread mmThread;
    private MemberDao mDao;

    /**
     * 网络正常连接
     */
    private static MulticastClient multicastClient;
    private static Handler sendHandler;
    private static Handler sendVoiceHandler;
    private Message message;

    private long oldTime;
    public static Activity activity;
    private Long getUserTime = 0L;


    /**
     * 获取多路广播实例
     *
     * @param context
     * @return
     * @throws IOException
     */
    public static MulticastClient getInstanceMulticastClient(Context context)
            throws IOException {
        if (null == multicastClient) {
            multicastClient = new MulticastClient(context);

        }

        return multicastClient;
    }

    public static void sendHandler(Handler mHandler) {
        sendHandler = mHandler;
    }


    public static void sendVoiceHandler(Handler mHandler, Activity activity) {
        sendVoiceHandler = mHandler;
        MulticastClient.activity = activity;
    }

    private MulticastClient(Context context) throws IOException {
        mContext = context;

        initData();
        mAddress = InetAddress.getByName(MulticastServer.BROADCAST_IP);//根据主机名字或IP获取主机地址
        mMultiSocket = new MulticastSocket(MulticastServer.CLIENT_RECEIVE_PORT);//创建多路广播并绑定端口
        try {
            mMultiSocket.joinGroup(mAddress);//加入多路广播
        } catch (Exception e) {
            LogUtil.e(e.toString());
        }
        mThread = new Thread(this);
        /** 延时清空在线人数的集合 */
        mmThread = new Thread(isonline);
    }

    private void initData() {
        sp = mContext.getSharedPreferences(Constats.SHARE_KEY,
                Context.MODE_PRIVATE);
        oldTime = sp.getLong(UserInfo.TIME, 0L);
        //获取团队成员管理实例
        mDao = MemberDao.getInstance(mContext);
        userid = sp.getString(UserInfo.USER_ID, "");

        guiderName = sp.getString(UserInfo.GUIDER_NAME, "");
        guiderNik = sp.getString(UserInfo.GUIDER_NICK, "");
        guiderICON = sp.getString(UserInfo.LOAD_ICON, "");
        guiderId = sp.getString(UserInfo.GUIDER_ID, "");
        stId = sp.getString(UserInfo.LAST_SERVICE_TEAM_ID, "");
        boolean isload = sp.getBoolean(UserInfo.ISLOAD, false);

        if (isload)
            tId = TeamDao.getInstance(mContext).selectLastTeamid(userid);
        else
            tId = sp.getString(UserInfo.LAST_TEAM_ID, "");
    }

    /**
     * 当前在线人数集合
     */
    private Set<String> uidSet = new HashSet<String>();
    private Set<String> uidSet2 = new HashSet<String>();
    /**
     * 是否清空
     */
    public static boolean isClear = false;
    public static boolean clearThreadStop = false;
    /**
     * 延时清空
     */
    private Runnable isonline = new Runnable() {
        @Override
        public void run() {
            while (!clearThreadStop) {
                final boolean isload = sp.getBoolean(UserInfo.ISLOAD, false);
                if (!isload) {
                    try {
                        mmThread.sleep(1000 * 30);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        continue;
                    }
                }
                //保存即将清空的集合，用于判断是否有新团员加入
                for (String str : uidSet) {
                    uidSet2.add(str);
                }
                LogUtil.d("uidSet", "uidSet2.size：" + uidSet2.size());
                isClear = true;
                uidSet.clear();
                LogUtil.d("uidSet", "清空1：" + uidSet.size());
                try {
                    mmThread.sleep(1000 * 14);
                    isClear = false;
                    if (sendHandler != null) {
                        sendHandler.sendEmptyMessage(TrouTeamMemberManagerActivity.REFRESH_LISTING);//刷新列表
                    }
                    mmThread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    LogUtil.d("uidSet", "清空异常：" + uidSet.size());
                } finally {
                    uidSet2.clear();
                }
                LogUtil.d("uidSet", "清空2：" + uidSet.size());
            }
        }
    };

    public void startReceive() {
        this.mScan = true;
        LogUtil.d("mThread", "mThread:" + mThread);
        mThread.start();
        mmThread.start();
        LogUtil.d(TAG, "startReceive播放");
    }

    public void stopReceive() {
        multicastClient = null;
        clearThreadStop = true;
        LogUtil.d(TAG, "stopReceive停止接受广播信息");
        this.mScan = false;
    }

    @Override
    public void run() {
        LogUtil.d(TAG, "run");
        // true保证在运用启动时一直运行本线程
        while (mScan) {
            scan_recv();
            LogUtil.d("scan_recv", "scan_recv");
        }
        LogUtil.d(TAG, "Client--mMultiSocket.close()");
        if (null != mMultiSocket) {
            mMultiSocket.close();
            LogUtil.d(TAG, "Client--mMultiSocket.close()");
        }
    }

    private String receiveStr = "";

    public void scan_recv() {
        try {
            mDatagram = new DatagramPacket(mBuffer, mBuffer.length);
            mMultiSocket.receive(mDatagram);
            if (mDatagram.getData().length > 1) {
                receiveStr = getData(new String(mDatagram.getData(), 0,
                        mDatagram.getLength(), "UTF-8"));
                LogUtil.v(TAG, "接收到的信息:" + receiveStr);
                if (!TextUtils.isEmpty(receiveStr)) {
                    mHandler.sendEmptyMessage(TOAST_MSG_RECEIVE);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 接收到信息后的message.what的值
     */
    private boolean isNewTime;
    private final int TOAST_MSG_RECEIVE = 0x01;

    private String guiderName = "";
    private String guiderNik = "";
    private String guiderICON = "";
    private String guiderId = "";
    private String tId = "";
    private String userid;
    private String stId;

    Handler mHandler = new
            Handler() {
                @Override
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case TOAST_MSG_RECEIVE:
                            // 获取本用户的权限是否是导游
                            boolean isload = sp.getBoolean(UserInfo.ISLOAD, false);
                            if (TextUtils.isEmpty(userid))
                                userid = sp.getString(UserInfo.USER_ID, "");
                            JsonParser parser = new JsonParser();
                            JsonElement element = parser.parse(receiveStr);
                            if (element.isJsonObject()) {
                                Gson gson = new Gson();
                                TeamMember member = gson.fromJson(receiveStr,
                                        TeamMember.class);
                                LogUtil.d("showItem", "receiveStr：" + receiveStr);
                                if (null == member) {
                                    break;
                                }
                                LogUtil.d("showItem", "member.time" + member.time);
                                LogUtil.d("showItem", "oldTime" + oldTime);
                                LogUtil.d("showItem", "userid" + userid);
                                LogUtil.d("showItem", "member.getUserid()" + member.getUserid());
                                //导游权限被剥夺
                                if ("1".equals(member.isload) && !userid.equals(member.getUserid()) && member.time > oldTime) {
                                    oldTime = member.time;
                                    //更换导游，改变导游的信息
                                    if (isload) {
                                        uidSet.clear();
                                        uidSet2.clear();
                                        //清空团队成员在线状态
                                        MemberDao mDao = MemberDao.getInstance(activity);
                                        mDao.cancelOnlineAll();
                                        isFirstReceiver = true;
                                        isload = false;
                                        sp.edit().putBoolean(UserInfo.ISLOAD, false).commit();
                                        //发送导游改变广播
                                        Intent infoChangeIntent = new Intent(ActionConstats.GUIDE_INFO_CHANGE);
                                        mContext.sendBroadcast(infoChangeIntent);
                                        Intent intent = new Intent(ActionConstats.STRCHANGE);
                                        mContext.sendBroadcast(intent);
                                        //开启service上传经纬度
                                        intent = new Intent(activity, SendLocationService.class);
                                        activity.startService(intent);
                                    }
                                }
                                //导游状态信息改变
                                if ("1".equals(member.isload)) {

                                    if (!guiderId.equals(member.getUserid()) && sendVoiceHandler != null) {
                                        guiderId = member.userid;
                                        sp.edit().putString(UserInfo.LOAD_ICON, member.img).apply();
                                        sp.edit().putString(UserInfo.GUIDER_NAME, member.username).apply();
                                        sp.edit().putString(UserInfo.GUIDER_NICK, member.nick).apply();
                                        sp.edit().putLong(UserInfo.TIME, member.time).apply();
                                        sp.edit().putString(UserInfo.GUIDER_PHONE, member.mobile).apply();
                                        sp.edit().putString(UserInfo.GUIDER_ID, member.userid).apply();
                                        sp.edit().putString(UserInfo.LAST_TEAM_ID, member.localid).apply();//最新一个旅游团 ID
                                        sp.edit().putString(UserInfo.LAST_SERVICE_TEAM_ID, member.teamid).apply();//最新一个旅游团 ID
                                        //导游改变voice页面重新请求导游头像
                                        sendVoiceHandler.sendEmptyMessage(FragmentMainVoice.GET_HEAD_ICON);
                                    }
                                    if (!guiderICON.equals(member.img) && sendVoiceHandler != null) {
                                        guiderICON = member.img;
                                        sp.edit().putString(UserInfo.LOAD_ICON, member.img).apply();
                                        //导游改变voice页面重新请求导游头像
                                        sendVoiceHandler.sendEmptyMessage(FragmentMainVoice.GET_HEAD_ICON);
                                    }
                                    if (!guiderNik.equals(member.nick)) {
                                        guiderNik = member.nick;
                                        sp.edit().putString(UserInfo.GUIDER_NICK, member.nick).apply();
                                    }
                                    if (!guiderName.equals(member.username) && sendVoiceHandler != null) {
                                        sendVoiceHandler.sendEmptyMessage(FragmentMainVoice.GET_HEAD_ICON);
                                        guiderName = member.username;
                                        sp.edit().putString(UserInfo.GUIDER_NAME, member.username).apply();
                                    }
                                    LogUtil.d("showItem", "tId:" + tId);
                                    if (tId == null || !tId.equals(member.localid)) {
                                        tId = member.localid;
                                        sp.edit().putString(UserInfo.LAST_TEAM_ID, member.localid).commit();//最新一个旅游团 ID
                                    }
                                    if (!stId.equals(member.teamid)) {
                                        stId = member.teamid;
                                        sp.edit().putString(UserInfo.LAST_SERVICE_TEAM_ID, member.teamid).commit();//最新一个旅游团 ID
                                    }
                                }

                                //匹配团员人数
                                if (TextUtils.isEmpty(tId)
                                        || TextUtils.isEmpty(member.userid) || !isload || TextUtils.isEmpty(member.getLocalid())) {
                                    break;
                                }
                                if (uidSet.add(member.userid)) {
                                    isFirstReceiver = false;
                                    LogUtil.d("uidSet", "添加在线人数：" + member);
                                    member.online = "1";//在线标记
                                    member.online_change_time = System.currentTimeMillis() + "";//在线时间

                                    mDao.insertMember(member);//插入数据库
                                    String username = member.username;
                                    String nick = member.nick;
                                    /**新用户加入提示*/
                                    message = Message.obtain();
                                    if (sendHandler != null) {
                                        if (isClear) {//如果正在清空状态，则需判断清空前集合是否包含该用户
                                            if (uidSet2.add(member.userid)) {
                                                message.what = TrouTeamMemberManagerActivity.ADD_NEW_MEMBER;
                                                if (TextUtils.isEmpty(username)) {
                                                    message.obj = nick + "";
                                                } else {
                                                    message.obj = username + "(" + nick + ")";
                                                }
                                                sendHandler.sendMessage(message);
                                                LogUtil.d("uidSet", "发送：" + username + "(" + nick + ")");
                                            }
                                        } else {
                                            message.what = TrouTeamMemberManagerActivity.ADD_NEW_MEMBER;
                                            if (TextUtils.isEmpty(username)) {
                                                message.obj = nick + "";
                                            } else {
                                                message.obj = username + "(" + nick + ")";
                                            }
                                            sendHandler.sendMessage(message);
                                            LogUtil.d("uidSet", "发送：" + username + "(" + nick + ")");
                                        }
                                    }
                                }
                            }

                    }
                    // 重置接收信息状态
                    mDatagram.setLength(mBuffer.length);
                    receiveStr = "";
                }
            };


    /**
     * 获取当前在线人员的ID的集合
     */
    public Set<String> getUidSet() {
        return uidSet;
    }

    /**
     * 获取清空前在线人数的ID的集合
     */
    public Set<String> getUidSet2() {
        return uidSet2;
    }

    public void clearAllSet() {
        uidSet2.clear();
        uidSet.clear();
    }
// public List<TeamMember> getList() {
// return list;
// }

    public String getData(String str) {
        // LogUtil.d(TAG, "String" + str);
        String data = "";
        if (TextUtils.isEmpty(str)) {
            return data;
        }
        int start = str.indexOf("{");
        int end = str.indexOf("}");
        if (start >= 0 && end > start) {
            data = str.substring(start, end + 1);
        }
        return data;
    }


    /**
     * 判断是否有这个设备
     */
    public boolean isexist(TeamMember member) {
        return mDao.findIdExist(member);
    }

    // private String url = Constats.HTTP_URL + Constats.ADD_TEAM_MEMBER_FUN;

    // /** 添加团成员用户信息到服务器 */
    // @SuppressWarnings("unchecked")
    // private void addUserInfoToServer(String teamid, final TeamMember
    // member) {
    // String sign = MD5Util.getMD5(Constats.S_KEY + teamid);
    // Map<String, String> params = new HashMap<String, String>();
    // params.put("tuanid", teamid);
    // params.put("userid", member.userid);
    // params.put("sign", sign);
    // JsonRequest<JSONObject> jsonRequest = VolleyJsonUtil
    // .createJsonObjectRequest(Method.POST, url, params,
    // new Listener<JSONObject>() {
    // @Override
    // public void onResponse(JSONObject response) {
    // }
    // }, new ErrorListener() {
    // @Override
    // public void onErrorResponse(VolleyError error) {
    // }
    // });
    // if (null != mRequestQueue) {
    // mRequestQueue.add(jsonRequest);
    // }
    // }

}