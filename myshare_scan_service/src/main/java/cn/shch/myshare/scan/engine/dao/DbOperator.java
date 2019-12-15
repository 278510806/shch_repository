package cn.shch.myshare.scan.engine.dao;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import cn.shch.myshare.scan.engine.ScanServer;
import cn.shch.myshare.scan.engine.common.JdbcUtils;
import cn.shch.myshare.scan.engine.common.LoggerUtils;
import cn.shch.myshare.scan.engine.common.MyFileUtils;
import cn.shch.myshare.scan.engine.domain.FileData;

public class DbOperator {
    // private static final String FILE_SEPERATER =
    // System.getProperty("file.separator");
    private static Logger logger = Logger.getLogger(DbOperator.class);

    public void saveAll2(List<FileData> list) {
	Connection con = null;
	PreparedStatement pstmt = null;
	ResultSet rs = null;
	try {
	    con = JdbcUtils.getConnection();
	    String sql = "insert into filedata(filename,filepath,isfile,isdirectory,ishidden,canexecute,canread,canwrite,"
	    		+ "freespace,totalspace,usablespace,filesize,createtime,lastaccess,lastmodified,filetype) values("
		    + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	    pstmt = con.prepareStatement(sql);
	    for (int i = 0; i < list.size(); i++) {
		FileData f = list.get(i);
		String fileName = f.getFileName();

		String path = f.getFilePath();
		// String path = tmp.substring(0,
		// tmp.lastIndexOf(ScanServer.FILE_SEPERATER));
		boolean isFile = f.isFile();
		boolean isDirectory = f.isDirectory();
		boolean isHidden = f.isHidden();
		// canExecute 适用于linux
		boolean canExecute = f.isCanExecute();
		Boolean canRead = f.isCanRead();
		Boolean canWrite = f.isCanWrite();
		// getFreeSpace适用于windows
		long freeSpace = f.getFreeSpace();
		long totalSpace = f.getTotalSpace();
		long usableSpace = f.getUsableSpace();
		long size = f.getFileSize();
		long createTime = f.getCreateTime();
		long lastAccess = f.getLastAccess();
		long lastModified = f.getLastModified();
		String fileType=f.getFileType();
		// Files.readAttributes(path, type, options)
		// long lastModified = f.lastModified();
		// // 获取文件时间属性
		// BasicFileAttributeView view = Files.getFileAttributeView(
		// Paths.get(path + ScanServer.FILE_SEPERATER + fileName),
		// BasicFileAttributeView.class,
		// LinkOption.NOFOLLOW_LINKS);
		// BasicFileAttributes attributes = view.readAttributes();
		// long createTime = attributes.creationTime().toMillis();
		// long lastAccess = attributes.lastAccessTime().toMillis();
		// long lastModified = attributes.lastModifiedTime().toMillis();
		// 获取文件所有者
		// FileOwnerAttributeView onerView =
		// Files.getFileAttributeView(Paths.get(path),
		// FileOwnerAttributeView.class, LinkOption.NOFOLLOW_LINKS);
		// String owner = onerView.getOwner().getName();
		// System.out.println(fileName + " " + path + " " + isFile + " "
		// + isDirectory + " " + isHidden + " "
		// + canExecute + " " + canRead + " " + canWrite + " " +
		// freeSpace + " " + totalSpace + " "
		// + usableSpace + " " + size + " " + createTime + " " +
		// lastAccess + " " + lastModified + " "
		// );
		pstmt.setObject(1, fileName);
		pstmt.setObject(2, path);
		pstmt.setObject(3, isFile);
		pstmt.setObject(4, isDirectory);
		pstmt.setObject(5, isHidden);
		pstmt.setObject(6, canExecute);
		pstmt.setObject(7, canRead);
		pstmt.setObject(8, canWrite);
		pstmt.setObject(9, freeSpace);
		pstmt.setObject(10, totalSpace);
		pstmt.setObject(11, usableSpace);
		pstmt.setObject(12, size);
		pstmt.setObject(13, createTime);
		pstmt.setObject(14, lastAccess);
		pstmt.setObject(15, lastModified);
		pstmt.setObject(16, fileType);
		pstmt.addBatch();
	    }
	    pstmt.executeBatch();
	} catch (Exception e) {
	    e.printStackTrace();
	} finally {
	    JdbcUtils.close(rs, pstmt, con);
	}
	// List<File> l = new ArrayList<File>();
	// for (int i = 0; i < list.size(); i++) {
	// // System.out.println("saveAll2:::i:"+i);
	// FileData fd = list.get(i);
	// File f = new File(fd.getFilePath() + ScanServer.FILE_SEPERATER +
	// fd.getFileName());
	// l.add(f);
	// }
	// //每pageCount条数据保存一次
	// int fromIndex = 0;
	// int toIndex = 0;
	// int pageSize=1000;
	// int pageCount = l.size() / pageSize + l.size() % pageSize == 0 ? 0 :
	// 1;
	// int subListSize=0;
	// for (int i = 1; i <=pageCount; i++) {
	// fromIndex = (i - 1) * pageSize;
	// toIndex = fromIndex + pageSize;
	// List<File> subList=null;
	// if(i==pageCount){
	// toIndex=l.size()%pageSize+1;
	// }
	// subList = l.subList(fromIndex, toIndex);
	// saveAll(subList);
	// subListSize+=subList.size();
	// LoggerUtils.print("已保存"+subListSize+"条记录",false);
	// }
    }

