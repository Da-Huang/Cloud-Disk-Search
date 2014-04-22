package crawl.yun;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import crawl.yun.util.UserSet;


public class UserCrawler {
	private static Logger logger = LogManager.getLogger(UserCrawler.class);
	
	public static void main(String[] args) throws Exception {
//		final Thread thread = new Thread(new UserThreadCrawler());
//		thread.start();
//		thread.join();
		UserCrawler.crawl();
		List<User> users = UserSet.getInstance().getUndealingUsers(10000000);
		for (int i = 0; i < users.size(); i ++) {
			System.out.print(i + 1 + ": ");
			System.out.println(users.get(i));
		}
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
		JSONObject res = null;
		try {
			res = Request.request(urlBase, args);
		} catch (Exception e) {
			logger.error(e);
		}
		return res;
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
		JSONObject res = null;
		try {
			res = Request.request(urlBase, args);
		} catch (Exception e) {
			logger.error(e);
		}
		return res;
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
		JSONObject res = null;
		try {
			res = Request.request(urlBase, args);
		} catch (Exception e) {
			logger.error(e);
		}
		return res;
	}
	
	static public JSONObject fetchHotType() {
		logger.entry();
		final String urlBase = "http://yun.baidu.com/pcloud/friend/gethottype";
		final Map<String, String> args = new HashMap<String, String>();
		JSONObject res = null;
		try {
			res = Request.request(urlBase, args);
		} catch (Exception e) {
			logger.error(e);
		}
		return res;
	}
	
	static public void crawlFirst(int type) {
		int start = 0;
		final int limit = 25;
		while ( true ) {
			JSONObject data = fetchFirst(type, start, limit);
			if ( data == null || !data.containsKey("errno") || data.getInt("errno") != 0 ) {
				break;
			}
			JSONArray list = data.getJSONArray("hotuser_list");
			if ( list.size() == 0 ) break;
			for (int i = 0; i < list.size(); i ++)
				saveFirst(list.getJSONObject(i));
			start += list.size();
		}
	}
	
	static private void saveFirst(JSONObject user) {
		final long uk = user.getLong("hot_uk");
		final String uname = user.getString("hot_uname");
		final int follows = user.getInt("follow_count");
		final int fans = user.getInt("fans_count");
		final int shares = user.getInt("pubshare_count");
		UserSet.getInstance().add(uk, uname, follows, fans, shares);
	}
	
	static private void saveFollow(JSONObject user) {
		final long uk = user.getLong("follow_uk");
		final String uname = user.getString("follow_uname");
		final int follows = user.getInt("follow_count");
		final int fans = user.getInt("fans_count");
		final int shares = user.getInt("pubshare_count");
		UserSet.getInstance().add(uk, uname, follows, fans, shares);
	}

	static private void saveFan(JSONObject user) {
		final long uk = user.getLong("fans_uk");
		final String uname = user.getString("fans_uname");
		final int follows = user.getInt("follow_count");
		final int fans = user.getInt("fans_count");
		final int shares = user.getInt("pubshare_count");
		UserSet.getInstance().add(uk, uname, follows, fans, shares);
	}
	
	static public void crawlFollow(long uk) {
		int start = 0;
		final int limit = 25;
		while ( true ) {
			JSONObject data = fetchFollow(uk, start, limit);
			if ( data == null || !data.containsKey("errno") || data.getInt("errno") != 0 ) {
				break;
			}
			JSONArray list = data.getJSONArray("follow_list");
			final int total = data.getInt("total_count");
			for (int i = 0; i < list.size(); i ++)
				saveFollow(list.getJSONObject(i));
			start += list.size(); 
			if ( list.size() == 0 || start >= total ) break;
		}
	}
	
	static public void crawlFan(long uk) {
		int start = 0;
		final int limit = 25;
		while ( true ) {
			JSONObject data = fetchFan(uk, start, limit);
			if ( data == null || !data.containsKey("errno") || data.getInt("errno") != 0 ) {
				break;
			}
			JSONArray list = data.getJSONArray("fans_list");
			final int total = data.getInt("total_count");
			for (int i = 0; i < list.size(); i ++)
				saveFan(list.getJSONObject(i));
			start += list.size(); 
			if ( list.size() == 0 || start >= total ) break;
		}
	}
	
	static public void crawl() {
		logger.entry();
		Thread thread = new Thread(new HotUserThreadCrawler());
		thread.start();
		try {
			thread.join();
		} catch (InterruptedException e) {
			logger.error(e);
		}
		logger.exit();
//		final JSONObject hotType = UserCrawler.fetchHotType();
//		final JSONArray hots = hotType.getJSONArray("hot_type");
//		for (int i = 0; i < hots.size(); i ++) {
//			final JSONObject element = hots.getJSONObject(i);
//			final String name = element.getString("type_name");
//			final int type = hots.getJSONObject(i).getInt("type");
//			logger.info("crawling: " + name);
//			crawlFirst(type);
//		}
	}
}
