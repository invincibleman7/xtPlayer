package com.tian.uitls;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 转换文件的编码格式
 * 
 * @author tian
 * 
 */
public class ConvertFileCode {
	public String converfile(String filepath) {
		File file = new File(filepath);
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		BufferedReader reader = null;
		StringBuffer text = new StringBuffer();
		try {
			fis = new FileInputStream(file);
			bis = new BufferedInputStream(fis);

			/*
			 * 根据JAVA官方文档的描述，mark(int
			 * readlimit)方法表示，标记当前位置，并保证在mark以后最多可以读取readlimit字节数据
			 * ，mark标记仍有效。如果在mark后读取超过readlimit字节数据，mark标记就会失效，调用reset()方法会有异常。
			 * 但实际的运行情况却和JAVA文档中的描述并不完全相符。 有时候在BufferedInputStream类中调用mark(int
			 * readlimit)方法后，即使读取超过readlimit字节的数据，mark标记仍有效，仍然能正确调用reset方法重置。
			 * 
			 * 事实上，mark在JAVA中的实现是和缓冲区相关的。只要缓冲区够大，mark后读取的数据没有超出缓冲区的大小，mark标记就不会失效
			 * 。如果不够大，mark后又读取了大量的数据，导致缓冲区更新，原来标记的位置自然找不到了。
			 * 
			 * 因此，mark后读取多少字节才失效，并不完全由readlimit参数确定，也和BufferedInputStream类的缓冲区大小有关
			 * 。 如果BufferedInputStream类的缓冲区大小大于readlimit，在mark以后只有读取超过缓冲区大小的数据，
			 * mark标记才会失效
			 */

			bis.mark(4);
			byte[] first3bytes = new byte[3];
			// 找到文档的前三个字节并自动判断文档类型。
			bis.read(first3bytes);
			bis.reset();
			if (first3bytes[0] == (byte) 0xEF && first3bytes[1] == (byte) 0xBB
					&& first3bytes[2] == (byte) 0xBF) {// utf-8
				// defaultCharBufferSize = 8192
				reader = new BufferedReader(new InputStreamReader(bis, "utf-8"));

			} else if (first3bytes[0] == (byte) 0xFF
					&& first3bytes[1] == (byte) 0xFE) {

				reader = new BufferedReader(new InputStreamReader(bis,
						"unicode"));
			} else if (first3bytes[0] == (byte) 0xFE
					&& first3bytes[1] == (byte) 0xFF) {

				reader = new BufferedReader(new InputStreamReader(bis,
						"utf-16be"));
			} else if (first3bytes[0] == (byte) 0xFF
					&& first3bytes[1] == (byte) 0xFF) {

				reader = new BufferedReader(new InputStreamReader(bis,
						"utf-16le"));
			} else {

				reader = new BufferedReader(new InputStreamReader(bis, "GBK"));
			}
			String str = "";

			while ((str = reader.readLine()) != null) {
				text.append(str + "/n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (bis != null) {
				try {
					bis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return text.toString().trim();
	}
}
