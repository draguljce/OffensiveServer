package offensive.Server.Sessions.Game;

import java.util.HashSet;

import offensive.Server.Hybernate.POJO.CurrentGame;
import offensive.Server.Sessions.Session;

public class Game {
	public CurrentGame game;
	
	public HashSet<Session> activeSessions = new HashSet<>();
	
	Game(){};
	
	Game(CurrentGame game, Session session) {
		this.game = game;
		this.activeSessions.add(session);
	}
	
	@Override
	public int hashCode() {
		return (int) this.game.getId();
	}
	
	@Override
	public boolean equals(Object other) {
		if(other == null) {
			return false;
		}
		
		if(other.getClass().equals(Game.class)) {
			Game otherGame = (Game)other;
			return this.game.equals(otherGame.game);
		}
		
		if(other.getClass().equals(CurrentGame.class)) {
			CurrentGame otherGame = (CurrentGame)other;
			return this.game.equals(otherGame); 
		}
		
		return false;
	}
	
	public int getRefCount() {
		return this.activeSessions.size();
	}
}
