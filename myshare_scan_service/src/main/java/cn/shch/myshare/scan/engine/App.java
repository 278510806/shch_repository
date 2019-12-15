package cn.shch.myshare.scan.engine;

/**
 * Hello world!
 *
 */
public class App {
	// @Test
	// public void demo(){
	// ScanServer server=new ScanServer();
	// server.start();
	// }
	public static void main(String[] args) {

		new Thread(new Runnable() {
			public void run() {
				while (true) {
					ScanServer server = new ScanServer();
					server.start(ScanServer.INCREMENTAL_UPDATE);

					if (!server.isBusy()) {
						break;
					} else {
						// 如果服务器忙碌，则等待五分钟后继续尝试执行；
						try {
							Thread.sleep(5 * 60 * 1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}).start();

	}
}
