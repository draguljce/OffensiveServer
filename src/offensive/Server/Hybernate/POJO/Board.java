package offensive.Server.Hybernate.POJO;

public class Board {
	private int id;
	
	private long version;
	
	public Board() {};
	
	public Board(int id) {
		this.id = id;
	};
	
	public int getId() {
		return this.id;
	}
	
	public void setId(int id) {
		this.id = id;
	}

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}
}
