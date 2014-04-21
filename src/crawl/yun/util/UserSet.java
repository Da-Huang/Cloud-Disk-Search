package crawl.yun.util;


public class UserSet {
	private static UserSet instance = null;
	public static UserSet getInstance() {
		if ( instance == null )
			instance = new UserSet();
		return instance;
	}
	private UserSet() {}
	
	public boolean add(long uk, String uname, long follows, long fans) {
		return true;
	}
}
