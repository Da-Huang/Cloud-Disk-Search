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


public class IPLists {
	private static final Logger logger = LogManager.getLogger(IPLists.class);
	private static IPLists instance = null;
	public static IPLists getInstance() {
		if ( instance == null )
			instance = new IPLists();
		return instance;
	}
	private IPLists() {
		try {
			BufferedReader br = new BufferedReader(
					new InputStreamReader(new FileInputStream("META-INF/ip.list")));
			String line;
			while ( (line = br.readLine()) != null ) {
				int mid = line.indexOf(':');
				if ( mid < 0 ) continue;
				String ip = line.substring(0, mid).trim();
				int port = Integer.parseInt(line.substring(mid + 1));
				ipLists.add(new AbstractMap.SimpleEntry<String, Integer>(ip, port));
			}
			br.close();
		} catch (IOException e) {
			logger.error(e);
		}
		ipLists.add(null);
		availables = ipLists.size();
	}
	
	private List<Entry<String, Integer>> ipLists = new ArrayList<>();
	private int availables = 0;
	
	synchronized public int getRandomIpd() {
		while ( availables <= 0 ) {
			try {
				wait();
			} catch (InterruptedException e) {
				logger.error(e);
			}
		}
		
		int index = (int) (Math.random() * availables);
		availables --;
		Entry<String, Integer> tmp = ipLists.get(index);
		ipLists.set(index, ipLists.get(availables));
		ipLists.set(availables, tmp);
		
		return availables;
	}
	
	public Entry<String, Integer> get(int ipd) {
		return ipLists.get(ipd);
	}
	
	synchronized public void release(int ipd) {
		Entry<String, Integer> tmp = ipLists.get(ipd);
		ipLists.set(ipd, ipLists.get(availables));
		ipLists.set(availables, tmp);
		availables ++;
		notify();
	}
}
