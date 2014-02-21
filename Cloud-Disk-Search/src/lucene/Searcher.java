package lucene;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class Searcher {
	private Searcher() {}
	private static Searcher INSTANCE = null;
	public static Searcher getInstance() {
		if ( INSTANCE == null ) INSTANCE = new Searcher();
		return INSTANCE;
	}
	
	public static void main(String[] args) throws IOException, ParseException {
		String index = "D:/index";
		String field = "name";
		int repeat = 0;
		boolean raw = false;
		String queryString = null;
		int hitsPerPage = 10;
		
		IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(index)));
		IndexSearcher searcher = new IndexSearcher(reader);
		Analyzer analyzer = new SmartChineseAnalyzer(Version.LUCENE_46);
		
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in, "utf8"));
		QueryParser parser = new QueryParser(Version.LUCENE_46, field, analyzer);
		parser.setDefaultOperator(QueryParser.Operator.AND);
		while ( true ) {
			if ( queryString == null )
				System.out.println("Enter query: ");
			String line = in.readLine();
			if ( line == null || line.trim().length() == 0 ) break;
			
			TokenStream stream = analyzer.tokenStream(null, new StringReader(line));
			CharTermAttribute cattr = stream.addAttribute(CharTermAttribute.class);
			stream.reset();
			PhraseQuery query = new PhraseQuery();
			while ( stream.incrementToken() ) {
				query.add(new Term(field, cattr.toString()));
				System.out.println(cattr.toString());
			}
			query.setSlop(10);
			stream.end();
			stream.close();
			
//			Query query = parser.parse(line);
			System.out.println(query);
			System.out.println("Searching for: " + query.toString(field));
			
			
			if ( repeat > 0 ) {
				Date start = new Date();
				for (int i = 0; i < repeat; i ++)
					searcher.search(query, null, 100);
				Date end = new Date();
				System.out.println("Time: " + (end.getTime() - start.getTime()) + "ms");
			}
			
			Searcher.getInstance().doPagingSearch(in, searcher, query, hitsPerPage, raw, true);
			
			if ( queryString != null ) break;
		}
		reader.close();
	}
	
	public void doPagingSearch(BufferedReader in, IndexSearcher searcher,
			Query query, int hitsPerPage, boolean raw, boolean interative) throws IOException {

		TopDocs results = searcher.search(query, 5 * hitsPerPage);
		ScoreDoc[] hits = results.scoreDocs;
		
		int numTotalHits = results.totalHits;
		System.out.println(numTotalHits + " total matching documents.");
		
		int start = 0;
		int end = Math.min(numTotalHits, hitsPerPage);
	
		while ( true ) {
			if ( end > hits.length ) {
				System.out.println("Only results 1 - " + hits.length + numTotalHits + " total matching documents collected.");
				System.out.println("Collect more (y/n) ?");
				String line = in.readLine().trim();
				if ( line.length() == 0 || line.charAt(0) == 'n' ) break;
				hits = searcher.search(query, numTotalHits).scoreDocs;
			}
			
			end = Math.min(hits.length, start + hitsPerPage);
			
			for (int i = start; i < end; i ++) {
				if ( raw ) {
					System.out.println("doc=" + hits[i].doc + "score=" + hits[i].score);
					continue;
				}
				
				Document doc = searcher.doc(hits[i].doc);
				System.out.println((i + 1) + ". " + doc.get("name"));
				System.out.println("\turl: " + doc.get("url"));
			}
		
			if ( !interative || end == 0 ) break;
			
			if ( numTotalHits >= end ) {
				boolean quit = false;
				while ( true ) {
					System.out.println("Press ");
					if ( start - hitsPerPage >= 0 )
						System.out.println("(p)revious page, ");
					if ( start + hitsPerPage < numTotalHits )
						System.out.println("(n)ext page, ");
					System.out.println("(q)uit or enter number to jump to a page.");
					
					String line = in.readLine().trim();
					if ( line.length() == 0 || line.charAt(0) == 'q' ) {
						quit = true;
						break;
					}
					
					if ( line.charAt(0) == 'p' ) {
						start = Math.max(0, start - hitsPerPage);
						break;
					} else if ( line.charAt(0) == 'n' ) {
						if ( start + hitsPerPage < numTotalHits )
							start += hitsPerPage;
						break;
					} else {
						int page = Integer.parseInt(line);
						if ( (page - 1) * hitsPerPage < numTotalHits ) {
							start = (page - 1) * hitsPerPage;
							break;
						} else {
							System.out.println("No such page.");
						}
					}
				}
				if ( quit ) break;
				end = Math.min(numTotalHits, start + hitsPerPage);
			}
		}

	}
}
