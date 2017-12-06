package cn.shch.myshare.scan.engine.domain;

import java.io.File;
import java.util.Date;

import cn.shch.myshare.scan.engine.ScanServer;

public class FileData implements Comparable<FileData> {
	private int id;
	private String fileName;
	private String filePath;
	private boolean isDirectory;
	private boolean isFile;
	private boolean isHidden;
	private boolean canExecute;
	private boolean canRead;
	private boolean canWrite;
	private long freeSpace;
	private long totalSpace;
	private long usableSpace;
	private long fileSize;
	private long createTime;
	private long lastAccess;
	private long lastModified;

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public boolean isDirectory() {
		return isDirectory;
	}

	public void setDirectory(boolean isDirectory) {
		this.isDirectory = isDirectory;
	}

	public boolean isFile() {
		return isFile;
	}

	public void setFile(boolean isFile) {
		this.isFile = isFile;
	}

	public boolean isHidden() {
		return isHidden;
	}

	public void setHidden(boolean isHidden) {
		this.isHidden = isHidden;
	}

	public boolean isCanExecute() {
		return canExecute;
	}

	public void setCanExecute(boolean canExecute) {
		this.canExecute = canExecute;
	}

	public boolean isCanRead() {
		return canRead;
	}

	public void setCanRead(boolean canRead) {
		this.canRead = canRead;
	}

	public boolean isCanWrite() {
		return canWrite;
	}

	public void setCanWrite(boolean canWrite) {
		this.canWrite = canWrite;
	}

	public long getFreeSpace() {
		return freeSpace;
	}

	public void setFreeSpace(long freeSpace) {
		this.freeSpace = freeSpace;
	}

	public long getTotalSpace() {
		return totalSpace;
	}

	public void setTotalSpace(long totalSpace) {
		this.totalSpace = totalSpace;
	}

	public long getUsableSpace() {
		return usableSpace;
	}

	public void setUsableSpace(long usableSpace) {
		this.usableSpace = usableSpace;
	}

	public long getFileSize() {
		return fileSize;
	}

	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public long getLastAccess() {
		return lastAccess;
	}

	public void setLastAccess(long lastAccess) {
		this.lastAccess = lastAccess;
	}

	public long getLastModified() {
		return lastModified;
	}

	public void setLastModified(long lastModified) {
		this.lastModified = lastModified;
	}

	@Override
	public String toString() {
		return "FileData [id=" + id + ", fileName=" + fileName + ", filePath=" + filePath + ", isDirectory="
				+ isDirectory + ", isFile=" + isFile + ", isHidden=" + isHidden + ", canExecute=" + canExecute
				+ ", canRead=" + canRead + ", canWrite=" + canWrite + ", freeSpace=" + freeSpace + ", totalSpace="
				+ totalSpace + ", usableSpace=" + usableSpace + ", fileSize=" + fileSize + ", createTime=" + createTime
				+ ", lastAccess=" + lastAccess + ", lastModified=" + lastModified + "]";
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int compareTo(FileData o) {
		if (this == o)
			return 0;
		// System.out.println("this's "+this.getFileName()+" o's
		// "+o.getFileName());
		// System.out.println("this's "+this.getFilePath()+" o's
		// "+o.getFilePath());
		// int
		// res=this.getFileName().compareTo(o.getFileName())+this.getFilePath().compareTo(o.getFilePath());
		int res = (this.getFilePath() + ScanServer.FILE_SEPERATER + this.getFileName())
				.compareTo(o.getFilePath() + ScanServer.FILE_SEPERATER + o.getFileName());
		// System.out.println("res:"+res);
		return res;
	}
	// public File toFile(){
	// return new
	// File(this.getFilePath()+ScanServer.FILE_SEPERATER+this.getFileName());
	// }
	// public int compareTo(FileData o) {
	// if(this==o) return 0;
	// return
	// this.fileName.compareTo(o.fileName)+this.filePath.compareTo(o.filePath)+(int)(this.fileSize-o.fileSize)
	// +new Boolean(isDirectory).compareTo(o.isDirectory)+new
	// Boolean(isFile).compareTo(o.isFile)
	// +new Boolean(canExecute).compareTo(o.canExecute)+new
	// Boolean(canRead).compareTo(o.canRead)
	// +new
	// Boolean(canWrite).compareTo(o.canWrite)+(int)(createTime-o.createTime)+(int)(lastModified-o.lastModified)
	// +(int)(lastAccess+o.lastAccess);
	// }

}
