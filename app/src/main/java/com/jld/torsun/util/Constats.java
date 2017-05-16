package com.jld.torsun.util;

/**
 * 1.常量 2.接口列表
 * */
public class Constats {
	/** 1 加密字符串 */
	public static final String S_KEY = "tucson88888888%#!88888888tucson";
    /** 1 服务器端的ip地址 */
    public static final String HTTP_URL = "http://112.74.215.42";
    /** app服务器下载地址 */
	public static final String APP_DOWNLOAD_URL = "";
	/** 导游认证信息提交地址 */
	public static final String COMMIT_GUIDER_INFO_URL = "https://dyb.torsun.com.cn/cgi-bin/luci/?ts=2e1c522d0e69a80bb07af7df3f13791a";
	public static final String GUIDE_LOGIN_INFO_URL= "/tripstuan/guidelogin";
	public static final int ANDROID = 1;
	/** shareprefece的key */
	public static final String SHARE_KEY = "share_key";
	/** 2 登录 */
	public static final String LOGIN_FUN = "/user/login";
	/** 2 注册 */
	public static final String REGIEST_FUN = "/user/reg";
	/** 2 修改密码 */
	public static final String CHANGE_PASSWORD_FUN = "/user/uppwd";
	/** 2 修改姓名 */
	public static final String CHANGE_NAME_FUN = "/user/uprealname";
	/** 2 修改昵称 */
	public static final String CHANGE_NIK_FUN = "/user/upnick";
	/** 找回密码 */
	public static final String FINDBACK_PASSWORD = "/user/findpwd";
	/** 2 上传头像 */
	public static final String POST_ICON_FUN = "/user/userimg";
	/** 2 注册短信验证 */
	public static final String SMS_FUN = "/tsmsg/sop";
	/** 2 找回短信验证 */
	public static final String SMS_GET = "/tsmsg/soppwd";
	/** 2 用户反馈 */
	public static final String FEED_BACK_FUN = "/tsmsg/feedb";
	/** 2 创建旅游团 */
	public static final String ADD_TEAM_FUN = "/tripstuan/add";
	/** 2 获取旅游团列表 */
	public static final String GET_TEAM_LIST_FUN = "/tripstuan/tuanlist";
	/** 2 删除旅游团 */
	public static final String DELETE_TEAM_LIST = "/tripstuan/deltriptuan";
	/** 2 创建旅行团成员接口 */
	public static final String ADD_TEAM_MEMBER_FUN = "/tripstuan/tripsadd";
	/** 2 获取旅行团成员列表接口 */
	public static final String GET_TEAM_MEMBER_FUN = "/tripstuan/tripstuanlist";
	/** 2 获取导游信息 */
	public static final String GET_GUIDER_INFO = "/tripstuan/getguide";
	/**上传点赞或者点心接口*/
	public static final String UPLOAD_GOOD_LOVE = "/tripstuan/gugoodadd";
	/** 2 修改用户头像 */
	public static final String CHANGE_HEAD_ICON = "/user/upuserico";
	/**发送经纬度*/
	public static final String SEND_JWD = "/mapts/setusermap";
	/**获取经纬度*/
	public static final String GET_JWD = "/mapts/getusermap";
	/** 账号 */
	public static String account_number = "";
	/** 是否登录状态 */
	public static boolean is_login = false;
	/** 获取WiFi信息 */
	public static String GET_WIFI_INFO = "https://www.torsun.com.cn/cgi-bin/getwifimsg";
	/** 获取版本信息和下载链接(后面更上当前版本号) */
	public static String GET_VERSION_CODE = "http://www.torsun.cn/download/checkv.php?t=1&v=";
	/** 内部下载链接 */
	public static String INTERIOR_DOWNLOAD_URL = "https://www.torsun.com.cn/android/torsun.apk";
	/** 外部下载链接 */
	public static String EXTERNAL_DOWNLOAD_URL = "http://192.168.1.1/android/torsun.apk";
	/**信息中心导游上传图片接口*/
	public static String MESSAGE_UPLOAD_IMAGE_URL = "/tsnotice/pic";
	/**信息中心上传信息接口*/
	public static String MESSAGE_UPLOAD_ALL = "/tsnotice/add";
	/**获取消息列表接口*/
	public static String MESSAGE_GET_LIST_URL = "/tsnotice/noticeitem";
	/**删除消息接口*/
	public static String MESSAGE_DELETE_MESSAGE = "/tsnotice/delnotice";
	/**获取团消息*/
	public static String MESSAGE_GET_TUAN_URL = "/tsnotice/tuanlist";
	/**已读接口*/
	public static String MESSAGE_GET_TUAN_READ_URL = "/tsnotice/setreaded";
	/**团详细信息接口*/
	public static String MESSAGE_GET_TUAN_ITEM_URL = "/tsnotice/noticemsg";
	/**导游请求游客发送经纬度*/
	public static String MAP_REQUES_LOCATION_URL = "/mapts/togetmap";
	/** 获取消息的未读数 0 没有未读 1 未读*/
	public static String GET_READ_MSG_COUNT = "/tsnotice/userreaded";



}
