package util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Record Variables
 * @author dhuang
 */
public class Variables {
  private static final Logger logger = LogManager.getLogger(Variables.class);
  private static Variables instance = null;
  public static Variables getInstance() {
    if ( instance == null ) {
      synchronized (Variables.class) {
        if ( instance == null )
          instance = new Variables();
      }
    }
    return instance;
  }
  private Variables() {
    try {
      properties.load(new FileInputStream("META-INF/conf.properties"));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    Variables.getInstance();
    logger.debug(Variables.getInstance().getProperty("proxylist"));
    logger.error("test");
  }

  private final Properties properties = new Properties();

  public Properties getProperties() {
    return properties;
  }

  public String getProperty(String key) {
    return properties.getProperty(key);
  }
}
