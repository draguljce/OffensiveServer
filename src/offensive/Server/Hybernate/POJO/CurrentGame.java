package offensive.Server.Hybernate.POJO;

import java.util.HashSet;
import java.util.Set;

public class CurrentGame {
	private long id;
	private String gameName;
	private short numberOfJoinedPlayers;
	private short numberOfPlayers;
	private Objective objective;
	private Phase phase;
	private Board board;
	private int currentRound;
	private boolean isOpen;
	private Set<Player> players;
	private Set<Territory> territories;
	private Set<Alliance> alliances;
	private Set<Command> commands;

	public CurrentGame() {};
	
	public CurrentGame(String name, int numberOfPlayers, Objective objective) {
		this.gameName = name;
		this.numberOfPlayers = (short)numberOfPlayers;
		this.objective = objective;
		
		this.phase = new Phase(0);
	};
	
	public long getId() {
		return this.id;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public String getGameName() {
		return gameName;
	}

	public void setGameName(String gameName) {
		this.gameName = gameName;
	}

	public short getNumberOfJoinedPlayers() {
		return numberOfJoinedPlayers;
	}

	public void setNumberOfJoinedPlayers(short numberOfJoinedPlayers) {
		this.numberOfJoinedPlayers = numberOfJoinedPlayers;
	}

	public short getNumberOfPlayers() {
		return numberOfPlayers;
	}

	public void setNumberOfPlayers(short numberOfPlayers) {
		this.numberOfPlayers = numberOfPlayers;
	}

	public Objective getObjective() {
		return objective;
	}

	public void setObjective(Objective objective) {
		this.objective = objective;
	}

	public Phase getPhase() {
		return phase;
	}

	public void setPhase(Phase phase) {
		this.phase = phase;
	}

	public Board getBoard() {
		return board;
	}

	public void setBoard(Board board) {
		this.board = board;
	}

	public int getCurrentRound() {
		return currentRound;
	}

	public void setCurrentRound(int currentRound) {
		this.currentRound = currentRound;
	}

	public Set<Player> getPlayers() {
		return this.players != null? this.players : new HashSet<Player>() ;
	}

	public void setPlayers(Set<Player> players) {
		this.players = players;
	}

	public Set<Territory> getTerritories() {
		return this.territories != null? this.territories : new HashSet<Territory>() ;
	}

	public void setTerritories(Set<Territory> territories) {
		this.territories = territories;
	}
	
	public Set<Alliance> getAlliances() {
		return this.alliances != null? this.alliances : new HashSet<Alliance>();
	}

	public void setAlliances(Set<Alliance> alliances) {
		this.alliances = alliances;
	}
	
	public Set<Command> getCommands() {
		return this.commands != null? this.commands : new HashSet<Command>();
	}

	public void setCommands(Set<Command> commands) {
		this.commands = commands;
	}
	
	public boolean getIsOpen() {
		return this.isOpen;
	}

	public void setIsOpen(boolean isOpen) {
		this.isOpen = isOpen;
	}
}
