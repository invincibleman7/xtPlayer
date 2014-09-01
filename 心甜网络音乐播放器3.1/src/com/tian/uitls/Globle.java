package com.tian.uitls;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.util.DisplayMetrics;
import android.widget.Toast;

import com.tian.R;
import com.tian.app.App;
import com.tian.downloadUtils.FileUtil;
import com.tian.downloadUtils.HttpDownloadUtils;

public class Globle {

	public static Tool tl;

	public static HttpDownloadUtils hdu;

	public static FileUtil fu;

	public static HttpDownloadUtils getDownload() {
		if (hdu == null) {
			return hdu = new HttpDownloadUtils();
		}
		return hdu;
	}

	public static FileUtil getFileUtil() {
		if (fu == null) {
			return fu = new FileUtil();
		}
		return fu;
	}

	public static Tool getTool() {
		if (tl == null) {
			return tl = new Tool();
		}
		return tl;
	}

	public static App getApp(Context context) {
		return (App) context.getApplicationContext();
	}

	/**
	 * 判断是否平板
	 * 
	 * @return
	 */
	private boolean isTabletDevice(Activity activity) {
		DisplayMetrics dm = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
		double diagonalPixels = Math.sqrt(Math.pow(dm.widthPixels, 2)
				+ Math.pow(dm.heightPixels, 2));
		double screenSize = diagonalPixels / (160 * dm.density);
		if (screenSize > 6) {
			return true;
		} else {
			return false;
		}
	}

	static Toast toast = null;

	public static void showToast(Context mContext, String msg, boolean isLong) {
		if (mContext == null || msg == null || msg.equals("")) {
			System.out
					.println("mContext==null||msg==null||msg.equals(\"\"):showToast");
			return;
		}
		if (toast == null) {
			toast = Toast.makeText(mContext, msg, isLong ? Toast.LENGTH_LONG
					: Toast.LENGTH_SHORT);
		} else {
			toast.setText(msg);
			toast.setDuration(isLong ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT);
		}
		toast.show();
	}

	static Builder mBuilder;

	public static void setSimpleDialog(Context mContext, String msg,
			String title) {
		if (mContext == null || msg == null || msg.equals("") || title == null
				|| title.equals("")) {
			System.out
					.println("mContext==null||msg==null||msg.equals(\"\"):showToast");
			return;
		}
		if (mBuilder == null) {
			mBuilder = new AlertDialog.Builder(mContext);
		}
		mBuilder.setTitle(title);
		mBuilder.setMessage(msg);
		mBuilder.setPositiveButton(R.string.confirm, null);
		mBuilder.setNegativeButton(R.string.cancle, null);
		mBuilder.create();
		mBuilder.show();
	}

}
