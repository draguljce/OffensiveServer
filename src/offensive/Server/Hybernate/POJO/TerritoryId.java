package offensive.Server.Hybernate.POJO;

import java.io.Serializable;

public class TerritoryId implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -1529411444146887773L;
	private CurrentGame game;
	private Field field;
	
	public TerritoryId() {};

	public CurrentGame getGame() {
		return game;
	}

	public void setGame(CurrentGame game) {
		this.game = game;
	}

	public Field getField() {
		return field;
	}

	public void setField(Field field) {
		this.field = field;
	};
	
	@Override
	public boolean equals(Object other) {
		if(other == null || !other.getClass().equals(TerritoryId.class)) {
			return false;
		}
		
		if(this == other) {
			return true;
		}
		
		TerritoryId otherTerritoryId = (TerritoryId)other;
		
		if(this.game.equals(otherTerritoryId.game) && this.field.equals(otherTerritoryId.field)) {
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return this.game.hashCode() ^ this.field.hashCode();
	}
}
