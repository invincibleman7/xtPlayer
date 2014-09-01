package com.tian.uitls;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class Tool {

	/**
	 * 剪贴后文本 如果xmlStr = first character of the specified string in this
	 * string,beginStr = character endStr = in;return
	 * " of the specified string "; 空格也算
	 * 
	 * @param xmlStr
	 *            原文本
	 * @param beginStr
	 *            开始文本
	 * @param endStr
	 *            结束文本
	 * @return 失败""
	 */
	public String cutText(String xmlStr, String beginStr, String endStr) {
		if (xmlStr == null || xmlStr.equals("") || beginStr == null
				|| beginStr.equals("") || endStr == null || endStr.equals(""))
			return "";
		int beginIndex = xmlStr.toLowerCase().indexOf(beginStr.toLowerCase());
		if (beginIndex == -1)
			return "";
		beginIndex += beginStr.length();
		int endIndex = xmlStr.indexOf(endStr.toLowerCase(), beginIndex);
		if (endIndex == -1)
			return "";
		// 包涵第一，不包涵最后一个
		return xmlStr.substring(beginIndex, endIndex);
	}

	/**
	 * 去标签的文本 取{@code <key></key>}中的值
	 * 
	 * @param xmlStr
	 *            原文本
	 * @param key
	 *            标签名字
	 * @return 失败""
	 */
	public String xmlString(String xmlStr, String key) {
		if (xmlStr == null || xmlStr.equals("") || key == null
				|| key.equals(""))
			return "";
		return cutText(xmlStr, "<" + key + ">", "</" + key + ">");
	}

	/**
	 * 去标签的文本 取《xxx/>中的值
	 * 
	 * @param xmlStr
	 *            原文本
	 * @param key
	 *            标签名字
	 * @return 失败""
	 */
	public String xmlStringOne(String xmlStr, String key) {
		if (xmlStr == null || xmlStr.equals("") || key == null
				|| key.equals(""))
			return "";
		return cutText(xmlStr, "<" + key, "/>");
	}

	/**
	 * 取标签属性的值
	 * 
	 * @param xmlStr
	 *            原文本
	 * @param key
	 *            属性名字
	 * @return 失败""
	 */
	public String xmlAttrs(String xmlStr, String key) {
		if (xmlStr == null || xmlStr.equals("") || key == null
				|| key.equals(""))
			return "";
		return cutText(xmlStr, key + "=\"", "\"");
	}

	/**
	 * 找到属性的次数
	 * 
	 * @param xmlStr
	 *            原文本
	 * @param key
	 *            属性名字
	 * @return 失败-1
	 */
	public int xmlFindAttris(String xmlStr, String key) {
		if (xmlStr == null || xmlStr.equals("") || key == null
				|| key.equals(""))
			return -1;
		int count = 0, fromIndex = 0, temp = 0;
		while ((temp = xmlStr.indexOf(key, fromIndex)) != -1) {
			count++;
			fromIndex = temp + key.length();
		}
		return count;
	}

	/**
	 * 取的标签文本包括标签本身
	 * 
	 * @param xmlStr
	 *            原文本
	 * @param beginStr
	 *            开始文本
	 * @param endStr
	 *            结束文本
	 * @return 失败""
	 */
	public String cutTextIncludeLabel(String xmlStr, String beginStr,
			String endStr) {
		if (xmlStr == null || xmlStr.equals("") || beginStr == null
				|| beginStr.equals("") || endStr == null || endStr.equals(""))
			return "";
		int beginIndex = xmlStr.toLowerCase().indexOf(beginStr.toLowerCase());
		if (beginIndex == -1)
			return "";
		beginIndex += beginStr.length();
		int endIndex = xmlStr.indexOf(endStr.toLowerCase(), beginIndex);
		if (endIndex == -1)
			return "";
		// 包涵第一，不包涵最后一个
		return xmlStr.substring(beginIndex - beginStr.length(), endIndex
				+ endStr.length());
	}

	/**
	 * 去掉原文本中注释的{@code <!-- -->}内容
	 * 
	 * @param xmlStr
	 *            原文本
	 * @param key1
	 *            开始属性名字,如果不填或为null{@code <!--}
	 * @param key2
	 *            结束属性名字,如果不填或为null{@code -->}
	 * @return 失败""
	 */
	public String xmlComment(String xmlStr, String key1, String key2) {
		if (xmlStr == null || xmlStr.equals(""))
			return "";
		if (key1 == null || key1.equals("")) {
			key1 = "<!--";
		}
		if (key2 == null || key2.equals("")) {
			key2 = "-->";
		}
		// System.out.println("key1 =" + key1 + ",key2 =" + key2);
		int count = xmlFindAttris(xmlStr, key1);
		if (count == -1) {
			return "";
		}
		for (int i = 0; i < count; i++) {
			String commentAll = cutTextIncludeLabel(xmlStr, key1, key2);
			if (commentAll.equals("")) {
				break;
			}
			xmlStr = xmlStr.replace(commentAll, "");
		}
		return xmlStr;
	}

	/**
	 * .后扩展名 取扩展名 第一步NEW FILE完后创建目录，第二步创建文件才算磁盘里有该文件
	 * 
	 * @param path
	 *            文件名字
	 * @return 失败""
	 */
	public String getExtName(String path) {
		if (path == null || path.equals(""))
			return "";
		String name = new File(path).getName();
		int li = name.lastIndexOf(".");
		if (li == -1)
			return "";
		return name.substring(li + 1);
	}

	/**
	 * 取得歌曲名字，不带后缀
	 * 
	 * @param name
	 * @return
	 */
	public static String getFileName(String name) {
		if (name == null || name.equals(""))
			return "";
		int nameIndex = name.lastIndexOf(".");
		if (nameIndex != -1) {
			name = name.substring(0, nameIndex);
		}
		return name;
	}

	/**
	 * 读文件在指定路径下面,自动根据前三字节编码,并将每一行存入app.lrcs
	 * 必须先FileInputStream然后BufferedInputStream，本来不出字符串
	 * 
	 * @param fullName
	 *            路径全名
	 * @param lrcs
	 *            存一句一句歌词的List
	 * @return 字符串，失败为空值
	 */
	public String readFileData(String fullName, List<String> lrcs) {
		if (fullName == null || fullName.equals("")) {
			return "";
		}
		System.out.println(fullName);
		String restr = "";
		BufferedReader br = null;
		BufferedInputStream bis = null;
		FileInputStream fis = null;
		StringBuffer sb = new StringBuffer();
		File file = new File(fullName);
		try {
			if (!file.exists()) {
				return "";
			}
			fis = new FileInputStream(file);
			bis = new BufferedInputStream(fis);
			bis.mark(4);
			byte[] first3Byte = new byte[3];
			bis.read(first3Byte);
			bis.reset();

			if (first3Byte[0] == (byte) 0xEF && first3Byte[1] == (byte) 0xBB
					&& first3Byte[2] == (byte) 0xBF) {// utf-8
				br = new BufferedReader(new InputStreamReader(bis, "utf-8"));
			} else if (first3Byte[0] == (byte) 0xFF
					&& first3Byte[1] == (byte) 0xFE) {

				br = new BufferedReader(new InputStreamReader(bis, "unicode"));
			} else if (first3Byte[0] == (byte) 0xFE
					&& first3Byte[1] == (byte) 0xFF) {

				br = new BufferedReader(new InputStreamReader(bis, "utf-16be"));
			} else if (first3Byte[0] == (byte) 0xFF
					&& first3Byte[1] == (byte) 0xFF) {

				br = new BufferedReader(new InputStreamReader(bis, "utf-16le"));
			} else {

				br = new BufferedReader(new InputStreamReader(bis, "GBK"));
			}

			while ((restr = br.readLine()) != null) {
				lrcs.add(restr);
				restr += "\n";
				sb.append(restr);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (br != null) {
					fis.close();
					bis.close();
					br.close();
					br = null;
					fis = null;
					bis = null;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	/**
	 * 时间转化：将毫秒转为DD:HH:MM:SS.MI
	 * 
	 * @param ms
	 *            传过来的总毫秒
	 * @param needDay
	 *            是否需要天
	 * @param needHour
	 *            是否需要小时
	 * @param needMills
	 *            是否需要毫秒
	 * @return 转化好的时间，失败为""
	 */
	public static String millisTimeToDotFormat(long ms, boolean needDay,
			boolean needHour, boolean needMills) {
		if (ms < 0)
			return "";

		long sec = 1000;
		long min = sec * 60;
		long hour = min * 60;
		long day = hour * 24;

		long days = ms / day;
		long hours = (ms - days * day) / hour;
		long mins = (ms - days * day - hours * hour) / min;
		long secs = (ms - days * day - hours * hour - mins * min) / sec;
		long millis = ms - days * day - hours * hour - mins * min - secs * sec;

		String strDay = days < 10 ? "0" + days : "" + days;
		String strHour = hours < 10 ? "0" + hours : "" + hours;
		String strMin = mins < 10 ? "0" + mins : "" + mins;
		String strSec = secs < 10 ? "0" + secs : "" + secs;
		String strMills = millis < 100 ? "0" + millis : "" + millis;

		String tmpForDay = needDay ? (strDay + ":") : "";
		String tmpForHour = needHour ? (strHour + ":") : "";
		String tmpForMills = needMills ? ("." + strMills) : "";

		return (tmpForDay + tmpForHour + strMin + ":" + strSec + tmpForMills);
	}

	/**
	 * 将不同格式的时间转化为毫秒，（DD:HH:MM:SS.MI、DD:HH:MM:SS、HH:MM:SS.MI、HH:MM:SS、MM:SS.MI、
	 * MM:SS）
	 * 
	 * @param fmTime
	 * @return 毫秒，失败-1
	 */
	public static long dotFormatToMills(String fmTime) {
		if (fmTime == null || fmTime.equals(""))
			return -1;
		long sec = 1000;
		long min = sec * 60;
		long hour = min * 60;
		long day = hour * 24;

		String[] times = fmTime.split(":");
		if (times == null) {
			return -1;
		}

		long longDay = 0;
		long longHour = 0;
		long longMin = 0;
		long longSec = 0;
		long longMills = 0;

		String strDay = "";
		String strHour = "";
		String strMin = "";
		String strSec = "";
		String strMills = "";

		int index = 0;

		switch (times.length) {
		case 2:
			strMin = times[0];
			strSec = times[1];
			longMin = Long.parseLong(strMin);
			index = strSec.indexOf(".");
			if (index != -1) {
				strMills = strSec.substring(index + 1);
				longMills = Long.parseLong(strMills);
				strSec = strSec.substring(0, index);
			}
			longSec = Long.parseLong(strSec);
			break;
		case 3:
			strHour = times[0];
			strMin = times[1];
			strSec = times[2];
			longHour = Long.parseLong(strHour);
			longMin = Long.parseLong(strMin);
			index = strSec.indexOf(".");
			if (index != -1) {
				strMills = strSec.substring(index + 1);
				strSec = strSec.substring(0, index);
				longMills = Long.parseLong(strMills);
			}
			longSec = Long.parseLong(strSec);
			break;
		case 4:
			strDay = times[0];
			strHour = times[1];
			strMin = times[2];
			strSec = times[3];
			longDay = Long.parseLong(strDay);
			longHour = Long.parseLong(strHour);
			longMin = Long.parseLong(strMin);
			index = strSec.indexOf(".");
			if (index != -1) {
				strMills = strSec.substring(index + 1);
				strSec = strSec.substring(0, index);
				longMills = Long.parseLong(strMills);
			}
			longSec = Long.parseLong(strSec);
			break;
		default:
			return -1;
		}
		return longMills + longSec * sec + longMin * min + longHour * hour
				+ longDay * day;
	}

	/**
	 * 歌曲过滤类
	 * 
	 * @author Administrator
	 * 
	 */
	public static class MusicFilter implements FilenameFilter {

		@Override
		public boolean accept(File dir, String filename) {
			// TODO Auto-generated method stub
			if (filename == null || filename.equals(""))
				return false;
			String ffLowCase = filename.toLowerCase();
			// ..加入支持文件类型
			return (ffLowCase.endsWith(".mp3") || ffLowCase.endsWith(".wma"));
		}

	}

	/**
	 * 时间剪贴器
	 * 
	 * @param allTime
	 *            传入时间（ms）
	 * @return 返回时间，失败返回空值
	 */
	String[] cutTime(int allTime) {
		String time = String.format("%.2f", (double) allTime
				/ ((double) 60 * 1000));
		int timeIndex = time.indexOf(".");
		if (timeIndex == -1) {
			return null;
		}
		// String[] times = new String[]{};为0个元素的数组
		String[] times = new String[4];
		times[0] = time.substring(0, timeIndex);
		times[1] = time.substring(timeIndex + 1);
		int t0 = Integer.parseInt(times[0]);
		int t1 = Integer.parseInt(times[1]);
		if (t1 > 59) {
			t0++;
			t1 = t1 - 60;
		}
		times[0] = t0 + "";
		times[1] = t1 + "";
		return times;
	}
}
