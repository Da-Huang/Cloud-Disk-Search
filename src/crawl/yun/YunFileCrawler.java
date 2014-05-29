package crawl.yun;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import crawl.yun.util.FileSet;

public class YunFileCrawler {
	private static final Logger logger = LogManager.getLogger(YunFileCrawler.class);
	
	/**
	 * @param uk user id
	 */
	static private JSONObject fetchTopLevel(long uk, int start, int limit) {
		logger.entry(uk, start, limit);
		final String urlBase = "http://yun.baidu.com/pcloud/feed/getsharelist";
		final Map<String, String> args = new HashMap<String, String>();
		args.put("auth_type", String.valueOf(1));
		args.put("query_uk", String.valueOf(uk));
		args.put("start", String.valueOf(start));
		args.put("limit", String.valueOf(limit));
		return Request.requestForceYun(urlBase, args);
	}
	
	/**
	 * @param uk user id
	 * @param sid share id
	 * @param dir directory path
	 */
	static private JSONObject fetchDir(long uk, long sid, String dir, int page) {
		logger.entry(uk, sid, dir, page);
		final String urlBase = "http://yun.baidu.com/share/list";
		final Map<String, String> args = new HashMap<String, String>();
		args.put("uk", String.valueOf(uk));
		args.put("sid", String.valueOf(sid));
		args.put("dir", dir);
		args.put("page", String.valueOf(page));
		return Request.requestForceYun(urlBase, args);
	}
	
	/**
	 * @param uk user id
	 * @param aid album id
	 */
	static private JSONObject fetchAlbum(long uk, long aid, int start, int limit) {
		logger.entry(uk, aid, start, limit);
		final String urlBase = "http://yun.baidu.com/pcloud/album/listfile";
		final Map<String, String> args = new HashMap<String, String>();
		args.put("query_uk", String.valueOf(uk));
		args.put("album_id", String.valueOf(aid));
		args.put("start", String.valueOf(start));
		args.put("limit", String.valueOf(limit));
		return Request.requestForceYun(urlBase, args);
	}
	
	static private void saveFilesInTopLevel(JSONObject record) {
		final String urlBase = "http://yun.baidu.com/share/link";
		long uk = record.getLong("uk");
		long sid = record.getLong("shareid");
		String desc = record.getString("desc");
		JSONObject file = record.getJSONArray("filelist").getJSONObject(0);
		String md5 = file.getString("md5");
		String title = file.getString("title");
		String url = String.format("%s?uk=%d&shareid=%d", urlBase, uk, sid);
		long size = file.getLong("size");
		long time = file.getLong("feed_time");
		int downloads = file.getInt("dCnt");
		int visits = file.getInt("vCnt");
		int saves = file.getInt("tCnt");
		FileSet.getInstance().add(uk, md5, title, url, size, desc, time, downloads, visits, saves);
	}
	
	static private void saveFilesInDir(long uk, long sid, String desc, 
			int downloads, int visits, int saves, JSONObject file) {
		final String urlBase = "http://yun.baidu.com/share/link";
		long fsid = file.getLong("fs_id");
		String url = String.format("%s?uk=%d&shareid=%d&fid=%d", urlBase, uk, sid, fsid);
		String md5 = file.getString("md5");
		long time = file.getLong("server_mtime");
		String title = file.getString("server_filename");
		long size = file.getLong("size");
		FileSet.getInstance().add(uk, md5, title, url, size, desc, time, downloads, visits, saves);
	}
	
	static private void saveFilesInAlbum(long uk, long aid, String desc, JSONObject file) {
		final String urlBase = "http://yun.baidu.com/pcloud/album/file";
		if ( !file.containsKey("size") ) return;
		long size = file.getLong("size");
		String md5 = file.getString("md5");
		long fsid = file.getLong("fs_id");
		String title = file.getString("server_filename");
		String url = String.format("%s?uk=%d&album_id=%d&fsid=%d", urlBase, uk, aid, fsid);
		long time = file.getLong("add_time");
		int downloads = file.getInt("dCnt");
		int visits = file.getInt("vCnt");
		int saves = file.getInt("tCnt");
		FileSet.getInstance().add(uk, md5, title, url, size, desc, time, downloads, visits, saves);
	}
	
