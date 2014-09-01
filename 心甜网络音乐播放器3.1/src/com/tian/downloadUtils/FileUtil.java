package com.tian.downloadUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import com.tian.mp3player.DownloadService;
import com.tian.mp3player.DownloadService.DownloadState;
import com.tian.mp3player.MusicReceiver;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;

public class FileUtil {
	
	private String SDPATH;
	
	public FileUtil(){
		if(SDPATH == null){
			SDPATH = Environment.getExternalStorageDirectory() + "/";
		}
	}
	
	/**
	 * 判断该文件是否存在
	 * @param fileName
	 * @return
	 */
	public boolean isFileExist(String fileName){
		File file = new File(SDPATH + fileName);
		return file.exists();
	}
	
	/**
	 * 从输入流中把内容写入到指定文件中
	 * @param path
	 * @param fileName
	 * @param inputStream 
	 * @return key值："file"
	 */
	@SuppressWarnings("rawtypes")
	public Map<String, Comparable> writeToSDFromInputStream(Context context,String path, String fileName,
			InputStream inputStream) {
		// TODO Auto-generated method stub
		File file = null;
		OutputStream osw = null;
		Map<String, Comparable> retMap = new HashMap<String, Comparable>();
		try {
			createSDDir(path);
			file = createSDFile(path+fileName);
			//FileNotFoundException
			osw = new FileOutputStream(file);
			byte buffer[] = new byte[4 * 1024];
			int readSize = inputStream.read(buffer);
			int currentSize = 0;
			long endTime = System.currentTimeMillis();
			while(readSize != -1){
				if(DownloadService.mState == DownloadState.paused){
					continue;
				}else if(DownloadService.mState == DownloadState.stoped){
					if(file.exists()){
						file.delete();
						file = null;
					}
					break;
				}
				osw.write(buffer,0,readSize);
				osw.flush();
				
				//利用广播发送消息
				Intent intent = new Intent(MusicReceiver.ACTION_BROAD_DOWNLAOD_DATA+"."+fileName);
				currentSize += readSize;
				intent.putExtra("currentSize", currentSize);
				long syTime = System.currentTimeMillis();
				intent.putExtra("currentTime", (int) (syTime - endTime));
				context.sendBroadcast(intent);
				
				readSize =inputStream.read(buffer);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				if(osw != null){
					osw.close();
					osw = null;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		retMap.put("file", file);
		return retMap;
	}

	/**
	 * 创建文件目录
	 * @param dirName 路径名字
	 */
	private void createSDDir(String dirName) {
		// TODO Auto-generated method stub
		File dirFile = new File(SDPATH + dirName);
		if(!dirFile.exists()){
			dirFile.mkdir();
		}
	}

	/**
	 * 创建文件
	 * @param allName 路径名加文件名
	 * @return 新建文件
	 */
	private File createSDFile(String allName) {
		// TODO Auto-generated method stub
		File file = new File(SDPATH + allName);
		try {
			file.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return file;
	}

	public String getSDPATH() {
		return SDPATH;
	}

	public void setSDPATH(String sDPATH) {
		SDPATH = sDPATH;
	}
}
