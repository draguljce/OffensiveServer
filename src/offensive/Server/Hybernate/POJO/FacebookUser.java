package offensive.Server.Hybernate.POJO;

public class FacebookUser {
	private long facebookId;
	private User user;
	
	private long version;
	
	public FacebookUser(){};
	
	public FacebookUser(long facebookId, User userId) {
		this.facebookId = facebookId;
		this.user = userId;
	}
	
	public User getUser() {
		return this.user;
	}
	
	public void setUser(User userId) {
		this.user = userId;
	}
	
	public long getFacebookId() {
		return this.facebookId;
	}
	
	public void setFacebookId(long facebookId) {
		this.facebookId = facebookId;
	}

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}
}
