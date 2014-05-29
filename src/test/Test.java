package test;

import java.util.Date;

import net.sf.json.JSONObject;
import crawl.yun.util.UserSet;

public class Test {
	static class A {
	    public synchronized void testA() {
	    	while ( true ) {
		    	System.out.println("A");
		    	try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		    	System.out.println("A-");
	    	}
	    }
	}

	static class B extends A {
	    public synchronized void testB() {
	    	while ( true ) {
		    	System.out.println("B");
		    	try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		    	System.out.println("B-");
	    	}
	    }
	}

	public static void main(String[] args) throws InterruptedException {
//		final A a = new B();
//		Thread t1 = new Thread(new Runnable() {
//			@Override
//			public void run() {
//				a.testA();
//			}
//		});
//		Thread t2 = new Thread(new Runnable() {
//			@Override
//			public void run() {
//				((B) a).testB();
//			}
//		});
//		t1.start();
//		t2.start();
//		t1.join();
//		t2.join();
//		System.out.println(UserSet.getInstance().size());
//		System.out.println(new Date(1389860622691L));
//		System.out.println(new Date(1401324183L));
		JSONObject o = JSONObject.fromObject("{b:'true'}");
		System.out.println(o.getBoolean("b"));
	}
}
