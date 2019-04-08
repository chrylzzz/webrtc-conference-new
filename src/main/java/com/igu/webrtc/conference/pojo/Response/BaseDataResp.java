package com.igu.webrtc.conference.pojo.Response;

public class BaseDataResp {

	private String code;
	
	private String msg;
	
	private String tips;
	
	private Object data;
	
	public BaseDataResp() {}
	
	public BaseDataResp(String code, String msg) {
		this.code = code;
		this.msg = msg;
	}
	
	public BaseDataResp(String code, String msg, Object data) {
		this(code, msg);
		this.data = data;
	}
	
	

	public BaseDataResp(String code, String msg, String tips) {
        super();
        this.code = code;
        this.msg = msg;
        this.tips = tips;
    }

    public String getTips() {
        return tips;
    }

    public void setTips(String tips) {
        this.tips = tips;
    }

    public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}
}
