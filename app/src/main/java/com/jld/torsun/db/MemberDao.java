package com.jld.torsun.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.jld.torsun.modle.TeamMember;
import com.jld.torsun.util.LogUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author liuzhi
 * @ClassName: MemberDao
 * @Description: 查询本地数据库操作 Dao层(userid mobile ,nick ,username ,img ,isload)
 * @date 2015-12-2 上午10:45:35
 */
public class MemberDao {
    private DataBaseHelper baseHelper;
    private SQLiteDatabase db;
    private static MemberDao userDao;
    private final String TAG = "MemberDao";


    private MemberDao(Context context) {
        this.baseHelper = new DataBaseHelper(context);
    }

    public static MemberDao getInstance(Context context) {
        if (null == userDao) {
            userDao = new MemberDao(context);
        }
        return userDao;
    }

    /**
     * 插入用户
     * <p/>
     * local_tid,userid,mobile,nick,username,img,isload
     */
    public void insertMember(TeamMember member) {
        if (null == member)
            return;
        if (TextUtils.isEmpty(member.userid) || TextUtils.isEmpty(member.mobile))
            return;
        if ("0".equals(member.userid))
            return;
        if (findIdExist(member))
            return;
        LogUtil.i(TAG, "插入用户" + member.nick);
        this.db = baseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            db.execSQL(
                    "insert into teammember(local_tid,userid,mobile,nick,username,online,online_change_time,img,isload)values(?,?,?,?,?,?,?,?,?)",
                    new Object[]{member.localid, member.userid,
                            member.mobile, member.nick, member.username,
                            member.online,member.online_change_time,
                            member.img, member.isload});

            db.setTransactionSuccessful();
            db.endTransaction();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 更新用户资料
     * <p/>
     * /** mobile ,nick ,username ,img
     */
    public void updateUser(TeamMember member) {
        if (null == member)
            return;
        if (TextUtils.isEmpty(member.userid))
            return;
        if ("0".equals(member.userid))
            return;
        LogUtil.i(TAG, "更新用户资料" + member.nick);

        db = baseHelper.getWritableDatabase();
        try {
            db.execSQL(
                    "update teammember set mobile=?,nick=?,username=?,img=? where userid = ? ",
                    new Object[]{member.mobile, member.nick, member.username,
                            member.img, member.userid});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * 修改在线状态
     */
    public void updateOnline(String userid, String online) {
        if (TextUtils.isEmpty(userid))
            return;
        if ("0".equals(userid))
            return;
        db = baseHelper.getWritableDatabase();
        try {
            db.execSQL("update teammember set online=? where userid = ? ",
                    new Object[]{online, userid});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * 修改在线状态
     */
    public void cancelOnlineAll() {

        db = baseHelper.getWritableDatabase();
        try {
            db.execSQL("update teammember set online=? where online = ? ",
                    new Object[]{"0", "1"});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * 修改在线时间
     */
    public void updateOnlineTime(String userid, String time) {
        if (TextUtils.isEmpty(userid))
            return;
        if ("0".equals(userid))
            return;
        db = baseHelper.getWritableDatabase();
        try {
            db.execSQL("update teammember set online_change_time=? where userid = ? ",
                    new Object[]{time, userid});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * 修改昵称
     */
    public void updateUserNik(String userid, String nick) {
        if (TextUtils.isEmpty(userid))
            return;
        if ("0".equals(userid))
            return;
        db = baseHelper.getWritableDatabase();
        try {
            db.execSQL("update teammember set nick=? where userid = ? ",
                    new Object[]{nick, userid});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 修改姓名
     */
    public void updateUserName(String userid, String username) {
        if (TextUtils.isEmpty(userid))
            return;
        if ("0".equals(userid))
            return;
        db = baseHelper.getWritableDatabase();
        try {
            db.execSQL("update teammember set username=? where userid = ?",
                    new Object[]{username, userid});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据用户名id查询所有数据 /**teamid userid mobile ,nick ,username ,img ,isload
     */
    public TeamMember findMemberByid(String userid) {
        TeamMember member = null;
        Cursor cursor = null;
        db = baseHelper.getReadableDatabase();
        try {
            cursor = db.rawQuery("select * from teammember where userid = ?",
                    new String[]{userid});
            while (cursor.moveToNext()) {
                member = new TeamMember();
                member.localid = cursor.getString(cursor.getColumnIndex("local_tid"));
                member.userid = cursor.getString(cursor.getColumnIndex("userid"));
                member.mobile = cursor.getString(cursor.getColumnIndex("mobile"));
                member.nick = cursor.getString(cursor.getColumnIndex("nick"));
                member.username = cursor.getString(cursor.getColumnIndex("username"));
                member.online = cursor.getString(cursor.getColumnIndex("online"));
                member.online_change_time = cursor.getString(cursor.getColumnIndex("online_change_time"));
                member.img = cursor.getString(cursor.getColumnIndex("img"));
                member.isload = cursor.getString(cursor.getColumnIndex("isload"));
            }
        } catch (Exception e) {
        } finally {
            if (null != cursor) {
                cursor.close();
            }
        }
        return member;
    }

    /**
     * 根据本地团id查询所有数据(查询同一个团队中的所有用户信息)
     */
    public List<TeamMember> findMemberByteamid(String teamid) {
        LogUtil.i(TAG, "查询数据" + teamid);

        List<TeamMember> list = new ArrayList<TeamMember>();
        TeamMember member = null;
        Cursor cursor = null;
        db = baseHelper.getReadableDatabase();
        try {
            cursor = db.rawQuery(
                    "select * from teammember where local_tid = ?",
                    new String[]{teamid});
            while (cursor.moveToNext()) {
                member = new TeamMember();
                member.localid = cursor.getString(cursor.getColumnIndex("local_tid"));
                member.userid = cursor.getString(cursor.getColumnIndex("userid"));
                member.mobile = cursor.getString(cursor.getColumnIndex("mobile"));
                member.nick = cursor.getString(cursor.getColumnIndex("nick"));
                member.username = cursor.getString(cursor.getColumnIndex("username"));
                member.online = cursor.getString(cursor.getColumnIndex("online"));
                member.online_change_time = cursor.getString(cursor.getColumnIndex("online_change_time"));
                member.img = cursor.getString(cursor.getColumnIndex("img"));
                member.isload = cursor.getString(cursor.getColumnIndex("isload"));
                list.add(member);
            }
        } catch (Exception e) {
        } finally {
            if (null != cursor) {
                cursor.close();
            }
        }
        return list;

    }

    /**
     * 根据本地团id查询游客ID，返回json格式
     */
    public String getUserids(String teamid) {
        Cursor cursor = null;
        JSONObject object = new JSONObject();
        JSONObject jsonObject;
        JSONArray jsonArray;
        db = baseHelper.getReadableDatabase();
        try {
            cursor = db.rawQuery(
                    "select * from teammember where local_tid = ?",
                    new String[]{teamid});
            while (cursor.moveToNext()) {
                jsonObject = new JSONObject();
                jsonArray = new JSONArray();
                jsonObject.put("localid", cursor.getString(cursor.getColumnIndex("local_tid")));//团队ID
                jsonObject.put("userid", cursor.getString(cursor.getColumnIndex("userid")));//用户ID
                jsonArray.put(jsonObject);
                object.put("", jsonArray);
            }
        } catch (Exception e) {
        } finally {
            if (null != cursor) {
                cursor.close();
            }
        }
        LogUtil.d("object.toString:", "" + object.toString());
        return object.toString();
    }

    /**
     * 根据本地团id查询游客头像URL
     *
     * @param teamid
     * @return
     */
    public Map<String, String> getIcons(String teamid) {
        HashMap<String, String> hashMap = new HashMap<>();
        Cursor cursor = null;
        try {
            db = baseHelper.getReadableDatabase();
            cursor = db.rawQuery(
                    "select * from teammember where local_tid = ?",
                    new String[]{teamid});
            while (cursor.moveToNext()) {
                hashMap.put(cursor.getString(cursor.getColumnIndex("userid")), cursor.getString(cursor.getColumnIndex("img")));
            }
        } catch (Exception e) {
        } finally {
            if (null != cursor) {
                cursor.close();
            }
        }
        LogUtil.d("object.toString:", "" + hashMap.toString());
        return hashMap;
    }

    /**
     * 根据本地团ID查询所有用户ID
     */
    public List<String> selectAllUidByTid(String localid) {
        List<String> mList = new ArrayList<String>();
        Cursor cursor = null;
        db = baseHelper.getReadableDatabase();
        try {
            cursor = db.rawQuery(
                    "select * from teammember where local_tid = ? ",
                    new String[]{localid});
            while (cursor.moveToNext()) {
                mList.add(cursor.getString(cursor.getColumnIndex("userid")).trim());
            }
        } catch (Exception e) {
        } finally {
            if (null != cursor) {
                cursor.close();
            }
        }
        return mList;
    }

    /**
     * 根据团ID查询所有用户的姓名 : 张三、李四、王五...
     */
    public String selectMemberByTeamID(String teamid) {
        boolean isfrist = true;
        StringBuffer sBuffer = new StringBuffer("");
        Cursor cursor = null;
        db = baseHelper.getReadableDatabase();
        try {
            cursor = db.rawQuery("select * from teammember where local_tid=?",
                    new String[]{teamid});
            while (cursor.moveToNext()) {
                if (isfrist) {
                    sBuffer.append(cursor.getString(cursor.getColumnIndex("nick")));
                    isfrist = false;
                } else {
                    sBuffer.append("、");
                    sBuffer.append(cursor.getString(cursor.getColumnIndex("nick")));
                }
            }
        } catch (Exception e) {
        } finally {
            if (null != cursor) {
                cursor.close();
            }
        }
        return sBuffer.toString();
    }

    /**
     * 判断用户在当前团中是否存在;存在:true,不存在:false; 判断id是否存在 teamid userid mobile ,nick
     * ,username ,img ,isload
     */
    public boolean findIdExist(TeamMember member) {
        boolean isExist = false;
        if (null == member) {
            return isExist;
        }
        Cursor cursor = null;
        db = baseHelper.getReadableDatabase();
        try {
            cursor = db
                    .rawQuery(
                            "select * from teammember where userid = ? and local_tid = ?",
                            new String[]{member.userid, member.localid});
            while (cursor.moveToNext()) {
                isExist = true;
            }
        } catch (Exception e) {
        } finally {
            if (null != cursor) {
                cursor.close();
            }
        }
        return isExist;
    }

    /**
     * 删除指定团id下的所有成员数据
     */
    public void deleteMember2Team(String local_tid) {
        this.db = baseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            db.execSQL("delete from teammember where local_tid = ?",
                    new Object[]{local_tid});
            db.setTransactionSuccessful();
            db.endTransaction();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除指定uid的成员数据
     */
    public void deleteMember2Uid(String userid) {
        this.db = baseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            db.execSQL("delete from teammember where userid = ?",
                    new Object[]{userid});
            db.setTransactionSuccessful();
            db.endTransaction();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
