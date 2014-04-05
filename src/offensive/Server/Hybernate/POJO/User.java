package offensive.Server.Hybernate.POJO;

import java.util.HashSet;
import java.util.Set;

public class User {
	private long id;	
	private UserType type;
	private Set<Invite> invites;
	private Set<CurrentGame> games;
	private Set<Player> players;

	public User() {};
	
	public User(UserType type) {
		this.type = type;
	}
	
	public long getId() {
		return this.id;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public UserType getType() {
		return this.type;
	}
	
	public void setType(UserType userType) {
		this.type = userType;
	}
	
	public Set<Invite> getInvites() {
		return this.invites != null ? this.invites: new HashSet<Invite>() ;
	}

	public void setInvites(Set<Invite> invites) {
		this.invites = invites;
	}

	public Set<Player> getPlayers() {
		return this.players != null ? this.players : new HashSet<Player>();
	}

	public void setPlayers(Set<Player> players) {
		this.players = players;
	}

	public Set<CurrentGame> getGames() {
		return this.games != null ? this.games : new HashSet<CurrentGame>();
	}

	public void setGames(Set<CurrentGame> games) {
		this.games = games;
	}
}
