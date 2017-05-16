package com.jld.torsun.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.jld.torsun.modle.TrouTeam;
import com.jld.torsun.util.LogUtil;
import com.jld.torsun.util.TimeUtil;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author liuzhi
 * @ClassName: TeamDao
 * @Description: 对本地团队信息的操作
 * @date 2015-12-4 上午10:09:50
 */
public class TeamDao {

    private DataBaseHelper baseHelper;
    private SQLiteDatabase db;
    private static TeamDao teamDao;
    private MemberDao memberDao;

    private TeamDao(Context context) {
        this.baseHelper = new DataBaseHelper(context);
        memberDao = MemberDao.getInstance(context);
    }

    public static TeamDao getInstance(Context context) {
        if (null == teamDao) {
            teamDao = new TeamDao(context);
        }
        return teamDao;
    }

    /**
     * (通过本地ID)查询该团队是否存在
     */
    public boolean findIdExist(String localid) {
        boolean flag = false;
        if (TextUtils.isEmpty(localid)) {
            return flag;
        }
        Cursor cursor = null;
        db = baseHelper.getReadableDatabase();
        try {
            cursor = db.rawQuery("select * from team where local_tid = ?",
                    new String[]{localid});
            while (cursor.moveToNext()) {
                flag = true;
            }
        } catch (Exception e) {
        } finally {
            if (null != cursor) {
                cursor.close();
            }
        }
        return flag;
    }

    /**
     * (通过服务器端ID)查询该团队是否存在
     */
    public boolean findIdExistByid(String tid) {
        boolean flag = false;
        if (TextUtils.isEmpty(tid) || "0".equals(tid)) {
            return flag;
        }
        Cursor cursor = null;
        db = baseHelper.getReadableDatabase();
        try {
            cursor = db.rawQuery("select * from team where tid = ?",
                    new String[]{tid});
            while (cursor.moveToNext()) {
                flag = true;
            }
        } catch (Exception e) {
        } finally {
            if (null != cursor) {
                cursor.close();
            }
        }
        return flag;
    }

    /**
     * 通过本地团队ID查询团队信息
     */
    public String getTeamNameByLocaltid(String localtid) {
        String teamName = "";
        Cursor cursor = null;
        db = baseHelper.getReadableDatabase();
        try {
            cursor = db.rawQuery("select * from team where local_tid = ?",
                    new String[]{localtid});
            while (cursor.moveToNext()) {
//				trouTeam = new TrouTeam();
//				trouTeam.localID = cursor.getString(0);
//				// trouTeam.id = cursor.getString(2);
//				trouTeam.name = cursor.getString(3);
                teamName = cursor.getString(3);
//				trouTeam.createtime = cursor.getString(4);
            }
        } catch (Exception e) {
        } finally {
            if (null != cursor) {
                cursor.close();
            }
        }
        return teamName;
    }

    /**
     * 通过本地团队ID查询团队信息
     */
    public TrouTeam selectInfoByLocaltid(String localtid) {
        TrouTeam trouTeam = null;
        Cursor cursor = null;
        db = baseHelper.getReadableDatabase();
        try {
            cursor = db.rawQuery("select * from team where local_tid = ?",
                    new String[]{localtid});
            while (cursor.moveToNext()) {
                trouTeam = new TrouTeam();
                trouTeam.localID = cursor.getString(0);
                // trouTeam.id = cursor.getString(2);
                trouTeam.name = cursor.getString(3);
                trouTeam.createtime = getLongTime(cursor.getString(4));
            }
        } catch (Exception e) {
        } finally {
            if (null != cursor) {
                cursor.close();
            }
        }
        return trouTeam;
    }


    /**
     * 通过本地ID获取服务器的团ID
     */
    public String LocalIdGetserviceId(String localId) {
        if (TextUtils.isEmpty(localId) || "0".equals(localId)) {
            return null;
        }
        String serviceId = null;
        Cursor cursor = null;
        db = baseHelper.getReadableDatabase();
        try {
            cursor = db.rawQuery("select * from team where local_tid = ?",
                    new String[]{serviceId});
            while (cursor.moveToNext()) {
                serviceId = cursor.getString(cursor.getColumnIndex("tid"));
            }
        } catch (Exception e) {
        } finally {
            if (null != cursor) {
                cursor.close();
            }
        }
        return serviceId;
    }

