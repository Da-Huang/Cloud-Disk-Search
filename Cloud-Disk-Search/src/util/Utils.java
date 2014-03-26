package util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class Utils {
	private static Logger logger = LogManager.getLogger(Utils.class.getName());

	public static void main(String[] args) {
		String test = "abcdefghijklmnopqrstdlfasjfl sdlfjasd fl dslfjasd fl sdlfj sdklfj sdf;j gladf ak" +
	" dfaaaaaaaaaaaaaaaaaaaaaaaaa fdjlfkasd fjjjjjjjjjjjjjjjjjjjj fdlfjaslk kkkkkkkkkkkkjdla  kl";
		System.out.println(test.length());
		String gzip = compress(test);
		System.out.println(gzip);
		System.out.println(gzip.length());
	}
	
	
	public static String compress(String str) {
		try {
		    ByteArrayOutputStream out = new ByteArrayOutputStream();
		    GZIPOutputStream gzip = new GZIPOutputStream(out);
		    gzip.write(str.getBytes());
		    gzip.close();
		    return out.toString("utf8");
		} catch (IOException e) {
			logger.error("Compress Error.");
		}
	    return "";
	}
}
