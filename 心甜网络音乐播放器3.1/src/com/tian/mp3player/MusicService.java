
package com.tian.mp3player;
import java.io.IOException;

import com.tian.R;
import com.tian.app.App;
import com.tian.uitls.Globle;
import com.tian.uitls.Tool;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

public class MusicService extends Service implements OnErrorListener,
		OnPreparedListener, OnCompletionListener {

	public static final String TAG = "MusicService";

	enum PlayStatus {
		PLAY, PAUSE, STOP
	}

	PlayStatus mPlayStatus = PlayStatus.STOP;

	/**
	 * 用于启动播放
	 */
	public static final String ACTION_PLAY = "com.tian.mp3Player.play";
	/**
	 * 用于停止播放
	 */
	public static final String ACTION_STOP = "com.tian.mp3Player.stop";
	/**
	 * 用于暂停播放
	 */
	public static final String ACTION_PAUSE = "com.tian.mp3Player.pause";
	/**
	 * 用于设置播放位置
	 */
	public static final String ACTION_SEEK_TO = "com.tian.mp3Player.seekTo";
	/**
	 * 用于停止广播线程
	 */
	public static final String ACTION_STOP_BROAD_CAST = "com.tian.mp3Player.stopBroadCast";
	/**
	 * 用于开始广播线程
	 */
	public static final String ACTION_START_BROAD_CAST = "com.tian.mp3Player.startBroadCast";
	/**
	 * 用于停止服务
	 */
	public static final String ACTION_STOP_SERVICE = "com.tian.mp3Player.stopService";
	/**
	 * 播放类 error（-38，0） MediaPlay 在reset（）处于state 0 状态如果这时用他的引用调用
	 * getCurrenProgress()、getDuration()、getVideoHeight()、getVideoWith()、
	 * setAudioStreamType(int)、setLooping(boolean)、setVolume(float,float)、
	 * pause()、start()、stop()、seekTo()、prepare()、prepareAsync()方法时，
	 * 不会触发OnErrorListenerError()事件，但是MediaPlayer对象如果调用了reset()方法后，
	 * 再使用这些方法则会触发OnErrorListenerError（）事件。
	 * 所以，当你调用了reset()方法后，又调用getDuration()时，就会报异常。
	 */
	volatile private MediaPlayer mPlayer;
	private App app;
	private AsksetTime mAsksetTime;
	volatile private boolean stopthread,stopBroadCast;
	final String PlayerTitle = "心甜";
	String songTitle;
	
	private AudioManager mAudioManager;
	private ComponentName mComponentName;

	// private AudioFocusH
	enum AudioFocus {
		NofocusNoDuck, // we don't have audio focus,and can't duck
		NofocusCanDuck, // we don't have audio focus,but can play at a low
						// volume("ducking")
		Focused // we have full audio focus
	}

	AudioFocus mAudioFocus = AudioFocus.NofocusCanDuck;

	NotificationManager mNotificationManager;
	Notification mNotification;

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		app = Globle.getApp(this);
		mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		mComponentName = new ComponentName(this, MusicReceiver.class);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		if (intent == null) {
			return super.onStartCommand(intent, flags, startId);
		}
		String action = intent.getAction();
		if (action.equals(ACTION_PLAY)) {
			int position = intent.getIntExtra("position", -1);
			if (position != -1) {
				requestPostPlay(position);
			}
		} else if (action.equals(ACTION_STOP)) {
			requestPostStop();
		} else if (action.equals(ACTION_PAUSE)) {
			requestPostPause();
		} else if (action.equals(ACTION_STOP_SERVICE)) {
			requestPostStopService();
		} else if (action.equals(ACTION_SEEK_TO)) {
			int progress = intent.getIntExtra("progress", -1);
			if (progress != -1) {
				requestPostSeekTo(progress);
			}
		} else if (action.equals(ACTION_START_BROAD_CAST)) {
			requestPostStartBroadCast();
		} else if (action.equals(ACTION_STOP_BROAD_CAST)) {
			requestPostStopBroadCast();
		}
		return super.onStartCommand(intent, flags, startId);
	}

	private void requestPostStopBroadCast() {
		// TODO Auto-generated method stub
		if (mPlayStatus != PlayStatus.STOP && mAsksetTime != null
				&& mAsksetTime.isAlive()) {
			stopBroadCast = true;
		}
	}

	private void requestPostStartBroadCast() {
		// TODO Auto-generated method stub
		if (mPlayStatus != PlayStatus.STOP && mAsksetTime != null
				&& mAsksetTime.isAlive()) {
			stopBroadCast = false;
		}
	}

	/**
	 * 跳到mediaPlay指定位置
	 */
	private void requestPostSeekTo(int progress) {
		// TODO Auto-generated method stub
		if (mPlayStatus != PlayStatus.STOP) {
			mPlayer.seekTo(progress);
			setUpAsForeground(songTitle + "(seekTo:"
					+ Tool.millisTimeToDotFormat(progress, false, false, false)
					+ ")");
		}
	}

	/**
	 * 停止服务
	 */
	private void requestPostStopService() {
		// TODO Auto-generated method stub
		releaseResources(true);
		stopSelf();
	}

	/**
	 * 暂停或继续播放
	 */
	private void requestPostPause() {
		// TODO Auto-generated method stub
//		Log.i(TAG, mPlayStatus.toString());
		if (mPlayStatus == PlayStatus.PLAY) {
			mPlayStatus = PlayStatus.PAUSE;
			mPlayer.pause();
			setUpAsForeground(songTitle + "(pause)");
		} else if (mPlayStatus == PlayStatus.PAUSE) {
			mPlayStatus = PlayStatus.PLAY;
			mPlayer.start();
			setUpAsForeground(songTitle + "(playing)");
		} else {
			requestPostPlay(app.currentChildPosition);
		}
	}

	/**
	 * 停止
	 */
	private void requestPostStop() {
		// TODO Auto-generated method stub
		if (mPlayStatus != PlayStatus.STOP) {
			mPlayStatus = PlayStatus.STOP;
			releaseResources(true);
			setUpAsForeground(songTitle + "(stoping)");
		}
	}

	/**
	 * 播放
	 */
	private void requestPostPlay(int index) {
		// TODO Auto-generated method stub
		if(app.musicList.isEmpty()||app.currentChildPosition>=app.musicList.size()){
			Globle.showToast(this, "app.musicList.isEmpty()||app.currentListItem>=app.musicList.size()", false);
			return;
		}
		createMediaIfNeed();
		try {
			mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mPlayer.setDataSource((String) app.musicList.get(index).get(
					"dataStr"));
			mPlayer.prepare();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		mNotificationManager.cancelAll();
		songTitle = (String) app.musicList.get(app.currentChildPosition).get("name");
		setUpAsForeground(songTitle + "(loading...)");
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Makes sure the media player exists and has been reset. This will create
	 * the media player if needed, or reset the existing media player if one
	 * already exists.
	 */
	private void createMediaIfNeed() {
		if (mPlayer == null) {
			mPlayer = new MediaPlayer();

			// Make sure the media player will acquire a wake-lock while
			// playing. If we don't do
			// that, the CPU might go to sleep while the song is playing,
			// causing playback to stop.
			//
			// Remember that to use this, we have to declare the
			// android.permission.WAKE_LOCK
			// permission in AndroidManifest.xml.
			mPlayer.setWakeMode(getApplicationContext(),
					PowerManager.PARTIAL_WAKE_LOCK);
			mPlayer.setOnCompletionListener(this);
			mPlayer.setOnErrorListener(this);
			mPlayer.setOnPreparedListener(this);
		} else {
			releaseResources(false);
			mPlayer.reset();
			
			stopthread = false;
		}
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		// TODO Auto-generated method stub
		mPlayStatus = PlayStatus.PLAY;
		if (!mPlayer.isPlaying())
			mPlayer.start();
		
		mAsksetTime = new AsksetTime();
		mAsksetTime.start();
		
		MediaButtonHelper.registerMediaButtonEventReceiverCompat(mAudioManager,
				mComponentName);

		setUpAsForeground(songTitle + "(playing)");
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		// TODO Auto-generated method stub
		if (what == -38 && extra == 0) {
		} else {
			Globle.showToast(this, "Media player error! Resetting.", false);
			Log.e(TAG, "Error: what=" + String.valueOf(what) + ", extra="
					+ String.valueOf(extra));
			mPlayStatus = PlayStatus.STOP;
			releaseResources(true);
		}
		return true;
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		// TODO Auto-generated method stub
		playNextMusic();
	}

	/**
	 * 播放下一曲
	 */
	private void playNextMusic() {
		// TODO Auto-generated method stub
		UiMusicPlayer.isFirstPlay = true;
		int len = app.musicList.size();
		if (UiMusicPlayer.playStyle == UiMusicPlayer.LOOP_PLAY) {
			if (++app.currentChildPosition >= len) {
				app.currentChildPosition = 0;
			}
			requestPostPlay(app.currentChildPosition);
		} else if (UiMusicPlayer.playStyle == UiMusicPlayer.SINGLE_LOOP_PLAY) {
			requestPostPlay(app.currentChildPosition);
		} else if (UiMusicPlayer.playStyle == UiMusicPlayer.SINGLE_ONCE_PLAY) {
			requestPostStop();
		} else if (UiMusicPlayer.playStyle == UiMusicPlayer.SINGLE_LIST_PLAY) {
			if (++app.currentChildPosition >= len) {
				app.currentChildPosition = 0;
			} else {
				requestPostPlay(app.currentChildPosition);
			}
		}
		// ....
	}

	/**
	 * 释放资源,当mAsksetTime线程不能释放，就进入下一曲
	 * 
	 * @param isReleasePlay
	 *            是否释放mediaPlay的资源
	 */
	private void releaseResources(boolean isReleasePlay) {
		if (mAsksetTime != null && mAsksetTime.isAlive()) {
			stopthread = true;
			int count = 0;
			while(mAsksetTime.isAlive()){
				if(count++>10){
					Log.i(TAG, "count++>10");
					break;
				}
				stopthread = true;
				try {
					mAsksetTime.join(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			mAsksetTime = null;
		}
		
		if (isReleasePlay && mPlayer != null) {
			mPlayer.reset();
			mPlayer.release();
			mPlayer = null;
		}
		mNotificationManager.cancelAll();
	}

	/**
	 * 广播数据线程
	 * 
	 * @author Administrator
	 * 
	 */
	class AsksetTime extends Thread {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			howSendMsg();
		}

	}

	/**
	 * 在hasDuration为true时有mediaPlay总时间,"duration","currentTime",最好用map进行封装
	 */
	private void howSendMsg() {
		if (mPlayStatus == PlayStatus.PLAY) {
			int currentTime = mPlayer.getCurrentPosition();
			int duration = mPlayer.getDuration();
			while (currentTime <= duration) {
				if (stopthread || mPlayStatus == PlayStatus.STOP) {
					stopthread = false;
					break;
				} else {
					currentTime = mPlayer.getCurrentPosition();
				}
				if(stopBroadCast){
					continue;
				}
				// 两种办法防止广播风暴，1、让该线程wait,然后notify;2、这里给个暂停变量来判断是否进行continue操作。
				Intent intent = new Intent(MusicReceiver.ACTION_BROAD_DATA);
				if (mPlayStatus == PlayStatus.PLAY) {
					intent.putExtra("playerIsPlay", true);
				}
				intent.putExtra("duration", mPlayer.getDuration());
				intent.putExtra("currentTime", currentTime);
				sendBroadcast(intent);
				delay(100);
			}
		}
	}

	/**
	 * 延时
	 * 
	 * @param time
	 *            时间
	 */
	private void delay(long time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@SuppressWarnings("deprecation")
	private void setUpAsForeground(CharSequence text) {
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, UiMusicPlayer.class),
				PendingIntent.FLAG_UPDATE_CURRENT);
		if (mNotification == null) {
			mNotification = new Notification(R.drawable.ic_stat_playing, text,
					System.currentTimeMillis());
			mNotification.flags |= Notification.FLAG_ONGOING_EVENT;
		}
		mNotification.tickerText = text;
		mNotification
				.setLatestEventInfo(this, PlayerTitle, text, pendingIntent);
		startForeground(1, mNotification);
	}

	// private void updateNotification(String text) {
	// PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
	// new Intent(this, UiMp3Player.class),
	// PendingIntent.FLAG_UPDATE_CURRENT);
	// mNotification
	// .setLatestEventInfo(this, PlayerTitle, text, pendingIntent);
	// mNotificationManager.notify(1, mNotification);
	// }

}
