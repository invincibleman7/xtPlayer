package com.tian.mp3player;

import com.tian.uitls.Globle;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.view.KeyEvent;

public class MusicReceiver extends BroadcastReceiver {

	/**
	 * 广播数据
	 */
	public static final String ACTION_BROAD_DATA = "com.tian.mp3Player.broadCastData";
	/**
	 * 广播下载数据
	 */
	public static final String ACTION_BROAD_DOWNLAOD_DATA = "com.tian.mp3Player.DownloadService.broadCastData";

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		String action = intent.getAction();
		if (action.equals(Intent.ACTION_MEDIA_BUTTON)) {
			KeyEvent keyEvent = (KeyEvent) intent.getExtras().get(
					Intent.EXTRA_KEY_EVENT);
			if(keyEvent.getAction()!=KeyEvent.ACTION_DOWN){
				return;
			}
			switch (keyEvent.getKeyCode()) {
			case KeyEvent.KEYCODE_HEADSETHOOK:
			case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
				context.startService(new Intent(MusicService.ACTION_PAUSE));
				break;
			case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
				Globle.showToast(context,
						"KeyEvent.KEYCODE_MEDIA_FAST_FORWARD", false);
				break;
			case KeyEvent.KEYCODE_MEDIA_NEXT:
				Globle.showToast(context, "KeyEvent.KEYCODE_MEDIA_NEXT", false);
				break;
			case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
				Globle.showToast(context, "KeyEvent.KEYCODE_MEDIA_PREVIOUS",
						false);
				break;
			case KeyEvent.KEYCODE_MEDIA_REWIND:
				Globle.showToast(context, "KeyEvent.KEYCODE_MEDIA_REWIND",
						false);
				break;
			case KeyEvent.KEYCODE_MEDIA_STOP:
				context.startService(new Intent(MusicService.ACTION_STOP));
				break;

			default:
				break;
			}
		} else if (action.equals(AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
			Globle.showToast(context, "Headphone disconnected.", false);
		}
	}
	
}
