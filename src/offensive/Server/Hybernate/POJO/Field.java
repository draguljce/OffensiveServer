package offensive.Server.Hybernate.POJO;

public class Field {
	private int id;
	private String name;
	
	private long version;
	
	public Field() {}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	};
}
