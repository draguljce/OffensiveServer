package offensive.Server.Hybernate.POJO;

import java.util.List;

public class Player {
	private int id;
	private CurrentGame game;
	private User user;
	private Color color;
	private boolean isPlayedMove;
	private int numberOfReinforcements;
	private List<Card> cards;

	public Player() {};
	
	public Player(User user, CurrentGame game, Color color) {
		this.user = user;
		this.game = game;
		this.color = color;
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

	public List<Card> getCards() {
		return cards;
	}

	public void setCards(List<Card> cards) {
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

	@Override
	public boolean equals(Object other) {
		if(other == null || !other.getClass().equals(Player.class)) {
			return false;
		}
		
		if(this == other) {
			return true;
		}
		
		Player otherPlayer = (Player)other;
		
		if(this.id == otherPlayer.id) {
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return this.id;
	}
}
