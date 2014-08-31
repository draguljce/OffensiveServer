package offensive.Server.Hybernate.POJO;

import communication.protos.DataProtos;

public class GameCard {
	private	int 		id;
	private	CurrentGame	game;
	private	Player 		player;
	private	Card		card;
	private	short		myRound;

	private	long 		version;
	
	
	public GameCard(){}
	
	public GameCard(CurrentGame game, Card card) {
		this.game = game;
		this.card = card;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public Player getPlayer() {
		return player;
	}
	public void setPlayer(Player player) {
		this.player = player;
	}
	public Card getCard() {
		return card;
	}
	public void setCard(Card card) {
		this.card = card;
	}
	public long getVersion() {
		return version;
	}
	public void setVersion(long version) {
		this.version = version;
	}

	public CurrentGame getGame() {
		return game;
	}
	public void setGame(CurrentGame game) {
		this.game = game;
	}
	
	public short getMyRound() {
		return myRound;
	}

	public void setMyRound(short myRound) {
		this.myRound = myRound;
	}
	
	public void nextRound() {
		this.myRound++;
	}
	
	public DataProtos.Card toProtoCard() {
		DataProtos.Card.Builder protoCardBuilder = DataProtos.Card.newBuilder();
		
		protoCardBuilder.setTerritoryId(this.card.getField().getId());
		protoCardBuilder.setType(this.card.getType().getId());
		
		return protoCardBuilder.build();
	}
}
