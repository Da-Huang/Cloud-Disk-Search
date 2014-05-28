package crawl.yun;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class YunFileCrawler {
	private static final Logger logger = LogManager.getLogger(YunFileCrawler.class);
	
	private static final String URL_BASE_TOP_LEVEL = "http://yun.baidu.com/pcloud/feed/getsharelist";
	private static final String URL_BASE_DIR = "http://yun.baidu.com/share/list";
	private static final String URL_BASE_ALBUM = "http://yun.baidu.com/pcloud/album/listfile";
	
	/**
	 * @param uk user id
	 */
	static private JSONObject fetchTopLevel(long uk, int start, int limit) {
		logger.entry(uk, start, limit);
		final Map<String, String> args = new HashMap<String, String>();
		args.put("auth_type", String.valueOf(1));
		args.put("query_uk", String.valueOf(uk));
		args.put("start", String.valueOf(start));
		args.put("limit", String.valueOf(limit));
		return Request.requestForceYun(URL_BASE_TOP_LEVEL, args);
	}
	
	/**
	 * @param uk user id
	 * @param sid share id
	 * @param dir directory path
	 */
	static private JSONObject fetchDir(long uk, long sid, String dir, int page) {
		logger.entry(uk, sid, dir, page);
		final Map<String, String> args = new HashMap<String, String>();
		args.put("uk", String.valueOf(uk));
		args.put("sid", String.valueOf(sid));
		args.put("dir", dir);
		args.put("page", String.valueOf(page));
		return Request.requestForceYun(URL_BASE_DIR, args);
	}
	
	/**
	 * @param uk user id
	 * @param aid album id
	 */
	static private JSONObject fetchAlbum(long uk, long aid, int start, int limit) {
		logger.entry(uk, aid, start, limit);
		final Map<String, String> args = new HashMap<String, String>();
		args.put("query_uk", String.valueOf(uk));
		args.put("album_id", String.valueOf(aid));
		args.put("start", String.valueOf(start));
		args.put("limit", String.valueOf(limit));
		return Request.requestForceYun(URL_BASE_ALBUM, args);
	}
	
	static private void saveFilesInTopLevel(JSONObject file) {
		
	}
	
	static private void saveFilesInDir(JSONObject file) {
		
	}
	
	static private void saveFilesInAlbum(JSONObject file) {
		
	}
	
	/**
	 * @param uk user id
	 */
	static public void crawlTopLevel(long uk) {
		logger.entry(uk);

		int start = 0;
		final int limit = 100;
		while ( true ) {
			JSONObject data = fetchTopLevel(uk, start, limit);
			if ( data == null || !data.containsKey("errno") || data.getInt("errno") != 0 ) {
				break;
			}
			JSONArray list = data.getJSONArray("records");
			final int total = data.getInt("total_count");
			for (int i = 0; i < list.size(); i ++) {
				JSONObject record = list.getJSONObject(i);
				if ( !record.containsKey("filelist") || record.getInt("filelist") < 1 ) {
					continue;
				}
				String feedType = record.getString("feed_type");
				if ( feedType.equals("album") ) {
					long aid = record.getLong("album_id");
					crawlAlbum(uk, aid);
					
				} else if ( feedType.equals("share") ) {
					// May be file or directory
					JSONArray filelist = record.getJSONArray("filelist");
					if ( filelist == null || filelist.size() < 1 ) continue;
					JSONObject file = filelist.getJSONObject(0);
					boolean isDir = file.getBoolean("isdir");

					if ( file.getBoolean("isdir") ) {
						// directory
						String path = file.getString("path");
						if ( path.startsWith("%") ) {
							try {
								path = URLDecoder.decode(path, "utf8");
							} catch (UnsupportedEncodingException e) {
								logger.error(e);
							}
						} else {
							
						}
						
					} else {
						// file
						saveFilesInTopLevel(record);
					}
				}
				saveFilesInTopLevel(record);
			}
			start += list.size(); 
			if ( list.size() == 0 || start >= total ) break;
		}
		logger.exit();
	}
	
	static public void crawlAlbum(long uk, long aid) {
		
	}
	
	static public void crawlDir(long uk) {
		
	}
}
