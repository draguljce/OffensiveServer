package offensive.Server.Hybernate.POJO;

public class Invite {
	private int id;
	private User creator;
	private CurrentGame currentGame;
	
	public Invite() {};
	
	public int getId() {
		return this.id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public User getUser() {
		return this.creator;
	}
	
	public void setUser(User creator) {
		this.creator = creator;
	}
	
	public CurrentGame getGame() {
		return this.currentGame;
	}
	
	public void setGame(CurrentGame currentGame) {
		this.currentGame = currentGame;
	}
}
