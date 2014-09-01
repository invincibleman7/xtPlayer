package com.tian.net;

import java.io.Serializable;
import java.util.HashMap;

/**
 * 响应数据
 * @author BIU
 *
 */
public class Response implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8199968402982321877L;

	/**
	 * 结果代码
	 * 
	 * 0成功
	 * 1失败
	 */
	private int code = -1;
	
	/**
	 * 标题
	 * 
	 * 返回的提示窗口标题
	 */
	private String title = null;
	
	/**
	 * 内容
	 * 
	 * 返回的提示信息
	 */
	private String content = null;
	
	/**
	 * 接口编码
	 * 
	 * 服务端返回的唯一标识
	 */
	private String methodName = null;
	
	/**
	 * 其他数据
	 */
	private HashMap<String, Object> map = null;

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public HashMap<String, Object> getMap() {
		return map;
	}

	public void setMap(HashMap<String, Object> map) {
		this.map = map;
	}
	

}
