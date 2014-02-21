package tcp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import javax.sound.midi.MidiDevice.Info;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.sf.json.JSONObject;


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
			BufferedReader br = new BufferedReader(
					new InputStreamReader(client.getInputStream()));
			String query = br.readLine();
			logger.info("query: " + query);
			if ( query != null ) {
				JSONObject json = JSONObject.fromObject(query.trim());
				json.put('a', "t");
//				logger.info(json.toString());
				BufferedWriter bw = new BufferedWriter(
						new OutputStreamWriter(client.getOutputStream()));
				bw.write(json.toString());
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