    /**
     * 添加团队
     */
    public boolean insertTeam(String userid, String teamName) {
        boolean flag = false;
        this.db = baseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            db.execSQL("insert into team(userid,name)values(?,?)",
                    new Object[]{userid, teamName});
            db.setTransactionSuccessful();
            db.endTransaction();
        } catch (Exception e) {
            e.printStackTrace();
            flag = false;
        }
        return flag;
    }

    /**
     * （将服务的团队信息，同步到本地数据库）添加团队
     */
    public boolean insertTeamToDir(String userid, TrouTeam team) {
        boolean flag = false;
        if (null == team || findIdExistByid(team.id)) {
            return flag;
        }
        this.db = baseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            db.execSQL(
                    "insert into team(userid,tid,name,createtime)values(?,?,?,?)",
                    new Object[]{userid, team.id, team.name,
                            strToTimestamp(team.createtime)});
            db.setTransactionSuccessful();
            db.endTransaction();
        } catch (Exception e) {
            e.printStackTrace();
            flag = false;
        }
        return flag;
    }

    /**
     * 将string格式转换成timestamp格式
     */
    public Timestamp strToTimestamp(String time) {
        return Timestamp.valueOf(TimeUtil.timeFormat(time));
    }


    /**
     * 将时间格式转换成时间戳格式
     */
    public static String getLongTime(String simpleTimeString) {
        String re_time = null;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Date date;
        try {
            date = df.parse(simpleTimeString);
            long l = date.getTime();
            String str = String.valueOf(l);
            re_time = str.substring(0, 10);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        LogUtil.d("---------时间戳格式转换结果--:" + re_time);
        return re_time;
    }

    /**
     * 更新团id信息
     */
    public void updateTeam(TrouTeam team) {
        db = baseHelper.getWritableDatabase();
        db.execSQL("update team set tid = ? where local_tid = ?", new Object[]{
                team.id, team.localID});
    }

    /**
     * 更新团id信息
     */
    public void updateTeam(String teamid, String localid) {
        db = baseHelper.getWritableDatabase();
        db.execSQL("update team set tid = ? where local_tid = ?", new Object[]{
                teamid, localid});
    }

    /**
     * 删除团队信息
     */
    public void deleteTeam(String local_tid) {
        this.db = baseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            db.execSQL("delete from team where local_tid = ?",
                    new Object[]{local_tid});
            db.setTransactionSuccessful();
            db.endTransaction();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * (本地团ID降序)获取本地保存的所有团队信息
     */
    public List<TrouTeam> selectAllTeamToDesc(String userid) {
        List<TrouTeam> mList = new ArrayList<TrouTeam>();
        TrouTeam team = null;
        Cursor cursor = null;
        db = baseHelper.getReadableDatabase();
        try {
            cursor = db
                    .rawQuery(
                            "select * from team where userid = ? order by createtime desc",
                            new String[]{userid});
            while (cursor.moveToNext()) {
                team = new TrouTeam();
                team.localID = cursor.getString(0);
                team.id = cursor.getString(2);
                team.name = cursor.getString(3);
                String cursorTime = cursor.getString(4);
                long time = Timestamp.valueOf(cursorTime).getTime() / 1000;
                team.createtime = TimeUtil.timeFormat(time + "");
                team.show = memberDao.selectMemberByTeamID(team.localID);
                mList.add(team);
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
     * 获取本地需要上传给服务器的所有团队信息
     */
    public List<TrouTeam> getNeedUpdataTrouTeam(String userid) {
        List<TrouTeam> mList = new ArrayList<TrouTeam>();
        TrouTeam team = null;
        Cursor cursor = null;
        db = baseHelper.getReadableDatabase();
        try {
            cursor = db
                    .rawQuery(
                            "select * from team where userid = ? and tid = 0",
                            new String[]{userid});
            while (cursor.moveToNext()) {
                team = new TrouTeam();
                team.localID = cursor.getString(0);
                //team.id = cursor.getString(2);
                team.name = cursor.getString(3);

                team.createtime = getLongTime(cursor.getString(4));
                //team.show = memberDao.selectMemberByTeamID(team.localID);
                mList.add(team);
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
     * 获取对应用户的所有具有服务器团id的团队集合
     */
    public List<TrouTeam> getTrouTeamName(String userid) {
        List<TrouTeam> mList = new ArrayList<TrouTeam>();
        TrouTeam team = null;
        Cursor cursor = null;
        db = baseHelper.getReadableDatabase();
        try {
            cursor = db
                    .rawQuery(
                            "select * from team where userid = ? and tid != 0 order by createtime desc",
                            new String[]{userid});
            while (cursor.moveToNext()) {
                team = new TrouTeam();
                team.localID = cursor.getString(0);
                team.id = cursor.getString(2);
                team.name = cursor.getString(3);

                team.createtime = getLongTime(cursor.getString(4));
                //team.show = memberDao.selectMemberByTeamID(team.localID);
                mList.add(team);
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
     * 获取最新的本地团队id
     */
    public String selectLastTeamid(String userid) {
        String teamid = null;
        Cursor cursor = null;
        db = baseHelper.getReadableDatabase();
        try {
            cursor = db.rawQuery(
                    "select * from team where userid = ? order by createtime desc limit 1",
                    new String[]{userid});
            while (cursor.moveToNext()) {
                teamid = cursor.getString(0);
            }
        } catch (Exception e) {
        } finally {
            if (null != cursor) {
                cursor.close();
            }
        }
        LogUtil.d("selectLastTeamid", "teamid：" + teamid);

        return teamid;
    }

    /**
     * 通过服务器ID获取团名称
     */
    public String tidGetTname(String tid) {


        String Tname = null;
        Cursor cursor = null;
        db = baseHelper.getReadableDatabase();
        try {
            cursor = db.rawQuery(
                    "select * from team where tid = ?",
                    new String[]{tid});
            while (cursor.moveToNext()) {
                Tname = cursor.getString(3);
            }
        } catch (Exception e) {
        } finally {
            if (null != cursor) {
                cursor.close();
            }
        }
        return Tname;
    }

    /**
     * 获取最新的服务端团队id
     */
    public String selectServiceLastTeamid(String userid) {
        String teamid = "0";
        Cursor cursor = null;
        db = baseHelper.getReadableDatabase();
        try {
            cursor = db.rawQuery(
                    "select * from team where userid = ? order by createtime desc limit 1",
                    new String[]{userid});
            while (cursor.moveToNext()) {
                teamid = cursor.getString(2);
            }
        } catch (Exception e) {
        } finally {
            if (null != cursor) {
                cursor.close();
            }
        }
        return teamid;
    }

    /**
     * 判断当前用户是否有团队数据记录
     */
    public boolean isHaveData(String userid) {
        boolean flag = false;
        Cursor cursor = null;
        db = baseHelper.getReadableDatabase();
        try {
            cursor = db.rawQuery(
                    "select * from team where userid = ? and tid != 0",
                    new String[]{userid});
            while (cursor.moveToNext()) {
                flag = true;
                break;
            }
        } catch (Exception e) {
        } finally {
            if (null != cursor) {
                cursor.close();
            }
        }
        return flag;
    }
}
