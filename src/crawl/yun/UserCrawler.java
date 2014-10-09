package crawl.yun;

import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import crawl.yun.util.UserSet;


public class UserCrawler {
  private static final Logger logger = LogManager.getLogger(UserCrawler.class);

  public static void main(String[] args) throws Exception {
    UserCrawler.crawl();
//    UserCrawler.crawlFollow(1463705406L);
  }

  /**
   * @param type hot type
   */
  static private JSONObject fetchFirst(int type, int start, int limit) {
    logger.entry(type, start, limit);
    final String urlBase = "http://yun.baidu.com/pcloud/friend/gethotuserlist";
    final Map<String, String> args = new HashMap<String, String>();
    args.put("type_id", String.valueOf(type));
    args.put("start", String.valueOf(start));
    args.put("limit", String.valueOf(limit));
    return Request.requestForceYun(urlBase, args);
  }

  /**
   * @param uk user id
   */
  static private JSONObject fetchFollow(long uk, int start, int limit) {
    logger.entry(uk, start, limit);
    final String urlBase = "http://yun.baidu.com/pcloud/friend/getfollowlist";
    final Map<String, String> args = new HashMap<String, String>();
    args.put("query_uk", String.valueOf(uk));
    args.put("start", String.valueOf(start));
    args.put("limit", String.valueOf(limit));
    return Request.requestForceYun(urlBase, args);
  }

  /**
   * @param uk user id
   */
  static private JSONObject fetchFan(long uk, int start, int limit) {
    logger.entry(uk, start, limit);
    final String urlBase = "http://yun.baidu.com/pcloud/friend/getfanslist";
    final Map<String, String> args = new HashMap<String, String>();
    args.put("query_uk", String.valueOf(uk));
    args.put("start", String.valueOf(start));
    args.put("limit", String.valueOf(limit));
    return Request.requestForceYun(urlBase, args);
  }

  static public JSONObject fetchHotType() {
    logger.entry();
    final String urlBase = "http://yun.baidu.com/pcloud/friend/gethottype";
    final Map<String, String> args = new HashMap<String, String>();
    return Request.requestForceYun(urlBase, args);
  }

  static public void crawlFirst(int type) {
    logger.entry(type);
    int start = 0;
    final int limit = 25;
    while ( true ) {
      final JSONObject data = fetchFirst(type, start, limit);
      final int errno = Integer.parseInt(data.optString("errno", "-1"));
      if ( errno != 0 ) break;
      final JSONArray list = data.getJSONArray("hotuser_list");
      if ( list.size() == 0 ) break;
      for (int i = 0; i < list.size(); i ++)
        saveFirst(list.getJSONObject(i));
      start += list.size();
    }
    logger.exit();
  }

  static private void saveFirst(JSONObject user) {
    final long uk = Long.parseLong(user.getString("hot_uk"));
    final String uname = user.getString("hot_uname");
    final String intro = user.getString("intro");
    final int follows = Integer.parseInt(user.getString("follow_count"));
    final int fans = Integer.parseInt(user.getString("fans_count"));
    final int shares = Integer.parseInt(user.getString("pubshare_count"));
    UserSet.getInstance().add(uk, uname, intro, follows, fans, shares);
  }

  static private void saveFollow(JSONObject user) {
    final long uk = Long.parseLong(user.getString("follow_uk"));
    final String uname = user.getString("follow_uname");
    final String intro = user.getString("intro");
    final int follows = Integer.parseInt(user.getString("follow_count"));
    final int fans = Integer.parseInt(user.getString("fans_count"));
    final int shares = Integer.parseInt(user.getString("pubshare_count"));
    UserSet.getInstance().add(uk, uname, intro, follows, fans, shares);
  }

  static private void saveFan(JSONObject user) {
    final long uk = Long.parseLong(user.getString("fans_uk"));
    final String uname = user.getString("fans_uname");
    final String intro = user.getString("intro");
    final int follows = Integer.parseInt(user.getString("follow_count"));
    final int fans = Integer.parseInt(user.getString("fans_count"));
    final int shares = Integer.parseInt(user.getString("pubshare_count"));
    UserSet.getInstance().add(uk, uname, intro, follows, fans, shares);
  }

  static public void crawlFollow(long uk) {
    logger.entry(uk);
    int start = 0;
    final int limit = 25;
    while ( true ) {
      final JSONObject data = fetchFollow(uk, start, limit);
      final int errno = Integer.parseInt(data.optString("errno", "-1"));
      if ( errno != 0 ) break;
      final JSONArray list = data.getJSONArray("follow_list");
      final int total = Integer.parseInt(data.getString("total_count"));
      for (int i = 0; i < list.size(); i ++)
        saveFollow(list.getJSONObject(i));
      start += list.size();
      if ( list.size() == 0 || start >= total ) break;
    }
    logger.exit();
  }

  static public void crawlFan(long uk) {
    int start = 0;
    final int limit = 25;
    while ( true ) {
      final JSONObject data = fetchFan(uk, start, limit);
      final int errno = Integer.parseInt(data.optString("errno", "-1"));
      if ( errno != 0 ) break;
      final JSONArray list = data.getJSONArray("fans_list");
      final int total = Integer.parseInt(data.getString("total_count"));
      for (int i = 0; i < list.size(); i ++)
        saveFan(list.getJSONObject(i));
      start += list.size();
      if ( list.size() == 0 || start >= total ) break;
    }
  }

  static public void crawl() {
    logger.entry();
    UserThreadCrawler userThreadCrawler = new UserThreadCrawler();
    final Thread thread = new Thread(userThreadCrawler);
    thread.start();
    try {
      thread.join();
    } catch (InterruptedException e) {
      logger.error(e);
    }
    userThreadCrawler.shutdown();
    logger.exit();
  }
}
