package test;

import net.sf.json.JSONObject;

public class TestJSON {
	public static void main(String[] args) {
//		JSONObject o = JSONObject.fromObject("{}");
//		System.out.println(o.getJSONArray("t"));
		JSONObject o = JSONObject.fromObject("{\"album_id\":\"8068746757916692393\"}");
		System.out.println(o.getString("album_id"));
		System.out.println(o.optLong("album_id"));
	}
}
