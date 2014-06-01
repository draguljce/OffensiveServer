package offensive.Server.Hybernate.POJO;

import communication.protos.DataProtos;

public class Command {
	private int id;
	private CurrentGame game;
	private short round;
	private Player player;
	private Field source;
	private Field destination;
	private CommandType type;
	private Phase phase;
	private int troopNumber;
	
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
	
	public Field getSource() {
		return source;
	}
	
	public void setSource(Field source) {
		this.source = source;
	}
	
	public Field getDestination() {
		return destination;
	}
	
	public void setDestination(Field destination) {
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
		commandBuilder.setSourceTerritory(this.source.getId());
		commandBuilder.setDestinationTerritory(this.destination.getId());
		commandBuilder.setNumberOfUnits(this.troopNumber);
		
		return commandBuilder.build();
	}
}
