package offensive.Server.WorkerThreads.BattleThread;

import java.util.Collection;
import java.util.LinkedList;

import offensive.Server.Hybernate.POJO.Command;
import communication.protos.DataProtos;

public class CommandContainer {
	Collection<Command> commands = new LinkedList<Command>();
	
	Collection<Army> oneSide = new LinkedList<>();
	Collection<Army> otherSide = new LinkedList<>();
	
	public CommandContainer(boolean isBorderClash) {
		this.isBorderClash = isBorderClash;
	}
	
	public boolean isBorderClash;
	
	public void add(Command command) {
		this.commands.add(command);
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
}
