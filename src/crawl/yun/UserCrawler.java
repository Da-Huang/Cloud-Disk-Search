package crawl.yun;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import crawl.yun.util.UserSet;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class UserCrawler {
	private static Logger logger = LogManager.getLogger(UserCrawler.class);
	
	public static void main(String[] args) throws Exception {
		crawl();
//		List<User> users = UserSet.getInstance().getUsers(100);
//		for (User user : users) {
//			System.out.println(user);
//		}
	}
	
	/**
	 * @param type hot type
	 */
	static private JSONObject fetchFirst(int type, int start, int limit) throws Exception {
		logger.entry(type, start, limit);
		final String urlBase = "http://yun.baidu.com/pcloud/friend/gethotuserlist";
		final Map<String, String> args = new HashMap<String, String>();
		args.put("type_id", String.valueOf(type));
		args.put("start", String.valueOf(start));
		args.put("limit", String.valueOf(limit));
		return Request.request(urlBase, args);
	}
	
	/**
	 * @param uk user id
	 */
	static private JSONObject fetchFollow(long uk, int start, int limit) throws Exception {
		logger.entry(uk, start, limit);
		final String urlBase = "http://yun.baidu.com/pcloud/friend/getfollowlist";
		final Map<String, String> args = new HashMap<String, String>();
		args.put("query_uk", String.valueOf(uk));
		args.put("start", String.valueOf(start));
		args.put("limit", String.valueOf(limit));
		return Request.request(urlBase, args);
	}
	
	static public JSONObject fetchHotType() throws Exception {
		logger.entry();
		final String urlBase = "http://yun.baidu.com/pcloud/friend/gethottype";
		final Map<String, String> args = new HashMap<String, String>();
		return Request.request(urlBase, args);
	}
	
	static public void crawlFirst(int type) throws Exception {
		int start = 0;
		final int limit = 25;
		while ( true ) {
			try {
				JSONObject data = fetchFirst(type, start, limit);
				if ( !data.containsKey("errno") || data.getInt("errno") != 0 ) {
					break;
				}
				JSONArray list = data.getJSONArray("hotuser_list");
				if ( list.size() == 0 ) break;
				for (int i = 0; i < list.size(); i ++)
					saveFirst(list.getJSONObject(i));
				start += list.size();
				
			} catch (IOException e) {
				logger.error(e);
			}
		}
	}
	
	static private void saveFirst(JSONObject user) {
		long uk = user.getLong("hot_uk");
		String uname = user.getString("hot_uname");
		int follows = user.getInt("follow_count");
		int fans = user.getInt("fans_count");
		int shares = user.getInt("pubshare_count");
		UserSet.getInstance().add(uk, uname, follows, fans, shares);
	}
	
	static private void saveFollow(JSONObject user) {
		long uk = user.getLong("follow_uk");
		String uname = user.getString("follow_uname");
		int follows = user.getInt("follow_count");
		int fans = user.getInt("fans_count");
		int shares = user.getInt("pubshare_count");
		UserSet.getInstance().add(uk, uname, follows, fans, shares);
	}
	
	static public void crawlFollow(long uk) throws Exception {
		int start = 0;
		final int limit = 25;
		while ( true ) {
			try {
				JSONObject data = fetchFollow(uk, start, limit);
				if ( !data.containsKey("errno") || data.getInt("errno") != 0 ) {
					break;
				}
				JSONArray list = data.getJSONArray("follow_list");
				final int total = data.getInt("total_count");
				for (int i = 0; i < list.size(); i ++)
					saveFollow(list.getJSONObject(i));
				start += list.size(); 
				if ( list.size() == 0 || start >= total ) break;
				
			} catch (IOException e) {
				logger.error(e);
			}
		}
	}
	
	static public void crawl() throws Exception {
		JSONObject hotType = UserCrawler.fetchHotType();
		JSONArray hots = hotType.getJSONArray("hot_type");
		for (int i = 0; i < hots.size(); i ++) {
			JSONObject element = hots.getJSONObject(i);
			String name = element.getString("type_name");
			int type = hots.getJSONObject(i).getInt("type");
			logger.info("crawling: " + name);
			crawlFirst(type);
		}
	}
}
