package offensive.Server.WorkerThreads;

import java.nio.channels.SelectionKey;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import offensive.Communicator.HandlerId;
import offensive.Communicator.ProtobuffMessage;
import offensive.Communicator.SendableMessage;
import offensive.Communicator.SerializationType;
import offensive.Server.Server;
import offensive.Server.Exceptions.InvalidStateException;
import offensive.Server.Exceptions.UserNotFoundException;
import offensive.Server.Hybernate.POJO.Board;
import offensive.Server.Hybernate.POJO.Card;
import offensive.Server.Hybernate.POJO.Color;
import offensive.Server.Hybernate.POJO.CommandType;
import offensive.Server.Hybernate.POJO.CompletedGameStatistics;
import offensive.Server.Hybernate.POJO.Continent;
import offensive.Server.Hybernate.POJO.Continents;
import offensive.Server.Hybernate.POJO.CurrentGame;
import offensive.Server.Hybernate.POJO.FacebookUser;
import offensive.Server.Hybernate.POJO.GameCard;
import offensive.Server.Hybernate.POJO.Invite;
import offensive.Server.Hybernate.POJO.Objective;
import offensive.Server.Hybernate.POJO.Phase;
import offensive.Server.Hybernate.POJO.Phases;
import offensive.Server.Hybernate.POJO.Territory;
import offensive.Server.Sessions.SessionManager;
import offensive.Server.Sessions.Game.GameManager;
import offensive.Server.Utilities.Constants;
import offensive.Server.Utilities.HibernateUtil;

import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.StaleStateException;
import org.hibernate.Transaction;

import com.google.protobuf.GeneratedMessage;

import communication.protos.CommunicationProtos.AddUnitRequest;
import communication.protos.CommunicationProtos.AddUnitResponse;
import communication.protos.CommunicationProtos.AdvancePhaseNotification;
import communication.protos.CommunicationProtos.AttackRequest;
import communication.protos.CommunicationProtos.AttackResponse;
import communication.protos.CommunicationProtos.CommandsSubmittedRequest;
import communication.protos.CommunicationProtos.CommandsSubmittedResponse;
import communication.protos.CommunicationProtos.CreateGameRequest;
import communication.protos.CommunicationProtos.CreateGameResponse;
import communication.protos.CommunicationProtos.FilterFriendsRequest;
import communication.protos.CommunicationProtos.FilterFriendsResponse;
import communication.protos.CommunicationProtos.GetOpenGamesRequest;
import communication.protos.CommunicationProtos.GetOpenGamesResponse;
import communication.protos.CommunicationProtos.GetUserDataRequest;
import communication.protos.CommunicationProtos.GetUserDataResponse;
import communication.protos.CommunicationProtos.InvokeAllianceRequest;
import communication.protos.CommunicationProtos.InvokeAllianceResponse;
import communication.protos.CommunicationProtos.JoinGameNotification;
import communication.protos.CommunicationProtos.JoinGameRequest;
import communication.protos.CommunicationProtos.JoinGameResponse;
import communication.protos.CommunicationProtos.MoveUnitsRequest;
import communication.protos.CommunicationProtos.MoveUnitsResponse;
import communication.protos.CommunicationProtos.PlayerCardCountNotification;
import communication.protos.CommunicationProtos.ReinforcementsNotification;
import communication.protos.CommunicationProtos.TradeCardsRequest;
import communication.protos.CommunicationProtos.TradeCardsResponse;
import communication.protos.DataProtos.Alliance;
import communication.protos.DataProtos.Command;
import communication.protos.DataProtos.GameContext;
import communication.protos.DataProtos.GameDescription;
import communication.protos.DataProtos.LightGameContext;
import communication.protos.DataProtos.Player;
import communication.protos.DataProtos.User;
import communication.protos.DataProtos.User.Builder;
import communication.protos.DataProtos.UserData;
import communication.protos.DataProtos.UserStatistics;

public class HandlerThread implements Runnable {

	private SelectionKey key;
	private offensive.Server.Sessions.Session session;
	
	private ProtobuffMessage request;
	
	private final int maxNumberOfTries = 5;
	
	public HandlerThread(SelectionKey key, ProtobuffMessage request) {
		this.key = key;
		this.session = (offensive.Server.Sessions.Session)key.attachment();
		this.request = request;
	}

	@Override
	public void run() {
		try {
			Server.getServer().logger.info("Started proccessing request");
			
			List<SendableMessage> response;
			
			response = this.proccessRequest(this.request);
			
			for(SendableMessage sendableMessage: response) {
				sendableMessage.send();
			}
		} catch (InvalidStateException invalidStateException) {
			Server.getServer().logger.error(invalidStateException.getMessage(), invalidStateException);
			SessionManager.onlyInstance.removeKey(this.key);
		} catch (Exception e) {
			Server.getServer().logger.error(e.getMessage(), e);
		} finally {
			Server.getServer().logger.info("Finished proccessing request.");
		}
	}
	
