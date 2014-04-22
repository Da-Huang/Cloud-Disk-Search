package crawl.yun;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import crawl.CrawlWorker;
import crawl.ThreadCrawler;


public class HotUserThreadCrawler extends ThreadCrawler {
	private static Logger logger = LogManager.getLogger(HotUserThreadCrawler.class);

	@Override
	public void run() {
		logger.entry();
		final JSONObject hotType = UserCrawler.fetchHotType();
		final JSONArray hots = hotType.getJSONArray("hot_type");
		for (int i = 0; i < hots.size(); i ++) {
			final JSONObject element = hots.getJSONObject(i);
			final String name = element.getString("type_name");
			final int type = hots.getJSONObject(i).getInt("type");
			logger.info("crawling: " + name);
			synchronized (this) {
				while ( threadsNum >= maxThreadsNum ) {
					try {
						wait();
					} catch (InterruptedException e) {
						logger.error(e);
					}
				}
				threadsNum ++;
				threadPool.execute(new CrawlWorker(this) {
					@Override
					public void crawl() {
						UserCrawler.crawlFirst(type);
					}
				});
			}
		}
		logger.exit();
	}

}
