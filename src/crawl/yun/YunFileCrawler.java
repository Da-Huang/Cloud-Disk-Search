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
		final long uk = Integer.parseInt(record.getString("uk"));
		final long sid = Long.parseLong(record.getString("shareid"));
		final String title = record.getString("title");
		final long time = Long.parseLong(record.getString("feed_time"));
		final String desc = record.getString("desc");
		final JSONObject file = record.getJSONArray("filelist").getJSONObject(0);
		final String md5 = file.getString("md5");
		final String url = String.format("%s?uk=%d&shareid=%d", urlBase, uk, sid);
		final long size = Long.parseLong(file.getString("size"));
		final int downloads = Integer.parseInt(record.getString("dCnt"));
		final int visits = Integer.parseInt(record.getString("vCnt"));
		final int saves = Integer.parseInt(record.getString("tCnt"));
		FileSet.getInstance().add(uk, md5, title, url, size, desc, time, downloads, visits, saves);
	}
	
	static private void saveFilesInDir(long uk, long sid, String desc, 
			int downloads, int visits, int saves, JSONObject file) {
		final String urlBase = "http://yun.baidu.com/share/link";
		final long fsid = Long.parseLong(file.getString("fs_id"));
		final String url = String.format("%s?uk=%d&shareid=%d&fid=%d", urlBase, uk, sid, fsid);
		final String md5 = file.getString("md5");
		final long time = Long.parseLong(file.getString("server_mtime"));
		final String title = file.getString("server_filename");
		final long size = Long.parseLong(file.getString("size"));
		FileSet.getInstance().add(uk, md5, title, url, size, desc, time, downloads, visits, saves);
	}
	
	static private void saveFilesInAlbum(long uk, long aid, String desc, JSONObject file) {
		final String urlBase = "http://yun.baidu.com/pcloud/album/file";
		if ( !file.containsKey("size") ) return;
		final long size = Long.parseLong(file.getString("size"));
		final String md5 = file.getString("md5");
		final long fsid = Long.parseLong(file.getString("fs_id"));
		final String title = file.getString("server_filename");
		final String url = String.format("%s?uk=%d&album_id=%d&fsid=%d", urlBase, uk, aid, fsid);
		final long time = Long.parseLong(file.getString("add_time"));
		final int downloads = Integer.parseInt(file.getString("dCnt"));
		final int visits = Integer.parseInt(file.getString("vCnt"));
		final int saves = Integer.parseInt(file.getString("tCnt"));
		FileSet.getInstance().add(uk, md5, title, url, size, desc, time, downloads, visits, saves);
	}
	
	/**
	 * Deal user's one record.
	 * @param uk user id
	 * @param record record
	 * @return Whether this record is valid.
	 */
	static private boolean dealWithRecordInTopLevel(long uk, JSONObject record) {
		final String feedType = record.getString("feed_type");
		if ( feedType.equals("album") ) {
			final long aid = Long.parseLong(record.getString("album_id"));
			final String desc = record.getString("desc");
			crawlAlbum(uk, aid, desc);
			
		} else if ( feedType.equals("share") ) {
			// May be file or directory
			final long sid = Long.parseLong(record.getString("shareid"));
			final JSONArray filelist = record.optJSONArray("filelist");
			if ( filelist != null && filelist.size() < 1 ) return false;
			final JSONObject file = filelist.getJSONObject(0);
			final boolean isDir = Integer.parseInt(file.getString("isdir")) != 0;

			if ( isDir ) {
				// directory
				final String path = decodePath(file.getString("path"));
				final String desc = record.getString("desc");
				final int downloads = Integer.parseInt(record.getString("dCnt"));
				final int visits = Integer.parseInt(record.getString("vCnt"));
				final int saves = Integer.parseInt(record.getString("tCnt"));
				crawlDir(uk, sid, path, desc, downloads, visits, saves);
				
			} else {
				// file
				saveFilesInTopLevel(record);
			}
			
		} else return false;
		return true;
	}
	
	static private boolean dealWithRecordInAlbum(long uk, long aid, String desc, JSONObject record) {
		final int fileStatus = Integer.parseInt(record.getString("file_status"));
		if ( fileStatus == 0 ) { // legal file
			saveFilesInAlbum(uk, aid, desc, record);
			return true;
			
		} else return false;
	}
	
	static private boolean dealWithRecordInDir(long uk, long sid, String dir, String desc, 
			int downloads, int visits, int saves, JSONObject record) {
		final boolean isDir = Integer.parseInt(record.getString("isdir")) != 0;
		if ( isDir ) {
			final String path = decodePath(record.getString("path"));
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
	
	static public void crawl(long uk) {
		crawlTopLevel(uk);
	}
	
	/**
	 * @param uk user id
	 */
	static public void crawlTopLevel(long uk) {
		logger.entry(uk);
		int start = 0;
		final int limit = 100;
		while ( true ) {
			final JSONObject data = fetchTopLevel(uk, start, limit);
			final int errno = Integer.parseInt(data.optString("errno", "-1"));
			if ( errno != 0 ) break;
			final JSONArray list = data.getJSONArray("records");
			final int total = Integer.parseInt(data.getString("total_count"));
			for (int i = 0; i < list.size(); i ++) {
				final JSONObject record = list.getJSONObject(i);
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
			final JSONObject data = fetchAlbum(uk, aid, start, limit);
			final int errno = Integer.parseInt(data.optString("errno", "-1"));
			if ( errno != 0 ) break;
			final JSONArray list = data.getJSONArray("list");
			final int total = Integer.parseInt(data.getString("count"));
			for (int i = 0; i < list.size(); i ++) {
				final JSONObject record = list.getJSONObject(i);
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
			final JSONObject data = fetchDir(uk, sid, dir, page);
			final int errno = Integer.parseInt(data.optString("errno", "-1"));
			if ( errno != 0 ) break;
			final JSONArray list = data.getJSONArray("records");
			for (int i = 0; i < list.size(); i ++) {
				final JSONObject record = list.getJSONObject(i);
				dealWithRecordInDir(uk, sid, dir, desc, downloads, visits, saves, record);
			}
			if ( list.size() == 0 ) break;
		}
		logger.exit();
	}
}
