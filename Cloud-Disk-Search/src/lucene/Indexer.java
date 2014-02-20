package lucene;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import database.DBConnection;


public class Indexer {
	private static Indexer INSTANCE = null;
	private Indexer() {};
	public static Indexer getInstance() {
		if ( INSTANCE == null ) INSTANCE = new Indexer();
		return INSTANCE;
	}
	
	public static void main(String[] args) throws SQLException, IOException {
		String indexPath = "D:/index";
		boolean create = false;
		
		Date start = new Date();
		Directory dir = FSDirectory.open(new File(indexPath));
		Analyzer analyzer = new SmartChineseAnalyzer(Version.LUCENE_46);
		IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_46, analyzer);
		
		if ( create ) {
			iwc.setOpenMode(OpenMode.CREATE);
		} else {
			iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
		}
		iwc.setRAMBufferSizeMB(256);
		
		IndexWriter writer = new IndexWriter(dir, iwc);
		Indexer.getInstance().indexDB(writer);
		
		writer.close();
		
		Date end = new Date();
		System.out.println(end.getTime() - start.getTime() + " total milliseconds.");
	}
	
	public void indexDB(IndexWriter writer) throws IOException, SQLException {
		Connection conn = DBConnection.getConnection();
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(
				"SELECT `name`, `url`, `md5` FROM `files` "
				+ "LIMIT 0, 10001"
		);
		while ( rs.next() ) {
			Document doc = new Document();
			Field name = new TextField("name", rs.getString("name"), Field.Store.YES);
			Field url = new StringField("url", rs.getString("url"), Field.Store.YES);
//			Field md5 = new StringField("md5", rs.getString("md5"), Field.Store.YES);
			doc.add(name);
			doc.add(url);
//			doc.add(md5);
			writer.addDocument(doc);
		}
		rs.close();
		stmt.close();
		conn.close();
	}
}
