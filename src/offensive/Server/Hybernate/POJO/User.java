package offensive.Server.Hybernate.POJO;

public class User {
	private int id;
	
	private UserType type;
	
	public User() {};
	
	public User(UserType type) {
		this.type = type;
	}
	
	public int getId() {
		return this.id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public UserType getType() {
		return this.type;
	}
	
	public void setType(UserType userType) {
		this.type = userType;
	}
}
