package offensive.Server.Hybernate.POJO;

public class Card {
	private int id;
	private CardType type;
	private int player;
	
	public Card() {}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public CardType getType() {
		return type;
	}

	public void setType(CardType type) {
		this.type = type;
	}

	public int getPlayer() {
		return player;
	}

	public void setPlayer(int player) {
		this.player = player;
	};
}
