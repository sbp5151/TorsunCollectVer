package com.jld.torsun.modle;

import java.io.Serializable;

/**
 * 用户 "userid":"10", "mobile":"15012763425", "nick":"jimmy", "username":"我有",
 * "img":”http://img.tucson.net.cn”
 * */

public class User implements Serializable {
	/** 
	* @Fields serialVersionUID : TODO(用一句话描述这个变量表示什么) 
	*/
	private static final long serialVersionUID = 1L;
	/** 用户id */
	public String userid;
	/** 用户手机号 */
	public String mobile;
	/** 昵称 */
	public String nick;
	/** 姓名 */
	public String username;
	/** 头像 */
	public String img;

	public String toJsonString() {
		if (null == mobile) {
			mobile = "无SIM卡";
		}
		if (null == nick) {
			nick = "途胜旅行";
		}
		if (null == username) {
			username = "";
		}
		if (null == img) {
			img = "";
		}
		return "{" + "\"userid\":\"" + userid + "\"," + "\"mobile\":\""
				+ mobile + "\"," + "\"nick\":\"" + nick + "\","
				+ "\"username\":\"" + username + "\"," + "\"img\":\"" + img
				+ "\"" + "}";
	}

	public void setUserid(String uid) {
		userid = uid;
	}
}
