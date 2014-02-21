package tcp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

import net.sf.json.JSONObject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TCPThreadServer extends Thread {
	private static Logger logger = LogManager.getLogger(TCPThreadServer.class.getName());
	public static void main(String[] args) throws IOException {

		ServerSocket server = new ServerSocket(7779);
		while ( true ) {
			Socket client = server.accept();
//			client.setSoTimeout(6000);
			
			BufferedReader br = new BufferedReader(
					new InputStreamReader(client.getInputStream()));
			String jsonString = br.readLine();
			logger.info("jsonString: " + jsonString);
			if ( jsonString != null ) {
				JSONObject json = JSONObject.fromObject(jsonString.trim());
				json.put('a', "t");
				logger.info(json.toString());
				BufferedWriter bw = new BufferedWriter(
						new OutputStreamWriter(client.getOutputStream()));
				bw.write(json.toString());
				bw.close();
			}
			br.close();
			client.close();
		}
//		server.close();
	}
	
}
