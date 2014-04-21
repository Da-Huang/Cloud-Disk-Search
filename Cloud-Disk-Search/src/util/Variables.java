package util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.XMLConfigurationFactory;

/**
 * Record Variables
 * @author dhuang
 *
 */
public class Variables {
	private static Logger logger = LogManager.getLogger(Variables.class);
	private Variables() {
		try {
			properties.load(new FileInputStream("META-INF/conf.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private static Variables INSTANCE = null;
	public static Variables getInstance() {
		if ( INSTANCE == null ) INSTANCE = new Variables();
		return INSTANCE;
	}
	
	public static void main(String[] args) {
		Variables.getInstance();
		logger.error("test");
	}
	
	private Properties properties = new Properties();
	
	public Properties getProperties() {
		return properties;
	}
	
	public String getProperty(String key) {
		return properties.getProperty(key);
	}
}
