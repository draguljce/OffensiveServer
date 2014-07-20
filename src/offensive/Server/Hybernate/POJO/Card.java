package offensive.Server.Hybernate.POJO;

public class Card {
	private int id;
	private CardType type;
	private int player;
	
	private long version;
	
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
	
	@Override
	public boolean equals(Object other) {
		if(this == other) {
			return true;
		}
		
		Card otherCard = (Card)other;
		
		if(otherCard == null) {
			return false;
		}
		
		if(this.id == otherCard.id) {
			return true;
		} else {
			return false;
		}
	}

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}
}
