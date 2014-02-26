package offensive.Server.Hybernate.POJO;

public class FacebookUser {
	private int id;
	private long facebookId;
	
	public FacebookUser(){};
	
	public FacebookUser(int id, long facebookId) {
		this.id = id;
		this.facebookId = facebookId;
	}
	
	public int getId() {
		return this.id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public long getFacebookId() {
		return this.facebookId;
	}
	
	public void setFacebookId(long facebookId) {
		this.facebookId = facebookId;
	}
}
