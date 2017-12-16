package cn.shch.myshare.scan.engine;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;
import org.hyperic.sigar.CpuInfo;
import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

import cn.shch.common.MyFileCommon;
import cn.shch.myshare.scan.engine.common.LoggerUtils;
import cn.shch.myshare.scan.engine.common.MyFileUtils;
import cn.shch.myshare.scan.engine.dao.DbOperator;
import cn.shch.myshare.scan.engine.domain.FileData;

public class ScanServer {
	// 调用start方法时传入的参数可选项，如果传入SCAN，扫描指定目录（ROOT_DIRECTORY），扫描后，将扫描到的所有文件信息保存到数据库；
	public static final int SCAN = 1;
	// 调用start方法时传入的参数可选项，如果传入的INCREMENTAL_UPDATE，表示增量更新，扫描指定目录后，将文件的改变更新入数据库；
	public static final int INCREMENTAL_UPDATE = 2;
	// 要扫描的目录
	public static final File[] ROOT_DIRECTORY;
	// 扫描强度，有两个选项，higher和medium，higher表示立刻扫描；medium表示等待系统不忙时扫描，
	public static final String SCAN_INTENSITY;
	// SCAN_INTENSITY_PREMISE指定了在cpu使用率为其所指定的百分比之下表示系统不忙；
	public static final int SCAN_INTENSITY_PREMISE;
	// 如果当前系统cpu使用率大于SCAN_INTENSITY_PREMISE所指定的值，则每隔SCAN_INTENSITY_IF_MEDIUM_OF_PERIOID所指定的秒数后，再检测一次
	public static final int SCAN_INTENSITY_IF_MEDIUM_OF_PERIOID;
	// 分析文件是否重复过程的线程数量
	public static final int ANALYZE_THREAD_COUNT;
	// 要扫描的所有文件类型
	public static final List<String> FILE_SUFFIX_LIST = new ArrayList<String>();

	// 根据不同key名分辨文件的额不同类型，方便保存数据库filetype字段
	//public static final Map<String, String> FILE_TYPE_MAP = new HashMap<String, String>();

	//
	public static final String SCAN_RECORD;

	public static final String FILE_SEPERATER = System.getProperty("file.separator");
	public static final int REPORT_SIZE = 10000;
	private static Logger logger = Logger.getLogger(ScanServer.class);

	static {
		// try {
		InputStream is = ScanServer.class.getClassLoader().getResourceAsStream("config.properties");
		Properties prop = new Properties();
		try {
			prop.load(is);
		} catch (IOException e) {
			logger.error(LoggerUtils.buildDebugMessage(e.getMessage()));
		}
		String scanPath = prop.getProperty("scanpath");
		String[] split = scanPath.split(",");
		ROOT_DIRECTORY = new File[split.length];
		for (int i = 0; i < split.length; i++) {
			ROOT_DIRECTORY[i] = new File(split[i]);
			if (!ROOT_DIRECTORY[i].exists()) {
				logger.error(LoggerUtils.buildDebugMessage("要检索的根路径不存在--" + split[i]));
				System.exit(0);
			}
			if (!ROOT_DIRECTORY[i].isDirectory()) {
				logger.error(LoggerUtils.buildDebugMessage("要检索的根路径必须是文件夹，不能是文件！"));
				System.exit(0);
			}
		}

		String s = prop.getProperty("file.suffix");
		if (s == null || s.trim().length() == 0) {
			logger.error(LoggerUtils.buildDebugMessage("未指定检索文件类型！！--" + s));
			System.exit(0);
		}
		SCAN_INTENSITY = prop.getProperty("scan.intensity");
		String tmp = prop.getProperty("scan.intensity.premise");
		SCAN_INTENSITY_PREMISE = Integer.parseInt(tmp);
		String tmp2 = prop.getProperty("scan.intensity.if.medium.of.period");
		SCAN_INTENSITY_IF_MEDIUM_OF_PERIOID = Integer.parseInt(tmp2);
		String[] suffixes = s.split(",");
		Arrays.sort(suffixes);
		for (int i = 0; i < suffixes.length; i++) {
			FILE_SUFFIX_LIST.add(suffixes[i]);
		}
		SCAN_RECORD = prop.getProperty("scan.log.path");
		String tmpcnt = prop.getProperty("analyze.thread.count");
		ANALYZE_THREAD_COUNT = Integer.parseInt(tmpcnt);
		// 将所有不同文件类型对应的后缀放入FILE_TYPE_MAP中
//		for (Enumeration<?> en = prop.propertyNames(); en.hasMoreElements();) {
//			String name = en.nextElement().toString();
//			if (name.startsWith("file.")) {
//				FILE_TYPE_MAP.put(name, prop.getProperty(name));
//			}
//		}
//		LoggerUtils.buildDebugMessage("FILE_TYPE_MAP:" + FILE_TYPE_MAP);
		// 排序，方便后面使用二分查找法以提高查找效率
		Collections.sort(FILE_SUFFIX_LIST);
		// } catch (Exception e) {
		// logger.error(LoggerUtils.buildDebugMessage(e.getMessage()));
		// }
	}

