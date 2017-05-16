package com.jld.torsun.config;

/**
 * 
 * @ClassName: Config
 * @Description: 数据配置
 * @author liuzhi
 * @date 2015-12-1 下午3:51:00
 */
public class Config {

	/** 创建团成员数据库表 */
	public static final String CREATE_TEAMMEMBER_TABLE = "create table if not exists teammember( "
			+ "id Integer primary key autoincrement,"
			+ "local_tid varchar(30),"
			+ "userid varchar(30),"
			+ "mobile varchar(30),"
			+ "nick varchar(30),"
			+ "username varchar(30),"
			+ "online varchar(30),"
			+ "online_change_time varchar(30),"
			+ "img varchar(100),"
			+ "isload varchar(10)" + ");";
	/** 创建团队数据库表 */
	public static final String CREATE_TEAM_TABLE = "create table if not exists team("
			+ "local_tid integer primary key autoincrement,"
			+ "userid varchar(30),"
			+ "tid varchar(30) default 0,"
			+ "name varchar(30),"
			+ "createtime TIMESTAMP default (datetime('now', 'localtime'))"
			+ ");";

	public static final String TYPE = "type";
	public static final String TYPE_GUIDER="guider";
	public static final String TYPE_NORMAL_PROMBLE="normal_promble";
	public static final String TYPE_CALL_US="call_us";
	public static final String TYPE_SET_PROTOCOL="user_protocol";

}
