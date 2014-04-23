package crawl;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;


public class IPLists {
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
				String ip = line.substring(0, mid);
				int port = Integer.parseInt(line.substring(mid + 1));
				ipLists.add(new AbstractMap.SimpleEntry<String, Integer>(ip, port));
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private List<Entry<String, Integer>> ipLists = new ArrayList<>();
	
	public Entry<String, Integer> getRandom() {
		int index = (int) (Math.random() * (ipLists.size() + 1));
		if ( index > ipLists.size() ) return null;
		return ipLists.get(index);
	}
}
