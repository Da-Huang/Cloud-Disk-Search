package database;

import java.util.HashMap;
import java.util.Map;

public class FileType {
	public static String IMAGE = "image";
	public static String ZIP = "zip";
	public static String MUSIC = "music";
	public static String VIDEO = "video";
	public static String TEXT = "text";
	public static String OFFICE = "office";
	public static String UNKNOWN = "unknown";
	
	private static Map<String, String> TYPE_MAP = new HashMap<>();
	private static Map<String, String> TYPES = new HashMap<>();
	
	static {
		TYPES.put(FileType.IMAGE, "jpg|jpeg|bmp|png|gif|ai|psd|tiff|pcx|raw");
		TYPES.put(FileType.ZIP, "zip|rar|7z|gzip|bz2");
		TYPES.put(FileType.MUSIC, "mp3|wav|aac|ac3|ogg|flac|m4a|ape|mid|mka|snd|cda");
		TYPES.put(FileType.VIDEO, "mp4|mpg|mpeg|avi|rm|rmvb|3gp|mkv|vob|mlv|flv|wmv");
		TYPES.put(FileType.TEXT, "txt|doc|docx");
		TYPES.put(FileType.OFFICE, "ppt|pptx|xls|xlsx");
		
		for (String type : TYPES.keySet()) {
			String [] postfixes = TYPES.get(type).split("|");
			for (String postfix : postfixes) {
				TYPE_MAP.put(postfix, type);
			}
		}
	}
	
	
	public static String getType(String postfix) {
		String type = TYPE_MAP.get(postfix);
		return type == null ? FileType.UNKNOWN : type;
	}
}
