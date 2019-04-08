package com.igu.webrtc.conference.pojo.Response;


import java.util.HashMap;
import java.util.Map;

public class SxbBaseRsp {
	private int errorCode = 0;
	private String errorInfo = "";
	private String id;
	
	private Object data;
	
	

	public SxbBaseRsp() {
		
	}
	
	public SxbBaseRsp(int errorCode) {
		super();
		this.errorCode = errorCode;
	}
	
	public SxbBaseRsp(int errorCode, String errorInfo) {
		super();
		this.errorCode = errorCode;
		this.errorInfo = errorInfo;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

	public String getErrorInfo() {
		return errorInfo;
	}

	public void setErrorInfo(String errorInfo) {
		this.errorInfo = errorInfo;
	}
	public Object getData() {
		return data;
	}
	public void setData(Object data) {
		this.data = data;
	}

	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	
	

}
