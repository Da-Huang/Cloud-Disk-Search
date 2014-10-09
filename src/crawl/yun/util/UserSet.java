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
  private static final Logger logger = LogManager.getLogger(UserSet.class);

  private static UserSet instance = null;
  public static UserSet getInstance() {
    if ( instance == null )
      instance = new UserSet();
    return instance;
  }
  private UserSet() {
    try {
      final Connection conn = DBConnection.getConnection();
      final Statement stmt = conn.createStatement();
      stmt.executeUpdate(""
          + "CREATE TABLE IF NOT EXISTS `users` ("
          + "  `uk` BIGINT PRIMARY KEY,"
          + "  `uname` TEXT,"
          + "  `intro` TEXT,"
          + "  `follows` INT,"
          + "  `fans` INT,"
          + "  `shares` INT,"
          + "  `dealing` BOOLEAN DEFAULT 0,"
          + "  `crawled` BOOLEAN DEFAULT 0"
          + ")");
      try {
        stmt.executeUpdate(""
            + "CREATE INDEX `users_follows` "
            + "ON `users` (`follows`)");
      } catch (SQLException e) {
        if ( !e.toString().contains("Duplicate key name") )
          logger.error(e);
      }
      try {
        stmt.executeUpdate(""
            + "CREATE INDEX `users_fans` "
            + "ON `users` (`fans`)");
      } catch (SQLException e) {
        if ( !e.toString().contains("Duplicate key name") )
          logger.error(e);
      }
      try {
        stmt.executeUpdate(""
            + "CREATE INDEX `users_shares` "
            + "ON `users` (`shares`)");
      } catch (SQLException e) {
        if ( !e.toString().contains("Duplicate key name") )
          logger.error(e);
      }
      try {
        stmt.executeUpdate(""
            + "CREATE INDEX `users_dealing` "
            + "ON `users` (`dealing`)");
      } catch (SQLException e) {
        if ( !e.toString().contains("Duplicate key name") )
          logger.error(e);
      }
      try {
        stmt.executeUpdate(""
            + "CREATE INDEX `users_crawled` "
            + "ON `users` (`crawled`)");
      } catch (SQLException e) {
        if ( !e.toString().contains("Duplicate key name") )
          logger.error(e);
      }
      stmt.close();
      conn.close();
    } catch (SQLException e) {
      logger.error(e);
    }
  }

  public boolean add(long uk, String uname, String intro, int follows, int fans, int shares) {
    logger.entry(uk, uname, intro, follows, fans, shares);
    boolean res = true;
    try {
      final Connection conn = DBConnection.getConnection();
      final Statement stmt = conn.createStatement();
      try {
        stmt.executeUpdate(String.format(""
            + "INSERT INTO `users` (`uk`, `uname`, `intro`, `follows`, `fans`, `shares`) "
            + "VALUES (%d, '%s', '%s', %d, %d, %d)", uk, uname, intro, follows, fans, shares));
      } catch (SQLException e) {
        logger.error(e + " : <" + uk + ":" + uname + ">");
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
      final Connection conn = DBConnection.getConnection();
      final Statement stmt = conn.createStatement();
      stmt.executeUpdate(String.format(""
          + "UPDATE `users` SET `dealing` = %d "
          + "WHERE `uk` = %d", dealing ? 1 : 0, uk));
      stmt.close();
      conn.close();
    } catch (SQLException e) {
      logger.error(e);
    }
  }

  public void setCrawled(long uk, boolean crawled) {
    try {
      final Connection conn = DBConnection.getConnection();
      final Statement stmt = conn.createStatement();
      stmt.executeUpdate(String.format(""
          + "UPDATE `users` SET `crawled` = %d "
          + "WHERE `uk` = %d", crawled ? 1 : 0, uk));
      stmt.close();
      conn.close();
    } catch (SQLException e) {
      logger.error(e);
    }
  }

  public List<User> getUndealingUsers(int limit) {
    List<User> users = new ArrayList<User>();
    try {
      final Connection conn = DBConnection.getConnection();
      final Statement stmt = conn.createStatement();
      final ResultSet rs = stmt.executeQuery(String.format(""
          + "SELECT * "
          + "FROM `users` WHERE dealing = 0 "
          + "LIMIT %d", limit));
      while ( rs.next() ) {
        users.add(new User(
            rs.getLong("uk"), rs.getString("uname"), rs.getString("intro"),
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

  public int uncrawledSize() {
    int size = 0;
    try {
      final Connection conn = DBConnection.getConnection();
      final Statement stmt = conn.createStatement();
      final ResultSet rs = stmt.executeQuery(""
          + "SELECT COUNT(*) FROM `users` "
          + "WHERE crawled = 0");
      if ( rs.next() ) size = rs.getInt(1);
      rs.close();
      stmt.close();
      conn.close();
    } catch (SQLException e) {
      logger.error(e);
    }
    return size;
  }

  public int size() {
    int size = 0;
    try {
      final Connection conn = DBConnection.getConnection();
      final Statement stmt = conn.createStatement();
      final ResultSet rs = stmt.executeQuery(""
          + "SELECT COUNT(*) FROM `users`");
      if ( rs.next() ) size = rs.getInt(1);
      rs.close();
      stmt.close();
      conn.close();
    } catch (SQLException e) {
      logger.error(e);
    }
    return size;
  }
}