	private List<SendableMessage> proccessRequest(ProtobuffMessage request) throws Exception {
		
		
		int numberOfTries = 0;
		
		while (numberOfTries++ <= this.maxNumberOfTries) {
			Session session = Server.getServer().sessionFactory.openSession();
			List<SendableMessage> response = new LinkedList<>();
			
			try {
				switch(request.handlerId) {
				case GetUserDataRequest:
					this.proccessGetUserDataRequest((GetUserDataRequest)request.data, session, response);
					break;
					
				case FilterFriendsRequest:
					this.proccessFilterFriendsRequest((FilterFriendsRequest)request.data, session, response);
					break;
					
				case CreateGameRequest:
					this.proccessCreateGameRequest((CreateGameRequest)request.data, session, response);
					break;
					
				case GetOpenGamesRequest:
					this.proccessGetOpenGamesRequest((GetOpenGamesRequest)request.data, session, response);
					break;
					
				case JoinGameRequest:
					this.proccessJoinGameRequest((JoinGameRequest)request.data, session, response);
					break;
					
				case InvokeAllianceRequest:
					this.proccessInvokeAllianceRequest((InvokeAllianceRequest)request.data, session, response);
					break;
					
				case TradeCardsRequest:
					this.proccessTradeCardsRequest((TradeCardsRequest)request.data, session, response);
					break;
					
				case AddUnitRequest:
					this.proccessAddUnitRequest((AddUnitRequest)request.data, session, response);
					break;
					
				case MoveUnitsRequest:
					this.proccessCommandRequest(((MoveUnitsRequest)request.data).getGameId(), ((MoveUnitsRequest)request.data).getCommand(), session, response);
					break;
					
				case AttackRequest:
					this.proccessCommandRequest(((AttackRequest)request.data).getGameId(), ((AttackRequest)request.data).getCommand(), session, response);
					break;
					
				case CommandsSubmittedRequest:
					this.proccessCommandsSubmittedRequest((CommandsSubmittedRequest)request.data, session, response);
					break;
					
				default:
					throw new IllegalArgumentException(String.format("Illegal handler ID: %s!!!", request.handlerId));
				}
			}
			catch(StaleStateException e) {
				Server.getServer().logger.error(e.getMessage(), e);
				continue;
			}
			finally {
				session.close();
			}
			if(response.size() == 0) {
				throw new InvalidStateException("Handler did not return any message!!!");
			}
			
			response.get(0).message.handlerId = request.handlerId;
			response.get(0).message.ticketId = request.ticketId;
			response.get(0).message.serializationType = SerializationType.protobuff;
			return response;
		}
		
		throw new InvalidStateException(String.format("Request couldn't be proccessed after maximum number of attempts (%s)", this.maxNumberOfTries));
	}
	
	// Here are handlers definitions.
	/**
	 * @throws UserNotFoundException *****************************************************************************************************************************************************/
	private void proccessGetUserDataRequest(GetUserDataRequest request, Session session, List<SendableMessage> response) throws UserNotFoundException {

		offensive.Server.Hybernate.POJO.User user = (offensive.Server.Hybernate.POJO.User)session.get(offensive.Server.Hybernate.POJO.User.class, request.getUserId());
		SessionManager.onlyInstance.removeIfExist(user);
		this.session.user = user;
		
		GetUserDataResponse.Builder getUserDataResponseBuilder = GetUserDataResponse.newBuilder();
		
		getUserDataResponseBuilder.setUserData(this.getUserData(request.getUserId(), session));
		
		response.add(new SendableMessage(new ProtobuffMessage(getUserDataResponseBuilder.build()), this.session));
		
		GameManager.onlyInstance.addGames(this.session);
	}
	
	private void proccessFilterFriendsRequest(FilterFriendsRequest request, Session session, List<SendableMessage> response) {
		FilterFriendsResponse.Builder filterFriendsResponseBuilder = FilterFriendsResponse.newBuilder();
		
		Query query = session.createQuery("FROM FacebookUser fbUsers WHERE fbUsers.facebookId IN (:ids)");
		
		List<Long> idList = request.getFacebookIdsCount() != 0 ? request.getFacebookIdsList() : new LinkedList<Long>();
		
		if (idList.size() == 0) {
			idList.add(-1L);
		}
		
		query.setParameterList("ids", idList);
		
		@SuppressWarnings("unchecked")
		List<FacebookUser> filteredFriends = query.list();
		
		for(FacebookUser friend: filteredFriends) {
			filterFriendsResponseBuilder.addFriends(this.getUserFromPOJO(friend));
		}
		
		response.add(new SendableMessage(new ProtobuffMessage(filterFriendsResponseBuilder.build()), this.session));
	}
	
