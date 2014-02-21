package util;

import java.io.IOException;
import java.util.Properties;

import database.DBConnection;


public class Variables {
	private Variables() {
		try {
			properties.load(DBConnection.class.getClassLoader().
					getResource("conf.properties").openStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private static Variables INSTANCE = null;
	public static Variables getInstance() {
		if ( INSTANCE == null ) INSTANCE = new Variables();
		return INSTANCE;
	}
	
	private Properties properties = new Properties();
	
	public Properties getProperties() {
		return properties;
	}
}
