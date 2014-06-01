package offensive.Server.WorkerThreads.BattleThread;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import offensive.Server.Server;

import communication.protos.DataProtos;

class Dices {
	int dice1;
	int dice2;
	int dice3;
	
	void roll(int numberOfDices) {
		dice1 = dice2 = dice3 = -1;
		
		switch (numberOfDices) {
		case 3:
			dice3 = Server.getServer().rand.nextInt(1, 7);
			
		case 2:
			dice2 = Server.getServer().rand.nextInt(1, 7);
			
		case 1:
			dice1 = Server.getServer().rand.nextInt(1, 7);
			
			break;

		default:
			throw new IllegalArgumentException();
		}
	}
	
	Collection<Integer> getDices() {
		List<Integer> values = new LinkedList<>();
		
		if(this.dice1 != -1){
			values.add(this.dice1);
		}
		
		if(this.dice2 != -1){
			values.add(this.dice2);
		}
		
		if(this.dice3 != -1){
			values.add(this.dice3);
		}
		
		return values;
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
			
			return 	this.dice1 == otherDices.dice1 &&
					this.dice2 == otherDices.dice2 &&
					this.dice3 == otherDices.dice3;
		}
		
		return false;
	}
	
	DataProtos.Dices toProtoDices() {
		DataProtos.Dices.Builder dicesBuilder = DataProtos.Dices.newBuilder();
		
		dicesBuilder.setDice1(this.dice1);
		dicesBuilder.setDice2(this.dice2);
		dicesBuilder.setDice3(this.dice3);
		
		return dicesBuilder.build();
	}
}
