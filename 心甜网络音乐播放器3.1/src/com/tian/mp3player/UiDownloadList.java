package com.tian.mp3player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.tian.R;
import com.tian.app.App;
import com.tian.db.InfoBean;
import com.tian.uitls.Globle;
import com.tian.uitls.Tool;

/**
 * 下载列表
 * 
 * @author tian
 * 
 */
public class UiDownloadList extends Activity {

	private static final int UPDATE = 0;
	private static final int ABOUT = 1;

	/**
	 * ulr
	 */
	private String urlStr;

	private ListView updateList;
	private ProgressDialog progressDialog;

	private App app;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ui_download_list);
		app = Globle.getApp(this);
		urlStr = app.urlBase;
		updateList = (ListView) findViewById(R.id.updateList);

		updateList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(UiDownloadList.this,
						UiDownload.class);
				intent.putExtra("position", position);
				startActivity(intent);
			}
		});

		new AskMP3Info().execute(urlStr);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		menu.add(0, UPDATE, 0, "更新列表");
		menu.add(0, ABOUT, 1, "关于");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		if (item.getItemId() == UPDATE) {
			new AskMP3Info().execute(urlStr);
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * 下xml来更新列表
	 * 
	 * @param urlStr
	 *            网络url
	 * @return 下载完后的xml内容，字符串
	 */
	public String updateList(String urlStr) {
		// TODO Auto-generated method stub
		return Globle.getDownload().download(urlStr);
	}

	private class AskMP3Info extends AsyncTask<String, Void, String> {

		// 执行任务前Main线程中调用
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			if (progressDialog != null) {
				progressDialog.dismiss();
			}
			progressDialog = ProgressDialog.show(UiDownloadList.this,
					"数据下载中...", "请稍候...", true, false);
		}

		// 执行任务时在Task线程中调用
		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			return updateList(params[0]);
		}

		// 执行任务后Main线程中调用
		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			progressDialog.dismiss();
			if (result == null || result.length() <= 0) {
				new AlertDialog.Builder(UiDownloadList.this).setTitle("下载失败")
						.setMessage("下载XML失败！")
						.setPositiveButton(R.string.confirm, null).show();
			} else {
				xmlParse(result);
			}
		}

	}

	/**
	 * 最好用dom4j去解析xml,key值："id"、"mp3.name"、"mp3.size"、"lrc.name"、"id"、"lrc.size"
	 * 
	 * @param downStr
	 *            下载的xml文档字符串
	 */
	public void xmlParse(String downStr) {
		// TODO Auto-generated method stub
		app.infos.clear();
		Tool tl = Globle.getTool();
		String dropCommentStr = tl.xmlComment(downStr, null, null);
		if (dropCommentStr.equals("")) {
			Globle.showToast(this, "去注释失败", false);
			return;
		}
		int tagCounts = tl.xmlFindAttris(dropCommentStr, "<resource>");
		if (tagCounts == -1) {
			Globle.showToast(this, "查找<resource>属性失败", false);
			return;
		}
		List<Map<String, String>> infos = new ArrayList<Map<String, String>>();
		for (int i = 0; i < tagCounts; i++) {
			infos.add(getMap(new String[] { "id", "mp3.name", "mp3.size" },
					dropCommentStr, true));
			infos.add(getMap(new String[] { "id", "lrc.name", "lrc.size" },
					dropCommentStr, false));

			int resIndex = dropCommentStr.indexOf("</resource>");
			if (resIndex == -1) {
				break;
			}
			dropCommentStr = dropCommentStr.substring(resIndex
					+ "</resource>".length());
		}
		SimpleAdapter adapter = new SimpleAdapter(UiDownloadList.this, infos,
				R.layout.ui_download_list_item,
				new String[] { "name", "size" }, new int[] { R.id.mp3Name,
						R.id.mp3Size });
		updateList.setAdapter(adapter);
	}

	/**
	 * 将指定信息放入MAP中，及{@link App#infos}中，MAP中主要设入Listview中，
	 * 
	 * @param keys
	 *            xml中的关键key值
	 * @param text
	 *            去注释后的xml字符串
	 * @param isMp3
	 *            true为MP3信息，false为歌词信息
	 * @return Map<String, String>设入listview适配器中
	 */
	private Map<String, String> getMap(String[] keys, String text, boolean isMp3) {
		if (keys == null || keys.length <= 0 || text == null
				|| text.length() <= 0) {
			return null;
		}
		Tool tl = Globle.getTool();
		Map<String, String> map = new HashMap<String, String>();

		String idStr = tl.xmlString(text, keys[0]);
		int id = Integer.parseInt(idStr);
		String nameStr = tl.xmlString(text, keys[1]);
		int nameIndex = nameStr.indexOf("&");
		if (nameIndex == -1) {
			return null;
		}
		String nameEN = nameStr.substring(nameIndex + 1);
		String nameCN = nameStr.substring(0, nameIndex);
		String sizeStr = tl.xmlString(text, keys[2]);
		// 最后把大小改为MB形式
		long sizeL = Long.parseLong(sizeStr);

		// 加信息加入到APP中的公共List中
		Map<String, InfoBean> InfoBeans = new HashMap<String, InfoBean>();
		InfoBean info = new InfoBean();
		info.setId(id);
		info.setNameCN(nameCN);
		info.setNameEN(nameEN);
		info.setSize(sizeL);
		InfoBeans.put((isMp3 ? "mp3Info" : "lyrInfo"), info);
		app.infos.add(InfoBeans);

		map.put("name", nameCN);
		map.put("size", sizeL + "");
		return map;
	}

}
