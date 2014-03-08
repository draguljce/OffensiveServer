package offensive.Server.Hybernate.POJO;

public class Play {
	private CurrentGame game;
	private Player player;
	
	public Play() {}

	public CurrentGame getGame() {
		return game;
	}

	public void setGame(CurrentGame game) {
		this.game = game;
	}

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	};
	
	
}
