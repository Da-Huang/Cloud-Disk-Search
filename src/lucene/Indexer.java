package lucene;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import util.Variables;
import database.DBConnection;
import database.FileType;


public class Indexer {
  private static final Logger logger = LogManager.getLogger(Indexer.class);
  private static Indexer INSTANCE = null;
  private Indexer() {};
  public static Indexer getInstance() {
    if ( INSTANCE == null ) INSTANCE = new Indexer();
    return INSTANCE;
  }

  public static void main(String[] args) throws SQLException, IOException {
    final boolean create = Boolean.parseBoolean(Variables.getInstance().getProperty("create"));

    final Date start = new Date();
    final Directory dir = FSDirectory.open(new File(
        Variables.getInstance().getProperties().getProperty("indexPath")));
    final Analyzer analyzer = new SmartChineseAnalyzer(Version.LUCENE_46);
    final IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_46, analyzer);

    if ( create ) {
      iwc.setOpenMode(OpenMode.CREATE);
    } else {
      iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
    }
    iwc.setRAMBufferSizeMB(256);

    final IndexWriter writer = new IndexWriter(dir, iwc);
    Indexer.getInstance().indexDB(writer);

    writer.close();

    final Date end = new Date();
    System.out.println(end.getTime() - start.getTime() + " total milliseconds.");
  }

  public void indexDB(IndexWriter writer) throws IOException, SQLException {
    logger.entry();
    int count = 0;
    final Connection conn = DBConnection.getConnection();
    final Statement stmt = conn.createStatement();
    final ResultSet rs = stmt.executeQuery(
        "SELECT `name`, `url`, `md5`, `size` FROM `files` "
//        + "LIMIT 0, 30000"
    );
    while ( rs.next() ) {
      final Document doc = new Document();
      final String fileName = rs.getString("name");

      final Field name = new TextField("name", fileName, Field.Store.YES);
      final Field url = new StringField("url", rs.getString("url"), Field.Store.YES);
      final Field size = new NumericDocValuesField("size", rs.getLong("size"));
      final Field storedSize = new StoredField("storedSize", rs.getLong("size"));
      doc.add(name);
      doc.add(url);
      doc.add(size);
      doc.add(storedSize);

      final String postfix = fileName.substring(fileName.lastIndexOf('.') + 1);
      final Field type = new StringField("type", FileType.getType(postfix), Field.Store.YES);
      doc.add(type);

      final Field valid = new StringField("valid", "Y", Field.Store.YES);
      doc.add(valid);

      final Field md5 = new StoredField("md5", DigestUtils.md5Hex(fileName + rs.getString("size")));
      doc.add(md5);

      final long downloadValue = (long) (Math.random() * 1000);
      final Field download = new NumericDocValuesField("download", downloadValue);
      final Field storedDownload = new StoredField("storedDownload", downloadValue);
      doc.add(download);
      doc.add(storedDownload);

      if ( writer.getConfig().getOpenMode() == OpenMode.CREATE ) {
        writer.addDocument(doc);
      } else {
        writer.updateDocument(new Term("url", rs.getString("url")), doc);
      }
      count ++;
      if ( count % 10000 == 0 ) logger.info("index " + count);
    }
    rs.close();
    stmt.close();
    conn.close();
    logger.exit();
  }
}
