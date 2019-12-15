package cn.shch.common;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.util.List;

import org.apache.log4j.Logger;

import cn.shch.myshare.scan.engine.ScanServer;

public class MyFileCommon {
	private static Logger logger = Logger.getLogger(ScanServer.class);

	private MyFileCommon() {
	}

	/**
	 * 根据给出的文件名返回其文件后缀（不带点"."）,如果文件没有后缀，返回null
	 * 
	 * @param fileName
	 * @return
	 */
	public static String getFileNameSuffix(String fileName) {
		int dot = fileName.lastIndexOf(".");
		if (dot >= 0) {
			return fileName.substring(dot + 1);
		}
		return null;
	}

	public static  void  saveAsFile(File savePath, List<String> files, String sep) {
		if (!savePath.exists()) {
			logger.error("扫描文件保存类型为文件，但保存的路径不存在！");
			throw new RuntimeException("保存的路径不存在！");
		}
		if (!savePath.isDirectory()) {
			logger.error("扫描文件保存类型为文件，需要指定保存的文件夹，而不是文件名称！");
			throw new RuntimeException("扫描文件保存类型为文件，需要指定保存的文件夹，而不是文件名称！");
		}
		String absolutePath = savePath.getAbsolutePath();
		BufferedWriter bw=null;
		try {
			bw=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(absolutePath+"/result.txt"), "utf-8"));
			for(String str:files) {
				bw.write(str);
				bw.newLine();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(bw!=null)
				try {
					bw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}
}
