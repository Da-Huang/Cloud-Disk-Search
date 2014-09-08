package crawl;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class IPList {
  private static final Logger logger = LogManager.getLogger(IPList.class);
  private static IPList instance = null;
  public static IPList getInstance() {
    if ( instance == null )
      instance = new IPList();
    return instance;
  }
  private IPList() {
    try {
      final BufferedReader br = new BufferedReader(
          new InputStreamReader(new FileInputStream("META-INF/ip.list")));
      String line;
      while ( (line = br.readLine()) != null ) {
        final int mid = line.indexOf(':');
        if ( mid < 0 ) continue;
        final String ip = line.substring(0, mid).trim();
        final int port = Integer.parseInt(line.substring(mid + 1));
        ipLists.add(new AbstractMap.SimpleEntry<String, Integer>(ip, port));
      }
      br.close();
    } catch (IOException e) {
      logger.error(e);
    }
    ipLists.add(null);
    availables = ipLists.size();
  }

  final private List<Entry<String, Integer>> ipLists = new ArrayList<>();
  private int availables;

  /**
   * Open a random ip descriptor, which can be used to
   * acquire ip by <code>get(int ipd)</code>. <br>
   * The descriptor need to be closed after used.
   * @return ip descriptor.
   */
  synchronized public int open() {
    while ( availables <= 0 ) {
      try {
        wait();
      } catch (InterruptedException e) {
        logger.error(e);
      }
    }

    final int index = (int) (Math.random() * availables);
    availables --;
    final Entry<String, Integer> tmp = ipLists.get(index);
    ipLists.set(index, ipLists.get(availables));
    ipLists.set(availables, tmp);

    logger.trace("ipd=availables=" + availables);
    return availables;
  }

  public Entry<String, Integer> get(int ipd) {
    return ipLists.get(ipd);
  }

  /**
   * Close the ip descriptor.
   * @param ipd ip descriptor
   */
  synchronized public void close(int ipd) {
    logger.trace("ipd=" + ipd + ",availables=" + availables);
    final Entry<String, Integer> tmp = ipLists.get(ipd);
    ipLists.set(ipd, ipLists.get(availables));
    ipLists.set(availables, tmp);
    availables ++;
    notify();
  }
}
