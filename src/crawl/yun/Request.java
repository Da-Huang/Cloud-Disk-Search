package crawl.yun;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;

import net.sf.json.JSONObject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import crawl.ProxyList;


public class Request {
  private static final Logger logger = LogManager.getLogger(Request.class);

  private static final Map<String, String> HEADER = new HashMap<>();
  static {
    HEADER.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
    HEADER.put("Accept-Encoding", "gzip,deflate,sdch");
    HEADER.put("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.6,en;q=0.4,zh-TW;q=0.2");
    HEADER.put("Cache-Control", "max-age=0");
    HEADER.put("Connection", "keep-alive");
    HEADER.put("Cookie", "BAIDUID=1ADE0E34476BB774355771D8DBE57A6A:FG=1; BAIDU_WISE_UID=wapp_1393681486226_801; BDUSS=Gd-aXc1UWY1Qld4cmwzSkNFVUxZRC1hMlVXLTBlMno1azNUblhycXlpRURhRHBUQVFBQUFBJCQAAAAAAAAAAAEAAAAfXL0vcHJvamVjdDAwMDF4AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAPbElMD2xJTc; PANWEB=1; Hm_lvt_773fea2ac036979ebb5fcc768d8beb67=1393744939; Hm_lvt_adf736c22cd6bcc36a1d27e5af30949e=1393744939; bdshare_firstime=1393744981338; locale=zh; cflag=64511%3A1; NBID=5006B2B5698C98015EB0A01DB34AB17D:FG=1; BDRCVFR[feWj1Vr5u3D]=I67x6TjHwwYf0; H_PS_PSSID=; Hm_lvt_1d15eaebea50a900b7ddf4fa8d05c8a0=1397971209,1397973787,1397973792,1397973884; Hm_lpvt_1d15eaebea50a900b7ddf4fa8d05c8a0=1397973884; Hm_lvt_f5f83a6d8b15775a02760dc5f490bc47=1397971209,1397973787,1397973792,1397973884; Hm_lpvt_f5f83a6d8b15775a02760dc5f490bc47=1397973884");
    HEADER.put("Host", "yun.baidu.com");
    HEADER.put("Referer", "http://yun.baidu.com/");
    HEADER.put("User-Agent", "MMozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/34.0.1847.116 Safari/537.36");
  }

  static public JSONObject requestForceYun(String urlStr, Map<String, String> args) {
    JSONObject res = requestJSONForce(urlStr, args);
    int tryTimes = 0;
    while ( res.containsKey("errno") && res.getInt("errno") == -55 ) {
      ++ tryTimes;
      logger.error("request json errno -55. redo -- " + tryTimes);
      try {
        Thread.sleep(10000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      res = requestJSONForce(urlStr, args);
    }
    return res;
  }

  static public JSONObject requestJSONForce(String urlStr, Map<String, String> args) {
    JSONObject res = null;
    int tryTimes = 0;
    while ( res == null ) {
      final int ipd = ProxyList.getInstance().open();
      try {
        res = requestJSON(urlStr, args, ipd);
      } catch (Exception e) {
        ++ tryTimes;
        logger.error(e + " --- " + tryTimes);
        try {
          Thread.sleep(2000);
        } catch (InterruptedException e1) {
          logger.error(e1);
        }
      }
      ProxyList.getInstance().close(ipd);
    }
    return res;
  }

  static public String requestPlainForce(String urlStr, Map<String, String> args) {
    String res = null;
    int tryTimes = 0;
    while ( res == null ) {
      final int ipd = ProxyList.getInstance().open();
      try {
        res = requestPlain(urlStr, args, ipd);
      } catch (Exception e) {
        ++ tryTimes;
        logger.error(e + " --- " + tryTimes);
        e.printStackTrace();
        try {
          Thread.sleep(2000);
        } catch (InterruptedException e1) {
          logger.error(e1);
        }
      }
      ProxyList.getInstance().close(ipd);
    }
    return res;
  }

  static private String request(String urlStr, int ipd) throws Exception {
    final URL url = new URL(urlStr);
    logger.info(url);
    InputStream in = null;
    HttpURLConnection conn = null;

    final Entry<String, Integer> address = ProxyList.getInstance().get(ipd);
    if ( address != null ) {
      final String ip = address.getKey();
      final int port = address.getValue();
      logger.info("requesting from " + ip + ":" + port);
      conn = (HttpURLConnection) url.openConnection(
          new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ip, port)));
    } else {
      logger.info("requesting without proxy.");
      conn = (HttpURLConnection) url.openConnection();
    }
    for (String key : HEADER.keySet()) {
      conn.setRequestProperty(key, HEADER.get(key));
    }
    conn.setRequestMethod("GET");
    conn.setReadTimeout(2000);
    conn.setConnectTimeout(2000);
    conn.connect();
    in = conn.getInputStream();
    final String encoding = conn.getHeaderField("Content-Encoding");
    if ( encoding != null && encoding.equalsIgnoreCase("gzip") ) in = new GZIPInputStream(in);
    final String type = conn.getHeaderField("Content-Type");
    final String charset = type == null ? "utf8" : getCharset(type);
    logger.debug(type);
    final BufferedReader br = new BufferedReader(new InputStreamReader(in, charset));
    final StringBuffer response = new StringBuffer();
    String line;
    while ( (line = br.readLine()) != null ) {
      response.append(line);
      response.append("\n");
    }
    br.close();
    conn.disconnect();

    final String res = response.toString().trim();
    logger.exit(res.substring(0, Math.min(res.length(), 100)).replaceAll("[\n\r]", "") + "...");
    return res;
  }

  static private String getCharset(String type) {
    type = type.toLowerCase();
    int index = type.indexOf("charset=");
    type = type.substring(index + "charset=".length());
    index = type.indexOf(";");
    if ( index >= 0 ) type = type.substring(0, index);
    return type;
  }

  static private JSONObject requestJSON(String urlStr, Map<String, String> args, int ipd) throws Exception {
    String plain = requestPlain(urlStr, args, ipd);
    return JSONObject.fromObject(plain);
  }

  static private String requestPlain(String urlStr, Map<String, String> args, int ipd) throws Exception {
    urlStr += "?";
    for (String key : args.keySet()) {
      urlStr += key;
      urlStr += "=";
      urlStr += URLEncoder.encode(args.get(key), "utf8");
      urlStr += "&";
    }
    String response = request(urlStr, ipd);
    return response;
  }
}