	private boolean isBusy = false;

	public ScanServer() {

	}

	public void start(int type) {
		// try {
		if (type == ScanServer.SCAN) {
			// List<File> fileList = new ArrayList<File>();
			List<FileData> fileList = null;
			try {
				for (int i = 0; i < ROOT_DIRECTORY.length; i++) {
					if (fileList != null) {
						fileList.addAll(loopFile(ROOT_DIRECTORY[i]));
						continue;
					}
					fileList = loopFile(ROOT_DIRECTORY[i]);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			DbOperator dbo = new DbOperator();
			dbo.saveAll2(fileList);
		} else if (type == ScanServer.INCREMENTAL_UPDATE) {
			/**
			 * 因WatchService存在缺陷，放弃使用随机监控方式，改为扫描方式
			 */
			// WatchService service =
			// FileSystems.getDefault().newWatchService();
			// registerAllDirectory(service);
			// while (true) {
			// WatchKey key = service.take();
			// List<WatchEvent<?>> events = key.pollEvents();
			// for (WatchEvent<?> event : events) {
			// Kind<?> kind = event.kind();
			// if (kind == StandardWatchEventKinds.OVERFLOW) {
			// continue;
			// }
			// String filename = event.context().toString();
			// System.out.println("文件：" + filename + " 事件：" + kind+"
			// 数量："+event.count());
			// boolean reset = key.reset();
			// if (!reset) {
			// logger.error(LoggerUtils.buildDebugMessage("无法重启watchkey!!程序将退出"));
			// break;
			// }
			// }
			// }
			// incrementUpdate(SCAN_INTENSITY, 0);
			if (SCAN_INTENSITY.equals("higher")) {
				for (int i = 0; i < ROOT_DIRECTORY.length; i++)
					work(i);

			} else if (SCAN_INTENSITY.equals("medium")) {
				Sigar sigar = new Sigar();
				CpuPerc cpu = null;
				try {
					cpu = sigar.getCpuPerc();
				} catch (SigarException e) {
					logger.error(LoggerUtils.buildDebugMessage(e.toString()));
				}
				// boolean begin = true;
				for (int i = 0; i < 3; i++) {
					double combined = cpu.getCombined();
					long round = Math.round(combined * 100);
					logger.debug("round:" + round);
					if (round > SCAN_INTENSITY_PREMISE) {
						this.setBusy(true);
						break;
					}
					try {
						LoggerUtils.print("开始检测服务器是否忙碌....", true);
						LoggerUtils.print("第" + i + "次     isbusy:" + this.isBusy(), true);
						Thread.sleep(SCAN_INTENSITY_IF_MEDIUM_OF_PERIOID * 1000);
					} catch (InterruptedException e) {
						logger.error(LoggerUtils.buildDebugMessage(e.toString()));
					}
				}
				if (!this.isBusy()) {
					for (int i = 0; i < ROOT_DIRECTORY.length; i++) {
						work(i);
					}
				} else {
					LoggerUtils.print("服务器忙碌，暂停扫描", true);
				}
			}
		}
		// } catch (Exception e) {
		// logger.error(LoggerUtils.buildDebugMessage(e.toString()));
		// }
	}

	private void work(int idx) {
		LoggerUtils.print("本次扫描开始时间：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), true);
		LoggerUtils.print("扫描路径：" + ROOT_DIRECTORY[idx], true);

		LoggerUtils.print("扫描文件类型：" + FILE_SUFFIX_LIST, true);

		LoggerUtils.print("扫描强度：" + SCAN_INTENSITY, true);

		LoggerUtils.print("扫描服务启动......请等待，等待时间由扫描目录文件数量多少决定,在此期间，请勿对" + ROOT_DIRECTORY[idx] + "目录进行任何操作，以免造成数据不准确...",
				true);
		long startTime = System.currentTimeMillis();

		List<FileData> list = null;
		LoggerUtils.print(
				"-----------------------------------------------------------------扫描过程开始----------------------------------------------------------------------------------",
				false);
		try {
			list = loopFile(ROOT_DIRECTORY[idx]);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		long endTime = System.currentTimeMillis();
		long second = (endTime - startTime) / 1000;
		LoggerUtils.print(
				"-----------------------------------------------------------------扫描过程结束----------------------------------------------------------------------------------",
				false);
		LoggerUtils.print("共扫描有效文件" + list.size() + "个，耗时：" + second + "秒", false);
		LoggerUtils.print(
				"-----------------------------------------------------------------分析数据过程开始----------------------------------------------------------------------------------",
				false);
		DbOperator dop = new DbOperator();
		List<FileData> dbList = dop.findAll();
		Map<String, List<FileData>> map = analyze(list, dbList);
		long endTime1 = System.currentTimeMillis();
		long second1 = (endTime1 - endTime) / 1000;
		LoggerUtils.print(
				"-----------------------------------------------------------------分析数据过程结束----------------------------------------------------------------------------------",
				false);
		LoggerUtils.print("耗时" + second1 + "秒", false);
		LoggerUtils.print(
				"-----------------------------------------------------------------保存数据过程开始----------------------------------------------------------------------------------",
				false);
		dop.incrementUpdate(map);
		long endTime2 = System.currentTimeMillis();
		long second2 = (endTime2 - endTime1) / 1000;
		LoggerUtils.print(
				"-----------------------------------------------------------------保存数据过程结束----------------------------------------------------------------------------------",
				false);
		LoggerUtils.print(
				"----------------------------------------------------------更新数据过程开始---------------------------------------------------------------------------------",
				false);
		LoggerUtils.print(
				"----------------------------------------------------------更新数据过程結束---------------------------------------------------------------------------------",
				false);
		LoggerUtils.print("耗时：" + second2 + "秒", false);
		LoggerUtils.print("扫描服务顺利完成......", true);
		LoggerUtils.print("共扫描文件：" + list.size() + "个", true);
		LoggerUtils.print("数据库操作记录数：", true);
		LoggerUtils.print("添加文件：" + map.get("add").size() + "个；修改文件：" + map.get("upd").size() + "个；删除文件："
				+ map.get("del").size() + "个", true);
		LoggerUtils.print("本次扫描结束时间：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), true);
	}

	// private void incrementUpdate(String scanIntensity, int scanFileType)
	// throws IOException {
	// if (scanIntensity.equals("higher")) {
	// List<File> list = loopFile(ROOT_DIRECTORY);
	//
	// } else if (scanIntensity.equals("medium")) {
	//
	// }
	// }

	private Map<String, List<FileData>> analyze(List<FileData> fileList, List<FileData> dbList) {
		List<FileData> delList = analyzeDelete(dbList);
		LoggerUtils.print("已完成获取删除文件列表过程", false);
		List<FileData> updateList = new ArrayList<FileData>();
		List<FileData> addList = new ArrayList<FileData>();
		Collections.sort(dbList);
		AtomicLong cnt = new AtomicLong(0);
		// long analyzeCount = 1000;
		int fileListCount = fileList.size();
		// System.out.println(fileList.size());
		int eachThreadSize = fileListCount / ANALYZE_THREAD_COUNT;
		int surplus = fileListCount % ANALYZE_THREAD_COUNT;
		// List<Thread> threadList=new ArrayList<Thread>();
		CountDownLatch cdl = new CountDownLatch(ANALYZE_THREAD_COUNT);
		for (int i = 0; i < ANALYZE_THREAD_COUNT; i++) {
			int fromIndex = i * eachThreadSize;
			int toIndex = (i + 1) * eachThreadSize;
			if (i == ANALYZE_THREAD_COUNT - 1)
				toIndex += surplus;
			List<FileData> list = fileList.subList(fromIndex, toIndex);
			// cnt = analyzeAddOrUpdateByEachThread(list, dbList, updateList,
			// addList, cnt);
			Thread th = new Thread(new MyTask(list, dbList, updateList, addList, cnt, cdl));
			th.start();
			// threadList.add(th);
		}
		// for(int i=0;i<threadList.size();i++) {
		// Thread t=threadList.get(i);
		// try {
		// t.join();
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// }
		// }
		try {
			cdl.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Map<String, List<FileData>> map = new HashMap<String, List<FileData>>();
		map.put("add", addList);
		map.put("upd", updateList);
		map.put("del", delList);
		return map;
	}

	class MyTask implements Runnable {
		private List<FileData> list;
		private List<FileData> dbList;
		private List<FileData> updateList;
		private List<FileData> addList;
		private AtomicLong cnt;
		private CountDownLatch cdl;

		MyTask(List<FileData> list, List<FileData> dbList, List<FileData> updateList, List<FileData> addList,
				AtomicLong cnt, CountDownLatch cdl) {
			this.list = list;
			this.dbList = dbList;
			this.updateList = updateList;
			this.addList = addList;
			this.cnt = cnt;
			this.cdl = cdl;
		}

		public void run() {
			// LoggerUtils.print(Thread.currentThread().getName()+":is
			// start....", false);
			for (FileData fd : list) {
				// FileData fd = fileToFileData(f);
				int idx = Collections.binarySearch(dbList, fd);
				if (idx >= 0) {
					FileData fd2 = dbList.get(idx);
					if (fd2.getLastModified() != fd.getLastModified() && fd2.getLastModified() < fd.getLastModified()) {
						fd.setId(fd2.getId());
						synchronized (dbList) {
							updateList.add(fd);
						}
					}
				} else {
					synchronized (dbList) {
						addList.add(fd);
					}
				}
				cnt.addAndGet(1);
				if (cnt.get() % REPORT_SIZE == 0)
					LoggerUtils.print("已分析" + cnt + "个文件......", false);
			}
			cdl.countDown();
			LoggerUtils.print("文件分析已完成" + ANALYZE_THREAD_COUNT + "分之一", false);
		}

	}

	private long analyzeAddOrUpdateByEachThread(List<File> fileList, List<FileData> dbList, List<FileData> updateList,
			List<FileData> addList, long cnt) {
		for (File f : fileList) {
			FileData fd = fileToFileData(f);
			int idx = Collections.binarySearch(dbList, fd);
			if (idx >= 0) {
				FileData fd2 = dbList.get(idx);
				if (fd2.getLastModified() != fd.getLastModified() && fd2.getLastModified() < fd.getLastModified()) {
					fd.setId(fd2.getId());
					updateList.add(fd);
				}
			} else {
				addList.add(fd);
			}
			cnt++;

			if (cnt % REPORT_SIZE == 0)
				LoggerUtils.print("已分析" + cnt + "个文件......", false);
		}
		return cnt;
	}

	private List<FileData> analyzeDelete(List<FileData> dbList) {
		List<FileData> delList = new ArrayList<FileData>();
		for (FileData fd : dbList) {
			String fileName = fd.getFileName();
			String filePath = fd.getFilePath();
			String realPath = filePath + FILE_SEPERATER + fileName;
			File f = new File(realPath);
			if (!f.exists()) {
				delList.add(fd);
			}
		}
		return delList;
	}

	private FileData fileToFileData(File f) {
		String fileName = f.getName();
		String tmp = f.getAbsolutePath();
		String path = tmp.substring(0, tmp.lastIndexOf(FILE_SEPERATER));
		boolean isFile = f.isFile();
		boolean isDirectory = f.isDirectory();
		boolean isHidden = f.isHidden();
		// canExecute 适用于linux
		boolean canExecute = f.canExecute();
		// Boolean canRead = MyFileUtils.canRead(f);
		boolean canRead = true;
		// =========================================调用MyFileUtils的canWrite方法会修改此文件，有可能造成数据不准确，此处先暂时设置为true==================
		Boolean canWrite = true;
		// ========================================="+f.getName()+"==================
		// getFreeSpace适用于windows
		long freeSpace = f.getFreeSpace();
		long totalSpace = f.getTotalSpace();
		long usableSpace = f.getUsableSpace();
		long size = f.length();
		BasicFileAttributeView view = Files.getFileAttributeView(Paths.get(path + FILE_SEPERATER + fileName),
				BasicFileAttributeView.class, LinkOption.NOFOLLOW_LINKS);
		BasicFileAttributes attributes = null;
		try {
			attributes = view.readAttributes();
		} catch (IOException e) {
			e.printStackTrace();
		}
		long createTime = attributes.creationTime().toMillis();
		long lastAccess = attributes.lastAccessTime().toMillis();
		long lastModified = attributes.lastModifiedTime().toMillis();
		// System.out.println("in
		// fileToFileData:==readAttributes的lastModified:"+lastModified);
		// System.out.println("in
		// fileToFileData:==f.lastModified:"+f.getName()+" "+f.lastModified());

		// 文件类型
		// String fileType=null;
		String suffix = MyFileCommon.getFileNameSuffix(fileName);
		FileData fd = new FileData();
		// fd.setId(rs.getInt("id"));
		fd.setFileName(fileName);
		fd.setFilePath(path);
		fd.setDirectory(isDirectory);
		fd.setFile(isFile);
		fd.setCanExecute(canExecute);
		fd.setCanRead(canRead);
		fd.setCanWrite(canWrite);
		fd.setHidden(isHidden);
		fd.setFreeSpace(freeSpace);
		fd.setTotalSpace(totalSpace);
		fd.setUsableSpace(usableSpace);
		fd.setFileSize(size);
		fd.setCreateTime(createTime);
		fd.setLastModified(lastModified);
		fd.setLastAccess(lastAccess);
		if (suffix != null)
			fd.setFileType(suffix);
		return fd;
	}

	/**
	 * 给所有指定目录添加监听,已取消
	 * 
	 * @param service
	 * @throws IOException
	 */
	// private void registerAllDirectory(final WatchService service) throws
	// IOException {
	//
	// Files.walkFileTree(Paths.get(ROOT_DIRECTORY.getCanonicalPath()), new
	// FileVisitor<Path>() {
	// public FileVisitResult preVisitDirectory(Path dir,
	// java.nio.file.attribute.BasicFileAttributes attrs)
	// throws IOException {
	// Path realPath = dir.toRealPath(LinkOption.NOFOLLOW_LINKS);
	// Paths.get(realPath.toString()).register(service,
	// StandardWatchEventKinds.ENTRY_CREATE,
	// StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
	// return FileVisitResult.CONTINUE;
	// };
	//
	// public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws
	// IOException {
	//
	// return FileVisitResult.CONTINUE;
	// }
	//
	// public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws
	// IOException {
	//
	// return FileVisitResult.CONTINUE;
	// }
	//
	// public FileVisitResult visitFileFailed(Path file, IOException exc) throws
	// IOException {
	// return FileVisitResult.CONTINUE;
	// };
	// });
	//
	// Paths.get(ROOT_DIRECTORY.getCanonicalPath()).register(service,
	// StandardWatchEventKinds.ENTRY_CREATE,
	// StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
	// }

	/**
	 * 采用递归方式遍历所有文件（夹），性能较低，放弃使用,改用重载的 loopFile(File rootDirectory)方法
	 * 
	 * @param f
	 * @param fileList
	 */
	@Deprecated
	private void loopFile(File f, List<File> fileList) {
		if (MyFileUtils.canRead(f)) {
			// System.out.println(f.getName());
			if (f.isDirectory()) {
				File[] files = f.listFiles(new FilenameFilter() {
					// 只保留confifg.properties中file.suffix指定的文件类型
					public boolean accept(File dir, String name) {
						// 获取文件名后缀
						int dot = name.lastIndexOf(".");
						if (dot == -1) {
							File ff = new File(dir, name);
							if (ff.isDirectory())
								return true;
							return false;
						}
						String suffix = name.substring(dot + 1);
						if (suffix != null && suffix.trim().length() > 0) {
							// 判断文件是否满足检索要求
							int idx = Collections.binarySearch(FILE_SUFFIX_LIST, suffix);
							if (idx >= 0) {
								return true;
							}
							return false;
						}
						return false;
					}
				});
				for (File ff : files) {
					fileList.add(ff);
					loopFile(ff, fileList);
				}
			} else {
				fileList.add(f);
			}
		}
	}

	class MyFileVisiter implements FileVisitor<Path> {
		private List<FileData> list;
		private long cnt = 0;
		int reportSize = ScanServer.REPORT_SIZE;

		public MyFileVisiter(List<FileData> list) {
			this.list = list;
		}

		public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
			setCnt(getCnt() + 1);
			// System.out.println("preVisitDirectory
			// filename:"+dir.getFileName()+" cnt:"+cnt);
			if (dir.getFileName() == null)
				return FileVisitResult.CONTINUE;
			String filename = dir.getFileName().toString();
			// System.out.println(filename);
			// 去除系统目录
			if (filename.startsWith("$")) {
				// 不访问子目录
				return FileVisitResult.SKIP_SUBTREE;
			}
			Path filePath = dir.toRealPath(LinkOption.NOFOLLOW_LINKS);
			if (filePath != null) {
				File f = new File(filePath.toString());
				// logger.debug(f.getName()+" "+f.lastModified());
				FileData fd = fileToFileData(f);
				list.add(fd);
			}
			if (getCnt() % reportSize == 0)
				LoggerUtils.print("已扫描" + getCnt() + "个文件......", false);
			return FileVisitResult.CONTINUE;
		}

		public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
			// System.out.println("postVisitDirectory：：：：：："+dir.getFileName()+"：：：：cnt:"+cnt);
			return FileVisitResult.CONTINUE;
		}

		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
			setCnt(getCnt() + 1);
			// System.out.println("visitFile filename:"+file.getFileName()+"
			// cnt:"+cnt);
			// System.out.println("file::::::::::::::::"+file);
			Path fileName = file.getFileName();
			if (fileName != null) {
				String fname = fileName.toString();
				if (fname.startsWith("$"))
					return FileVisitResult.CONTINUE;
				int dot = fname.lastIndexOf(".");
				if (dot != -1) {
					String suffix = fname.substring(dot + 1);
					if (suffix != null && suffix.trim().length() > 0) {
						// 判断文件是否是需要被检索的文件
						int idx = Collections.binarySearch(FILE_SUFFIX_LIST, suffix);
						if (idx >= 0) {
							File ff = new File(file.toRealPath(LinkOption.NOFOLLOW_LINKS).toString());
							FileData fd = fileToFileData(ff);
							list.add(fd);
						}
					}
				}
			}
			if (getCnt() % reportSize == 0)
				LoggerUtils.print("已扫描" + getCnt() + "个文件......", false);
			return FileVisitResult.CONTINUE;
		}

		public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
			// System.out.println("visitFileFalied：：：：：："+file.getFileName()+"：：：：cnt:"+cnt);
			return FileVisitResult.CONTINUE;
		}

		public long getCnt() {
			return cnt;
		}

		public void setCnt(long cnt) {
			this.cnt = cnt;
		}

	}

	private List<FileData> loopFile(File rootDirectory) throws IOException {
		final List<FileData> list = new ArrayList<FileData>();
		MyFileVisiter mfv = new MyFileVisiter(list);
		Files.walkFileTree(Paths.get(rootDirectory.getAbsolutePath()), mfv);
		LoggerUtils.print("共扫描文件" + mfv.getCnt() + "个，注：此数字为共扫描文件数，非有效文件数！！", false);
		return list;
	}

	public boolean isBusy() {
		return isBusy;
	}

	public void setBusy(boolean isBusy) {
		this.isBusy = isBusy;
	}
}
