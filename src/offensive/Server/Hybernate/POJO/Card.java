package offensive.Server.Hybernate.POJO;

import communication.protos.DataProtos;

public class Card {
	private int id;
	private CardType type;
	private Field field;
	
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

	public Field getField() {
		return this.field;
	}

	public void setField(Field field) {
		this.field = field;
	};
	
	public DataProtos.Card toProtoCard() {
		DataProtos.Card.Builder cardBuilder = DataProtos.Card.newBuilder();
		
		cardBuilder.setTerritoryId(this.field.getId());
		cardBuilder.setType(this.type.getId());
		
		return cardBuilder.build();
	}
	
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
}
