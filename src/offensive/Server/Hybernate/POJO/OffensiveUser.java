package offensive.Server.Hybernate.POJO;

public class OffensiveUser {
	private long id;
	private String userName;
	private String password;
	
	public OffensiveUser(){};
	
	public OffensiveUser(long id, String userName, String password) {
		this.id = id;
		this.userName = userName;
		this.password = password;
	}
	
	public long getId() {
		return this.id;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public String getUserName() {
		return this.userName;
	}
	
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	public String getPassword() {
		return this.password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
}
