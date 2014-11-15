package crawl;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Queue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import util.Variables;


public class ProxyList {
  private static final Logger logger = LogManager.getLogger(ProxyList.class);
  private static ProxyList instance = null;
  public static ProxyList getInstance() {
    if ( instance == null ) {
      synchronized (ProxyList.class) {
        if ( instance == null )
          instance = new ProxyList();
      }
    }
    return instance;
  }
  private ProxyList() {
    try {
      final BufferedReader br = new BufferedReader(
          new InputStreamReader(new FileInputStream(Variables.getInstance().getProperty("proxylist"))));
      String line;
      while ( (line = br.readLine()) != null ) {
        final int mid = line.indexOf(':');
        if ( mid < 0 ) continue;
        final String ip = line.substring(0, mid).trim();
        final int port = Integer.parseInt(line.substring(mid + 1));
        proxyLists.add(new AbstractMap.SimpleEntry<String, Integer>(ip, port));
      }
      br.close();
    } catch (IOException e) {
      logger.error(e);
    }
    proxyLists.add(null);
    availables = proxyLists.size();

    mapping = new int [proxyLists.size()];
    reverseMapping = new int [proxyLists.size()];
    for (int i = 0; i < availables; i ++) queue.add(i);
  }

  final private List<Entry<String, Integer>> proxyLists = new ArrayList<>();
  final private int [] mapping;
  final private int [] reverseMapping;
  final private Queue<Integer> queue = new LinkedList<>();
  private int availables;

  /**
   * Open a random proxy descriptor, which can be used to
   * acquire proxy by <code>get(int proxyd)</code>. <br>
   * The descriptor need to be closed after used.
   * @return proxy descriptor.
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
    final Entry<String, Integer> tmp = proxyLists.get(index);
    proxyLists.set(index, proxyLists.get(availables));
    proxyLists.set(availables, tmp);
    
    final int proxyd = queue.poll();
    mapping[proxyd] = availables;
    reverseMapping[availables] = proxyd;

//    logger.fatal("open:" + new ArrayList<>(queue));
    logger.trace("proxyd=" + proxyd + ",availables=" + availables);
    return proxyd;
  }

  synchronized public Entry<String, Integer> get(int proxyd) {
    return proxyLists.get(mapping[proxyd]);
  }

  /**
   * Close the proxy descriptor.
   * @param proxyd proxy descriptor
   */
  synchronized public void close(int proxyd) {
    final int index = mapping[proxyd];
    logger.trace("proxyd=" + proxyd + ",index=" + index + ",availables=" + availables);
    final Entry<String, Integer> tmp = proxyLists.get(index);
    proxyLists.set(index, proxyLists.get(availables));
    proxyLists.set(availables, tmp);
    mapping[reverseMapping[availables]] = index;
    reverseMapping[index] = reverseMapping[availables];

    queue.offer(proxyd);
//    logger.fatal("close:" + new ArrayList<>(queue));
    availables ++;
    notify();
  }
}
