package offensive.Server.Hybernate.POJO;

public class PlayerStatistics {
	private int id;
	
	private short ranking;
	
	public PlayerStatistics() {};
	
	public PlayerStatistics(short ranking) {
		this.ranking = ranking;
	}
	
	public int getId() {
		return this.id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public short getRanking() {
		return this.ranking;
	}
	
	public void setRanking(short ranking) {
		this.ranking = ranking;
	}
}
