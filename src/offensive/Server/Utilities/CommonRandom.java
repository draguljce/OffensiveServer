package offensive.Server.Utilities;

import java.util.Random;

public class CommonRandom {
	private static Random rand = new Random();
	
	public static int next() {
		return CommonRandom.rand.nextInt();
	}
	
	public static int next(int max) {
		return CommonRandom.rand.nextInt(max);
	}
}
