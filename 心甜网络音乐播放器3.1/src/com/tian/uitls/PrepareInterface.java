package com.tian.uitls;

import com.tian.net.Response;

/**
 * 该接口用于{@link PreAsyn}做回调
 * @author tian
 *
 */
public interface PrepareInterface {

	/**
	 * 后台线程具体内容
	 * @param params 传过的字符串参数，零或多个
	 * @return {@link Response}
	 */
	Response howDoInBackground(String... params);
	
	/**
	 * 在得到数据后，做出的判断
	 * @param result 后台得到的数据{@link Response}
	 */
	void howOnPostExecute(Response result);
	
}
