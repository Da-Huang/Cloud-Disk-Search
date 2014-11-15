package test;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import crawl.ProxyList;


public class TestProxyList {
  private static final Logger logger = LogManager.getLogger(TestProxyList.class);

  public static void main(String[] args) {

    Thread thread = new Thread(new Runnable() {

      @Override
      public void run() {
        int proxyd = ProxyList.getInstance().open();
        logger.debug(proxyd + ":" + ProxyList.getInstance().get(proxyd));
        try {
          Thread.sleep(10000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        ProxyList.getInstance().close(proxyd);
      }
    });
    thread.start();

    logger.trace("main");
    int ipd = ProxyList.getInstance().open();
    logger.debug(ipd + ":" + ProxyList.getInstance().get(ipd));
    int ipd2 = ProxyList.getInstance().open();
    logger.debug(ipd + ":" + ProxyList.getInstance().get(ipd2));
  }
}
