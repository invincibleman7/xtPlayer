package com.tian.downloadUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import com.tian.mp3player.DownloadService.DownloadState;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

public class HttpDownloadUtils {

	public final static String TAG = "HttpDownloadUtils";

	/**
	 * {@link FileUil}该类引用
	 */
	private FileUtil fileUtil;

	/**
	 * 表示文件下载失败
	 */
	public static final int DOWNLOAD_ERROE = -1;
	/**
	 * 下载成功
	 */
	public static final int DOWNLOAD_SUCCESS = 0;
	/**
	 * 文件已存在
	 */
	public static final int DOWNLOAD_EXIST = 1;

	/**
	 * -1表示文件下载失败，0下载成功，1文件已存在 自己的TOMCAT只能下普通文本文件,3.1可以 TOMCAT中的下载文件名中不能有 -
	 * 或者其他转意字符
	 * 
	 * @param urlStr
	 * @param path
	 * @param fileName
	 * @param fileSize
	 * @return
	 */
	public int downloadFile(Context context, String urlStr, String path,
			String fileName, long fileSize) {
		HttpURLConnection conn = null;
		Map<String, ?> retMap = null;
		InputStream inputStream = null;
		try {
			fileUtil = new FileUtil();
			URL url = new URL(urlStr);
			conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(5 * 1000);
			conn.setRequestProperty("Connection", "Keep-Alive");
			conn.connect();
			int status = conn.getResponseCode();
			if (HttpURLConnection.HTTP_OK == status
					|| HttpURLConnection.HTTP_PARTIAL == status) {
				Log.i(TAG, "headSize=" + conn.getContentLength());
			} else if (-1 == status) {
				return DOWNLOAD_ERROE;
			}
			if (fileUtil.isFileExist(path + fileName)) {
				return DOWNLOAD_EXIST;
			} else {
				inputStream = getInputStreamFromUrl(conn);
				if (inputStream == null) {
					return DOWNLOAD_ERROE;
				}
				retMap = fileUtil.writeToSDFromInputStream(context, path,
						fileName, inputStream);
				File fileResult = (File) retMap.get("file");
				if (fileResult == null || fileResult.length() <= 0
						|| fileResult.length() < fileSize) {
					if (fileResult != null && fileResult.exists()) {
						fileResult.delete();
					}
					return DOWNLOAD_ERROE;
				}
				Log.i(TAG, "fileResult.length=" + fileResult.length());
			}
		} catch (Exception e) {
			e.printStackTrace();
			return DOWNLOAD_ERROE;
		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
				conn.disconnect();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				return DOWNLOAD_ERROE;
			}
		}
		return DOWNLOAD_SUCCESS;
	}

	/**
	 * 拿到输入流
	 * 
	 * @param httpConn
	 *            {@link HttpURLConnection}
	 * @return 失败为null
	 * @exception {@link MalformedURLException},{@link IOException}
	 */
	public InputStream getInputStreamFromUrl(HttpURLConnection httpConn) {
		// TODO Auto-generated method stub
		InputStream inputStream = null;
		try {
			inputStream = httpConn.getInputStream();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			// httpConn.disconnect();
		}
		return inputStream;
	}

	/**
	 * 从网络下载文件内容到STRING中
	 * 
	 * @param urlStr url
	 * @return 下载完的字符串
	 */
	public String download(String urlStr) {
		StringBuffer sb = new StringBuffer();
		String line = null;
		BufferedReader buffReader = null;
		HttpURLConnection httpConn = null;
		try {
			URL url = new URL(urlStr);
			httpConn = (HttpURLConnection) url.openConnection();

			httpConn.setConnectTimeout(5 * 1000);
			Log.i(TAG, "---------HttpDownloadUtils.download()------------");
			httpConn.setRequestProperty("Connection", "Keep-Alive");
			httpConn.connect();
			Log.i(TAG, "download.code=" + httpConn.getResponseCode());
			// sockectException
			buffReader = new BufferedReader(new InputStreamReader(
					httpConn.getInputStream()));
			while ((line = buffReader.readLine()) != null) {
				sb.append(line);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (buffReader != null) {
					buffReader.close();
				}
				httpConn.disconnect();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	public FileUtil getFileUtil() {
		return fileUtil;
	}

	public void setFileUtil(FileUtil fileUtil) {
		this.fileUtil = fileUtil;
	}
}
