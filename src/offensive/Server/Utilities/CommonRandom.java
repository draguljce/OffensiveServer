package offensive.Server.Utilities;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import offensive.Server.Server;

public class CommonRandom {
	public long seed;
	
	public CommonRandom() {
		this((new Date()).getTime());
	}
	
	public CommonRandom(long seed) {
		this.seed = (seed ^ 0x5DEECE66DL) & ((1L << 48) - 1);
	}
	
	private int next(int bits) {
		this.seed = (seed * 0x5DEECE66DL + 0xBL) & ((1L << 48) - 1);
		
		return (int)(seed >>> (48 - bits));
	}
	
	public int nextInt(int n) {
		if (n <= 0) {
			throw new IllegalArgumentException("n must be positive");
		}

		if ((n & -n) == n) {  // i.e., n is a power of 2
			return (int)((n * (long)next(31)) >> 31);
		}

		int bits, val;
		do {
			bits = next(31);
			val = bits % n;
		} while (bits - val + (n-1) < 0);
	
		return val;
	}
	
	public int nextInt(int min, int max) {
		int range = max - min;
		
		return this.nextInt(range) + min;
	}
	
	public long nextLong() {
		return ((long)next(32) << 32) + next(32);
	}
	
	public <T> Collection<T> chooseRandomSubset(Collection<T> sourceCollection, int numberOfElements) {
		List<T> copyCollection = new LinkedList<T>();
		
		sourceCollection.forEach(element -> copyCollection.add(element));
		
		Collections.shuffle(copyCollection);
		
		return copyCollection.subList(0, numberOfElements);
	}
	
	public <T> T chooseRandomElement(Collection<T> sourceCollection) {
		int chosenElement = Server.getServer().rand.nextInt(sourceCollection.size());
		
		int currentElement = 0;
		for(T element: sourceCollection) {
			if(chosenElement == currentElement++) {
				return element;
			}
		}
		
		return null;
	}
}
