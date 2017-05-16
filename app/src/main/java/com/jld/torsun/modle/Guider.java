package com.jld.torsun.modle;

import java.io.Serializable;

/**导游*/
public class Guider implements Serializable{

	/** 
	* @Fields serialVersionUID 
	*/
	private static final long serialVersionUID = 1L;
	/**名字*/
	public String realname;
	/**头像*/
	public String _hearimage;
	/**手机号*/
	public String mobile;
	/**地点*/
	public String address;
	/**旅行社*/
	public String company;
	/**导游头像*/
	public String img;
	/**点赞数*/
	public String good;
	/**点心数*/
	public String flower;
	/**级别*/
	public String start;
	/**是否被点过赞*/
	public String checkgood;
	/**是否被点过心*/
	public String checkflower;
}
