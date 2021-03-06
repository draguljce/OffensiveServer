package offensive.Server.Hybernate.POJO;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import offensive.Server.Server;
import offensive.Server.Validator.CommandValidator;

public class CurrentGame {
	private long 			id;
	
	private String 			gameName;
	private short 			numberOfJoinedPlayers;
	private short 			numberOfPlayers;
	private Objective 		objective;
	private Phase 			phase;
	private Board 			board;
	private short 			currentRound;
	private boolean 		isOpen;
	private short			cardCounter;
	private Set<Player> 	players;
	private Set<Territory>	territories;
	private Set<Alliance>	alliances;
	private Set<Command>	commands;
	private Set<Invite>		invites;
	private Set<GameCard>	cards;
	
	private long version;
	
	public CommandValidator validator;

	public CurrentGame() {
		this.validator = new CommandValidator(this);
	};
	
	public CurrentGame(String name, int numberOfPlayers, Objective objective, boolean isOpen, Collection<Card> allCards) {
		this.gameName = name;
		this.numberOfPlayers = (short)numberOfPlayers;
		this.objective = objective;
		this.isOpen = isOpen;
		
		this.phase = new Phase(0);
		this.validator = new CommandValidator(this);
		this.numberOfJoinedPlayers = 1;
		
		this.cards = new HashSet<GameCard>();
		for(Card card :allCards) {
			this.cards.add(new GameCard(this, card));
		}
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
		this.validator.setPhase(phase);
	}

	public Board getBoard() {
		return board;
	}

	public void setBoard(Board board) {
		this.board = board;
	}

	public short getCurrentRound() {
		return currentRound;
	}

	public void setCurrentRound(short currentRound) {
		this.currentRound = currentRound;
	}

	public Set<Player> getPlayers() {
		if(this.players == null) {
			this.players = new HashSet<Player>();
		}
		
		return this.players;
	}

	public void setPlayers(Set<Player> players) {
		this.players = players;
	}

	public Set<Territory> getTerritories() {
		if(this.territories == null) {
			this.territories = new HashSet<Territory>();
		}
		
		return this.territories;
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
		Set<Command> commands = this.commands != null? new HashSet<Command>(this.commands) : new HashSet<Command>();
		commands.removeIf(command -> command.getRound() != this.currentRound);
		
		return commands;
	}
	
	public Set<Command> getMoveCommands() {
		Set<Command> moveCommands = this.commands != null? new HashSet<Command>(this.commands) : new HashSet<Command>();
		
		moveCommands.removeIf(command -> command.getType().getId() != 1 || command.getRound() != this.currentRound);
		
		return moveCommands;
	}
	
	public Collection<Command> getPendingCommands() {
		if(this.phase.getId() == Phases.Move.ordinal()) {
			return this.getMoveCommands();
		} else {
			return this.getCommands();
		}
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
	
	public void nextRound() {
		this.currentRound++;
	}
	
	public void joinPlayer() {
		this.numberOfJoinedPlayers++;
	}
	
	public boolean isAllplayersJoined() {
		return this.numberOfJoinedPlayers == this.numberOfPlayers;
	}
	
	public Set<Invite> getInvites() {
		if(this.invites == null) {
			this.invites = new HashSet<Invite>();
		}
		
		return invites;
	}

	public void setInvites(Set<Invite> invites) {
		this.invites = invites;
	}
	
	public Territory getTerritory(int fieldId) {
		for(Territory territory: this.getTerritories()) {
			if (territory.getField().getId() == fieldId) {
				return territory;
			}
		}
		
		return null;
	}
	
	@Override
	public boolean equals(Object other) {
		if(other == null) {
			return false;
		}
		
		if(!other.getClass().equals(this.getClass())) {
			return false;
		}
		
		CurrentGame otherGame = (CurrentGame)other;
		return this.id == otherGame.id;
	}

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}
	
	public Player getPlayer(User user) {
		for(Player player :this.players) {
			if(player.getUser() != null && player.getUser().getId() == user.getId()) {
				return player;
			}
		}
		
		return null;
	}
	
	public Set<GameCard> getCards() {
		return this.cards;
	}

	public void setCards(Set<GameCard> cards) {
		this.cards = cards;
	}
	
	public Collection<GameCard> getAvaiableCards() {
		Set<GameCard> allCards = new HashSet<GameCard>(this.cards);
		allCards.removeIf(card -> card.getMyRound() != this.cardCounter);
		
		if(allCards.size() == 0) {
			this.cardCounter++;
		}
		
		allCards = new HashSet<GameCard>(this.cards);
		allCards.removeIf(card -> card.getMyRound() != this.cardCounter);
		
		return allCards;
	}
	
	public short getCardCounter() {
		return this.cardCounter;
	}

	public void setCardCounter(short cardCounter) {
		this.cardCounter = cardCounter;
	}
	
	public GameCard getRandomGameCard() {
		return Server.getServer().rand.chooseRandomElement(this.getAvaiableCards());
	}
}
