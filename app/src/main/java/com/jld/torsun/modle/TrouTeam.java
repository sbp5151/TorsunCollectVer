package com.jld.torsun.modle;

//import java.util.ArrayList;

/**
 * 旅游团bean
 * */
public class TrouTeam {
	/** 
	* @Fields serialVersionUID : TODO(用一句话描述这个变量表示什么) 
	*/
	private static final long serialVersionUID = 1L;
	/**(服务器端)旅游团ID*/
	public String id;
	/**旅游团名称*/
	public String name;
	/**创建时间*/
	public String createtime;
	public String show;
	/**本地团ID*/
	public String localID;
	
	@Override
	public String toString() {
		return "TrouTeam [id=" + id + ", name=" + name + ", createtime="
				+ createtime + ", show=" + show + ", localID=" + localID + "]";
	}


}
