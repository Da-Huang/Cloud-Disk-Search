package crawl.yun;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import util.Variables;


public class ThreadUserCrawler implements Runnable {
	private static Logger logger = LogManager.getLogger(ThreadUserCrawler.class);
	
	private boolean stop = false;
	private static final int maxThreadsNum = Integer.parseInt(
			Variables.getInstance().getProperty("threadNum"));
	int threadsNum = 0;
	private ExecutorService threadPool = Executors.newFixedThreadPool(maxThreadsNum);

	public static void main(String[] args) throws InterruptedException {
		Thread t = new Thread(new ThreadUserCrawler());
		t.start();
		System.out.println("ok");
		t.join();
	}
	
	@Override
	public void run() {
		logger.entry();
		while ( !stop ) {
			synchronized (this) {
				while ( threadsNum >= maxThreadsNum ) {
					try {
						this.wait();
					} catch (InterruptedException e) {
						logger.error(e);
					}
				}
				threadsNum ++;
				threadPool.execute(new UserCrawlWorker(this));
				logger.info(threadsNum);
			}
		}
		logger.exit();
	}
	

	public void stop() {
		stop = true;
	}
}