    public void saveAll(List<File> list) {
	Connection con = null;
	PreparedStatement pstmt = null;
	ResultSet rs = null;
	try {
	    con = JdbcUtils.getConnection();
	    String sql = "insert into filedata(filename,filepath,isfile,isdirectory,ishidden,canexecute,canread,canwrite,freespace,totalspace,usablespace,filesize,createtime,lastaccess,lastmodified) values("
		    + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	    pstmt = con.prepareStatement(sql);
	    for (int i = 0; i < list.size(); i++) {
		File f = list.get(i);
		String fileName = f.getName();

		String tmp = f.getCanonicalPath();
		String path = tmp.substring(0, tmp.lastIndexOf(ScanServer.FILE_SEPERATER));
		boolean isFile = f.isFile();
		boolean isDirectory = f.isDirectory();
		boolean isHidden = f.isHidden();
		// canExecute 适用于linux
		boolean canExecute = f.canExecute();
		Boolean canRead = MyFileUtils.canRead(f);
		Boolean canWrite = MyFileUtils.canWrite(f);
		// getFreeSpace适用于windows
		long freeSpace = f.getFreeSpace();
		long totalSpace = f.getTotalSpace();
		long usableSpace = f.getUsableSpace();
		long size = f.length();
		// Files.readAttributes(path, type, options)
		// long lastModified = f.lastModified();
		// 获取文件时间属性
		BasicFileAttributeView view = Files.getFileAttributeView(
			Paths.get(path + ScanServer.FILE_SEPERATER + fileName), BasicFileAttributeView.class,
			LinkOption.NOFOLLOW_LINKS);
		BasicFileAttributes attributes = view.readAttributes();
		long createTime = attributes.creationTime().toMillis();
		long lastAccess = attributes.lastAccessTime().toMillis();
		long lastModified = attributes.lastModifiedTime().toMillis();
		// 获取文件所有者
		// FileOwnerAttributeView onerView =
		// Files.getFileAttributeView(Paths.get(path),
		// FileOwnerAttributeView.class, LinkOption.NOFOLLOW_LINKS);
		// String owner = onerView.getOwner().getName();
		// System.out.println(fileName + " " + path + " " + isFile + " "
		// + isDirectory + " " + isHidden + " "
		// + canExecute + " " + canRead + " " + canWrite + " " +
		// freeSpace + " " + totalSpace + " "
		// + usableSpace + " " + size + " " + createTime + " " +
		// lastAccess + " " + lastModified + " "
		// );
		pstmt.setObject(1, fileName);
		pstmt.setObject(2, path);
		pstmt.setObject(3, isFile);
		pstmt.setObject(4, isDirectory);
		pstmt.setObject(5, isHidden);
		pstmt.setObject(6, canExecute);
		pstmt.setObject(7, canRead);
		pstmt.setObject(8, canWrite);
		pstmt.setObject(9, freeSpace);
		pstmt.setObject(10, totalSpace);
		pstmt.setObject(11, usableSpace);
		pstmt.setObject(12, size);
		pstmt.setObject(13, createTime);
		pstmt.setObject(14, lastAccess);
		pstmt.setObject(15, lastModified);
		pstmt.addBatch();
	    }
	    pstmt.executeBatch();
	} catch (Exception e) {
	    e.printStackTrace();
	} finally {
	    JdbcUtils.close(rs, pstmt, con);
	}
    }

