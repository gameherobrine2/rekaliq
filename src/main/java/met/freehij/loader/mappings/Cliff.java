package met.freehij.loader.mappings;


public class Cliff {
	public static Cliff theCliff = new Cliff();
	public static short cliffstatic = 1;
	public int cliffCnt = 1337;
	public short cliffSize = 1666;
	public Cliff parentCliff;
	
	public void printCliff() {
		System.out.println("hi im cliff");
	}
	
	public static void printStaticCliff() {
		System.out.println("hi im cliff but static");
	}
}
