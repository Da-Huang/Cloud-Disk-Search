package tcp;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import util.Variables;

public class TCPThreadServer implements Runnable {
	private static Logger logger = LogManager.getLogger(TCPThreadServer.class);
	
	private boolean stop = false;
	private static final int maxThreadsNum = Integer.parseInt(
			Variables.getInstance().getProperty("threadNum"));
	private ExecutorService threadPool = Executors.newFixedThreadPool(maxThreadsNum);
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
		Thread thread = new Thread(new TCPThreadServer());
		thread.start();
		thread.join();
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
