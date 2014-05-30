package test;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import crawl.IPList;


public class TestIPList {
	private static final Logger logger = LogManager.getLogger(TestIPList.class);
	
	public static void main(String[] args) {
		
		
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				int ipd = IPList.getInstance().open();
				logger.debug(ipd + ":" + IPList.getInstance().get(ipd));
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				IPList.getInstance().close(ipd);
			}
		});
		thread.start();

		logger.trace("main");
		int ipd = IPList.getInstance().open();
		logger.debug(ipd + ":" + IPList.getInstance().get(ipd));
		int ipd2 = IPList.getInstance().open();
		logger.debug(ipd + ":" + IPList.getInstance().get(ipd2));
	}
}
