package crawl.yun;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class UserCrawlWorker implements Runnable {
	private static Logger logger = LogManager.getLogger(UserCrawlWorker.class);
	
	private ThreadUserCrawler threadUserCrawler;
	public UserCrawlWorker(ThreadUserCrawler threadUserCrawler) {
		this.threadUserCrawler = threadUserCrawler;
	}
	
	@Override
	public void run() {
		logger.entry();
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			logger.error(e);
		}
		synchronized (threadUserCrawler) {
			threadUserCrawler.threadsNum --;
			threadUserCrawler.notify();
		}
		logger.exit();
	}

}