	@SuppressWarnings("unchecked")
	private void proccessCreateGameRequest(CreateGameRequest request, Session session, List<SendableMessage> response) throws UserNotFoundException {
		CreateGameResponse.Builder createGameResponseBuilder = CreateGameResponse.newBuilder();
		
		// Create game cards
		List<Card> allCards = (List<Card>) HibernateUtil.executeHql("FROM Card", session);
		
		offensive.Server.Hybernate.POJO.CurrentGame newGame = new CurrentGame(request.getGameName(), request.getNumberOfPlayers(), new Objective(request.getObjectiveCode()), request.getUserIdsCount() == 0, allCards);

		List<Color> allColors = (List<Color>)HibernateUtil.executeHql("FROM Color", session);
		
		Collection<Color> chosenColors = Server.getServer().rand.chooseRandomSubset(allColors, request.getNumberOfPlayers());
		
		Transaction tran = session.beginTransaction();
		
		try {
			int numberOfTroops = 50 - 5 * request.getNumberOfPlayers();
			boolean isFirst = true;
			for(Color color: chosenColors) {
				offensive.Server.Hybernate.POJO.Player newPlayer = new offensive.Server.Hybernate.POJO.Player(newGame, color, numberOfTroops);
				
				if(isFirst) {
					newPlayer.setUser(this.session.user);
					isFirst = false;
				}
				
				newGame.getPlayers().add(newPlayer);
			}
			
			for(long userId: request.getUserIdsList()){
				newGame.getInvites().add(new Invite(this.session.user, newGame, HibernateUtil.getPojoUser(userId, session)));
			}
			
			this.divideTerritories(newGame, session);
			
			session.save(newGame);
			
			GameManager.onlyInstance.addGame(newGame, this.session);
 			tran.commit();
		}
		catch(Exception e){
			Server.getServer().logger.error(e.getMessage(), e);
			tran.rollback();
			return;
		}
		
		createGameResponseBuilder.setGameContext(this.getGameContextFromPOJO(newGame, session));

		response.add(new SendableMessage(new ProtobuffMessage(createGameResponseBuilder.build()), this.session));
	}
	
	@SuppressWarnings("unchecked")
	private void proccessGetOpenGamesRequest (GetOpenGamesRequest request, Session session, List<SendableMessage> response) throws UserNotFoundException {
		Transaction tran = session.beginTransaction();
		GetOpenGamesResponse.Builder getOpenGamesResponseBuilder = GetOpenGamesResponse.newBuilder();
		
		List<CurrentGame> openedGames;
		
		try {
			// Give me all games where i haven't joined already and that are either open or i have invite.
			String sql = "SELECT * FROM currentgames game WHERE (game.numberOfJoinedPlayers < game.numberOfPlayers) AND (game.isopen = true OR game.id IN (SELECT game FROM invites WHERE inviteduser = :userId)) AND (game.id NOT IN (SELECT game FROM players player WHERE player.userId = :userId));";
			SQLQuery query = session.createSQLQuery(sql);
			query.setParameter("userId", this.session.user.getId());
			
			query.addEntity(CurrentGame.class);
			
			openedGames = query.list();
		} catch (Exception e) {
			Server.getServer().logger.error(e.getMessage(), e);
			return;
		} finally {
			tran.commit();
		}
		
		if (openedGames != null) {
			for(CurrentGame openGame: openedGames) {
				getOpenGamesResponseBuilder.addGameDescription(this.getGameDescriptionFromGame(openGame));
			}
		}
		
		response.add(new SendableMessage(new ProtobuffMessage(getOpenGamesResponseBuilder.build()), this.session));
	}
	
	private void proccessJoinGameRequest (JoinGameRequest request, Session session, List<SendableMessage> response) throws UserNotFoundException {
		Transaction tran = session.beginTransaction();
		JoinGameResponse.Builder responsebuilder = JoinGameResponse.newBuilder();
		offensive.Server.Hybernate.POJO.Player chosenPlayer;
		
		CurrentGame targetGame;
		try {
			targetGame = (CurrentGame)session.get(CurrentGame.class, request.getGameId());

			Collection<offensive.Server.Hybernate.POJO.Player> eligiblePlayers = new ArrayList<>(targetGame.getPlayers());
			eligiblePlayers.removeIf(player -> player.getUser() != null);
			
			chosenPlayer = Server.getServer().rand.chooseRandomElement(eligiblePlayers);
			
			chosenPlayer.setUser(this.session.user);
			
			targetGame.joinPlayer();
			
			if(targetGame.isAllplayersJoined()) {
				targetGame.setPhase(new Phase(Phases.Reinforcements.ordinal()));
			}
			
			session.update(targetGame);
			
			tran.commit();
			GameManager.onlyInstance.addGame(targetGame, this.session);
			responsebuilder.setGameContext(this.getGameContextFromPOJO(targetGame, session));
			
		} catch (Exception e) {
			Server.getServer().logger.error(e.getMessage(), e);
			tran.rollback();
			return;
		}
		
		response.add(new SendableMessage(new ProtobuffMessage(responsebuilder.build()), this.session));
		
		JoinGameNotification.Builder joinGameNotificationBuilder = JoinGameNotification.newBuilder();
		joinGameNotificationBuilder.setPlayer(this.getPlayerFromPOJO(chosenPlayer, session));
		joinGameNotificationBuilder.setGameId(request.getGameId());
		
		JoinGameNotification joinGameNotification = joinGameNotificationBuilder.build();
		
		// We should notify the rest of players of a new player.
		for(offensive.Server.Sessions.Session playerSession: GameManager.onlyInstance.getSessionsForGame(request.getGameId())) {
			if(playerSession != this.session) {
				response.add(new SendableMessage(new ProtobuffMessage(HandlerId.JoinGameNotification, 0, joinGameNotification), playerSession));
			}
		}
	}
	
