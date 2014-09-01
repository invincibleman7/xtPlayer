package com.tian.mp3player;

import java.util.HashMap;
import java.util.Map;

import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;

import com.tian.R;
import com.tian.app.App;
import com.tian.db.InfoBean;
import com.tian.net.Request;
import com.tian.net.Response;
import com.tian.uitls.Globle;
import com.tian.uitls.PreAsyn;
import com.tian.uitls.PrepareInterface;
import com.tian.uitls.Tool;

/**
 * 下载管理器
 * 
 * @author tian
 * 
 */
public class DownloadService extends Service {

	final String TAG = "DownloadService";
	
	public enum DownloadState {
		starting, paused, stoped
	}

	volatile public static DownloadState mState = DownloadState.stoped;
	/**
	 * 开始下载
	 */
	public final static String ACTION_START_DOWNLOAD = "com.tian.mp3player.DownloadService.start";
	/**
	 * 开始下载
	 */
	public final static String ACTION_PAUSE_DOWNLOAD = "com.tian.mp3player.DownloadService.pause";
	/**
	 * 开始下载
	 */
	public final static String ACTION_STOP_DOWNLOAD = "com.tian.mp3player.DownloadService.stop";
	/**
	 * 开始下载
	 */
	public final static String ACTION_STOP_DOWNLOAD_SERVICE = "com.tian.mp3player.DownloadService.stopService";

	App app;

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		app = Globle.getApp(this);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		String action = intent.getAction();
		if (action.equals(ACTION_START_DOWNLOAD)) {
			long size = intent.getLongExtra("size", -1);
			if (size != -1) {
				String path = intent.getStringExtra("path");
				String fileName = intent.getStringExtra("fileName");
				int position = intent.getIntExtra("position", -1);
				processDownloadStart(path, fileName, size, position);
			}
		} else if (action.equals(ACTION_PAUSE_DOWNLOAD)) {
			processDownloadPause();
		} else if (action.equals(ACTION_STOP_DOWNLOAD)) {
			processDownloadStop();
		} else if (action.equals(ACTION_STOP_DOWNLOAD_SERVICE)) {
			processDownloadStopService();
		}
		return super.onStartCommand(intent, flags, startId);
	}

	private void processDownloadStopService() {
		// TODO Auto-generated method stub
		stopSelf();
	}

	/**
	 * 停止下载
	 */
	private void processDownloadStop() {
		// TODO Auto-generated method stub
		mState = DownloadState.stoped;
	}

	/**
	 * 暂停下载
	 */
	private void processDownloadPause() {
		// TODO Auto-generated method stub
		if (mState == DownloadState.starting) {
			mState = DownloadState.paused;
		} else if (mState == DownloadState.paused) {
			mState = DownloadState.starting;
		}
	}

	/**
	 * 开始下载
	 */
	private void processDownloadStart(String path, String fileName,
			long fileSize, final int position) {
		// TODO Auto-generated method stub
		mState = DownloadState.starting;
		new PreAsyn(this, getString(R.string.downloading),
				getString(R.string.waitting), new PrepareInterface() {

					@Override
					public void howOnPostExecute(Response result) {
						// TODO Auto-generated method stub
						if (result == null || result.getCode() != 0) {
							Globle.showToast(
									DownloadService.this,
									result.getContent() + ":"
											+ result.getTitle(), false);
						} else {
							Globle.showToast(DownloadService.this,
									result.getTitle(), false);
							Map<String, InfoBean> map = app.infos.get(position);
							if (map.containsKey("mp3Info")) {
								insert(map.get("mp3Info"));
								Globle.showToast(DownloadService.this,
										"插入音乐数据库成功", false);
							}
						}
						// mState = DownloadState.stoped;
					}

					@Override
					public Response howDoInBackground(String... params) {
						// TODO Auto-generated method stub
						return Request.requestDownloadFile(
								DownloadService.this, params[0], params[1],
								params[2]);
					}
				}).execute(path, fileName, fileSize + "");
	}

	/**
	 * 把下载成功的歌曲插入到MediaStore.Audio.Media.EXTERNAL_CONTENT_URI中
	 */
	void insert(InfoBean bean) {
		String nameCN = bean.getNameCN();
		updateList(nameCN, bean.getSize(), Globle.getDownload().getFileUtil()
				.getSDPATH()
				+ app.dlPath + nameCN);
		String id = bean.getId() + "";
		int slipIndex = nameCN.indexOf("-");
		Uri resUri = null;
		ContentResolver cr = getContentResolver();
		Cursor cursor = cr.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
				new String[] { MediaStore.Audio.Media._ID },
				MediaStore.Audio.Media.IS_MUSIC + "=1", null, null);
		if (cursor == null || cursor.getCount() <= 0) {
			resUri = insert(id, nameCN, slipIndex, cr);
		} else {
			boolean isExist = false;
			while (cursor.moveToNext()) {
				Log.i(TAG, cursor.getString(0)+""+",id="+id);
				if (cursor.getString(0).equals(id)) {
					isExist = true;
					break;
				}
			}
			if(!isExist){
				resUri = insert(id, nameCN, slipIndex, cr);
			}
		}
		if (!cursor.isClosed()) {
			cursor.close();
		}
		if (resUri != null)
			Log.i(TAG,"resUri=" + resUri.toString());
	}

	Uri insert(String id, String nameCN, int slipIndex, ContentResolver cr) {
		ContentValues values = getValues(id, nameCN, slipIndex);
		
		return cr.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values);
	}
	
	int update(String id, String nameCN, int slipIndex, ContentResolver cr) {
		ContentValues values = getValues(id, nameCN, slipIndex);
		
		return cr.update(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values, "_id='"+id+"'", null);
	}
	
	ContentValues getValues(String id, String nameCN, int slipIndex){
		ContentValues values = new ContentValues();
		values.put(MediaStore.Audio.Media._ID, id);
		values.put(MediaStore.Audio.Media.ALBUM, "MY_DL");
		values.put(MediaStore.Audio.Media.DISPLAY_NAME, nameCN);
		values.put(MediaStore.Audio.Media.DATA, Globle.getDownload()
				.getFileUtil().getSDPATH()
				+ app.dlPath + nameCN);
		if (slipIndex != -1) {
			String artist = nameCN.substring(0, slipIndex).trim();
			String musicName = nameCN.substring(slipIndex + 1).trim();
			values.put(MediaStore.Audio.Media.ARTIST, artist);
			values.put(MediaStore.Audio.Media.TITLE, musicName);
		} else {
			values.put(MediaStore.Audio.Media.ARTIST, nameCN);
			values.put(MediaStore.Audio.Media.TITLE, nameCN);
		}
		return values;
	}

	void updateList(String name, long size, String dataStr) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("name", Tool.getFileName(name));
		map.put("size", String.format("%.2f", (double) size / (1024 * 1024))
				+ "Mb");
		map.put("dataStr", dataStr);
		app.musicList.add(map);
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}
