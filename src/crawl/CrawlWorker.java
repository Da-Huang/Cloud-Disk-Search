package crawl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import crawl.ThreadCrawler;


abstract public class CrawlWorker implements Runnable {
	private static final Logger logger = LogManager.getLogger(CrawlWorker.class);
	
	private ThreadCrawler threadUserCrawler;
	public CrawlWorker(ThreadCrawler threadUserCrawler) {
		this.threadUserCrawler = threadUserCrawler;
	}
	
	@Override
	final public void run() {
		logger.entry();
		crawl();
		synchronized (threadUserCrawler) {
			threadUserCrawler.threadsNum --;
			threadUserCrawler.notify();
		}
		logger.exit();
	}

	abstract public void crawl();
}
