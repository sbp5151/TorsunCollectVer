package com.jld.torsun.db;

import com.jld.torsun.config.Config;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 数据库建表
 * 
 * @<功能简述>
 * @<功能详细描述>
 */
public class DataBaseHelper extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "tucson.db";// 数据库名称
	private static final int DATABASE_VERSION = 1;// 数据库当前版本，老版本为1

	public DataBaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	/**
	 * {"userid":"41", "mobile":"13823133104", "nick":"yaozu", "username":"hh",
	 * "img":"http:\/\/img.tucson.net.cn\/2015-11\/1447473975_51953.png"}
	 * */
	@Override
	public void onCreate(SQLiteDatabase db) {
		// 用户表username為账号是不可以改变的
		db.execSQL(Config.CREATE_TEAMMEMBER_TABLE);
		//创建团队列表
		db.execSQL(Config.CREATE_TEAM_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

}
