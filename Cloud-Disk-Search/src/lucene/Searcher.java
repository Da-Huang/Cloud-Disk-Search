package lucene;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import util.Variables;

public class Searcher {
	private static Logger logger = LogManager.getLogger(Searcher.class);
	private Searcher() {}
	private static Searcher INSTANCE = null;
	public static Searcher getInstance() {
		if ( INSTANCE == null ) INSTANCE = new Searcher();
		return INSTANCE;
	};
	
	public static void main(String[] args) throws IOException, ParseException {
		IndexReader reader = DirectoryReader.open(FSDirectory.open(
				new File(Variables.getInstance().getProperties().getProperty("indexPath"))));
		IndexSearcher searcher = new IndexSearcher(reader);

		int start = 10;
		int limit = 20;
		System.out.println(Searcher.getInstance().hot(searcher, null, start, limit));
		
		reader.close();
	}
	
	public JSONObject hot(IndexSearcher searcher, String fileType,
			int start, int limit) throws IOException {
		logger.entry(fileType, start, limit);
		TopDocs tops = searcher.search(QueryParser.getInstance().parseHot(fileType), start + limit,
				new Sort(new SortField("size", SortField.Type.LONG, true)));
		return makeup(searcher, tops, start, limit);
	}
	
	public JSONObject search(IndexSearcher searcher, Query query, 
				int start, int limit) throws IOException {
		logger.entry(query, start, limit);
		TopDocs tops = searcher.search(query, start + limit);
		return makeup(searcher, tops, start, limit);
	}
	
	private static JSONObject makeup(IndexSearcher searcher, TopDocs tops, 
			int start, int limit) throws IOException {
		JSONObject res = new JSONObject();
		ScoreDoc[] hits = tops.scoreDocs;
		final int totalHits = tops.totalHits;
		res.put("totalNum", totalHits);
		logger.info("totalNum=" + totalHits);
		JSONArray list = new JSONArray();
		for (int i = start; i < start + limit && i < hits.length; i ++) {
			JSONObject file = new JSONObject();
			Document doc = searcher.doc(hits[i].doc);
			file.put("name", new String(doc.get("name").getBytes("utf8"), "utf8"));
			file.put("url", doc.get("url"));
			file.put("size", doc.get("storedSize"));
			file.put("md5", "0123456789abcdef");
			file.put("download", (long) (Math.random() * 100));
			list.add(file);
		}
		res.put("filesList", list);
		logger.exit(res.toString().substring(0, Math.min(res.toString().length(), 100)) + "...");
		return res;
	}
}
