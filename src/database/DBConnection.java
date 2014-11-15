package database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.apache.commons.dbcp.BasicDataSource;

import util.Variables;


/**
 * DataBase Connecting Pool
 * @author dhuang
 *
 */
public class DBConnection {
  static private BasicDataSource ds = null;
  private DBConnection() {}

  public static void main(String[] args) throws SQLException {
    final Connection conn = DBConnection.getConnection();
    final Statement stmt = conn.createStatement();
//    ResultSet rs = stmt.executeQuery("SELECT `name`, `url`, `md5` FROM `files`");
//    while ( rs.next() ) {
//      System.out.println(rs.getString("name"));
//      System.out.println(rs.getString("url"));
//      System.out.println(rs.getString("md5"));
//    }
    final ResultSet rs = stmt.executeQuery(
        "SELECT COUNT(*) FROM `a`");
    while ( rs.next() ) {
      System.out.println(rs.getInt(1));
    }
    rs.close();
    stmt.close();
    conn.close();
  }

  public static Connection getConnection() throws SQLException {
    if ( ds == null ) {
      synchronized (DBConnection.class) {
        if ( ds == null ) {
          final Properties prop = Variables.getInstance().getProperties();
          ds = new BasicDataSource();
          ds.setMaxIdle(Integer.parseInt(prop.getProperty("maxIdle")));
          ds.setMaxActive(Integer.parseInt(prop.getProperty("maxActive")));
          ds.setUsername(prop.getProperty("username"));
          ds.setPassword(prop.getProperty("password"));
          ds.setUrl(prop.getProperty("url"));
          ds.setDriverClassName(prop.getProperty("driverClassName"));
        }
      }
    }
    return ds.getConnection();
  }
}
