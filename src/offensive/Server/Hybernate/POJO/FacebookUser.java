package offensive.Server.Hybernate.POJO;

public class FacebookUser {
	private long id;
	private long facebookId;
	
	public FacebookUser(){};
	
	public FacebookUser(long id, long facebookId) {
		this.id = id;
		this.facebookId = facebookId;
	}
	
	public long getId() {
		return this.id;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public long getFacebookId() {
		return this.facebookId;
	}
	
	public void setFacebookId(long facebookId) {
		this.facebookId = facebookId;
	}
}
