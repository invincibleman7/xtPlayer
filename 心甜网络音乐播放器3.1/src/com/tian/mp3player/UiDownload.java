package com.tian.mp3player;

import java.util.Map;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tian.R;
import com.tian.app.App;
import com.tian.db.InfoBean;
import com.tian.mp3player.DownloadService.DownloadState;
import com.tian.uitls.Globle;

public class UiDownload extends Activity implements OnClickListener {

//	public final static String TAG = "UiDownload";

	private String nameEN, nameCN, dlPath;
	private ProgressBar proBar;
	private TextView dlText, dlPercentageText;
	private Button dlStart, dlStop;
	private long size;
	private DownloadState uiState = DownloadState.stoped;
	private boolean isFirstDl = true;
	private App app;
	int position;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ui_download);
		app = Globle.getApp(this);
		position = getIntent().getIntExtra("position", -1);
		if (position == -1) {
			return;
		}
		Map<String, InfoBean> info = app.infos.get(position);
		initWidget();
		InfoBean bean = null;
		if (info.containsKey("mp3Info")) {
			bean = (InfoBean) info.get("mp3Info");
			dlPath = app.dlPath;
		} else if (info.containsKey("lyrInfo")) {
			bean = (InfoBean) info.get("lyrInfo");
			dlPath = app.dlPath + app.dlLrcPath;
		}
		nameEN = bean.getNameEN();
		nameCN = bean.getNameCN();
		size = bean.getSize();
		
		dlWidgetControl();
		app.url = app.urlBase + nameEN;
		
		dlText.setText(nameCN);
		dlStart.setOnClickListener(this);
		dlStop.setOnClickListener(this);
	}

	/**
	 * 启动控件初始化
	 */
	private void initWidget() {
		proBar = (ProgressBar) findViewById(R.id.dlProBar);
		dlText = (TextView) findViewById(R.id.dlText);
		dlPercentageText = (TextView) findViewById(R.id.dlPercentageText);
		dlStart = (Button) findViewById(R.id.dlStart);
		dlStop = (Button) findViewById(R.id.dlStop);
	}

	/**
	 * 下载控件初始化
	 * 
	 */
	private void dlWidgetControl() {
		proBar.setMax((int) size);
		proBar.setProgress(0);
		dlPercentageText.setVisibility(TextView.VISIBLE);
		dlPercentageText.setTextColor(Color.MAGENTA);
		dlPercentageText.setText(nameCN + "开始下载");
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v == dlStart) {
			if (isFirstDl) {
				isFirstDl = false;
				dlWidgetControl();
				uiState = DownloadState.starting;
				dlStart.setText("暂停");
				initBtnState(true, true);
				Intent intent = new Intent(
						DownloadService.ACTION_START_DOWNLOAD);
				intent.putExtra("path", dlPath);
				intent.putExtra("fileName", nameCN);
				intent.putExtra("size", size);
				intent.putExtra("position", position);
				startService(intent);
			} else {
				if (uiState == DownloadState.starting) {
					uiState = DownloadState.paused;
					dlStart.setText("开始");
				} else if (uiState == DownloadState.paused) {
					uiState = DownloadState.starting;
					dlStart.setText("暂停");
				}
				startService(new Intent(DownloadService.ACTION_PAUSE_DOWNLOAD));
			}
		} else if (v == dlStop) {
			stopDownload();
		}
	}

	private void stopDownload() {
		// TODO Auto-generated method stub
		uiState = DownloadState.stoped;
		initBtnState(true, false);
		dlStart.setText("开始");
		isFirstDl = true;
		proBar.setMax(0);
		proBar.setProgress(0);
		startService(new Intent(DownloadService.ACTION_STOP_DOWNLOAD));
	}

	private void initBtnState(boolean startBtn, boolean stopBtn) {
		dlStart.setEnabled(startBtn);
		dlStop.setEnabled(stopBtn);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		if (mDownloadReciever == null) {
			mDownloadReciever = new DownloadReciever();
		}
		registerReceiver(mDownloadReciever, new IntentFilter(
				MusicReceiver.ACTION_BROAD_DOWNLAOD_DATA+"."+nameCN));
		super.onResume();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		unregisterReceiver(mDownloadReciever);
		super.onStop();
	}

	DownloadReciever mDownloadReciever;

	private class DownloadReciever extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			int currentSize = intent.getIntExtra("currentSize", -1);
			int currentTime = intent.getIntExtra("currentTime", -1);
			if (currentSize != -1 && currentTime != -1) {
				if(DownloadService.mState == DownloadState.starting){
					dealMsg(currentSize, currentTime);
				}
			}
		}

	}

	private void dealMsg(int currentSize, int currentTime) {
		// TODO Auto-generated method stub
		proBar.setProgress(currentSize);
		dlPercentageText.setText(formatText(currentSize, currentTime));
	}

	private String formatText(int currentSize, int currentTime) {
		double dlSpeed = ((double) currentSize * 1000)
				/ ((long) currentTime * 1024);
		String speedUnit = "Kb/s";
		if (dlSpeed >= 1024) {
			speedUnit = "Mb/s";
			dlSpeed /= 1024;
			if (dlSpeed >= 1024) {
				speedUnit = "Gb/s";
				dlSpeed /= 1024;
			}
			// ..
		}
		String strText = "总共大小:" + String.format("%.2f", (double)size/(1024*1024)) + "MB ,下载："
				+ String.format("%.2f", ((double) currentSize) / size * 100)
				+ "%,速度:" + String.format("%.2f", dlSpeed) + speedUnit;
		return strText;
	}
}
