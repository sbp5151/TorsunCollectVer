package com.jld.torsun.activity.tours;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.jld.torsun.db.TeamDao;
import com.jld.torsun.modle.ActionConstats;
import com.jld.torsun.modle.TeamMember;
import com.jld.torsun.util.Constats;
import com.jld.torsun.util.LogUtil;
import com.jld.torsun.util.UserInfo;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

//import android.content.SharedPreferences;

/**
 * @author liuzhi
 * @ClassName: MulticastServer
 * @Description: 组团功能中的发送广播
 * @date 2015-12-2 下午4:39:52
 */
public class MulticastServer implements Runnable {

    private InetAddress mBroadcastAddr;
    private DatagramSocket mSocket;
    private DatagramPacket mDatagram;

    private byte[] mBuffer = null;

    public static final int SERVER_SEND_PORT = 4445;
    public static final int CLIENT_RECEIVE_PORT = 4446;

    /**
     * 发送用户信息的组播地址
     */
    public static final String BROADCAST_IP = "224.0.0.252";

    private Thread mThread = null;

    private static final String TAG = "MulticastServer";

    private Gson mGson;
    private Context mContext = null;

    private boolean isLoad;
    private String userId;
    //    private String teamid;
    private long times;
    private SharedPreferences sp;
    private static MulticastServer multicastServer;

    public static MulticastServer getInstanceMulticastServer(Context context) {
        if (null == multicastServer) {
            multicastServer = new MulticastServer(context);
        }
        return multicastServer;
    }

    private MulticastServer(Context context) {
        Log.d(TAG, "######## MulticastServer ##########");
        mContext = context;
        sp = mContext.getSharedPreferences(Constats.SHARE_KEY, Context.MODE_PRIVATE);
        // WifiManager wifiManager =
        // (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        // WifiManager.MulticastLock multicastLock =
        // wifiManager.createMulticastLock("mydebuginfo");
        // multicastLock.acquire();
        try {
            mSocket = new DatagramSocket(SERVER_SEND_PORT);//获得发送广播socket
            mBroadcastAddr = InetAddress.getByName(BROADCAST_IP);//获取IP地址
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        mGson = new Gson();
        userId = sp.getString(UserInfo.USER_ID, "");
        times = sp.getLong(UserInfo.TIME, 0L);
        sendStr = sp.getString(UserInfo.JSONSTR, "");

        mThread = new Thread(this);
    }

    public void startSend() {
        LogUtil.d(TAG, "startSend");
        SEND_FLAG = true;
        mThread.start();
    }

    public void stopSend() {
        SEND_FLAG = false;
    }

    private boolean SEND_FLAG = true;


    @Override
    public void run() {
        LogUtil.i(TAG, "--------MulticastServer.run()");
        while (SEND_FLAG) {
            try {
                member = mGson.fromJson(sendStr, TeamMember.class);//通过登录或注册返回的json数据获取团成员对象
                isLoad = sp.getBoolean(UserInfo.ISLOAD, false);
                if(TextUtils.isEmpty(userId))
                    userId = sp.getString(UserInfo.USER_ID,"");
                boolean isLogining = sp.getBoolean(UserInfo.LOGINING, false);
                if (null != member && isLogining) {
                    if (isLoad) {
                        LogUtil.d(TAG, "发送导游信息：" + member.nick);
                        member.isload = "1";
                        member.time = times;
                        //如果是导游将本地ID和服务器ID传过去
                        member.localid = TeamDao.getInstance(mContext).selectLastTeamid(userId);
                        member.teamid = TeamDao.getInstance(mContext).selectServiceLastTeamid(userId);
                    } else {
                        member.localid = sp.getString(UserInfo.LAST_TEAM_ID, "");
                        member.isload = "0";
                    }
                    sendStr = mGson.toJson(member);
                    mBuffer = sendStr.getBytes();
                    mDatagram = new DatagramPacket(mBuffer, mBuffer.length,
                            mBroadcastAddr, CLIENT_RECEIVE_PORT);

                    mSocket.send(mDatagram);
                    LogUtil.d(TAG, "member.teamid：" + member.teamid);
                    LogUtil.d(TAG, "sendStr：" + sendStr);
                }
                Thread.sleep(600);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        LogUtil.d(TAG, "Server--mSocket.close()");
        if (null != mSocket){
            mSocket.close();
        }
    }

    private String sendStr = "";

    private TeamMember member;

    public static final String LOGOUTCHANGE = "com.jld.torsun.activity.tours.MulticastServer.LOGOUTCHANGE";

    public class ChangeBroadcast extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            LogUtil.d(TAG, "=============ChangeBroadcast=========");
            if (ActionConstats.STRCHANGE.equals(action)) {
                LogUtil.d(TAG, "=============STRCHANGE=========");
                sendStr = sp.getString(UserInfo.JSONSTR, "");
                isLoad = sp.getBoolean(UserInfo.ISLOAD, false);
                times = intent.getLongExtra("CreatTime", sp.getLong(UserInfo.TIME, 0L));
                LogUtil.d(TAG, "=============times=========:" + times);
                //teamid = sp.getString(UserInfo.LAST_TEAM_ID, "");
            }
        }

    }

    // public static int getGroupSize() {
    // List<InetAddress> list = new ArrayList<InetAddress>();
    // try {
    // Enumeration<InetAddress> enumeration = NetworkInterface.getByName(
    // BROADCAST_IP).getInetAddresses();
    // // .getByInetAddress(mBroadcastAddr).getInetAddresses();
    // while (enumeration.hasMoreElements()) {
    // InetAddress inetAddress = (InetAddress) enumeration
    // .nextElement();
    // list.add(inetAddress);
    // }
    // } catch (SocketException e) {
    // e.printStackTrace();
    // }
    // return list.size();
    // }

    // 获取这一网络接口中的所有InetAddress地址
    // Enumeration<InetAddress>
    // enumIpAddr=NetworkInterface.getByInetAddress(mBroadcastAddr).getInetAddresses();

    // public String getLocalIpAddress() {
    // try {
    // for (Enumeration<NetworkInterface> en = NetworkInterface
    // .getNetworkInterfaces(); en.hasMoreElements();) {
    // NetworkInterface intf = en.nextElement();
    // for (Enumeration<InetAddress> enumIpAddr = intf
    // .getInetAddresses(); enumIpAddr.hasMoreElements();) {
    // InetAddress inetAddress = enumIpAddr.nextElement();
    // if (!inetAddress.isLoopbackAddress()) {
    // return inetAddress.getHostAddress().toString();
    // }
    // }
    // }
    // } catch (SocketException ex) {
    // Log.e("WifiPreference IpAddress", ex.toString());
    // }
    // return "";
    // }

    // private final int TOAST_MSG_SEND = 0x01;
    // private Handler mHandler = new Handler() {
    // @Override
    // public void handleMessage(Message msg) {
    // switch (msg.what) {
    // case TOAST_MSG_SEND:
    // // Toast.makeText(mContext, sendStr, Toast.LENGTH_SHORT).show();
    // }
    // }
    // };

}
