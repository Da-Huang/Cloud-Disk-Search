package util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class Utils {
	private static final Logger logger = LogManager.getLogger(Utils.class);

	public static void main(String[] args) {
//		String test = "abcdefghijklmnopqrstdlfasjfl sdlfjasd fl dslfjasd fl sdlfj sdklfj sdf;j gladf ak" +
//	" dfaaaaaaaaaaaaaaaaaaaaaaaaa fdjlfkasd fjjjjjjjjjjjjjjjjjjjj fdlfjaslk kkkkkkkkkkkkjdla  kl";
//		System.out.println(test.length());
//		String gzip = compress(test);
//		System.out.println(gzip);
//		System.out.println(gzip.length());
	}
	
	public static byte[] compress(byte[] bytes) {
		try {
			final ByteArrayOutputStream out = new ByteArrayOutputStream();
			final GZIPOutputStream gzip = new GZIPOutputStream(out);
		    gzip.write(bytes);
		    gzip.close();
		    return out.toByteArray();
		} catch (IOException e) {
			logger.error("Compress Error.");
		}
	    return null;
	}
	
	public static String compress(String str) {
		try {
			final ByteArrayOutputStream out = new ByteArrayOutputStream();
			final GZIPOutputStream gzip = new GZIPOutputStream(out);
		    gzip.write(str.getBytes("utf8"));
		    gzip.close();
		    return out.toString();
		} catch (IOException e) {
			logger.error("Compress Error.");
		}
	    return null;
	}
}
