package crawl.yun;

import java.sql.Date;

public class YunFile {
  public final long uk;
  public final String md5;
  public final String title;
  public final String url;
  public final long size;
  public final String desc;
  public final long time;
  public final int downloads;
  public final int visits;
  public final int saves;

  public YunFile(long uk, String md5, String title, String url, long size,
      String desc, long time, int downloads, int visits, int saves) {
    this.uk = uk;
    this.md5 = md5;
    this.title = title;
    this.url = url;
    this.size = size;
    this.desc = desc;
    this.time = time;
    this.downloads = downloads;
    this.visits = visits;
    this.saves = saves;
  }

  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append("uk=" + uk + ",");
    sb.append("md5=" + md5 + ",");
    sb.append("title=" + title + ",");
    sb.append("url=" + url + ",");
    sb.append("size=" + size + ",");
    sb.append("desc=" + desc + ",");
    sb.append("time=" + new Date(time) + ",");
    sb.append("downloads=" + downloads + ",");
    sb.append("visits=" + visits + ",");
    sb.append("saves=" + saves + ",");
    return sb.toString();
  }
}
