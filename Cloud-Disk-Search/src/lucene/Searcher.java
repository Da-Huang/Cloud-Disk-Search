package lucene;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

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
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import util.Variables;

public class Searcher {
	private static Logger logger = LogManager.getLogger(Searcher.class.getName());
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
		
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in, "utf8"));
		String line = in.readLine();
		if ( line != null ) {
			JSONObject result = Searcher.getInstance().search(searcher, 
					QueryParser.getInstance().parseAsField(line.trim(), "name"), 0, 100);
			System.out.println(result);
		}
		reader.close();
	}
	
	public JSONObject search(IndexSearcher searcher, Query query,
				int start, int size) throws IOException {
		logger.entry(query, start, size);
		JSONObject res = new JSONObject();
		TopDocs results = searcher.search(query, start + size);
		final int totalNum = results.totalHits;
		res.put("totalNum", totalNum);
		ScoreDoc[] hits = results.scoreDocs;
		
		JSONArray list = new JSONArray();
		for (int i = start; i < start + size && i < hits.length; i ++) {
			JSONObject file = new JSONObject();
			Document doc = searcher.doc(hits[i].doc);
			file.put("name", doc.get("name"));
			file.put("url", doc.get("url"));
			file.put("size", doc.get("size"));
			list.add(file);
		}
		res.put("filesList", list);
		
		logger.exit("info-len: " + res.toString().length());
		return res;
	}
}
