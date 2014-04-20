package crawl.yun;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.sf.json.JSONObject;

public class UserCrawler {
	private static Logger logger = LogManager.getLogger(UserCrawler.class.getName());
	
	private JSONObject fetchFirst(int type, int start, int limit) throws IOException {
		logger.entry(type, start, limit);
		final String urlBase = "http://yun.baidu.com/pcloud/friend/gethotuserlist";
		final Map<String, String> args = new HashMap<String, String>();
		args.put("type_id", String.valueOf(type));
		args.put("start", String.valueOf(start));
		args.put("limit", String.valueOf(limit));
		return Request.request(urlBase, args);
	}
	
	private JSONObject fetchFollow(int uk, int start, int limit) {
		return null;
	}
	
	public void crawlFirst() {
		
	}
	
	public void crawlFollow() {
	}
}
