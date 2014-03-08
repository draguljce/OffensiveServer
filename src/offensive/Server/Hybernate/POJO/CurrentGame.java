package offensive.Server.Hybernate.POJO;

public class CurrentGame {
	private int id;
	private String gameName;
	private short numberOfJoinedPlayers;
	private short numberOfPlayers;
	private Objective objective;
	private Phase phase;
	private Board board;
	private int currentRound;
	
	public CurrentGame() {};
	
	public int getId() {
		return this.id;
	}
	
	public void setId(int id) {
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
}
