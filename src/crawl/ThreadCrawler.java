package crawl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import util.Variables;


public abstract class ThreadCrawler implements Runnable {
	private static Logger logger = LogManager.getLogger(ThreadCrawler.class);
	
	protected static final int maxThreadsNum = Integer.parseInt(
			Variables.getInstance().getProperty("threadNum"));
	public int threadsNum = 0;
	protected ExecutorService threadPool = Executors.newFixedThreadPool(maxThreadsNum);

	/**
	 * Wait until all threads are finished.
	 */
	public void join() {
		synchronized (this) {
			while ( threadsNum > 0 ) {
				try {
					wait();
				} catch (InterruptedException e) {
					logger.error(e);
				}
			}
		}
	}
	
	/**
	 * Shutdown the thread crawler.
	 */
	final public void shutdown() {
		threadPool.shutdown();
	}
	
	/**
	 * It's usage is as follows.<br>
	 * <code>
	 * synchronized (this) {
	 *   waitForIdle();
	 *   // do something
	 * }
	 * </code>
	 */
	public void waitForIdle() {
		while ( threadsNum >= maxThreadsNum ) {
			try {
				wait();
			} catch (InterruptedException e) {
				logger.error(e);
			}
		}
		threadsNum ++;
	}
}
