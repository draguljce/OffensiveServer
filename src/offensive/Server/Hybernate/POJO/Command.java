package offensive.Server.Hybernate.POJO;

import offensive.Server.WorkerThreads.BattleThread.Army;
import communication.protos.DataProtos;

public class Command {
	private int id;
	private CurrentGame game;
	private short round;
	private Player player;
	private Territory source;
	private Territory destination;
	private CommandType type;
	private Phase phase;
	private int troopNumber;
	
	private long version;
	
	public Command () {};
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public CurrentGame getGame() {
		return game;
	}
	
	public void setGame(CurrentGame game) {
		this.game = game;
	}
	
	public short getRound() {
		return round;
	}
	
	public void setRound(short round) {
		this.round = round;
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public void setPlayer(Player player) {
		this.player = player;
	}
	
	public Territory getSource() {
		return source;
	}
	
	public void setSource(Territory source) {
		this.source = source;
	}
	
	public Territory getDestination() {
		return destination;
	}
	
	public void setDestination(Territory destination) {
		this.destination = destination;
	}
	
	public int getTroopNumber() {
		return troopNumber;
	}
	
	public void setTroopNumber(int troopNumber) {
		this.troopNumber = troopNumber;
	}
	
	public CommandType getType() {
		return type;
	}

	public void setType(CommandType type) {
		this.type = type;
	}

	public Phase getPhase() {
		return phase;
	}

	public void setPhase(Phase phase) {
		this.phase = phase;
	}
	
	public communication.protos.DataProtos.Command toProtoCommand() {
		DataProtos.Command.Builder commandBuilder = DataProtos.Command.newBuilder();
		
		commandBuilder.setCommandId(this.id);
		commandBuilder.setSourceTerritory(this.source.getField().getId());
		commandBuilder.setDestinationTerritory(this.destination.getField().getId());
		commandBuilder.setNumberOfUnits(this.troopNumber);
		
		return commandBuilder.build();
	}
	
	public Army toArmy() {
		return new Army(this);
	}

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}
}