    public List<FileData> findAll() {
	Connection con = null;
	PreparedStatement pstmt = null;
	ResultSet rs = null;
	List<FileData> list = new ArrayList<FileData>();
	try {
	    con = JdbcUtils.getConnection();
	    pstmt = con.prepareStatement("select * from filedata");
	    rs = pstmt.executeQuery();
	    while (rs.next()) {
		FileData fd = new FileData();
		fd.setId(rs.getInt("id"));
		fd.setFileName(rs.getString("filename"));
		fd.setFilePath(rs.getString("filepath"));
		fd.setDirectory(rs.getBoolean("isdirectory"));
		fd.setFile(rs.getBoolean("isfile"));
		fd.setCanExecute(rs.getBoolean("canexecute"));
		fd.setCanRead(rs.getBoolean("canread"));
		fd.setCanWrite(rs.getBoolean("canwrite"));
		fd.setHidden(rs.getBoolean("ishidden"));
		fd.setFreeSpace(rs.getLong("freespace"));
		fd.setTotalSpace(rs.getLong("totalspace"));
		fd.setUsableSpace(rs.getLong("usablespace"));
		fd.setFileSize(rs.getLong("filesize"));
		fd.setCreateTime(rs.getLong("createtime"));
		fd.setLastModified(rs.getLong("lastmodified"));
		fd.setLastAccess(rs.getLong("lastaccess"));
		list.add(fd);
	    }
	    return list;
	} catch (Exception e) {
	    logger.error(LoggerUtils.buildDebugMessage(e.getMessage()));
	} finally {
	    JdbcUtils.close(rs, pstmt, con);
	}
	return null;
    }

    public void incrementUpdate(Map<String, List<FileData>> map) {
	List<FileData> addList = map.get("add");
	//System.out.println("addList size:"+addList.size());
	if (addList.size() > 0) {
	    // 每pageCount条数据保存一次
	    int fromIndex = 0;
	    int toIndex = 0;
	    int pageSize = 1000;
	    int pageCount = addList.size() / ScanServer.REPORT_SIZE + (addList.size() % ScanServer.REPORT_SIZE == 0 ? 0 : 1);
	    int subListSize = 0;
	    for (int i = 1; i <= pageCount; i++) {
		fromIndex = (i - 1) * ScanServer.REPORT_SIZE;
		toIndex = fromIndex + ScanServer.REPORT_SIZE;
		List<FileData> subList = null;
		if (i == pageCount) {
		    toIndex = fromIndex+addList.size() % ScanServer.REPORT_SIZE ;
		}
		subList = addList.subList(fromIndex, toIndex);
		saveAll2(subList);
		subListSize += subList.size();
		LoggerUtils.print("已保存" + subListSize + "条记录......", false);
		//this.saveAll2(subList);
	    }
	}
	List<FileData> updList = map.get("upd");
	if (updList.size() != 0) {
	    this.updateAll(updList);
	}
	List<FileData> delList = map.get("del");
	if (delList.size() > 0) {
	    this.delAll(delList);
	}
    }

    private void delAll(List<FileData> delList) {
	Connection con = null;
	PreparedStatement pstmt = null;
	ResultSet rs = null;
	try {
	    con = JdbcUtils.getConnection();
	    String sql = "delete from filedata where id=?";
	    pstmt = con.prepareStatement(sql);
	    for (int i = 0; i < delList.size(); i++) {
		FileData f = delList.get(i);
		pstmt.setObject(1, f.getId());
		pstmt.addBatch();
	    }
	    pstmt.executeBatch();
	} catch (Exception e) {
	    e.printStackTrace();
	} finally {
	    JdbcUtils.close(rs, pstmt, con);
	}

    }

    private void updateAll(List<FileData> updList) {
	Connection con = null;
	PreparedStatement pstmt = null;
	ResultSet rs = null;
	try {
	    con = JdbcUtils.getConnection();
	    String sql = "update  filedata set filename=?,filepath=?,isfile=?,isdirectory=?,ishidden=?,"
		    + "canexecute=?,canread=?,canwrite=?,freespace=?,totalspace=?,usablespace=?,"
		    + "filesize=?,createtime=?,lastaccess=?,lastmodified=? where id=?";
	    pstmt = con.prepareStatement(sql);
	    for (int i = 0; i < updList.size(); i++) {
		FileData f = updList.get(i);
		pstmt.setObject(1, f.getFileName());
		pstmt.setObject(2, f.getFilePath());
		pstmt.setObject(3, f.isFile());
		pstmt.setObject(4, f.isDirectory());
		pstmt.setObject(5, f.isHidden());
		pstmt.setObject(6, f.isCanExecute());
		pstmt.setObject(7, f.isCanRead());
		pstmt.setObject(8, f.isCanWrite());
		pstmt.setObject(9, f.getFreeSpace());
		pstmt.setObject(10, f.getTotalSpace());
		pstmt.setObject(11, f.getUsableSpace());
		pstmt.setObject(12, f.getFileSize());
		pstmt.setObject(13, f.getCreateTime());
		pstmt.setObject(14, f.getLastAccess());
		pstmt.setObject(15, f.getLastModified());
		pstmt.setObject(16, f.getId());
		pstmt.addBatch();
	    }
	    pstmt.executeBatch();
	} catch (Exception e) {
	    e.printStackTrace();
	} finally {
	    JdbcUtils.close(rs, pstmt, con);
	}

    }

}
