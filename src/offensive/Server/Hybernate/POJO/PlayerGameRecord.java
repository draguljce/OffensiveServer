package offensive.Server.Hybernate.POJO;

public class PlayerGameRecord {
	CompletedGame game;
	Player player;
	PlayerStatistics playerStatistics;
	
	public PlayerGameRecord() {};
	
	public CompletedGame getGame() {
		return this.game;
	}
	
	public void setGame(CompletedGame game) {
		this.game = game;
	}
	
	public Player getPlayer() {
		return this.player;
	}
	
	public void setPlayer(Player player) {
		this.player = player;
	}
	
	public PlayerStatistics getPlayerStatistics() {
		return this.playerStatistics;
	}
	
	public void setPlayerStatistics(PlayerStatistics playerStatistics) {
		this.playerStatistics = playerStatistics;
	}
}
