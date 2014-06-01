package offensive.Server.WorkerThreads.BattleThread;

import offensive.Server.Server;
import offensive.Server.Hybernate.POJO.Command;
import offensive.Server.Hybernate.POJO.Field;
import offensive.Server.Hybernate.POJO.Player;

import communication.protos.DataProtos;

public class Army {
	int troopNumber;
	
	Field sourceTerritory;
	Field destinationTerritory;
	
	Player player;
	
	boolean isDefending;
	
	long seed = Server.getServer().rand.nextLong();
	
	Army(int troopNumber, Player player) {
		this.sourceTerritory = null;
		this.destinationTerritory = null;
		this.isDefending = true;
		
		this.troopNumber = troopNumber;
		this.player = player;
	}
	
	Army(Command command) {
		this.sourceTerritory = command.getSource();
		this.destinationTerritory = command.getDestination();
		
		this.isDefending = false;
		
		this.troopNumber = command.getTroopNumber();
		
		this.player = command.getPlayer();
	}
	
	DataProtos.Command toProtoCommand() { 
		DataProtos.Command.Builder commandBuilder = DataProtos.Command.newBuilder();
		
		commandBuilder.setSourceTerritory(this.sourceTerritory.getId());
		commandBuilder.setDestinationTerritory(this.destinationTerritory.getId());
		commandBuilder.setNumberOfUnits(this.troopNumber);
		
		commandBuilder.setSeed(this.seed);
		return commandBuilder.build();
	}
}