	private void proccessInvokeAllianceRequest(InvokeAllianceRequest request, Session session, List<SendableMessage> response) {
		InvokeAllianceResponse.Builder responseBuilder = InvokeAllianceResponse.newBuilder();
		
		Transaction tran = session.beginTransaction();
		
		try {
			
		} catch (Exception e) {
			
		} finally {
			tran.commit();
		}
		
		response.add(new SendableMessage(new ProtobuffMessage(responseBuilder.build()), this.session));
	}
	
	private void proccessTradeCardsRequest(TradeCardsRequest request, Session session, List<SendableMessage> response) {
		TradeCardsResponse.Builder responseBuilder = TradeCardsResponse.newBuilder();
		
		short numberOfReinforcements = this.tradeCards(request.getCardId1(), request.getCardId2(), request.getCardId3());
		 
		Transaction tran = null;
		
		try {
			tran = session.beginTransaction();
			
			@SuppressWarnings("unchecked")
			List<offensive.Server.Hybernate.POJO.Player> players = (List<offensive.Server.Hybernate.POJO.Player>)HibernateUtil.executeHql(String.format("FROM Player player WHERE player.user.id = %s", this.session.user.getId()), session);
			
			offensive.Server.Hybernate.POJO.Player player = players.get(0);
			player.increaseReinforcements(numberOfReinforcements);
			
			Collection<GameCard> cardsToRemove = new ArrayList<GameCard>(3);
			if(numberOfReinforcements != 0) {
				for(GameCard gameCard :player.getCards()) {
					if(	gameCard.getCard().getField().getId() == request.getCardId1().getTerritoryId() ||
						gameCard.getCard().getField().getId() == request.getCardId2().getTerritoryId() ||
						gameCard.getCard().getField().getId() == request.getCardId3().getTerritoryId()) {
						
						if(gameCard.getGame().getTerritory(gameCard.getCard().getField().getId()).getPlayer().getUser() != null && gameCard.getGame().getTerritory(gameCard.getCard().getField().getId()).getPlayer().getUser().getId() == this.session.user.getId()) {
							gameCard.getGame().getTerritory(gameCard.getCard().getField().getId()).increaseNumberOfTroops((short)2);
						}
						cardsToRemove.add(gameCard);
					}
				}
				
				cardsToRemove.forEach(card -> player.getCards().remove(card));
			}
			
						
			PlayerCardCountNotification.Builder playerCardCountNotificationBuilder = PlayerCardCountNotification.newBuilder();
			
			playerCardCountNotificationBuilder.setCardCount(player.getCards().size());
			playerCardCountNotificationBuilder.setPlayerId(player.getId());
			playerCardCountNotificationBuilder.setGameId(request.getGameId());
			
			SendableMessage sendableMessage = new SendableMessage(new ProtobuffMessage(HandlerId.PlayerCardCountNotification, 0, playerCardCountNotificationBuilder.build()), GameManager.onlyInstance.getSessionsForGame(request.getGameId()));
			sendableMessage.send();
			
			session.update(player);

			responseBuilder.setNumberOfReinforcements(numberOfReinforcements);
			tran.commit();
		} catch (Exception e) {
			Server.getServer().logger.error(e.getMessage(), e);
			tran.rollback();
			return;
		}
		
		response.add(new SendableMessage(new ProtobuffMessage(responseBuilder.build()), this.session));
	}
	
