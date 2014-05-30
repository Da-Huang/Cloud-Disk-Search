package test;

import java.util.HashMap;

import org.apache.commons.lang.StringEscapeUtils;

import crawl.IPList;
import crawl.yun.Request;

public class TestUnicode {
	public static void main(String[] args) throws Exception {
//		String name = "\\u3010\\u6e38\\u6c11\\u661f\\u7a7a\\u4e0b\\u8f7d\\u533a\\u3011\\u3010\\u6e38\\u6c11\\u661f\\u7a7a\\u4e0b\\u8f7d\\u533a\\u3011\\u300a\\u771f\\u4e09\\u56fd\\u65e0\\u53cc6\\uff1a\\u731b\\u5c06\\u4f20\\u300b";
//		System.out.println(StringEscapeUtils.unescapeJavaScript(name));
//		String info = Request.requestHTMLForce("http://pan.baidu.com/share/link?uk=3158078488&shareid=77227273");
		String info = Request.requestPlainForce("http://pan.baidu.com/s/1EIWT6", new HashMap<String, String>());
		int begin = info.indexOf("parent_path\\\":\\\"") + "parent_path\\\":\\\"".length();
//		System.out.println(info.indexOf("\"parent_path\":\""));
		int end = info.indexOf("\\\"", begin);
		String parentPath = info.substring(begin, end);
//		parentPath = StringEscapeUtils.escapeJavaScript(parentPath);
//		System.out.println(parentPath);
//		System.out.println(info.substring(begin, end));
		
	}
}
