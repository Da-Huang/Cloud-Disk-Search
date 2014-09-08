package crawl.yun;

import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import crawl.CrawlWorker;
import crawl.ThreadCrawler;
import crawl.yun.util.UserSet;


public class UserThreadCrawler extends ThreadCrawler {
  private static final Logger logger = LogManager.getLogger(UserThreadCrawler.class);

  @Override
  public void run() {
    logger.entry();
    List<User> users;
    final JSONObject hotType = UserCrawler.fetchHotType();
    final JSONArray hots = hotType.getJSONArray("hot_type");
    for (int i = 0; i < hots.size(); i ++) {
      final JSONObject element = hots.getJSONObject(i);
      final String name = element.getString("type_name");
      final int type = hots.getJSONObject(i).getInt("type");
      logger.info("saving " + name);
      synchronized (this) {
        waitForIdle();
        threadPool.execute(new CrawlWorker(this) {
          @Override
          public void crawl() {
            logger.entry(type);
            UserCrawler.crawlFirst(type);
            logger.exit();
          }
        });
      }
    }
    join();
    logger.trace("Hot crawling finished.");

    users = UserSet.getInstance().getUndealingUsers(Integer.MAX_VALUE);
    for (final User user : users) {
      logger.info("crawling " + user.uname + "'s fans.");
      synchronized (this) {
        waitForIdle();
        threadPool.execute(new CrawlWorker(this) {
          @Override
          public void crawl() {
            UserCrawler.crawlFan(user.uk);
          }
        });
      }
    }
    join();
    logger.trace("Hot fan crawling finished.");

    final int BLOCK_SIZE = 1000;
    users = UserSet.getInstance().getUndealingUsers(BLOCK_SIZE);
    while ( users.size() > 0 ) {
      for (final User user : users) {
        logger.info("crawling " + user.uname + "'s follows.");
        UserSet.getInstance().setDealing(user.uk, true);
        synchronized (this) {
          waitForIdle();
          threadPool.execute(new CrawlWorker(this) {
            @Override
            public void crawl() {
              UserCrawler.crawlFollow(user.uk);
              UserSet.getInstance().setCrawled(user.uk, true);
            }
          });
        }
        logger.info(user.uname + "'s follows crawled.");
      }
      users = UserSet.getInstance().getUndealingUsers(BLOCK_SIZE);
      while ( users.size() == 0 ) {
        final int uncrawledSize = UserSet.getInstance().uncrawledSize();
        if ( uncrawledSize == 0 ) break;
        synchronized (this) {
          try {
            wait();
          } catch (InterruptedException e) {
            logger.error(e);
          }
        }
        users = UserSet.getInstance().getUndealingUsers(BLOCK_SIZE);
      }
    }
    join();
    logger.trace("Follows crawling finished.");
    logger.exit();
  }
}
