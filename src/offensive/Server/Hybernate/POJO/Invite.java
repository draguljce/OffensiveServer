package offensive.Server.Hybernate.POJO;

public class Invite {
	private int id;
	private User creator;
	private CurrentGame game;
	private User invitedUser;
	
	public Invite() {}
	
	public Invite(User creator, CurrentGame game, User invitedUser) {
		this.creator = creator;
		this.game = game;
		this.invitedUser = invitedUser;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public User getCreator() {
		return creator;
	}

	public void setCreator(User creator) {
		this.creator = creator;
	}

	public CurrentGame getGame() {
		return game;
	}

	public void setGame(CurrentGame game) {
		this.game = game;
	}

	public User getInvitedUser() {
		return invitedUser;
	}

	public void setInvitedUser(User invitedUser) {
		this.invitedUser = invitedUser;
	}
}
