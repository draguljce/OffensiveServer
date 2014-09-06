package offensive.Server.WorkerThreads.BattleThread;

import offensive.Server.Server;
import offensive.Server.Hybernate.POJO.Command;
import offensive.Server.Hybernate.POJO.Player;
import offensive.Server.Hybernate.POJO.Territory;
import offensive.Server.Utilities.CommonRandom;

import communication.protos.DataProtos;

public class Army {
	int troopNumber;
	
	Territory sourceTerritory;
	Territory destinationTerritory;
	
	Player player;
	
	boolean isDefending;
	
	CommonRandom rand = new CommonRandom(Server.getServer().rand.nextInt() ^ Server.getServer().rand.nextInt());
	
	Dices dices = new Dices(this.rand);
	
	public Army(int troopNumber, Player player, Territory sourceTerritory) {
		this.destinationTerritory = this.sourceTerritory = sourceTerritory;
		this.isDefending = true;
		
		this.troopNumber = troopNumber;
		this.player = player;
	}
	
	public Army(Command command) {
		this.sourceTerritory = command.getSource();
		this.destinationTerritory = command.getDestination();
		
		this.isDefending = false;
		
		this.troopNumber = command.getTroopNumber();
		
		this.player = command.getPlayer();
	}
	
	DataProtos.Command toProtoCommand() { 
		DataProtos.Command.Builder commandBuilder = DataProtos.Command.newBuilder();
		
		commandBuilder.setSourceTerritory(this.sourceTerritory.getField().getId());
		commandBuilder.setDestinationTerritory(this.destinationTerritory.getField().getId());
		commandBuilder.setNumberOfUnits(this.troopNumber);
		
		commandBuilder.setSeed(this.rand.seed);
		return commandBuilder.build();
	}
	
	public void roll() {
		this.dices.roll(this.getNumberOfDices());
	}
	
	private int getNumberOfDices() {
		return Math.min(3, this.troopNumber);
	}
	
	@Override
	public int hashCode() {
		return (this.sourceTerritory.getField().getId() << 16) ^ this.destinationTerritory.getField().getId();
	}
	
	@Override
	public boolean equals(Object other) {
		if(this == other) {
			return true;
		}
		
		if(this.getClass() != other.getClass()) {
			return false;
		}
		
		Army otherArmy = (Army)other;
		
		if(this.sourceTerritory.getField().getId() == otherArmy.sourceTerritory.getField().getId() && this.destinationTerritory.getField().getId() == otherArmy.destinationTerritory.getField().getId()) {
			return true;
		}
		else {
			return false;
		}
	}
}
