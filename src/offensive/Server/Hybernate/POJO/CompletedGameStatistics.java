package offensive.Server.Hybernate.POJO;

public class CompletedGameStatistics {
	private int id;
	private Player player;
	private short ranking;
	
	private long version;
	
	public CompletedGameStatistics() {}

	public int getId() {
		return id;
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

	public short getRanking() {
		return ranking;
	}

	public void setRanking(short ranking) {
		this.ranking = ranking;
	}

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	};
}
