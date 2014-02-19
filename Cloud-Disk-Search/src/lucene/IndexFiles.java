package lucene;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;


public class IndexFiles {
	public static void main(String[] args) {
		String indexPath = "path";
		String docsPath = "D:/input";
		boolean create = true;
		
		final File docDir = new File(docsPath);
		Date start = new Date();
		try {
			Directory dir = FSDirectory.open(new File(indexPath));
			Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_46);
			IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_46, analyzer);
			
			if ( create ) {
				iwc.setOpenMode(OpenMode.CREATE);
			} else {
				iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
			}
			iwc.setRAMBufferSizeMB(256);
			
			IndexWriter writer = new IndexWriter(dir, iwc);
			indexDocs(writer, docDir);
			
			writer.close();
			
			Date end = new Date();
			System.out.println(end.getTime() - start.getTime() + " total milliseconds.");
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	static void indexDocs(IndexWriter writer, File file) {
		if ( file.canRead() ) {
			if ( file.isDirectory() ) {
				String[] files = file.list();
				if ( files != null ) {
					for (int i = 0; i < files.length; i ++) {
						indexDocs(writer, new File(file, files[i]));
					}
				}
			} else {
				try {
					FileInputStream fis = new FileInputStream(file);
					Document doc = new Document();
					Field pathField = new StringField("path", file.getName(), Field.Store.YES);
					doc.add(pathField);
					doc.add(new LongField("modified", file.lastModified(), Field.Store.YES));
					doc.add(new TextField("contents", new BufferedReader(new InputStreamReader(fis, "utf8"))));
					
					if ( writer.getConfig().getOpenMode() == OpenMode.CREATE ) {
						System.out.println("adding: " + file.getName());
						writer.addDocument(doc);
					} else {
						System.out.println("updating: " + file.getName());
						writer.updateDocument(new Term("path", file.getCanonicalPath()), doc);
					}
					
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
			}
		}
	}
}
