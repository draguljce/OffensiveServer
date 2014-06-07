package offensive.Server.Hybernate.POJO;

import offensive.Server.WorkerThreads.BattleThread.Army;

public class Territory {
	private Integer id;
	private CurrentGame game;
	private Field field;
	private Player player;
	private short troopsOnIt;
	private short addedTroops;
	
	public Territory() {};
	
	public Territory(CurrentGame game, Field field, Player player) {
		this.game = game;
		this.field = field;
		this.player = player;
		this.troopsOnIt = 1;
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public short getTroopsOnIt() {
		return troopsOnIt;
	}

	public void setTroopsOnIt(short troopsOnIt) {
		this.troopsOnIt = troopsOnIt;
	};
	
	public CurrentGame getGame() {
		return game;
	}

	public void setGame(CurrentGame game) {
		this.game = game;
	}

	public Field getField() {
		return field;
	}

	public void setField(Field field) {
		this.field = field;
	}

	public short getAddedTroops() {
		return addedTroops;
	}

	public void setAddedTroops(short addedTroops) {
		this.addedTroops = addedTroops;
	}
	
	public void incrementNumberOfTroops() {
		this.troopsOnIt++;
	}
	
	public void addTroop() {
		this.addedTroops++;
	}
	
	public Army getArmy() {	
		return new Army(this.troopsOnIt, this.player, this);
	}

	public void submitTroops() {
		this.troopsOnIt += this.addedTroops;
	}
}
