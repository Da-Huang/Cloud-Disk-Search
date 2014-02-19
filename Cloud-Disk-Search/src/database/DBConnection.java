package database;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.commons.dbcp.BasicDataSource;

public class DBConnection {
	static private BasicDataSource ds = null;
	private DBConnection() {}
	
	public static Connection getConnection() throws SQLException {
		if ( ds == null ) {
			ds = new BasicDataSource();
			try {
				Properties prop = new Properties();
				prop.load(DBConnection.class.getClassLoader().
						getResource("conf.properties").openStream());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return ds.getConnection();
	}
}
