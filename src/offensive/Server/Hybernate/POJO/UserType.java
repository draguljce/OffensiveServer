package offensive.Server.Hybernate.POJO;

public class UserType {
	private String name;
	
	private long version;
	
	public UserType() {};
	
	public UserType(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}
}