package com.tian.uitls;

import com.tian.net.Response;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

/**
 * 通用的异步线程和{@link PrepareInterface}接口配合使用
 * @author tian
 *
 */
public class PreAsyn extends AsyncTask<String, Void, Response> {
	
//	private ProgressDialog proD;
	private Context mContext;
	private String mTitle;
	private String mMsg;
	private PrepareInterface mPrepareInterface;
	
	public PreAsyn(Context mContext,String mTitle,String mMsg,PrepareInterface mPrepareInterface) {
		// TODO Auto-generated constructor stub
		this.mContext = mContext;
		this.mTitle = mTitle;
		this.mMsg = mMsg;
		this.mPrepareInterface = mPrepareInterface;
	}
	
	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		super.onPreExecute();
//		if(proD!=null){
//			proD.dismiss();
//		}
//		proD = ProgressDialog.show(mContext, mTitle, mMsg, true, false);
	}
	
	@Override
	protected Response doInBackground(String... params) {
		// TODO Auto-generated method stub
		return mPrepareInterface.howDoInBackground(params);
	}

	@Override
	protected void onPostExecute(Response result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
//		proD.dismiss();
		mPrepareInterface.howOnPostExecute(result);
	}

}
