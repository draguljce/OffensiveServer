package offensive.Server.Hybernate.POJO;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class Player {
	private int id;
	private CurrentGame game;
	private User user;
	private Color color;
	private boolean isPlayedMove;
	private int numberOfReinforcements;
	private List<GameCard> cards;
	private Set<Territory> territories;
	
	private long version;
	
	public Player() {};
	
	public Player(User user, CurrentGame game, Color color) {
		this.user = user;
		this.game = game;
		this.color = color;
	}
	
	public Player(CurrentGame game, Color color, int numberOfTroops) {
		this.game = game;
		this.color = color;
		this.numberOfReinforcements = numberOfTroops;
	}
	
	public int getId() {
		return this.id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public User getUser() {
		return this.user;
	}
	
	public void setUser(User user) {
		this.user = user;
	}
	
	public Color getColor() {
		return this.color;
	}
	
	public void setColor(Color color) {
		this.color = color;
	}
	
	public boolean getIsPlayedMove() {
		return isPlayedMove;
	}

	public void setIsPlayedMove(boolean isPlayedMove) {
		this.isPlayedMove = isPlayedMove;
	}

	public int getNumberOfReinforcements() {
		return numberOfReinforcements;
	}

	public void setNumberOfReinforcements(int numberOfReinforcements) {
		this.numberOfReinforcements = numberOfReinforcements;
	}

	public List<GameCard> getCards() {
		if(this.cards == null) {
			this.cards = new LinkedList<GameCard>();
		}
		
		return cards;
	}

	public void setCards(List<GameCard> cards) {
		this.cards = cards;
	}
	
	public CurrentGame getGame() {
		return game;
	}

	public void setGame(CurrentGame game) {
		this.game = game;
	}
	
	public void decreaseNumberOfUnits() {
		this.numberOfReinforcements--;
	}

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}
	
	public Set<Territory> getTerritories() {
		return territories;
	}

	public void setTerritories(Set<Territory> territories) {
		this.territories = territories;
	}
	
	public void increaseReinforcements(int additionalReinforcements) {
		this.numberOfReinforcements += additionalReinforcements;
	}

}
