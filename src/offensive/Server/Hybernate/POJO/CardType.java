package offensive.Server.Hybernate.POJO;

public class CardType {
	private int id;
	private String type;
	
	private long version;
	
	public CardType () {};
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}
}
