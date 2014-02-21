package database;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.apache.commons.dbcp.BasicDataSource;


/**
 * DataBase Connecting Pool
 * @author dhuang
 *
 */
public class DBConnection {
	static private BasicDataSource ds = null;
	private DBConnection() {}
	
	public static void main(String[] args) throws SQLException {
		Connection conn = DBConnection.getConnection();
		Statement stmt = conn.createStatement();
//		ResultSet rs = stmt.executeQuery("SELECT `name`, `url`, `md5` FROM `files`");
//		while ( rs.next() ) {
//			System.out.println(rs.getString("name"));
//			System.out.println(rs.getString("url"));
//			System.out.println(rs.getString("md5"));
//		}
		ResultSet rs = stmt.executeQuery(
				"SELECT `sql` FROM `sqlite_master` "
				+ "WHERE `name`='files' AND `type`='table'");
		while ( rs.next() ) {
			System.out.println(rs.getString("sql"));
		}
		rs.close();
		stmt.close();
		conn.close();
	}
	
	public static Connection getConnection() throws SQLException {
		if ( ds == null ) {
			ds = new BasicDataSource();
			try {
				Properties prop = new Properties();
				prop.load(DBConnection.class.getClassLoader().
						getResource("conf.properties").openStream());
				ds.setMaxIdle(Integer.parseInt(prop.getProperty("maxIdle")));
				ds.setMaxActive(Integer.parseInt(prop.getProperty("maxActive")));
				ds.setUrl(prop.getProperty("url"));
				ds.setDriverClassName(prop.getProperty("driverClassName"));
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return ds.getConnection();
	}
}
