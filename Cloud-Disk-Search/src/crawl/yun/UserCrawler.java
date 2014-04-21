package crawl.yun;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class UserCrawler {
	private static Logger logger = LogManager.getLogger(UserCrawler.class);
	
	public static void main(String[] args) throws Exception {
		crawl();
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
	static private JSONObject fetchFollow(int uk, int start, int limit) throws Exception {
		logger.entry(uk, start, limit);
		final String urlBase = "http://yun.baidu.com/pcloud/friend/getfollowlist";
		final Map<String, String> args = new HashMap<String, String>();
		args.put("query_uk", String.valueOf(uk));
		args.put("start", String.valueOf(start));
		args.put("limit", String.valueOf(limit));
		return Request.request(urlBase, args);
	}
	
	static private JSONObject fetchHotType() throws Exception {
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
				start += list.size();
				
			} catch (IOException e) {
				logger.error(e);
			}
		}
	}
	
	static public void crawlFollow(int uk) throws Exception {
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
