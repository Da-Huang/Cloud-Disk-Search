package util;

import java.io.IOException;
import java.util.Properties;

import database.DBConnection;


public class Arguments {
	private Arguments() {
		try {
			properties.load(DBConnection.class.getClassLoader().
					getResource("conf.properties").openStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private static Arguments INSTANCE = null;
	public static Arguments getInstance() {
		if ( INSTANCE == null ) INSTANCE = new Arguments();
		return INSTANCE;
	}
	
	private Properties properties = new Properties();
	
	public Properties getProperties() {
		return properties;
	}
}
