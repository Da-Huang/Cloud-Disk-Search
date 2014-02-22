package tcp;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import util.Variables;

public class TCPThreadServer implements Runnable {
	private static Logger logger = LogManager.getLogger(TCPThreadServer.class.getName());
	private boolean stop = false;
	private ExecutorService threadPool = Executors.newFixedThreadPool(
			Integer.parseInt(Variables.getInstance().getProperties().getProperty("threadNum")));
	private ServerSocket server = null;
	
	public TCPThreadServer() {
		this(Integer.parseInt(Variables.getInstance().getProperties().getProperty("port")));
	}
	public TCPThreadServer(int port) {
		try {
			server = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws InterruptedException {
		new Thread(new TCPThreadServer()).run();
		while ( true ) Thread.sleep(Long.MAX_VALUE);
	}
	

	@Override
	public void run() {
		while ( !stop ) {
			try {
				threadPool.execute(new TCPWorker(server.accept()));
			} catch (IOException e) {
				logger.error("Server Stoped.");
			}
		}
	}
	
	public void stop() {
		stop = true;
		try {
			server.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
