package crawl.yun;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import crawl.CrawlWorker;
import crawl.ThreadCrawler;
import crawl.yun.util.UserSet;

public class YunFileThreadCrawler extends ThreadCrawler {
  private static final Logger logger = LogManager.getLogger(YunFileThreadCrawler.class);

  @Override
  public void run() {
    logger.entry();
    List<User> users;
    final int BLOCK_SIZE = 1000;
    users = UserSet.getInstance().getStatusUsers("ready2", BLOCK_SIZE);
    while ( users.size() > 0 ) {
      logger.info("users.size:" + users.size());
      for (final User user : users) {
        logger.info("crawling " + user.uname + "'s files.");
        UserSet.getInstance().setStatus(user.uk, "running2");
        synchronized (this) {
          waitForIdle();
          threadPool.execute(new CrawlWorker(this) {
            @Override
            public void crawl() {
              YunFileCrawler.crawl(user.uk);
              logger.info(user.uname + "'s files crawled.");
              UserSet.getInstance().setStatus(user.uk, "done");
            }
          });
        }
      }
      users = UserSet.getInstance().getStatusUsers("ready2", BLOCK_SIZE);
      while ( users.size() == 0 ) {
        if ( threadsNum > 0 ) {
          synchronized (this) {
            try {
              wait();
            } catch (InterruptedException e) {
              logger.error(e);
            }
          }
          users = UserSet.getInstance().getStatusUsers("ready2", BLOCK_SIZE);

        } else {
          users = UserSet.getInstance().getStatusUsers("ready2", BLOCK_SIZE);
          break;
        }
      }
    }
    join();
    logger.trace("Files crawling finished.");
    logger.exit();
  }
}
