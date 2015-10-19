package util;

public class Util {
	public static void myassert(boolean val, String msg) {
		if(!val) {
			Thread.dumpStack();
			System.exit(1);
		}
	}
	
	public static void println(String s) {
		if(true) {
			System.out.println(s);
		}
	}
}
