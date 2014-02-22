package lucene;

import java.io.IOException;
import java.io.StringReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;

import util.Variables;


public class QueryParser {
	private static Logger logger = LogManager.getLogger(QueryParser.class.getName());
	private static QueryParser INSTANCE = null;
	private QueryParser() {}
	public static QueryParser getInstance() {
		if ( INSTANCE == null ) INSTANCE = new QueryParser();
		return INSTANCE;
	}
	
	public Query parseAsField(String qText, String field) throws IOException {
		logger.entry(qText, field);
		Analyzer analyzer = new SmartChineseAnalyzer(Version.LUCENE_46);
		TokenStream stream = analyzer.tokenStream(null, new StringReader(qText));
		CharTermAttribute cattr = stream.addAttribute(CharTermAttribute.class);
		stream.reset();
		PhraseQuery query = new PhraseQuery();
		while ( stream.incrementToken() ) {
			query.add(new Term(field, cattr.toString()));
		}
		query.setSlop(Integer.parseInt(Variables.getInstance().getProperties().getProperty("phraseSlop")));
		stream.end();
		stream.close();
		analyzer.close();
		logger.exit(query);
		return query;
	}
}

