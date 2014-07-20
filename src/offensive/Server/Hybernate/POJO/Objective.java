package offensive.Server.Hybernate.POJO;

public class Objective {
	private int id;
	private String description;
	
	private long version;
	
	public Objective() {};
	
	public Objective(int id) {
		this.id = id;
	}
	
	public int getId() {
		return this.id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getDescription() {
		return this.description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}
}
