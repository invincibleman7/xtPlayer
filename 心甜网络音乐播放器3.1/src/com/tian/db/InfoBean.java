package com.tian.db;

import java.io.Serializable;

/**
 * 音频文件的信息
 * @author tian
 *
 */
public class InfoBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5534006916584607115L;

	/**
	 * 中文名字
	 */
	private String nameCN;
	
	/**
	 * 英文名字
	 */
	private String nameEN;

	/**
	 * 文件大小
	 */
	private long size;
	
	/**
	 * 文件ID
	 */
	private int id;
	
	public String getNameCN() {
		return nameCN;
	}

	public void setNameCN(String nameCN) {
		this.nameCN = nameCN;
	}

	public String getNameEN() {
		return nameEN;
	}

	public void setNameEN(String nameEN) {
		this.nameEN = nameEN;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

}
