package offensive.Server.WorkerThreads.BattleThread;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.StringJoiner;

import offensive.Server.Server;
import communication.protos.DataProtos;

public class BattleContainer {
	Collection<Army> armies = new LinkedList<Army>();
	
	LinkedList<Army> oneSide = new LinkedList<>();
	LinkedList<Army> otherSide = new LinkedList<>();
	
	public void add(Army army) {
		this.armies.add(army);
	}

	public DataProtos.BattleInfo toProtoBattleInfo() {
		DataProtos.BattleInfo.Builder protoBattleInfoBuilder = DataProtos.BattleInfo.newBuilder();
		
		for(Army army: this.oneSide) {
			protoBattleInfoBuilder.addOneSide(army.toProtoCommand());
		}
		
		for(Army army: this.otherSide) {
			protoBattleInfoBuilder.addOtherSide(army.toProtoCommand());
		}
		
		return protoBattleInfoBuilder.build();
	}
	
	public void nextRound() {
		if(this.oneSide.size() == 0 && this.otherSide.size() == 0) {
			this.executeSpoilsOfWar();
		} else {
			this.executeTwoSideBattle();
		}
	}
	
	private void executeSpoilsOfWar() {
		this.armies.forEach(army -> army.roll());
		
		int numberOfDices = this.numberOfDicesSpoilsOfWas();
		
		for(int i = 0; i < numberOfDices; i++) {
			int maxDice = this.getMaxDice(this.armies, false);
			
			for(Army army: this.armies) {
				if(army.dices.diceValues.remove() < maxDice) {
					army.troopNumber--;
				}
			}
		}
	}
	
	private void executeTwoSideBattle() {
		this.oneSide.forEach(army -> army.roll());
		this.otherSide.forEach(army -> army.roll());
		
		Server.getServer().logger.info("One side attack info: " + this.printAttackInfo(this.oneSide));
		Server.getServer().logger.info("One side attack info: " + this.printAttackInfo(this.otherSide));
		
		Server.getServer().logger.info("One side dices: " + this.printDices(this.oneSide));
		Server.getServer().logger.info("Other side dices: " + this.printDices(this.otherSide));
		
		int numberOfdices = this.numberOfDices();
		
		for(int i = 0; i <  numberOfdices; i++) {
			int maxOneSide = this.getMaxDice(this.oneSide, true);
			int maxOtherSide = this.getMaxDice(this.otherSide, true);
			
			if(maxOneSide < maxOtherSide) {
				this.removeUnits(this.oneSide);
			} else if (maxOneSide > maxOtherSide) {
				this.removeUnits(this.otherSide);
			} else if (this.oneSide.peek().isDefending ^ this.otherSide.peek().isDefending) {
				if(this.otherSide.peek().isDefending) {
					this.removeUnits(this.oneSide);
				} else {
					this.removeUnits(this.otherSide);
				}
			}
		}
	}
	
	private int numberOfDices() {
		Comparator<Army> diceSizeComparer = (firstArmy, secondArmy) -> Integer.compare(firstArmy.dices.diceValues.size(), secondArmy.dices.diceValues.size());  
		return Math.min(Collections.min(this.oneSide, diceSizeComparer).dices.diceValues.size(), Collections.min(this.otherSide, diceSizeComparer).dices.diceValues.size());
	}
	
	private int numberOfDicesSpoilsOfWas() {
		Comparator<Army> diceSizeComparer = (firstArmy, secondArmy) -> Integer.compare(firstArmy.dices.diceValues.size(), secondArmy.dices.diceValues.size());
		return Collections.min(this.armies, diceSizeComparer).dices.diceValues.size();
	}
	
	private int getMaxDice(Collection<Army> armies, boolean remove) {
		int max = 0;
		
		for(Army army: armies) {
			int armyDice;
			
			if(remove) {
				armyDice = army.dices.diceValues.remove();
			} else {
				armyDice = army.dices.diceValues.peek();
			}
			
			if(armyDice > max) {
				max = armyDice;
			}
		}
		
		return max;
	}
	
	private void removeUnits(Collection<Army> armies) {
		armies.forEach(army -> army.troopNumber--);
		armies.removeIf(army -> army.troopNumber == 0);
	}
	
	private String printDices(Collection<Army> armies) {
		StringJoiner armyDicesJoiner = new StringJoiner(", ");
		
		armies.forEach(army -> armyDicesJoiner.add(army.dices.toString()));
		
		return armyDicesJoiner.toString();
	}
	
	private String printAttackInfo(Collection<Army> armies) {
		StringJoiner armyDicesJoiner = new StringJoiner("; ");
		
		armies.forEach(army -> armyDicesJoiner.add(String.format("%s->%s (%s)", army.sourceTerritory.getField().getName(), army.destinationTerritory.getField().getName(), army.troopNumber)));
		
		return armyDicesJoiner.toString();
	}
}