	/**
	 * Deal user's one record.
	 * @param uk user id
	 * @param record record
	 * @return Whether this record is valid.
	 */
	static private boolean dealWithRecordInTopLevel(long uk, JSONObject record) {
		if ( !record.containsKey("filelist") || record.getInt("filelist") < 1 ) {
			return false;
		}
		String feedType = record.getString("feed_type");
		if ( feedType.equals("album") ) {
			long aid = record.getLong("album_id");
			String desc = record.getString("desc");
			crawlAlbum(uk, aid, desc);
			
		} else if ( feedType.equals("share") ) {
			// May be file or directory
			long sid = record.getLong("shareid");
			JSONArray filelist = record.getJSONArray("filelist");
			if ( filelist == null || filelist.size() < 1 ) return false;
			JSONObject file = filelist.getJSONObject(0);
			boolean isDir = file.getInt("isdir") != 0;

			if ( isDir ) {
				// directory
				String path = decodePath(file.getString("path"));
				String desc = record.getString("desc");
				int downloads = file.getInt("dCnt");
				int visits = file.getInt("vCnt");
				int saves = file.getInt("tCnt");
				crawlDir(uk, sid, path, desc, downloads, visits, saves);
				
			} else {
				// file
				saveFilesInTopLevel(record);
			}
			
		} else return false;
		return true;
	}
	
	static private boolean dealWithRecordInAlbum(long uk, long aid, String desc, JSONObject record) {
		int fileStatus = record.getInt("file_status");
		if ( fileStatus == 0 ) { // legal file
			saveFilesInAlbum(uk, aid, desc, record);
			return true;
			
		} else return false;
	}
	
	static private boolean dealWithRecordInDir(long uk, long sid, String dir, String desc, 
			int downloads, int visits, int saves, JSONObject record) {
		boolean isDir = record.getInt("isdir") != 0;
		if ( isDir ) {
			String path = decodePath(record.getString("path"));
			crawlDir(uk, sid, path, desc, downloads, visits, saves);
			
		} else {
			saveFilesInDir(uk, sid, desc, downloads, visits, saves, record);
		}
		return true;
	}
	
	/**
	 * Decode a path value to utf8
	 * @param path path value in json
	 * @return path coded in utf8
	 */
	static private String decodePath(String path) {
		if ( path.startsWith("%") ) {
			try {
				path = URLDecoder.decode(path, "utf8");
			} catch (UnsupportedEncodingException e) {
				logger.error(e);
			}
		} else {
			try {
				path = new String(path.getBytes(), "utf8");
			} catch (UnsupportedEncodingException e) {
				logger.error(e);
			}
		}
		return path;
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
				dealWithRecordInTopLevel(uk, record);
			}
			start += list.size(); 
			if ( list.size() == 0 || start >= total ) break;
		}
		logger.exit();
	}
	
	static public void crawlAlbum(long uk, long aid, String desc) {
		logger.entry(uk, aid);
		int start = 0;
		final int limit = 100;
		while ( true ) {
			JSONObject data = fetchAlbum(uk, aid, start, limit);
			if ( data == null || !data.containsKey("errno") || data.getInt("errno") != 0 ) {
				break;
			}
			JSONArray list = data.getJSONArray("records");
			final int total = data.getInt("total_count");
			for (int i = 0; i < list.size(); i ++) {
				JSONObject record = list.getJSONObject(i);
				dealWithRecordInAlbum(uk, aid, desc, record);
			}
			start += list.size(); 
			if ( list.size() == 0 || start >= total ) break;
		}
		logger.exit();
	}
	
	static public void crawlDir(long uk, long sid, String dir, String desc, 
			int downloads, int visits, int saves) {
		logger.entry(uk, sid, dir);
		int page = 1;
		while ( true ) {
			JSONObject data = fetchDir(uk, sid, dir, page);
			if ( data == null || !data.containsKey("errno") || data.getInt("errno") != 0 ) {
				break;
			}
			JSONArray list = data.getJSONArray("records");
			for (int i = 0; i < list.size(); i ++) {
				JSONObject record = list.getJSONObject(i);
				dealWithRecordInDir(uk, sid, dir, desc, downloads, visits, saves, record);
			}
			if ( list.size() == 0 ) break;
		}
		logger.exit();
	}
}
