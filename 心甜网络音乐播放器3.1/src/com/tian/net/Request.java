package com.tian.net;

import android.content.Context;

import com.tian.app.App;
import com.tian.mp3player.DownloadService.DownloadState;
import com.tian.uitls.Globle;

public class Request {

	static Response mResponse;

	public static Response requestDownloadFile(Context context, String path,
			String fileName, String fileSize) {
		if (context == null || path == null || "".equals(path)
				|| fileName == null || "".equals(fileName) || fileSize == null
				|| "".equals(fileSize)) {
			return getResInstance(-1, "失败", "传入的参数为null或为空");
		}
		App app = Globle.getApp(context);
		
		if(app.url.equals("")){
			return getResInstance(-1, "失败", "app.url为null或为空");
		}
		
		int code = Globle.getDownload().downloadFile(context, app.url, path,
				fileName, Long.parseLong(fileSize));
		Response response = null;
		if (code == 0) {
			response = getResInstance(code, "下载成功", "等待插入url中");
		} else if (code == -1) {
			response = getResInstance(code, "下载失败", "网络或下载文件配置原因");
		} else {
			response = getResInstance(code, "下载失败", "文件已存在");
		}
		return response;
	}

	private static Response getResInstance(int code, String title,
			String content) {
		// TODO Auto-generated method stub
		if (mResponse == null) {
			mResponse = new Response();
		}
		mResponse.setCode(code);
		mResponse.setContent(content);
		mResponse.setTitle(title);
		return mResponse;
	}

}
