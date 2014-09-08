package ui;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import lucene.Indexer;
import lucene.QueryParser;
import lucene.Searcher;
import net.sf.json.JSONObject;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.FSDirectory;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import crawl.yun.UserCrawler;
import crawl.yun.YunFileCrawler;
import crawl.yun.util.UserSet;
import tcp.TCPThreadServer;
import util.Variables;

class Args4J {
  @Option(name = "-index", usage = "Index Data.")
  boolean doIndex;

  @Option(name = "-search", usage = "Do Default Search. ex. -search \"物语 花\"")
  String searchQuery = null;

  @Option(name = "-serve", usage = "Run The Searching Server.")
  boolean doServe;

  @Option(name = "-crawl-users", usage = "Crawl users.")
  boolean crawlUsers;

  @Option(name = "-crawl-files", usage = "Crawl files.")
  boolean crawlFiles;

  @Option(name = "-stat", usage = "Statistics on db.")
  boolean stat;
}

public class Main {
  public static void main(String[] args) throws CmdLineException, IOException, InterruptedException, SQLException {
    final Args4J args4j = new Args4J();
    final CmdLineParser parser = new CmdLineParser(args4j);
    parser.parseArgument(args);
    if ( args.length == 0 ) {
      parser.printUsage(System.out);
      return;
    }

    if ( args4j.doIndex ) {
      Indexer.main(null);

    } else if ( args4j.searchQuery != null ) {
      final IndexReader reader = DirectoryReader.open(FSDirectory.open(
          new File(Variables.getInstance().getProperties().getProperty("indexPath"))));
      final IndexSearcher searcher = new IndexSearcher(reader);
      final JSONObject result = Searcher.getInstance().search(searcher,
          QueryParser.getInstance().parseAsField(args4j.searchQuery, "name"), 0, 100);
      System.out.println(result);
      reader.close();

    } else if ( args4j.doServe ) {
      final Thread thread = new Thread(new TCPThreadServer());
      thread.start();
      thread.join();

    } else if ( args4j.crawlUsers ) {
      UserCrawler.crawl();

    } else if ( args4j.stat ) {
      System.out.println(UserSet.getInstance().uncrawledSize());
      System.out.println(UserSet.getInstance().size());

    } else if ( args4j.crawlFiles ) {
      YunFileCrawler.crawl();

    } else {
      parser.printUsage(System.out);
    }
  }
}
