package offensive.Server.Hybernate.POJO;

public class Alliance {
	private int id;
	private CurrentGame game;
	private Player player1;
	private Player player2;
	private AllianceType type;
	
	private long version;
	
	public Alliance () {};
	
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
	
	public Player getPlayer1() {
		return player1;
	}
	
	public void setPlayer1(Player player1) {
		this.player1 = player1;
	}
	
	public Player getPlayer2() {
		return player2;
	}
	
	public void setPlayer2(Player player2) {
		this.player2 = player2;
	}
	
	public AllianceType getType() {
		return type;
	}
	
	public void setType(AllianceType type) {
		this.type = type;
	}

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}
}
