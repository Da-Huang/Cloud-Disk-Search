package crawl.yun.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import crawl.yun.User;
import database.DBConnection;


public class UserSet {
	private static Logger logger = LogManager.getLogger(UserSet.class);
	
	private static UserSet instance = null;
	public static UserSet getInstance() {
		if ( instance == null )
			instance = new UserSet();
		return instance;
	}
	private UserSet() {
		try {
			Connection conn = DBConnection.getConnection();
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(""
					+ "CREATE TABLE IF NOT EXISTS `users` ("
					+ "  `uk` BIGINT PRIMARY KEY,"
					+ "  `uname` TEXT,"
					+ "  `follows` INT,"
					+ "  `fans` INT,"
					+ "  `shares` INT,"
					+ "  `dealing` BOOLEAN DEFAULT 0,"
					+ "  `crawled` BOOLEAN DEFAULT 0"
					+ ")");
			stmt.executeUpdate(""
					+ "CREATE INDEX IF NOT EXISTS `users_follows` "
					+ "ON `users` (`follows`)");
			stmt.executeUpdate(""
					+ "CREATE INDEX IF NOT EXISTS `users_fans` "
					+ "ON `users` (`fans`)");
			stmt.executeUpdate(""
					+ "CREATE INDEX IF NOT EXISTS `users_shares` "
					+ "ON `users` (`shares`)");
			stmt.executeUpdate(""
					+ "CREATE INDEX IF NOT EXISTS `users_dealing` "
					+ "ON `users` (`dealing`)");
			stmt.executeUpdate(""
					+ "CREATE INDEX IF NOT EXISTS `users_crawled` "
					+ "ON `users` (`crawled`)");
			stmt.close();
			conn.close();
		} catch (SQLException e) {
			logger.error(e);
		}
	}
	
	public boolean add(long uk, String uname, int follows, int fans, int shares) {
		logger.entry(uk, uname, follows, fans);
		boolean res = true;
		try {
			Connection conn = DBConnection.getConnection();
			Statement stmt = conn.createStatement();
			try {
				stmt.executeUpdate(String.format(""
						+ "INSERT INTO `users` (`uk`, `uname`, `follows`, `fans`, `shares`) "
						+ "VALUES (%d, '%s', %d, %d, %d)", uk, uname, follows, fans, shares));
			} catch (SQLException e) {
				logger.error(e);
				res = false;
			}
			stmt.close();
			conn.close();
		} catch (SQLException e) {
			logger.error(e);
			res = false;
		}
		return res;
	}
	
	public void setDealing(long uk, boolean dealing) {
		try {
			Connection conn = DBConnection.getConnection();
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(String.format(""
					+ "UPDATE `users` SET `dealing` = %d "
					+ "WHERE `uk` = %d", dealing, uk));
			stmt.close();
			conn.close();
		} catch (SQLException e) {
			logger.error(e);
		}
	}

	public void setCrawled(long uk, boolean crawled) {
		try {
			Connection conn = DBConnection.getConnection();
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(String.format(""
					+ "UPDATE `users` SET `crawled` = %d "
					+ "WHERE `uk` = %d", crawled, uk));
			stmt.close();
			conn.close();
		} catch (SQLException e) {
			logger.error(e);
		}
	}
	
	public List<User> getUndealingUsers(int limit) {
		List<User> users = new ArrayList<User>();
		try {
			Connection conn = DBConnection.getConnection();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(String.format(""
					+ "SELECT * "
					+ "FROM `users` WHERE dealing = 0 "
					+ "LIMIT %d", limit));
			while ( rs.next() ) {
				users.add(new User(
						rs.getLong("uk"), rs.getString("uname"), 
						rs.getInt("follows"), rs.getInt("fans"),
						rs.getInt("shares")));
			}
			rs.close();
			stmt.close();
			conn.close();
		} catch (SQLException e) {
			logger.error(e);
		}
		return users;
	}
}