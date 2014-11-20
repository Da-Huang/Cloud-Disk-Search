package crawl.yun;

import java.util.Map;

import net.sf.json.JSONObject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import crawl.Request;


public class YunRequest extends Request {
  private static final Logger logger = LogManager.getLogger(YunRequest.class);

  private static YunRequest instance = null;
  public static YunRequest getInstance() {
    if ( instance == null ) {
      synchronized (YunRequest.class) {
        if ( instance == null )
          instance = new YunRequest();
      }
    }
    return instance;
  }

  private YunRequest() {
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

  public JSONObject requestForceYun(String urlStr, Map<String, String> args) {
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
}
