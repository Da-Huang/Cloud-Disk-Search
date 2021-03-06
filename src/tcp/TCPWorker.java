package tcp;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
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

import util.Utils;
import util.Variables;
import exception.AppException;


public class TCPWorker implements Runnable {
  private static final Logger logger = LogManager.getLogger(TCPWorker.class);

  private Socket client = null;

  public TCPWorker(Socket client) {
    this.client = client;
  }

  @Override
  public void run() {
    logger.entry(client);
    IndexReader reader = null;
    BufferedReader br = null;
    try {
      reader = DirectoryReader.open(FSDirectory.open(
          new File(Variables.getInstance().getProperties().getProperty("indexPath"))));
      final IndexSearcher searcher = new IndexSearcher(reader);

      br = new BufferedReader(
          new InputStreamReader(client.getInputStream(), "utf8"));
      String request = br.readLine();

      logger.info("request: " + request);
      if ( request != null ) {
        final JSONObject jin = JSONObject.fromObject(request.trim());
        String type = jin.getString("type");
        if ( type == null ) throw new AppException("No Type.");
        type = type.trim();

        if ( type.equals("search") ) {
          final String query = jin.getString("query");
          final int start = jin.getInt("start");
          final int limit  = jin.getInt("limit");
          final String fileType = jin.getString("fileType");

          final JSONObject jout = Searcher.getInstance().search(searcher,
              QueryParser.getInstance().parseAsField(query, fileType, "name"), start, limit);
          client.getOutputStream().write(Utils.compress(jout.toString().getBytes("utf8")));

        } else if ( type.equals("hot") ) {
          final int start = jin.getInt("start");
          final int limit = jin.getInt("limit");
          String fileType = jin.getString("fileType");
          fileType = fileType.toLowerCase();
          if ( fileType.equals("all") ) fileType = null;

          final JSONObject jout = Searcher.getInstance().hot(searcher, fileType, start, limit);
          client.getOutputStream().write(Utils.compress(jout.toString().getBytes("utf8")));

        } else throw new AppException("Type Error.");
      }
      br.close();
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
      logger.error(e);
    }
    logger.exit();
  }
}
