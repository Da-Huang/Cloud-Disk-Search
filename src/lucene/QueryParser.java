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
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.util.Version;

import database.FileType;
import util.Variables;


public class QueryParser {
  private static final Logger logger = LogManager.getLogger(QueryParser.class);
  private static QueryParser INSTANCE = null;
  private QueryParser() {}
  public static QueryParser getInstance() {
    if ( INSTANCE == null ) INSTANCE = new QueryParser();
    return INSTANCE;
  }

  public Query parseHot(String fileType) {
    logger.entry(fileType);
    Query query;
    if ( fileType != null ) query = new TermQuery(new Term("type", fileType));
    else query = new TermQuery(new Term("valid", "Y"));
    logger.exit(query);
    return query;
  }

  public Query parseAsField(String qText, String field) throws IOException {
    logger.entry(qText, field);
    final Analyzer analyzer = new SmartChineseAnalyzer(Version.LUCENE_46);
    final TokenStream stream = analyzer.tokenStream(null, new StringReader(qText));
    final CharTermAttribute cattr = stream.addAttribute(CharTermAttribute.class);
    stream.reset();
    final PhraseQuery query = new PhraseQuery();
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

  public Query parseAsField(String qText, String fileType, String field) throws IOException {
    logger.entry(qText, fileType, field);
    if ( ! FileType.containsType(fileType) ) fileType = null;

    final Query textQuery = parseAsField(qText, field);
    Query query = textQuery;
    if ( fileType != null ) {
      Query fileQuery = new TermQuery(new Term("type", fileType));
      BooleanQuery bQuery = new BooleanQuery();
      bQuery.add(textQuery, Occur.MUST);
      bQuery.add(fileQuery, Occur.MUST);
      query = bQuery;
    }
    logger.exit(query);
    return query;
  }
}