	private void proccessAddUnitRequest(AddUnitRequest request, Session session, List<SendableMessage> response) {
		AddUnitResponse.Builder responseBuilder = AddUnitResponse.newBuilder();
		
		Transaction tran = session.beginTransaction();
		Server.getServer().logger.debug("AddUnit handler started transaction");
		
		try{
			offensive.Server.Hybernate.POJO.Player player = this.getPlayerForGame(request.getGameId(), session);
			offensive.Server.Hybernate.POJO.CurrentGame game = player.getGame();
			
			Server.getServer().logger.debug(String.format("Adding units in game:%s", game.getGameName()));
			if(game.getPhase().getId() != 0 && game.getPhase().getId() != 1) {
				return;
			}
			
			if(player.getNumberOfReinforcements() > 0 && !player.getIsPlayedMove()) { 
				offensive.Server.Hybernate.POJO.Territory territory = game.getTerritory(request.getTerritoryId());
				
				Server.getServer().logger.debug(String.format("Adding troop on territory:%s\nTroops before:%s\nTroops added before:%s", territory.getField().getName(), territory.getTroopsOnIt(), territory.getAddedTroops()));
				territory.addTroop();
				
				player.decreaseNumberOfUnits();
				Server.getServer().logger.debug(String.format("Player:%s has %s troops left", player.getId(), player.getNumberOfReinforcements()));

				session.update(territory);
				session.update(player);

				Server.getServer().logger.debug(String.format("Adding troop on territory:%s\nTroops after:%s\nTroops added after:%s", territory.getField().getName(), territory.getTroopsOnIt(), territory.getAddedTroops()));
				responseBuilder.setIsSuccessfull(true);
			} else {
				responseBuilder.setIsSuccessfull(false);
			}
			
			tran.commit();
			Server.getServer().logger.debug("AddUnit handler commited transaction");
		}
		catch (Exception e) {
			tran.rollback();
			Server.getServer().logger.debug("AddUnit handler rolled back transaction");
			throw e;
		}
		
		response.add(new SendableMessage(new ProtobuffMessage(responseBuilder.build()), this.session));
	}
	
	private void proccessCommandRequest(long gameId, Command request, Session session, List<SendableMessage> response) throws Exception {
		
		@SuppressWarnings("rawtypes")
		GeneratedMessage.Builder responseBuilder = null;
		
		offensive.Server.Hybernate.POJO.Command command = new offensive.Server.Hybernate.POJO.Command();

		Transaction tran = session.beginTransaction();

		try {
			CurrentGame game = (CurrentGame)session.get(CurrentGame.class, gameId);
			
			if(game.getPhase().getId() == Phases.Attack.ordinal()) {
				responseBuilder = AttackResponse.newBuilder();
				command.setType(new CommandType(0));
			} else if (game.getPhase().getId() == Phases.Move.ordinal()) {
				responseBuilder = MoveUnitsResponse.newBuilder();
				command.setType(new CommandType(1));
			}
			
			command.setGame(game);
			command.setPlayer(this.getPlayerForGame(gameId, session));
			command.setRound(game.getCurrentRound());
			command.setPhase(game.getPhase());
			
			offensive.Server.Hybernate.POJO.Territory sourceTerritory = game.getTerritory(request.getSourceTerritory());
			offensive.Server.Hybernate.POJO.Territory destinationTerritory = game.getTerritory(request.getDestinationTerritory());
			
			game.validator.validate(game.getPlayer(this.session.user), request);
			
			command.setSource(sourceTerritory);
			command.setDestination(destinationTerritory);
			command.setTroopNumber(request.getNumberOfUnits());
			
			game.getCommands().add(command);
			
			session.save(command);
			session.flush();
			
			tran.commit();
		} catch (Exception e) {
			Server.getServer().logger.error(e.getMessage(), e);
			tran.rollback();
			throw e;
		}
		
		response.add(new SendableMessage(new ProtobuffMessage((GeneratedMessage)responseBuilder.build()), this.session));
	}
	
	private void proccessCommandsSubmittedRequest(CommandsSubmittedRequest request, Session session, List<SendableMessage> response) {
		offensive.Server.Hybernate.POJO.Player player = this.getPlayerForGame(request.getGameId(), session);
		CommandsSubmittedResponse.Builder responseBuilder = CommandsSubmittedResponse.newBuilder(); 
		
		Transaction tran = session.beginTransaction();
		
		int nextPhase = 0;
		long gameId = 0;
		
		try {
			player.setIsPlayedMove(true);
			session.update(player);
			
			CurrentGame game = (CurrentGame)session.get(CurrentGame.class, request.getGameId());
			response.add(new SendableMessage(new ProtobuffMessage(responseBuilder.build()), this.session));
			if(this.shouldAdvanceToNextPhase(game)) {
				response.addAll(this.advanceToNextPhase(game, session));
			}
			
			nextPhase = game.getPhase().getId();
			
			if(nextPhase == Phases.Battle.ordinal()) {
				for(offensive.Server.Hybernate.POJO.Command command :game.getCommands()) {
					command.getSource().decreaseNumberOfTroops((short) command.getTroopNumber());
				}
			}

			gameId = game.getId();
			tran.commit();
		} catch(Exception e) {
			tran.rollback();
			Server.getServer().logger.error(e.getMessage(), e);
			return;
		} finally {
			if(nextPhase == 2) {
				SessionManager.onlyInstance.startBattle(gameId);
			}
		}

		return;
	}
	
	// Helper methods
	// ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	private UserData getUserData(long userId, Session session) throws UserNotFoundException {
		UserData.Builder userDataBuilder = UserData.newBuilder();
		
		User user = this.getUser(userId, session);
		userDataBuilder.setMe(user);
		userDataBuilder.setStatistics(this.getUserStatistics(user, session));
		userDataBuilder.addAllInvites(this.getInvites(user, session));
		userDataBuilder.addAllJoinedGames(this.getJoinedGames(user, session));

