package offensive.Server.Utilities;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import offensive.Server.Server;

public class CommonRandom {
	private int modConst = 2147483647;
	
	public int seed;
	
	public CommonRandom() {
		this(Math.abs((int)(new Date()).getTime()));
	}
	
	public CommonRandom(int seed) {
		this.seed = seed;
	}
	
	private double next() {
		this.seed = (int) ((this.seed * (long)16807) % modConst);
		
		if(this.seed < 0) {
			this.seed += modConst;
		}
		
		return this.seed / (double)0x7FFFFFFF + 0.000000000233;
	}
	
	public int nextInt() {
		return this.nextInt(Integer.MAX_VALUE);
	}
	
	public int nextInt(int max) {
		return this.nextInt(0, max);
	}
	
	public int nextInt(int min, int max) {
		return (int) Math.floor(this.next() * (max - min) + min);
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
