package com.tian.mp3player;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.util.EncodingUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Layout;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SimpleExpandableListAdapter;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.SlidingDrawer;
import android.widget.SlidingDrawer.OnDrawerCloseListener;
import android.widget.SlidingDrawer.OnDrawerOpenListener;
import android.widget.SlidingDrawer.OnDrawerScrollListener;
import android.widget.TextSwitcher;
import android.widget.TextView;

import com.tian.R;
import com.tian.app.App;
import com.tian.uitls.Globle;
import com.tian.uitls.Tool;

/**
 * 播放界面
 * 
 * @author tian
 * 
 */
public class UiMusicPlayer extends Activity implements OnItemLongClickListener,
		OnClickListener, OnSeekBarChangeListener, OnChildClickListener,
		OnGroupClickListener {

	public static final String TAG = "UiMp3Player";

	// 菜单 开始
	public final static int NEW_ACTIVITY_GROUP = 0;
	public final static int DL_LIST = 0;
	public final static int QUIT = 1;

	public final static int PLAY_STYLE_GROUP = 1;
	public final static int LOOP_PLAY = 0;
	public final static int SINGLE_LIST_PLAY = 1;
	public final static int SINGLE_LOOP_PLAY = 2;
	public final static int SINGLE_ONCE_PLAY = 3;

	// 菜单 结束

	private List<String> lrcBody = new ArrayList<String>();
	private List<Long> lrcTime = new ArrayList<Long>();

	/**
	 * 歌曲ListView表
	 */
	private ExpandableListView mp3List;

	/**
	 * musicPlayName:当前播放歌曲名字，musicListName：listview上的Textview名字，startTime：
	 * 开始时间Textview，endTime：结束时间Textview
	 */
	private TextView musicPlayName, startTime, endTime;
	private LinearLayout musicListName;
	/**
	 * 上一曲，下一曲，开始，停止按钮
	 */
	private Button prevBtn, nextBtn, startBtn, stopBtn;
	/**
	 * 时间进度条
	 */
	private SeekBar sb;

	/**
	 * 歌曲路径，歌词路径
	 */
	private String MUSIC_PATH, LYRIC_PATH;
	/**
	 * 任何一曲歌是否第一次播放
	 */
	public static boolean isFirstPlay = true;
	/**
	 * 是否为播放状态
	 */
	private boolean isPlay;
	/**
	 * 播放方式
	 */
	public static int playStyle;
	private int switchCount;
	private App app;

	private Button imageBtn,addListBtn;
	private TextSwitcher textSch;
	private TextView lrcTextView;
	private SimpleExpandableListAdapter adapter;

	// ------------------
	private DataReceiveFromService mDataReceiveFromService;
	private int duration, currentTime;
	private final String slipSignalInCfg = "&", priFileName = "configureFile";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ui_music_player);
		app = Globle.getApp(this);
		initWidget();
		listMusic();
		mp3List.setOnChildClickListener(this);
		mp3List.setOnGroupClickListener(this);
		startBtn.setOnClickListener(this);
		stopBtn.setOnClickListener(this);
		nextBtn.setOnClickListener(this);
		prevBtn.setOnClickListener(this);
		addListBtn.setOnClickListener(this);
		sb.setOnSeekBarChangeListener(this);

		onDrawAction();
	}

	/**
	 * 根据播放歌曲ID查找歌词文件，歌曲ID和歌词ID相同
	 * 
	 * @return 查找的歌词文件的字符串,失败返回空值
	 */
	private String findLrc() {
		// TODO Auto-generated method stub
		LYRIC_PATH = Globle.getFileUtil().getSDPATH() + app.dlPath
				+ app.dlLrcPath;
		app.lrcs.clear();
		return Globle.getTool().readFileData(
				LYRIC_PATH
						+ app.musicList.get(app.currentChildPosition).get(
								"name") + ".lrc", app.lrcs);
	}

	/**
	 * slipDraw打开时初始化
	 */
	public void showDrawer() {
		mp3List.setVisibility(ListView.INVISIBLE);
		musicListName.setVisibility(TextView.INVISIBLE);
		musicPlayName.setVisibility(TextView.INVISIBLE);
		textSch.setVisibility(TextSwitcher.VISIBLE);
	}

	/**
	 * slipdraw关闭时初始化
	 */
	public void disappearDrawer() {
		// textSch.setVisibility(TextSwitcher.VISIBLE);
		mp3List.setVisibility(ListView.VISIBLE);
		musicListName.setVisibility(TextView.VISIBLE);
		musicPlayName.setVisibility(TextView.VISIBLE);
		if (adapter != null) {
			adapter.notifyDataSetChanged();
		}
	}

	/**
	 * slipDraw 监听器 总共3个
	 */
	@SuppressWarnings("deprecation")
	private void onDrawAction() {
		// TODO Auto-generated method stub
		// ---------slipDraw 监听器 总共3个 开始
		SlidingDrawer drawer = (SlidingDrawer) findViewById(R.id.drawer);
		drawer.setOnDrawerCloseListener(new OnDrawerCloseListener() {

			@Override
			public void onDrawerClosed() {
				// TODO Auto-generated method stub
				disappearDrawer();
			}
		});

		drawer.setOnDrawerOpenListener(new OnDrawerOpenListener() {

			@Override
			public void onDrawerOpened() {
				// TODO Auto-generated method stub
				if (app.musicList.size() > 0) {
					showLyric();
				}
			}

		});

		drawer.setOnDrawerScrollListener(new OnDrawerScrollListener() {

			@Override
			public void onScrollStarted() {
				// TODO Auto-generated method stub

			}

			@Override
			public void onScrollEnded() {
				// TODO Auto-generated method stub
				showDrawer();
			}
		});
		// ---------slipDraw 监听器 总共3个 结束
	}

	/**
	 * 显示歌词
	 */
	private void showLyric() {
		// TODO Auto-generated method stub
		if (switchCount > 1000) {
			switchCount = 1;
		}
		if (switchCount++ != 0) {
			return;
		}
		Map<String, String> lrcHead = new HashMap<String, String>();
		textSch.removeAllViews();
		lrcBody.clear();
		lrcTime.clear();
		findLrc();
		Iterator<String> iter = app.lrcs.iterator();
		int flag = 0;
		while (iter.hasNext()) {
			String lrcLineStr = iter.next();
			if (flag++ < 7) {
				String[] lrcLines = lrcLineStr.substring(1,
						lrcLineStr.length() - 1).split(":");
				if (lrcLines.length != 2) {
					lrcHead.put(lrcLines[0], "");
				} else {
					lrcHead.put(lrcLines[0], lrcLines[1]);
				}
			} else {
				flag = 7;
				String[] lrcTimes = lrcLineStr.split("]");
				lrcTime.add(Tool.dotFormatToMills(lrcTimes[0].substring(1)));
				lrcBody.add(lrcTimes[1]);
			}
		}
		app.currentLrcItem = 0;
		if (lrcTextView == null) {
			lrcTextView = new TextView(UiMusicPlayer.this);
			lrcTextView.setTextSize(28);
			lrcTextView.setHorizontallyScrolling(true);
			lrcTextView.setMarqueeRepeatLimit(-1);
			// lrcTextView.setSingleLine(true);
			lrcTextView.setTextColor(Color.WHITE);
			lrcTextView.setGravity(Gravity.CENTER);
			// lrcTextView.setPadding(0, 100, 0, 0);
		}
		lrcTextView.setText(getHead(lrcHead));
		textSch.addView(lrcTextView, new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT));
	}

	/**
	 * key:"by","sign","total","ar","offset","al","ti"
	 * 
	 * @param headMap
	 *            传过来的歌曲头map
	 * @return 歌曲头字符串
	 */
	private String getHead(Map<String, String> headMap) {
		if (headMap == null || headMap.isEmpty()) {
			return null;
		}
		String[] titleStrs = { "by", "sign", "total", "ar", "offset", "al",
				"ti" };
		String[] titleCNStrs = { "歌词作者", "sign", "总大小", "歌曲作者", "offset", "al",
				"歌曲名字" };
		String str = "";
		for (int i = 0; i < headMap.size(); i++) {
			String msg = headMap.get(titleStrs[i]);
			msg = msg.equals("") ? titleCNStrs[i] + ":" : titleCNStrs[i] + ":"
					+ msg;
			str += (i != (headMap.size() - 1) ? msg + "\n" : msg);
		}
		return str;
	}

	/**
	 * 拿到num行字符串，歌词
	 * 
	 * @param num
	 *            最大行数
	 * @param start
	 *            开始行数
	 * @param lrcBody
	 *            歌词内容
	 * @return 字符串，失败""
	 */
	private String getNumLrc(int num, int start, List<String> lrcBody) {
		// TODO Auto-generated method stub
		if (num <= 0 || start < 0 || start >= num || lrcBody.size() <= 0)
			return "";
		String lrcStrShow = "";
		for (int i = start; i < num; i++) {
			String msg = lrcBody.get(i);
			lrcStrShow += (i != (num - 1) ? msg + "\n" : msg);
		}
		return lrcStrShow;
	}

	/**
	 * 切换到下一曲
	 */
	void onClickNext() {
		if (app.musicList.size() <= 0) {
			return;
		}
		if (++app.currentChildPosition >= app.musicList.size()) {
			app.currentChildPosition = 0;
		}
		isFirstPlay = true;
		playMusic(app.currentChildPosition);
	}

	/**
	 * 切换到前一曲
	 */
	void onClickPrev() {
		if (app.musicList.size() <= 0) {
			return;
		}
		if (--app.currentChildPosition < 0) {
			app.currentChildPosition = app.musicList.size() - 1;
		}
		isFirstPlay = true;
		playMusic(app.currentChildPosition);
	}

	/**
	 * 第一次按Start则是isFirstPlay为true开启播放，否则进入暂停和继续播放
	 */
	void playOrPause(boolean isNeedStartService) {
		if (isFirstPlay) {
			playMusicCutText();
		} else {
			adapter.notifyDataSetChanged();
			if (isPlay) {
				isPlay = false;
				startBtn.setBackgroundResource(R.drawable.btn_play);
			} else {
				isPlay = true;
				startBtn.setBackgroundResource(R.drawable.btn_pause);
			}
			if (isNeedStartService) {
				startService(new Intent(MusicService.ACTION_PAUSE));
			}
		}
	}

	/**
	 * 从TextView中剪贴到ID再从歌曲列表中取出
	 */
	void playMusicCutText() {
		String nameStr = (String) musicPlayName.getText();
		int nameIndex = nameStr.lastIndexOf("&");
		if (nameIndex == -1) {
			Globle.showToast(UiMusicPlayer.this, "歌曲不存在", false);
			return;
		}
		playMusic(Integer.parseInt(nameStr.substring(nameIndex + 1)));
	}

	/**
	 * 播放歌曲
	 * 
	 * @param itemIndex
	 *            歌曲在音乐列表中的位置
	 */
	void playMusic(int itemIndex) {
		Intent intent = new Intent(MusicService.ACTION_PLAY);
		intent.putExtra("position", itemIndex);
		startService(intent);
		updatePlayStatus(itemIndex);
	}

	/**
	 * 更新播放状态变量
	 * 
	 * @param itemIndex
	 */
	void updatePlayStatus(int itemIndex) {
//		mp3List.smoothScrollToPosition(app.currentChildPosition);
		adapter.notifyDataSetChanged();
		if (textSch.isShown() && switchCount != 0) {
			switchCount = 0;
			showLyric();
		} else if (switchCount != 0) {
			switchCount = 0;
		}
		isFirstPlay = false;
		isPlay = true;
		setTimeCtrl(true, TextView.VISIBLE);
		startBtn.setBackgroundResource(R.drawable.btn_pause);
		musicPlayName.setText(app.musicList.get(itemIndex).get("name") + "&"
				+ itemIndex);
	}

	/**
	 * 在MediaStore.Audio.Media.EXTERNAL_CONTENT_URI删除储存信息
	 * 
	 * @param id
	 *            删除信息的ID
	 * @return 删除的行
	 */
	public int delete(String id) {
		String selection = MediaStore.Audio.Media._ID + "=" + id;
		return getContentResolver().delete(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, selection, null);
	}

	/**
	 * 在MediaStore.Audio.Media.EXTERNAL_CONTENT_URI查找储存信息
	 * 
	 * @return cursor
	 */
	public Cursor query() {
		// private String[] projection={MediaStore.Audio.Media._ID,//歌曲ID
		// MediaStore.Audio.Media.TITLE,//歌曲标题
		// MediaStore.Audio.Media.ALBUM,//歌曲专辑
		// MediaStore.Audio.Media.ARTIST,//歌手
		// MediaStore.Audio.Media.DURATION,//播放时间
		// MediaStore.Audio.Media.SIZE,//文件大小
		// MediaStore.Audio.Media.DATA//文件路径
		// };
		String[] projections = { MediaStore.Audio.Media._ID,
				MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.TITLE,
				MediaStore.Audio.Media.DATA,
				MediaStore.Audio.Media.DISPLAY_NAME,
				MediaStore.Audio.Media.DURATION };
		String selection = MediaStore.Audio.Media.IS_MUSIC + "=1";
		// getContentResolver().delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,selection
		// , null);
		// 自己手机用EXTERNAL_CONTENT_URI
		return this.getContentResolver().query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projections,
				selection, null, null);
	}

	/**
	 * 从下载路径文件夹中下载歌曲
	 */
	private void listMusicFromFile() {
		// TODO Auto-generated method stub
		// 只是搜索当前设定目录
		MUSIC_PATH = Globle.getFileUtil().getSDPATH() + app.dlPath;
		File[] musicFiles = new File(MUSIC_PATH)
				.listFiles(new Tool.MusicFilter());
		if (musicFiles != null) {
			if (musicFiles.length <= 0) {
				/*ArrayAdapter<String> adapter = new ArrayAdapter<String>(
						UiMusicPlayer.this,
						android.R.layout.simple_list_item_1,
						new String[] { "没有匹配的歌曲" });
				mp3List.setAdapter(adapter);
				mp3List.setEnabled(false);*/
			} else {
				if (app.musicList.size() >= 0) {
					app.musicList.clear();
				}
				for (File file : musicFiles) {
					Map<String, String> map = new HashMap<String, String>();
					map.put("name", Tool.getFileName(file.getName()));
					map.put("time",
							String.format("%.2f", (double) file.length()
									/ (1024 * 1024))
									+ "Mb");
					map.put("dataStr", MUSIC_PATH + file.getName());
					app.musicList.add(map);
				}
				setList("time");
			}
		}
	}

	/**
	 * 配置适配器
	 * 
	 * @param groupData
	 *            父节点的数据,map的key："groupName"
	 * @param childData
	 *            子节点的数据,map的key："name", secStr
	 * @param secStr
	 *            子节点的数据中map第二个key值
	 * @return 配置好的适配器,{@link SimpleExpandableListAdapter}
	 */
	private SimpleExpandableListAdapter getAdatper(
			List<Map<String, String>> groupData,
			List<List<Map<String, String>>> childData, String secStr) {
		// TODO Auto-generated method stub
		if (adapter != null) {
			return adapter;
		}
		adapter = new SimpleExpandableListAdapter(this, groupData,
				R.layout.music_group, new String[] { "groupName" },
				new int[] { R.id.groupName }, childData, R.layout.music_item,
				new String[] { "name", secStr }, new int[] { R.id.mp3Name,
						R.id.mp3Size }) {

			@Override
			public View getGroupView(int groupPosition, boolean isExpanded,
					View convertView, ViewGroup parent) {
				// TODO Auto-generated method stub
				View view = super.getGroupView(groupPosition, isExpanded,
						convertView, parent);
				if (app.currentGroupPosition == groupPosition && isExpanded) {
					view.setBackgroundDrawable(getResources().getDrawable(
							R.drawable.group_pressed_text_bottom));
				} else {
					view.setBackgroundDrawable(getResources().getDrawable(
							R.drawable.group_list_text_bottom));
				}
				return view;
			}

			@Override
			public View getChildView(int groupPosition, int childPosition,
					boolean isLastChild, View convertView, ViewGroup parent) {
				// TODO Auto-generated method stub
				View view = super.getChildView(groupPosition, childPosition,
						isLastChild, convertView, parent);
				if (app.currentGroupPosition == groupPosition
						&& app.currentChildPosition == childPosition) {
					view.setBackgroundDrawable(getResources().getDrawable(
							R.drawable.child_pressed_text_bottom));
					TranslateAnimation translateAnimation = new TranslateAnimation(
							0, view.getWidth(), 0, view.getHeight());

					translateAnimation.setDuration(500);
					view.startAnimation(translateAnimation);
				} else {
					view.setBackgroundDrawable(getResources().getDrawable(
							R.drawable.child_list_text_bottom));
				}
				return view;
			}

		};

		return adapter;
	}

	/**
	 * 列出歌曲到列表中
	 */
	private void listMusic() {
		// listMusicFromFile();
		Cursor cursor = query();
		if (cursor == null || /* !cursor.moveToNext() || */cursor.getCount() <= 0) {
			listMusicFromFile();
		} else {
			app.musicList.clear();
			while (cursor.moveToNext()) {
				Map<String, String> map = new HashMap<String, String>();
				map.put("name", Tool.getFileName(cursor.getString(4)));
				map.put("time", Tool.millisTimeToDotFormat(
						Integer.parseInt(cursor.getString(5)), false, false,
						false));
				map.put("dataStr", cursor.getString(3));
				map.put("title", cursor.getString(2));
				map.put("artist", cursor.getString(1));
				map.put("id", cursor.getString(0));
				app.musicList.add(map);
			}
			setList("time");
			if (!cursor.isClosed()) {
				cursor.close();
			}
		}
	}
	/**
	 * 父节点数据，其map的key："groupName"
	 */
	List<Map<String, String>> groupData = new ArrayList<Map<String, String>>();
	/**
	 * 子节点数据,其map的key："name","time"
	 */
	List<List<Map<String, String>>> childData = new ArrayList<List<Map<String, String>>>();
	/**
	 * 设置{@link UiMusicPlayer#mp3List}中的值
	 * 
	 * @param secStr
	 *            子节点的数据中map第二个key值
	 */
	void setList(String secStr) {
		if (!mp3List.isEnabled()) {
			mp3List.setEnabled(true);
		}
		// 在TextView中设置播放歌曲信息
		if (app.currentChildPosition >= app.musicList.size()) {
			app.currentChildPosition = 0;
		}
		musicPlayName.setText((String) app.musicList.get(
				app.currentChildPosition).get("name")
				+ slipSignalInCfg + app.currentChildPosition);
		app.currentGroupPosition = 0;
		
		Map<String, String> map = new HashMap<String, String>();
		map.put("groupName", "所有歌曲("+app.musicList.size()+")");
		groupData.add(map);

		childData.add(app.musicList);

		mp3List.setAdapter(getAdatper(groupData, childData, secStr));
	}

	/**
	 * 初始化控件
	 */
	private void initWidget() {
		// TODO Auto-generated method stub
		mp3List = (ExpandableListView) findViewById(R.id.mp3List);
		musicPlayName = (TextView) findViewById(R.id.musicPlayName);
		musicListName = (LinearLayout)findViewById(R.id.musicListName);
		startTime = (TextView) findViewById(R.id.startTime);
		endTime = (TextView) findViewById(R.id.endTime);
		startBtn = (Button) findViewById(R.id.startBtn);
		stopBtn = (Button) findViewById(R.id.stopBtn);
		nextBtn = (Button) findViewById(R.id.nextBtn);
		prevBtn = (Button) findViewById(R.id.prevBtn);
		textSch = (TextSwitcher) findViewById(R.id.textSch);
		imageBtn = (Button) findViewById(R.id.imageBtn);
		addListBtn = (Button) findViewById(R.id.addList);
		imageBtn.setVisibility(TextView.INVISIBLE);
		sb = (SeekBar) findViewById(R.id.sb);
		setTimeCtrl(false, TextView.VISIBLE);
		endTime.setText("");
		startTime.setText("");
		String pstyle = readFileData(priFileName);
		if (pstyle == null || pstyle.equals("")) {
			playStyle = 2;
			app.currentChildPosition = 0;
			writeFileData(priFileName, "2" + slipSignalInCfg + "0");
		} else {
			// 真正退出才赋值，防止后台运行，界面退出了
			// Log.i(TAG, pstyle);
			if (app.currentChildPosition == -1) {
				String[] pStyles = pstyle.split(slipSignalInCfg);
				playStyle = Integer.parseInt(pStyles[0]);
				app.currentChildPosition = Integer.parseInt(pStyles[1]);
			}
		}
	}

	/**
	 * 当sbEnable为true，timeVisible为TextView.Visible时开启时间控件，否则都设为不可用状态
	 * 
	 * @param sbEnable
	 *            seekBar控件开关
	 * @param timeVisible
	 *            TextView控件开关
	 */
	void setTimeCtrl(boolean sbEnable, int timeVisible) {
		startTime.setVisibility(timeVisible);
		endTime.setVisibility(timeVisible);
		sb.setEnabled(sbEnable);
		if (sbEnable) {
			sb.setMax(duration);
			String timeStr = Tool.millisTimeToDotFormat(duration, false, false,
					false);
			endTime.setText(timeStr);
		}
	}

	/**
	 * 同步歌词
	 * 
	 * @param currentTime
	 *            歌曲播放时间
	 */
	private void cmpTimeShowLrc(int currentTime) {
		// TODO Auto-generated method stub
		if (textSch.isShown() && lrcTextView != null) {
			if (app.currentLrcItem >= lrcTime.size()) {
				return;
			}
			long currentLrcTime = lrcTime.get(app.currentLrcItem);
			if (currentLrcTime <= currentTime) {
				String lrcStrShow = getNumLrc(app.currentLrcItem + 1,
						app.currentLrcItem++, lrcBody);
				if (lrcStrShow != null && lrcStrShow.length() > 0) {
					String[] strs = lrcStrShow.split(" ");
					if (strs != null && strs.length > 1) {
						lrcStrShow = "";
						for (int i = 0; i < strs.length; i++) {
							lrcStrShow += (i == i - 1 ? strs[i] : strs[i]
									+ "\n\n");
						}
					}
					lrcTextView.setText(lrcStrShow);
				}
			}
		}
	}

	void quitApp() {
		new AlertDialog.Builder(this)
				.setTitle("退出心甜音乐播放器")
				.setMessage("真的要心甜音乐播放器吗？")
				.setPositiveButton(R.string.confirm,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
								writeFileData(priFileName, playStyle
										+ slipSignalInCfg
										+ app.currentChildPosition);
								startService(new Intent(
										MusicService.ACTION_STOP_SERVICE));
								startService(new Intent(
										DownloadService.ACTION_STOP_DOWNLOAD_SERVICE));
								finish();
								System.exit(0);
							}
						}).setNegativeButton(R.string.cancle, null).show();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getGroupId()) {
		case NEW_ACTIVITY_GROUP:
			if (item.getItemId() == DL_LIST) {
				Intent intent = new Intent(UiMusicPlayer.this,
						UiDownloadList.class);
				startActivity(intent);
			} else if (item.getItemId() == QUIT) {
				quitApp();
			}
			break;
		case PLAY_STYLE_GROUP:
			if (item.getItemId() == LOOP_PLAY) {
				Globle.showToast(this, "全部循环", false);
				playStyle = LOOP_PLAY;
			} else if (item.getItemId() == SINGLE_LIST_PLAY) {
				Globle.showToast(this, "顺序播放", false);
				playStyle = SINGLE_LIST_PLAY;
			} else if (item.getItemId() == SINGLE_LOOP_PLAY) {
				Globle.showToast(this, "单曲循环", false);
				playStyle = SINGLE_LOOP_PLAY;
			} else if (item.getItemId() == SINGLE_ONCE_PLAY) {
				Globle.showToast(this, "单曲播放", false);
				playStyle = SINGLE_ONCE_PLAY;
			}
			writeFileData(priFileName, item.getItemId() + slipSignalInCfg
					+ app.currentChildPosition);
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		menu.add(NEW_ACTIVITY_GROUP, DL_LIST, DL_LIST, "下载列表");
		menu.add(NEW_ACTIVITY_GROUP, QUIT, QUIT, "退出");

		menu.add(PLAY_STYLE_GROUP, LOOP_PLAY, LOOP_PLAY, "全部循环");
		menu.add(PLAY_STYLE_GROUP, SINGLE_LIST_PLAY, SINGLE_LIST_PLAY, "顺序播放");
		menu.add(PLAY_STYLE_GROUP, SINGLE_LOOP_PLAY, SINGLE_LOOP_PLAY, "单曲循环");
		menu.add(PLAY_STYLE_GROUP, SINGLE_ONCE_PLAY, SINGLE_ONCE_PLAY, "单曲播放");
		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * 一、私有文件夹下的文件存取（/data/data/包名/files）
	 * 
	 * @param fileName
	 * @param msg
	 */
	public void writeFileData(String fileName, String msg) {
		if (fileName == null || fileName.equals("") || msg == null
				|| msg.equals("")) {
			return;
		}
		FileOutputStream fos = null;
		try {
			fos = openFileOutput(fileName, MODE_PRIVATE);
			fos.write(msg.getBytes());
			fos.flush();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (fos != null) {
					fos.close();
					fos = null;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * 读文件在./data/data/包名/files/下面
	 * 
	 * @param fileName
	 * @return
	 */
	public String readFileData(String fileName) {
		if (fileName == null || fileName.equals("")) {
			return "";
		}
		String restr = "";
		FileInputStream fis = null;
		try {
			fis = openFileInput(fileName);
			int length = fis.available();
			byte[] res = new byte[length];
			fis.read(res);
			restr = EncodingUtils.getString(res, "utf-8");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			return restr;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (fis != null) {
					fis.close();
					fis = null;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return restr;
	}
	
	void showLongClickDialog(final int position){
		new AlertDialog.Builder(UiMusicPlayer.this)
		.setTitle("请选择")
		.setIcon(R.drawable.launcher)
		.setSingleChoiceItems(new String[] { "删除", "详细信息" }, 0,
				new DialogInterface.OnClickListener() {

					/**
					 * 删除音乐文件
					 */
					@Override
					public void onClick(DialogInterface dialog,
							int which) {
						// TODO Auto-generated method stub
						switch (which) {
						case 0:
							String filePath = (String) app.musicList
									.get(position).get("dataStr");
							File file = new File(filePath);
							if (file.exists()) {
								file.delete();
								if (position == app.currentChildPosition) {
									startService(new Intent(
											MusicService.ACTION_STOP));
								}
								delete((String) app.musicList.get(
										position).get("id"));
								app.musicList.remove(position);
								if (position == app.currentChildPosition) {
									if (++app.currentChildPosition >= app.musicList
											.size()) {
										app.currentChildPosition = 0;
									}
								}
							}
							adapter.notifyDataSetChanged();
							break;
						case 1:
							Map<String, String> fileMap = (Map<String, String>) app.musicList
									.get(position);
							String fileInfo = "name="
									+ fileMap.get("name") + ",time="
									+ fileMap.get("time");

							Globle.showToast(UiMusicPlayer.this,
									fileInfo, false);
							break;
						default:
							break;
						}
						dialog.dismiss();
					}

				}).setPositiveButton(R.string.cancle, null).show();
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		// TODO Auto-generated method stub
		showLongClickDialog(position);
		return true;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.startBtn:
			playOrPause(true);
			break;
		case R.id.stopBtn:
			stopMediaPlay();
			break;
		case R.id.nextBtn:
			onClickNext();
			break;
		case R.id.prevBtn:
			onClickPrev();
			break;
		case R.id.addList:
			showDialog(this,"添加列表","列表名字");
			break;
			
		default:
			break;
		}
	}

	/**
	 * 添加列表
	 * @param addListName
	 */
	private void onAddList(String addListName) {
		// TODO Auto-generated method stub
		Map<String, String> map = new HashMap<String, String>();
		map.put("groupName", addListName);
		groupData.add(map);
		List<Map<String, String>> chilList = new ArrayList<Map<String,String>>();
		map = new HashMap<String, String>();
//		map.put("time", "");
//		map.put("name", "");
		chilList.add(map);
		childData.add(chilList);
		adapter.notifyDataSetChanged();
	}
	
	/**
	 * 显示对话框,{@link AlertDialog.Builder}
	 * @param context 上下环境
	 * @param title 标题
	 * @param hint 影显部分
	 */
	void showDialog(Context context,String title,String hint){
		Builder builder = new AlertDialog.Builder(context);
		
		final EditText input = new EditText(context);
		input.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		input.setHint(hint);
		input.setTextSize(15);
		builder.setView(input);
		builder.setTitle(title);
		builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				String inputStr = input.getText().toString();
				if(inputStr!=null&&inputStr.length()>0){
					onAddList(inputStr);
				}
			}
		});
		builder.setNegativeButton(R.string.cancle, null);
		
		builder.create();
		builder.show();
	}
	
	/**
	 * 停止播放
	 */
	private void stopMediaPlay() {
		// TODO Auto-generated method stub
		startService(new Intent(MusicService.ACTION_STOP));
		isFirstPlay = true;
		setTimeCtrl(false, TextView.VISIBLE);
		startTime.setText("00:00");
		startBtn.setBackgroundResource(R.drawable.btn_play);
		sb.setMax(0);
		sb.setProgress(0);
	}

	/**
	 * 同步歌词，当seekbar变动时
	 * 
	 * @param curTime
	 *            当前时间
	 * @return 歌词list的位置
	 */
	private int aysLyrOnProChanged(long curTime) {
		// TODO Auto-generated method stub
		for (int i = 0; i < lrcTime.size(); i++) {
			long time = lrcTime.get(i);
			if (time >= curTime) {
				return i;
			}
		}
		return 0;
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		// TODO Auto-generated method stub
		if (fromUser) {
			Intent intent = new Intent(MusicService.ACTION_SEEK_TO);
			intent.putExtra("progress", seekBar.getProgress());
			startService(intent);
			app.currentLrcItem = aysLyrOnProChanged(currentTime);
			// dealMsg();
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub

	}

	// =================后面改造=========================//
	@Override
	protected void onResume() {
		if (adapter != null) {
			adapter.notifyDataSetChanged();
		}
		if (mDataReceiveFromService == null) {
			mDataReceiveFromService = new DataReceiveFromService();
		}
		registerReceiver(mDataReceiveFromService, new IntentFilter(
				MusicReceiver.ACTION_BROAD_DATA));
		startService(new Intent(MusicService.ACTION_START_BROAD_CAST));
		super.onResume();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		unregisterReceiver(mDataReceiveFromService);
		startService(new Intent(MusicService.ACTION_STOP_BROAD_CAST));
		super.onStop();
	}

	private class DataReceiveFromService extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			int durationFrom = intent.getIntExtra("duration", -1);
			if (durationFrom != -1 && duration != durationFrom) {
				duration = durationFrom;
				updatePlayStatus(app.currentChildPosition);
			}
			currentTime = intent.getIntExtra("currentTime", -1);
			dealMsg(intent.getBooleanExtra("playerIsPlay", false));
		}

	}

	/**
	 * 处理广播接收到的数据
	 * 
	 * @param playerIsPlay
	 *            从广播得到的数据判断
	 */
	private void dealMsg(boolean playerIsPlay) {
		// TODO Auto-generated method stub
		if (currentTime != -1) {
			dealMsg();
			if (isPlay != playerIsPlay) {
				playOrPause(false);
			}
		}
	}

	/**
	 * 处理广播接收到的数据
	 */
	private void dealMsg() {
		// TODO Auto-generated method stub
		if (currentTime != -1) {
			sb.setProgress(currentTime);
			cmpTimeShowLrc(currentTime);
			startTime.setText(Tool.millisTimeToDotFormat(currentTime, false,
					false, false));
		}
	}

	@Override
	public boolean onChildClick(ExpandableListView parent, View v,
			int groupPosition, int childPosition, long id) {
		// TODO Auto-generated method stub
		if (app.currentChildPosition == childPosition) {
			playOrPause(true);
		} else {
			playMusic(childPosition);
			app.currentChildPosition = childPosition;
		}
		adapter.notifyDataSetChanged();
		return true;
	}

	@Override
	public boolean onGroupClick(ExpandableListView parent, View v,
			int groupPosition, long id) {
		// TODO Auto-generated method stub
		if(mp3List.isGroupExpanded(groupPosition)){
			mp3List.collapseGroup(groupPosition);
		}else{
			mp3List.expandGroup(groupPosition);
		}
		app.currentGroupPosition = groupPosition;
		return true;
	}

}