		return userDataBuilder.build();
	}
	
	private User getUser(long userId, Session session) throws UserNotFoundException {
		User user = null;

		offensive.Server.Hybernate.POJO.User userPOJO = (offensive.Server.Hybernate.POJO.User)session.get(offensive.Server.Hybernate.POJO.User.class, userId);
		
		user = this.getUserFromPOJO(userPOJO, session);
		
		if(user == null) {
			throw new UserNotFoundException(String.format("User with id = %s is not found", userId));
		}
		
		return user;
	}
	
	private UserStatistics getUserStatistics(User user, Session session) {
		if(user == null || !user.hasUserId()) {
			return null;
		}
		
		Transaction tran = session.beginTransaction();

		UserStatistics.Builder userStatisticsBuilder = UserStatistics.newBuilder();
		boolean successfullyBuilt = false;
		try {
			@SuppressWarnings("unchecked")
			List<CompletedGameStatistics> playerGameRecords = (List<CompletedGameStatistics>) HibernateUtil.executeHql(String.format("FROM CompletedGameStatistics completedGames WHERE completedGames.player.user.id = %s", user.getUserId()), session);
			
			userStatisticsBuilder.setNumberOfGames(playerGameRecords.size());
			
			int gamesWon = 0;
			
			for(CompletedGameStatistics completedGameStatistic: playerGameRecords) {
				if(completedGameStatistic.getRanking() == 1) {
					gamesWon++;
				}
			}
			
			userStatisticsBuilder.setNumberOfWins(gamesWon);
			successfullyBuilt = true;
		} finally {
			tran.rollback();
		}
		
		return successfullyBuilt ? userStatisticsBuilder.build() : null;
	}
	
	private List<GameDescription> getInvites(User user, Session session) throws UserNotFoundException {
		if(user == null || !user.hasUserId()) {
			return null;
		}
		List<GameDescription> invites = new LinkedList<GameDescription>();
		
		offensive.Server.Hybernate.POJO.User pojoUser = HibernateUtil.getPojoUser(user.getUserId(), session);
		
		if(pojoUser != null) {
			for(Invite invite: pojoUser.getInvites()) {
				invites.add(this.getGameDescriptionFromGame(invite.getGame()));
			}
		} else {
			throw new UserNotFoundException(String.format("User with id = %s is not found", user.getUserId()));
		}
			
		return invites;
	}
	
	private List<GameContext> getJoinedGames(User user, Session session) throws UserNotFoundException {
		if(user == null || !user.hasUserId()) {
			return null;
		}
		
		List<GameContext> joinedGames = new LinkedList<GameContext>();
		
		offensive.Server.Hybernate.POJO.User pojoUser = HibernateUtil.getPojoUser(user.getUserId(), session);
		
		for(CurrentGame game: pojoUser.getGames()) {
			// For each game that user plays, add game context to list.
			joinedGames.add(this.getGameContextFromPOJO(game, session));
		}	

		return joinedGames;
	}
	
	private GameDescription getGameDescriptionFromGame(CurrentGame game) {
		if(game == null) {
			return null;
		}
		
		GameDescription.Builder gameDescriptionBuilder = GameDescription.newBuilder();
		
		gameDescriptionBuilder.setGameId(game.getId());
		gameDescriptionBuilder.setGameName(game.getGameName());
		gameDescriptionBuilder.setNumberOfPlayers(game.getNumberOfPlayers());
		gameDescriptionBuilder.setNumberOfJoinedPlayers(game.getNumberOfJoinedPlayers());
		gameDescriptionBuilder.setObjective(game.getObjective().getId());
		
		return gameDescriptionBuilder.build();
	}
	
	private LightGameContext getLightGameContextFromGame(CurrentGame game, Session session) throws UserNotFoundException {
		if(game == null) {
			return null;
		}
		
		LightGameContext.Builder lightGameContextBuilder = LightGameContext.newBuilder();
		
		lightGameContextBuilder.setGameDescription(this.getGameDescriptionFromGame(game));
		lightGameContextBuilder.setRound(game.getCurrentRound());
		lightGameContextBuilder.setPhase(game.getPhase().getId());
		
		game.getPlayers().iterator();
		
		for(offensive.Server.Hybernate.POJO.Player player: game.getPlayers()) {
			lightGameContextBuilder.addPlayersInGame(this.getPlayerFromPOJO(player, session));
		}
	
		return lightGameContextBuilder.build();
	}
	
	private Player getPlayerFromPOJO(offensive.Server.Hybernate.POJO.Player player, Session session) throws UserNotFoundException {
		Player.Builder playerBuilder = Player.newBuilder();
		
		playerBuilder.setPlayerId(player.getId());
		playerBuilder.setColor(player.getColor().getId());
		playerBuilder.setIsPlayedMove(player.getIsPlayedMove());
		
		if(player.getUser() != null) {
			playerBuilder.setUser(this.getUser(player.getUser().getId(), session));
		}
		
		playerBuilder.setCardCount(player.getCards().size());
		
		playerBuilder.setNumberOfReinforcments(player.getNumberOfReinforcements());
		
		playerBuilder.setIsPlayedMove(player.getIsPlayedMove());
		
		return playerBuilder.build();
	}
		
	private Alliance getAllianceFromPOJO(offensive.Server.Hybernate.POJO.Alliance alliance) {
		Alliance.Builder allianceBuilder = Alliance.newBuilder();
		
		allianceBuilder.setUserId1(alliance.getPlayer1().getUser().getId());
		allianceBuilder.setUserId2(alliance.getPlayer2().getUser().getId());
		allianceBuilder.setType(alliance.getType().getId());
		
		return allianceBuilder.build();
	}
	
	private Command getCommandFromPOJO(offensive.Server.Hybernate.POJO.Command command) {
		Command.Builder commandBuilder = Command.newBuilder();
		
		commandBuilder.setCommandId(command.getType().getId());
		commandBuilder.setSourceTerritory(command.getSource().getField().getId());
		commandBuilder.setDestinationTerritory(command.getDestination().getField().getId());
		commandBuilder.setNumberOfUnits(command.getTroopNumber());
		
		return commandBuilder.build();
	}
	
	private User getUserFromPOJO(offensive.Server.Hybernate.POJO.User user, Session session) {
		Builder userBuilder = User.newBuilder();
		
		if(user != null) {
			userBuilder.setUserId(user.getId());
			
			if(user.getType().getName().equals(Constants.FacebookUserType)) {
				@SuppressWarnings("unchecked")
				List<FacebookUser> facebookUser = (List<FacebookUser>)HibernateUtil.executeHql(String.format("FROM FacebookUser fbUser WHERE fbUser.user.id = %s", user.getId()), session);
				
				userBuilder.setFacebookId(facebookUser.get(0).getFacebookId());
			}
		} else {
			return null;
		}
		
		return userBuilder.build();
	}
	
	private User getUserFromPOJO(offensive.Server.Hybernate.POJO.FacebookUser user) {
		Builder userBuilder = User.newBuilder();
		
		if(user != null) {
			userBuilder.setUserId(user.getUser().getId());
			
			userBuilder.setFacebookId(user.getFacebookId());
		} else {
			return null;
		}
		
		return userBuilder.build();
	}
	
	private GameContext getGameContextFromPOJO(offensive.Server.Hybernate.POJO.CurrentGame game, Session session) throws UserNotFoundException {
		GameContext.Builder gameContextBuilder = GameContext.newBuilder();
		
		gameContextBuilder.setLightGameContext(this.getLightGameContextFromGame(game, session));
		
		for(offensive.Server.Hybernate.POJO.Territory territory: game.getTerritories()) {
			gameContextBuilder.addTerritories(territory.toProtoTerritory(this.session.user));
		}
		
		for(offensive.Server.Hybernate.POJO.Alliance alliance: game.getAlliances()) {
			gameContextBuilder.addAlliances(this.getAllianceFromPOJO(alliance));
		}
		
		for(offensive.Server.Hybernate.POJO.Command command: game.getPendingCommands()) {
			if(command.getPlayer().getUser().equals(this.session.user)) {
				gameContextBuilder.addPendingComands(this.getCommandFromPOJO(command));
			}
		}
		
		for(offensive.Server.Hybernate.POJO.GameCard card: game.getPlayer(this.session.user).getCards()) {
			gameContextBuilder.addMyCards(card.getCard().toProtoCard());
		}
		
		return gameContextBuilder.build();
	}
	
	private short tradeCards(communication.protos.DataProtos.Card card, communication.protos.DataProtos.Card card2, communication.protos.DataProtos.Card card3) {
		if(card.getType() != card2.getType() && card.getType() != card3.getType() && card2.getType() != card3.getType()) {
			return 10;
		}
		
		if(card.getType() == card2.getType() && card2.getType() == card3.getType()) {
			switch (card.getType()) {
			case 0:
				return 4;
			
			case 1:
				return 6;
				
			case 2:
				return 8;

			default:
				break;
			}
		}
		
		return 0;
	}
	
	private offensive.Server.Hybernate.POJO.Player getPlayerForGame(long gameId, Session session) {
		return (offensive.Server.Hybernate.POJO.Player) HibernateUtil.executeScalarHql(String.format("FROM Player player WHERE player.game.id = %s AND player.user.id = %s", gameId, this.session.user.getId()), session);
	}
	
	private boolean shouldAdvanceToNextPhase(CurrentGame game) {
		boolean shouldAdvanceToNextPhase = true;
		
		for(offensive.Server.Hybernate.POJO.Player player: game.getPlayers()) {
			shouldAdvanceToNextPhase &= player.getIsPlayedMove();
		}
		
		return shouldAdvanceToNextPhase;
	}
	
	private Collection<SendableMessage> advanceToNextPhase(CurrentGame game, Session session) {
		Collection<SendableMessage> response = new ArrayList<SendableMessage>();
		Collection<SendableMessage> reinforcementsInfo = null;
		
		AdvancePhaseNotification.Builder advancePhaseNotificationBuilder = AdvancePhaseNotification.newBuilder();
		
		advancePhaseNotificationBuilder.setGameId(game.getId());
		
		Phase nextPhase = (Phase)session.get(Phase.class, game.getPhase().nextPhaseId());
		game.setPhase(nextPhase);
		
		if(nextPhase.getId() == Phases.Reinforcements.ordinal()) {
			// Execute commands.
			for(offensive.Server.Hybernate.POJO.Command command :game.getMoveCommands()) {
				command.getSource().decreaseNumberOfTroops((short)command.getTroopNumber());
				command.getDestination().increaseNumberOfTroops((short)command.getTroopNumber());
			}

			// Calculate reinforcements.
			reinforcementsInfo = this.addReinforcements(game, session);
			
			game.nextRound();
		} else if(nextPhase.getId() == Phases.Attack.ordinal()) {
			for(offensive.Server.Hybernate.POJO.Territory territory :game.getTerritories()) {
				territory.submitTroops();
			}
		}
		
		for(offensive.Server.Hybernate.POJO.Territory territory :game.getTerritories()) {
			advancePhaseNotificationBuilder.addTerritories(territory.toProtoTerritory(this.session.user));
		}
		
		for(offensive.Server.Hybernate.POJO.Player player: game.getPlayers()) {
			player.setIsPlayedMove(false);
		}
		
		session.update(game);
		
		response.add(new SendableMessage(new ProtobuffMessage(HandlerId.AdvancePhaseNotification, 0, advancePhaseNotificationBuilder.build()), GameManager.onlyInstance.getSessionsForGame(game.getId())));
		
		if(reinforcementsInfo != null) {
			response.addAll(reinforcementsInfo);
		}
		
		return response;
	}
	
	private void divideTerritories(CurrentGame game, Session session) {
		@SuppressWarnings("unchecked")
		List<offensive.Server.Hybernate.POJO.Field> fields = (List<offensive.Server.Hybernate.POJO.Field>)HibernateUtil.executeHql("FROM Field", session);
		
		Collections.shuffle(fields);
		
		int nextPlayer = 0;
		offensive.Server.Hybernate.POJO.Player[] players = game.getPlayers().toArray(new offensive.Server.Hybernate.POJO.Player[game.getPlayers().size()]);

		for(offensive.Server.Hybernate.POJO.Field field: fields) {
			game.getTerritories().add(new offensive.Server.Hybernate.POJO.Territory(game, field, players[nextPlayer]));
			
			players[nextPlayer].decreaseNumberOfUnits();
			nextPlayer = ++nextPlayer % players.length;
		}

		game.setBoard((Board)session.get(Board.class, 0));
	}
	
	private Collection<SendableMessage> addReinforcements(CurrentGame game, Session session) {
		Collection<SendableMessage> reinforcementsInfo = new ArrayList<SendableMessage>();
		
		// Check continent bonus.
		offensive.Server.Hybernate.POJO.Player[] playersByContinents = new offensive.Server.Hybernate.POJO.Player[Continents.values().length];
		int[] belongsToOnePlayer = new int[Continents.values().length];
		
		for(Territory territory :game.getTerritories()) {
			if(belongsToOnePlayer[territory.getField().getContinent().getId() - 1] != 2) {
				if(belongsToOnePlayer[territory.getField().getContinent().getId() - 1] == 0) {
					belongsToOnePlayer[territory.getField().getContinent().getId() - 1] = 1;
					playersByContinents[territory.getField().getContinent().getId() - 1] = territory.getPlayer();
				} else if (belongsToOnePlayer[territory.getField().getContinent().getId() -1] == 1) {
					if (playersByContinents[territory.getField().getContinent().getId() -1].getId() != territory.getPlayer().getId()) {
						belongsToOnePlayer[territory.getField().getContinent().getId() -1] = 2;
					}
				}
			}
		}
		
		for(int i = 0; i < belongsToOnePlayer.length; i++) {
			if(belongsToOnePlayer[i] == 1) {
				playersByContinents[i].setNumberOfReinforcements(((Continent)session.get(Continent.class, i + 1)).getBonus());
			}
		}
		
		for(offensive.Server.Hybernate.POJO.Player player :game.getPlayers()) {
			player.increaseReinforcements(Math.max(player.getTerritories().size() / 3, 2));
			
			ReinforcementsNotification.Builder reinforcementsNotificationBuilder = ReinforcementsNotification.newBuilder();
			reinforcementsNotificationBuilder.setGameId(game.getId());
			reinforcementsNotificationBuilder.setNumberOfReinforcements(player.getNumberOfReinforcements());
			
			reinforcementsInfo.add(new SendableMessage(new ProtobuffMessage(HandlerId.ReinforcementsNotification, 0, reinforcementsNotificationBuilder.build()), GameManager.onlyInstance.getSessionForPlayer(player)));
		}
		
		return reinforcementsInfo;
	}
}