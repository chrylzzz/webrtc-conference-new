package com.igu.webrtc.conference.pojo;

import java.io.Serializable;

/**
 * 
 * 
 * @date 2018-08-14 10:13:43
 */
public class AccountEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 用户名
	 */
	private String uid;
	/**
	 * 用户密码
	 */
	private String pwd;
	/**
	 * 用户token
	 */
	private String token;
	/**
	 * 登录状态
	 */
	private Integer state;
	
	/**
	 * sig
	 */
	private String userSig;
	/**
	 * 注册时间戳
	 */
	private Long registerTime;
	/**
	 * 登录时间戳
	 */
	private Long loginTime;
	/**
	 * 退出时间戳
	 */
	private Long logoutTime;
	/**
	 * 最新请求时间戳
	 */
	private Long lastRequestTime;
	/**
	 * 当前appid
	 */
	private Integer currentAppid;
	
	private String wechatNickName;

	/**
	 * 设置：用户名
	 */
	public void setUid(String uid) {
		this.uid = uid;
	}
	/**
	 * 获取：用户名
	 */
	public String getUid() {
		return uid;
	}
	/**
	 * 设置：用户密码
	 */
	public void setPwd(String pwd) {
		this.pwd = pwd;
	}
	/**
	 * 获取：用户密码
	 */
	public String getPwd() {
		return pwd;
	}
	/**
	 * 设置：用户token
	 */
	public void setToken(String token) {
		this.token = token;
	}
	/**
	 * 获取：用户token
	 */
	public String getToken() {
		return token;
	}
	/**
	 * 设置：登录状态
	 */
	public void setState(Integer state) {
		this.state = state;
	}
	/**
	 * 获取：登录状态
	 */
	public Integer getState() {
		return state;
	}
	/**
	 * 设置：sig
	 */
	public void setUserSig(String userSig) {
		this.userSig = userSig;
	}
	/**
	 * 获取：sig
	 */
	public String getUserSig() {
		return userSig;
	}
	/**
	 * 设置：注册时间戳
	 */
	public void setRegisterTime(Long registerTime) {
		this.registerTime = registerTime;
	}
	/**
	 * 获取：注册时间戳
	 */
	public Long getRegisterTime() {
		return registerTime;
	}
	/**
	 * 设置：登录时间戳
	 */
	public void setLoginTime(Long loginTime) {
		this.loginTime = loginTime;
	}
	/**
	 * 获取：登录时间戳
	 */
	public Long getLoginTime() {
		return loginTime;
	}
	/**
	 * 设置：退出时间戳
	 */
	public void setLogoutTime(Long logoutTime) {
		this.logoutTime = logoutTime;
	}
	/**
	 * 获取：退出时间戳
	 */
	public Long getLogoutTime() {
		return logoutTime;
	}
	/**
	 * 设置：最新请求时间戳
	 */
	public void setLastRequestTime(Long lastRequestTime) {
		this.lastRequestTime = lastRequestTime;
	}
	/**
	 * 获取：最新请求时间戳
	 */
	public Long getLastRequestTime() {
		return lastRequestTime;
	}
	/**
	 * 设置：当前appid
	 */
	public void setCurrentAppid(Integer currentAppid) {
		this.currentAppid = currentAppid;
	}
	/**
	 * 获取：当前appid
	 */
	public Integer getCurrentAppid() {
		return currentAppid;
	}
	public String getWechatNickName() {
		return wechatNickName;
	}
	public void setWechatNickName(String wechatNickName) {
		this.wechatNickName = wechatNickName;
	}
	
	
	
}
