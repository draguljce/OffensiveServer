package offensive.Server.Hybernate.POJO;

public class InviteRecord {
	private Invite invite;
	private User invitedUser;
	
	public InviteRecord() {}

	public Invite getInvite() {
		return invite;
	}

	public void setInvite(Invite invite) {
		this.invite = invite;
	}

	public User getInvitedUser() {
		return invitedUser;
	}

	public void setInvitedUser(User invitedUser) {
		this.invitedUser = invitedUser;
	};
	
	
}
