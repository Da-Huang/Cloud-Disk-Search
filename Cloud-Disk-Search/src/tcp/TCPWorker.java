package tcp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.zip.GZIPOutputStream;

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
import exception.AppException;


public class TCPWorker implements Runnable {
	private static Logger logger = LogManager.getLogger(TCPWorker.class.getName());
	
	private Socket client = null;
	
	public TCPWorker(Socket client) {
		this.client = client;
	}
	
	@Override
	public void run() {
		logger.entry(client);
		IndexReader reader = null;
		BufferedReader br = null;
		BufferedWriter bw = null;
		try {
			reader = DirectoryReader.open(FSDirectory.open(
					new File(Variables.getInstance().getProperties().getProperty("indexPath"))));
			IndexSearcher searcher = new IndexSearcher(reader);
			
			br = new BufferedReader(
					new InputStreamReader(client.getInputStream(), "utf8"));
			bw = new BufferedWriter(
					new OutputStreamWriter(new GZIPOutputStream(client.getOutputStream()), "utf8"));
			String request = br.readLine();
			br.close();
			logger.info("request: " + request);
			if ( request != null ) {
				JSONObject jin = JSONObject.fromObject(request.trim());
				String type = jin.getString("type");
				if ( type == null ) throw new AppException("No Type.");
				type = type.trim();
				
				if ( type.equals("search") ) {
					String query = jin.getString("query");
					int start = jin.getInt("start");
					int limit  = jin.getInt("limit");
					String fileType = jin.getString("fileType");
					
					JSONObject jout = Searcher.getInstance().search(searcher, 
							QueryParser.getInstance().parseAsField(query, fileType, "name"), start, limit);
//					client.getOutputStream().write(Utils.compress(jout.toString().getBytes("utf8")));
//					client.getOutputStream().close();
					bw.write(jout.toString());
					
				} else if ( type.equals("hot") ) {
					int start = jin.getInt("start");
					int limit = jin.getInt("limit");
					String fileType = jin.getString("fileType");
					
					JSONObject jout = Searcher.getInstance().search(searcher, 
							QueryParser.getInstance().parseHot(fileType), start, limit);
					
//					client.getOutputStream().write(Utils.compress(jout.toString().getBytes("utf8")));
//					client.getOutputStream().close();
					bw.write(jout.toString());
					
				} else throw new AppException("Type Error.");
			}
			bw.close();
			reader.close();
			client.close();
		} catch (AppException e) {
			try {
				reader.close();
				client.close();
			} catch (IOException ioe) {
				logger.error(ioe);
			}
			logger.error(e);
		} catch (IOException e) {
			logger.error("Client Error.");
		}
		logger.exit();
	}
}
