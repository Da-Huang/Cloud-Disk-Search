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
	
	public static void main(String[] args) {
//		YunFileCrawler.crawl();
		YunFileCrawler.crawl(3158078488L);
	}
	
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
	static private JSONObject fetchDir(long uk, long sid, String title, int page) {
		logger.entry(uk, sid, title, page);
		final String urlBase = "http://yun.baidu.com/share/list";
		String dir = fetchParentPath(uk, sid);
		if ( dir == null ) return null;
		dir += "/";
		dir += title;
		final Map<String, String> args = new HashMap<String, String>();
		args.put("uk", String.valueOf(uk));
		args.put("shareid", String.valueOf(sid));
		args.put("dir", dir);
		args.put("page", String.valueOf(page));
		return Request.requestForceYun(urlBase, args);
	}
	
	static private String fetchParentPath(long uk, long sid) {
		final String urlBase = "http://pan.baidu.com/share/link";
		final Map<String, String> args = new HashMap<String, String>();
		args.put("uk", String.valueOf(uk));
		args.put("shareid", String.valueOf(sid));
		final String page = Request.requestPlainForce(urlBase, args);
		int begin = page.indexOf("parent_path\\\":\\\"");
		if ( begin < 0 ) return null;
		begin += "parent_path\\\":\\\"".length();
		final int end = page.indexOf("\\\"", begin);
		String parentPath = page.substring(begin, end);
		try {
			parentPath = URLDecoder.decode(parentPath, "utf8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return parentPath;
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
		final long uk = Long.parseLong(record.getString("uk"));
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
				final String name = file.getString("server_filename");
				final String desc = record.getString("desc");
				final int downloads = Integer.parseInt(record.getString("dCnt"));
				final int visits = Integer.parseInt(record.getString("vCnt"));
				final int saves = Integer.parseInt(record.getString("tCnt"));
				crawlDir(uk, sid, name, desc, downloads, visits, saves);
				
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
	
	static private boolean dealWithRecordInDir(long uk, long sid, String desc, 
			int downloads, int visits, int saves, JSONObject record) {
		final boolean isDir = Integer.parseInt(record.getString("isdir")) != 0;
		if ( isDir ) {
			final String name = record.getString("server_filename"); 
			crawlDir(uk, sid, name, desc, downloads, visits, saves);
			
		} else {
			saveFilesInDir(uk, sid, desc, downloads, visits, saves, record);
		}
		return true;
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
	
	static public void crawlDir(long uk, long sid, String name, String desc, 
			int downloads, int visits, int saves) {
		logger.entry(uk, sid, name);
		int page = 1;
		while ( true ) {
			final JSONObject data = fetchDir(uk, sid, name, page);
			if ( data == null ) break;
			final int errno = Integer.parseInt(data.optString("errno", "-1"));
			if ( errno != 0 ) break;
			final JSONArray list = data.getJSONArray("list");
			for (int i = 0; i < list.size(); i ++) {
				final JSONObject record = list.getJSONObject(i);
				dealWithRecordInDir(uk, sid, desc, downloads, visits, saves, record);
			}
			page ++;
			if ( list.size() == 0 ) break;
		}
		logger.exit();
	}
	
	static public void crawl() {
		logger.entry();
		YunFileThreadCrawler yunFileThreadCrawler = new YunFileThreadCrawler();
		final Thread thread = new Thread(yunFileThreadCrawler);
		thread.start();
		try {
			thread.join();
		} catch (InterruptedException e) {
			logger.error(e);
		}
		yunFileThreadCrawler.shutdown();
		logger.exit();
	}
}
