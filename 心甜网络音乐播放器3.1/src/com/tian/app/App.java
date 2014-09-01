package com.tian.app;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.Application;

import com.tian.db.InfoBean;

public class App extends Application {
	/**
	 * 其中Map<String,InfoBean>的key:"mp3Info","lyrInfo"
	 */
	public List<Map<String,InfoBean>> infos = new ArrayList<Map<String,InfoBean>>();
	
	/**
	 * 歌词
	 */
	public List<String> lrcs = new ArrayList<String>();
	
	/**
	 * 歌曲
	 */
	public List<Map<String, String>> musicList = new ArrayList<Map<String, String>>();
	/**
	 * currentListItem：歌曲在listview或{@link App#musicList}当前位置，currentLrcItem：歌词在{@link App#lrcs}的位置
	 */
	public int currentChildPosition=-1,currentLrcItem;
	
//	public String urlStr = "http://192.168.0.89:8080/mp3Player/linjujie - cracksInTheSun.mp3";
//	public String urlStr = "http://192.168.0.89:8080/mp3Player";
	/**
	 * url，模拟器在tomcat上的地址
	 */
	public String urlBase = "http://10.0.2.2:8080/mp3Player/";
	public String url = "";
	
	/**
	 * 下载地址，包含音乐文件和歌词文件夹
	 */
	public String dlPath = "XTMp3Player/";
	
	/**
	 * 歌词文件下载地址
	 */
	public String dlLrcPath = "lyric/";
	
	//===============新改动的===========//
	public int currentGroupPosition=-1;
	
}
