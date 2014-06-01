package offensive.Server.WorkerThreads.BattleThread;

import java.util.LinkedList;

import offensive.Server.Utilities.CommonRandom;

class Dices {
	LinkedList<Integer> diceValues = new LinkedList<Integer>();
	
	CommonRandom rand;
	
	public Dices(CommonRandom randomGenerator) {
		this.rand = randomGenerator;
	}
	
	void roll(int numberOfDices) {
		this.diceValues.clear();
		
		for(int i = 0; i < numberOfDices; i++) {
			this.diceValues.add(this.rand.nextInt(1, 7));
		}
		
		this.diceValues.sort((oneInt, otherInt) -> Integer.compare(oneInt, otherInt));
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
				if(this.diceValues.remove() != otherDices.diceValues.remove()) {
					return false;
				}
			}
			
			return true;
		}
		
		return false;
	}
}
