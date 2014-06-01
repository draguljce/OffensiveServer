package offensive.Server.Hybernate.POJO;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import communication.protos.DataProtos;

public class User {
	private long id;	
	private UserType type;
	private Set<Invite> invites;
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

	public List<CurrentGame> getGames() {
		List<CurrentGame> allGames = new LinkedList<CurrentGame>();
		for(Player player: this.getPlayers()) {
			allGames.add(player.getGame());
		}
		
		return allGames;
	}
	
	public DataProtos.User toProtoUser() {
		DataProtos.User.Builder userBuilder = DataProtos.User.newBuilder();
		
		userBuilder.setUserId(this.id);
		
		return userBuilder.build();
	}
	
	@Override
	public int hashCode() {
		return (int) this.id;
	}
	
	@Override
	public boolean equals(Object other) {
		if(other == null) {
			return false;
		}
		
		if(this == other) {
			return true;
		}
		
		if(this.getClass().equals(other.getClass())) {
			User otherUser = (User)other;
			
			return this.id == otherUser.id;
		}
		
		return false;
	}
}
