package crawl.yun.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import crawl.yun.YunFile;
import database.DBConnection;


public class FileSet {
  private static final Logger logger = LogManager.getLogger(FileSet.class);

  private static FileSet instance = null;
  public static FileSet getInstance() {
    if ( instance == null ) {
      synchronized (FileSet.class) {
        if ( instance == null )
          instance = new FileSet();
      }
    }
    return instance;
  }
  private FileSet() {
    try {
      Connection conn = DBConnection.getConnection();
      Statement stmt = conn.createStatement();
      stmt.executeUpdate(""
          + "CREATE TABLE IF NOT EXISTS `files` ("
          + "  `uk` BIGINT,"
          + "  `md5` CHAR(32),"
          + "  `title` TEXT,"
          + "  `url` TEXT,"
          + "  `urlmd5` CHAR(32),"
          + "  `size` BIGINT,"
          + "  `desc` TEXT,"
          + "  `time` BIGINT,"
          + "  `downloads` INT,"
          + "  `visits` INT,"
          + "  `saves` INT,"
          + "  PRIMARY KEY (`uk`,`urlmd5`)"
          + ")");
      try {
        stmt.executeUpdate(""
            + "CREATE INDEX `files_uk` "
            + "ON `files` (`uk`)");
      } catch (SQLException e) {
        if ( !e.toString().contains("Duplicate key name") )
          logger.error(e);
      }
      try {
        stmt.executeUpdate(""
            + "CREATE INDEX `files_md5` "
            + "ON `files` (`md5`)");
      } catch (SQLException e) {
        if ( !e.toString().contains("Duplicate key name") )
          logger.error(e);
      }
      try {
        stmt.executeUpdate(""
            + "CREATE INDEX `files_size` "
            + "ON `files` (`size`)");
      } catch (SQLException e) {
        if ( !e.toString().contains("Duplicate key name") )
          logger.error(e);
      }
      try {
        stmt.executeUpdate(""
            + "CREATE INDEX `files_time` "
            + "ON `files` (`time`)");
      } catch (SQLException e) {
        if ( !e.toString().contains("Duplicate key name") )
          logger.error(e);
      }
      try {
        stmt.executeUpdate(""
            + "CREATE INDEX `files_downloads` "
            + "ON `files` (`downloads`)");
      } catch (SQLException e) {
        if ( !e.toString().contains("Duplicate key name") )
          logger.error(e);
      }
      try {
        stmt.executeUpdate(""
            + "CREATE INDEX `files_visits` "
            + "ON `files` (`visits`)");
      } catch (SQLException e) {
        if ( !e.toString().contains("Duplicate key name") )
          logger.error(e);
      }
      try {
        stmt.executeUpdate(""
            + "CREATE INDEX `files_saves` "
            + "ON `files` (`saves`)");
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

  public boolean add(YunFile file) {
    return add(file.uk, file.md5, file.title, file.url, file.size,
        file.desc, file.time, file.downloads, file.visits, file.saves);
  }

  public boolean add(long uk, String md5, String title, String url, long size,
      String desc, long time, int downloads, int visits, int saves) {
    logger.entry(uk, md5, title, url, size, desc, downloads, visits, saves);
    boolean res = true;
    try {
      final Connection conn = DBConnection.getConnection();
      final PreparedStatement stmt = conn.prepareStatement(""
          + "INSERT INTO `files` (`uk`, `md5`, `title`, `url`, `urlmd5`, `size`, "
          + "`desc`, `time`, `downloads`, `visits`, `saves`) "
          + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
      try {
        stmt.setLong(1, uk);
        stmt.setString(2, md5);
        stmt.setString(3, title);
        stmt.setString(4, url);
        stmt.setString(5, DigestUtils.md5Hex(url));
        stmt.setLong(6, size);
        stmt.setString(7, desc);
        stmt.setLong(8, time);
        stmt.setInt(9, downloads);
        stmt.setInt(10, visits);
        stmt.setInt(11, saves);
        stmt.executeUpdate();
      } catch (SQLException e) {
        logger.error(e + " : <" + uk + ":" + url + ">");
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

  public int size() {
    int size = 0;
    try {
      Connection conn = DBConnection.getConnection();
      Statement stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery(""
          + "SELECT COUNT(*) FROM `files`");
      if ( rs.next() ) size = rs.getInt(1);
      rs.close();
      stmt.close();
      conn.close();
    } catch (SQLException e) {
      logger.error(e);
    }
    return size;
  }

  public YunFile fetchYunFile(long uk, String md5) {
    YunFile file = null;
    try {
      Connection conn = DBConnection.getConnection();
      Statement stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery(String.format(""
          + "SELECT * FROM `files` WHERE `uk` = %d AND md5 = '%s'",
          uk, md5));
      if ( rs.next() ) {
        String title = rs.getString("title");
        String url = rs.getString("url");
        long size = rs.getLong("size");
        String desc = rs.getString("desc");
        long time = rs.getLong("time");
        int downloads = rs.getInt("downloads");
        int visits = rs.getInt("visits");
        int saves = rs.getInt("saves");
        file = new YunFile(uk, md5, title, url, size, desc, time, downloads, visits, saves);
      }
      rs.close();
      stmt.close();
      conn.close();
    } catch (SQLException e) {
      logger.error(e);
    }
    return file;
  }
}
