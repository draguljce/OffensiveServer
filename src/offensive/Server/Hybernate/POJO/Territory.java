package offensive.Server.Hybernate.POJO;

public class Territory {
	private int id;
	private CurrentGame game;
	private Field field;
	private Player player;
	private short troopsOnIt;
	
	public Territory() {};

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
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

}
