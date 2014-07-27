package offensive.Server.Sessions.Game;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import offensive.Server.Exceptions.InvalidStateException;
import offensive.Server.Hybernate.POJO.CurrentGame;
import offensive.Server.Sessions.Session;

public class GameManager {
	public HashMap<Long, Game> allGames = new HashMap<>();
	
	public static GameManager onlyInstance = new GameManager();
	
	private GameManager(){};
	
	public void addGames(Session session) {
		for(CurrentGame pojoGame: session.user.getGames()) {
			this.addGame(pojoGame, session);
		}
	}
	
	public void addGame(CurrentGame pojoGame, Session session) {
		Game game = this.allGames.get(pojoGame.getId());
		
		if (game == null) {
			Game newGame = new Game(pojoGame, session);
			
			this.allGames.put(pojoGame.getId(), newGame);
		} else {
			game.activeSessions.add(session);
		}
	}
	
	public void removeGames(Session session) {
		for(CurrentGame pojoGame: session.user.getGames()) {
			Game game = this.allGames.get(pojoGame.getId());
			
			if(game != null) {
				game.activeSessions.remove(session);
				
				if(game.getRefCount() == 0) {
					this.allGames.remove(pojoGame.getId());
				}
			}
		}
	}
	
	public Collection<Session> getSessionsForGame(long gameId) {
		List<Session> response = new LinkedList<Session>();
		
		Game game = this.allGames.get(gameId);
		
		if(game != null) {
			response.addAll(game.activeSessions);
		}
		
		return response;
	}
	
	public CurrentGame getCurrentGame(long gameId) throws InvalidStateException {
		CurrentGame wantedGame = this.allGames.get(gameId).game;
		
		if (wantedGame == null) {
			throw new InvalidStateException("Game is not loaded in memory!!!");
		}
		
		return wantedGame;
	}
}
