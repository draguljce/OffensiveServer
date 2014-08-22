package offensive.Server.WorkerThreads.BattleThread;

import java.util.ArrayList;
import java.util.Collections;

import offensive.Server.Utilities.CommonRandom;

class Dices {
	ArrayList<Integer> diceValues = new ArrayList<Integer>();
	
	CommonRandom rand;
	
	public Dices(CommonRandom randomGenerator) {
		this.rand = randomGenerator;
	}
	
	void roll(int numberOfDices) {
		this.diceValues.clear();;
		
		for(int i = 0; i < numberOfDices; i++) {
			this.diceValues.add(this.rand.nextInt(1, 7));
		}
		
		Collections.sort(this.diceValues, Collections.reverseOrder());
	}
	
	@Override
	public boolean equals(Object other) {
		if(this == other) {
			return true;
		}
		
		if(other == null) {
			return false;
		}
		
		if(this.getClass().equals(other.getClass())) {
			Dices otherDices = (Dices)other;
			
			if(this.diceValues.size() != otherDices.diceValues.size()) {
				return false;
			}
			
			for(int i = 0; i < this.diceValues.size(); i++) {
				if(this.diceValues.get(i) != otherDices.diceValues.get(i)) {
					return false;
				}
			}
			
			return true;
		}
		
		return false;
	}
	
	@Override 
	public String toString() {
		StringBuilder diceValuesBuilder = new StringBuilder("[");
		
		for(int value :this.diceValues) {
			diceValuesBuilder.append(value).append(", ");
		}
		
		if(this.diceValues.size() > 0) {
			diceValuesBuilder.delete(diceValuesBuilder.length() - 2, diceValuesBuilder.length());
		}
		
		diceValuesBuilder.append("]");
		
		return diceValuesBuilder.toString();
	}
}
