package crawl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import util.Variables;


public abstract class ThreadCrawler implements Runnable {
	
	protected static final int maxThreadsNum = Integer.parseInt(
			Variables.getInstance().getProperty("threadNum"));
	public int threadsNum = 0;
	protected ExecutorService threadPool = Executors.newFixedThreadPool(maxThreadsNum);

}
