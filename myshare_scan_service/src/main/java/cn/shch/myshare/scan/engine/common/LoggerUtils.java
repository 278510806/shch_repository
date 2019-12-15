package cn.shch.myshare.scan.engine.common;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import cn.shch.myshare.scan.engine.ScanServer;

public class LoggerUtils {
    private static Logger logger = Logger.getLogger(LoggerUtils.class);

    private LoggerUtils() {
    }

    public static String buildDebugMessage(String msg) {
	return new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date()) + ":<" + msg + ">";
    }
    public static void print(String string) {
    	print(string,true);
    }
    public static void print(String string, boolean isSleep) {
	BufferedWriter bw = null;
	try {
	    bw = new BufferedWriter(new FileWriter(new File(ScanServer.SCAN_RECORD), true));
	    bw.write(string);
	    bw.newLine();
	} catch (IOException e) {
	    e.printStackTrace();
	} finally {
	    try {
		bw.close();
	    } catch (IOException e) {
		logger.error(LoggerUtils.buildDebugMessage(e.toString()));
	    }
	}
	System.out.println(string);
	if (isSleep) {
	    try {
		Thread.sleep(500);
	    } catch (InterruptedException e1) {
		logger.error(LoggerUtils.buildDebugMessage(e1.toString()));
	    }
	}
    }
}
