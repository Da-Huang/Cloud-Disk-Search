package tcp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import lucene.QueryParser;
import lucene.Searcher;
import net.sf.json.JSONObject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.FSDirectory;

import util.Variables;


public class TCPWorker implements Runnable {
	private static Logger logger = LogManager.getLogger(TCPWorker.class.getName());
	
	private Socket client = null;
	
	public TCPWorker(Socket client) {
		this.client = client;
	}
	
	@Override
	public void run() {
		logger.entry(client);
		try {
			IndexReader reader = DirectoryReader.open(FSDirectory.open(
					new File(Variables.getInstance().getProperties().getProperty("indexPath"))));
			IndexSearcher searcher = new IndexSearcher(reader);
			
			BufferedReader br = new BufferedReader(
					new InputStreamReader(client.getInputStream()));
			String request = br.readLine();
			logger.info("request: " + request);
			if ( request != null ) {
				JSONObject jin = JSONObject.fromObject(request.trim());
				String query = jin.getString("query").trim();
				int start = jin.getInt("start");
				int size  = jin.getInt("size");
				
				JSONObject jout = Searcher.getInstance().search(searcher, 
						QueryParser.getInstance().parseAsField(query, "name"), start, size);
				BufferedWriter bw = new BufferedWriter(
						new OutputStreamWriter(client.getOutputStream()));
				bw.write(jout.toString());
				bw.close();
			}
			br.close();
			client.close();
		} catch (IOException e) {
			logger.error("Client Error.");
		}
		logger.exit();
	}
}
