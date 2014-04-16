package offensive.Server.WorkerThreads;

import java.util.LinkedList;
import java.util.List;

import offensive.Communicator.Communicator;
import offensive.Communicator.Message;
import offensive.Communicator.ProtobuffMessage;
import offensive.Server.Server;
import offensive.Server.Exceptions.UserNotFoundException;
import offensive.Server.Hybernate.POJO.Card;
import offensive.Server.Hybernate.POJO.Color;
import offensive.Server.Hybernate.POJO.CompletedGameStatistics;
import offensive.Server.Hybernate.POJO.CurrentGame;
import offensive.Server.Hybernate.POJO.FacebookUser;
import offensive.Server.Hybernate.POJO.Invite;
import offensive.Server.Hybernate.POJO.Objective;
import offensive.Server.Utilities.CommonRandom;
import offensive.Server.Utilities.Constants;
import offensive.Server.Utilities.HibernateUtil;

import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.UnknownFieldSet.Field;

import communication.protos.CommunicationProtos.AddUnitRequest;
import communication.protos.CommunicationProtos.AddUnitResponse;
import communication.protos.CommunicationProtos.AttackRequest;
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
import communication.protos.CommunicationProtos.JoinGameRequest;
import communication.protos.CommunicationProtos.JoinGameResponse;
import communication.protos.CommunicationProtos.MoveUnitsRequest;
import communication.protos.CommunicationProtos.RollDiceRequest;
import communication.protos.CommunicationProtos.RollDiceResponse;
import communication.protos.CommunicationProtos.TradeCardsRequest;
import communication.protos.CommunicationProtos.TradeCardsResponse;
import communication.protos.DataProtos.Alliance;
import communication.protos.DataProtos.Command;
import communication.protos.DataProtos.GameContext;
import communication.protos.DataProtos.GameDescription;
import communication.protos.DataProtos.LightGameContext;
import communication.protos.DataProtos.Player;
import communication.protos.DataProtos.Territory;
import communication.protos.DataProtos.User;
import communication.protos.DataProtos.User.Builder;
import communication.protos.DataProtos.UserData;
import communication.protos.DataProtos.UserStatistics;

public class HandlerThread implements Runnable {

	private offensive.Server.Sessions.Session session;
	
	private ProtobuffMessage request;
	
	public HandlerThread(offensive.Server.Sessions.Session session, ProtobuffMessage request) {
		this.session = session;
		this.request = request;
	}

	@Override
	public void run() {
		try {
			Server.getServer().logger.info("Started proccessing request");
			
			Message response;
			
			response = this.proccessRequest(this.request);
			
			Communicator.getCommunicator().sendMessage(response, this.session.socketChannel);
			
		} catch (Exception e) {
			Server.getServer().logger.error(e.getMessage(), e);
		} finally {
			Server.getServer().logger.info("Finished proccessing request.");
		}
	}
	
	private Message proccessRequest(ProtobuffMessage request) throws UserNotFoundException {
		ProtobuffMessage response = new ProtobuffMessage(request.handlerId, request.ticketId);
		Session session = Server.getServer().sessionFactory.openSession();
		
		try {
			switch(request.handlerId) {
			case GetUserDataRequest:
				response.data = this.proccessGetUserDataRequest((GetUserDataRequest)request.data, session);
				break;
				
			case FilterFriendsRequest:
				response.data = this.proccessFilterFriendsRequest((FilterFriendsRequest)request.data, session);
				break;
				
			case CreateGameRequest:
				response.data = this.proccessCreateGameRequest((CreateGameRequest)request.data, session);
				break;
				
			case GetOpenGamesRequest:
				response.data = this.proccessGetOpenGamesRequest((GetOpenGamesRequest)request.data, session);
				break;
				
			case JoinGameRequest:
				response.data = this.proccessJoinGameRequest((JoinGameRequest)request.data, session);
				break;
				
			case InvokeAllianceRequest:
				response.data = this.proccessInvokeAllianceRequest((InvokeAllianceRequest)request.data, session);
				break;
				
			case TradeCardsRequest:
				response.data = this.proccessTradeCardsRequest((TradeCardsRequest)request.data, session);
				break;
				
			case AddUnitRequest:
				response.data = this.proccessAddUnitRequest((AddUnitRequest)request.data, session);
				break;
				
			case MoveUnitsRequest:
				response.data = this.proccessCommandRequest(((MoveUnitsRequest)request.data).getGameId(), ((MoveUnitsRequest)request.data).getCommand(), session);
				break;
				
			case AttackRequest:
				response.data = this.proccessCommandRequest(((AttackRequest)request.data).getGameId(), ((AttackRequest)request.data).getCommand(), session);
				break;
				
			case RollDiceRequest:
				response.data = this.proccessRollDiceRequest((RollDiceRequest)request.data, session);
				break;
				
			default:
				throw new IllegalArgumentException(String.format("Illegal handler ID: %s!!!", request.handlerId));
			}
		}
		finally {
			session.close();
		}
		
		return response;
	}
	
	// Here are handlers definitions.
	/**
	 * @throws UserNotFoundException *****************************************************************************************************************************************************/
	private GeneratedMessage proccessGetUserDataRequest(GetUserDataRequest request, Session session) throws UserNotFoundException {

		this.session.user = (offensive.Server.Hybernate.POJO.User)session.get(offensive.Server.Hybernate.POJO.User.class, request.getUserId());
		GetUserDataResponse.Builder getUserDataResponseBuilder = GetUserDataResponse.newBuilder();
		
		getUserDataResponseBuilder.setUserData(this.getUserData(request.getUserId(), session));
		
		return getUserDataResponseBuilder.build();
	}
	
	private GeneratedMessage proccessFilterFriendsRequest(FilterFriendsRequest request, Session session) {
		FilterFriendsResponse.Builder filterFriendsResponseBuilder = FilterFriendsResponse.newBuilder();
		
		StringBuilder conditionBuilder = new StringBuilder();
		
		for(long facebookId: request.getFacebookIdsList()) {
			conditionBuilder.append(String.format("fbUsers.facebookId = %s", facebookId)).append(" OR ");
		}
		
		// Remove last "OR"
		conditionBuilder.delete(conditionBuilder.length() - 4, conditionBuilder.length());
		
		@SuppressWarnings("unchecked")
		List<FacebookUser> filteredFriends = (List<FacebookUser>) HibernateUtil.executeHql(String.format("FROM FacebookUser fbUsers WHERE %s", conditionBuilder.toString()), session);
		
		for(FacebookUser friend: filteredFriends) {
			filterFriendsResponseBuilder.addFriends(this.getUserFromPOJO(friend));
		}
		
		return filterFriendsResponseBuilder.build();
	}
	
	private GeneratedMessage proccessCreateGameRequest(CreateGameRequest request, Session session) throws UserNotFoundException {
		CreateGameResponse.Builder createGameResponseBuilder = CreateGameResponse.newBuilder();
		
		offensive.Server.Hybernate.POJO.CurrentGame newGame = new CurrentGame(request.getGameName(), request.getNumberOfPlayers(), new Objective(request.getObjectiveCode()), request.getUserIdsCount() == 0);

		@SuppressWarnings("unchecked")
		List<Color> allColors = (List<Color>)HibernateUtil.executeHql("FROM Color", session);
		
		Color chosenColor = allColors.get(CommonRandom.next(allColors.size()));
		
		offensive.Server.Hybernate.POJO.Player newPlayer = new offensive.Server.Hybernate.POJO.Player(this.session.user, newGame, chosenColor);
		
		List<Invite> invites = new LinkedList<Invite>();
		
		Transaction tran = session.beginTransaction();
		
		try{
			session.save(newGame);
			session.save(newPlayer);
			tran.commit();
		}
		catch(Exception e){
			Server.getServer().logger.error(e.getMessage(), e);
			tran.rollback();
			return null;
		}
		
		for(long userId: request.getUserIdsList()){
			invites.add(new Invite(this.session.user, newGame, HibernateUtil.getPojoUser(userId, session)));
		}
		
		tran = session.beginTransaction();
		try{
			for(Invite invite: invites) {
				session.save(invite);
			}
			
			tran.commit();
		}
		catch(Exception e){
			Server.getServer().logger.error(e.getMessage(), e);
			tran.rollback();
			return null;
		}
		
		createGameResponseBuilder.setGameContext(this.getGameContextFromPOJO(newGame, session));

		return createGameResponseBuilder.build();
	}
	
	@SuppressWarnings("unchecked")
	private GeneratedMessage proccessGetOpenGamesRequest (GetOpenGamesRequest request, Session session) throws UserNotFoundException {
		Transaction tran = session.beginTransaction();
		GetOpenGamesResponse.Builder getOpenGamesResponseBuilder = GetOpenGamesResponse.newBuilder();
		
		List<CurrentGame> openedGames;
		
		try {
			// Give me all games where i haven't joined already and that are either open or i have invite.
			String sql = "SELECT * FROM currentgames game WHERE (game.isopen = true OR game.id IN (SELECT game FROM invites WHERE inviteduser = :userId)) AND (game.id NOT IN (SELECT game FROM players player WHERE player.userId = :userId));";
			SQLQuery query = session.createSQLQuery(sql);
			query.setParameter("userId", this.session.user.getId());
			
			query.addEntity(CurrentGame.class);
			
			openedGames = query.list();
		} catch (Exception e) {
			Server.getServer().logger.error(e.getMessage(), e);
			return null;
		} finally {
			tran.commit();
		}
		
		if (openedGames != null) {
			for(CurrentGame openGame: openedGames) {
				getOpenGamesResponseBuilder.addGameDescription(this.getGameDescriptionFromGame(openGame));
			}
		}
		
		return getOpenGamesResponseBuilder.build();
	}
	
	private GeneratedMessage proccessJoinGameRequest (JoinGameRequest request, Session session) {
		Transaction tran = session.beginTransaction();
		JoinGameResponse.Builder responsebuilder = JoinGameResponse.newBuilder();
		
		CurrentGame targetGame;
		try {
			targetGame = (CurrentGame)session.get(CurrentGame.class, request.getGameId());
			
			@SuppressWarnings("unchecked")
			List<Color> allColors = (List<Color>)HibernateUtil.executeHql("FROM Color", session);
			
			for(offensive.Server.Hybernate.POJO.Player player: targetGame.getPlayers()) {
				allColors.remove(player.getColor());
			}
			
			Color chosenColor = allColors.get(CommonRandom.next(allColors.size()));
			
			offensive.Server.Hybernate.POJO.Player newPlayer = new offensive.Server.Hybernate.POJO.Player(this.session.user, targetGame, chosenColor);
			
			session.save(newPlayer);
			
			responsebuilder.setGameContext(this.getGameContextFromPOJO(targetGame, session));
			
		} catch (Exception e) {
			Server.getServer().logger.error(e.getMessage(), e);
		} finally {
			tran.commit();
		}
		
		return responsebuilder.build();
	}
	
	private GeneratedMessage proccessInvokeAllianceRequest(InvokeAllianceRequest request, Session session) {
		InvokeAllianceResponse.Builder responseBuilder = InvokeAllianceResponse.newBuilder();
		
		Transaction tran = session.beginTransaction();
		
		try {
			
		} catch (Exception e) {
			
		} finally {
			tran.commit();
		}
		
		return responseBuilder.build();
	}
	
	private GeneratedMessage proccessTradeCardsRequest(TradeCardsRequest request, Session session) {
		TradeCardsResponse.Builder responseBuilder = TradeCardsResponse.newBuilder();
		
		short numberOfReinforcements = this.tradeCards(request.getCardId1(), request.getCardId2(), request.getCardId3());
		 
		Transaction tran = session.beginTransaction();
		
		try {
			@SuppressWarnings("unchecked")
			List<offensive.Server.Hybernate.POJO.Player> players = (List<offensive.Server.Hybernate.POJO.Player>)HibernateUtil.executeHql(String.format("FROM Player player WHERE player.id = %s AND player.game.id = %s", this.session.user.getId(), request.getGameId()), session);
			
			offensive.Server.Hybernate.POJO.Player player = players.get(0);
			player.setNumberOfReinforcements(player.getNumberOfReinforcements() + numberOfReinforcements);
			
			List<Integer> cardsToRemove = new LinkedList<Integer>();
			cardsToRemove.add(request.getCardId1());
			cardsToRemove.add(request.getCardId2());
			cardsToRemove.add(request.getCardId3());
			
			for(Card card: player.getCards()) {
				if(cardsToRemove.remove(new Integer(card.getType().getId()))) {
					session.delete(card);
				}
				
				if(cardsToRemove.size() == 0) {
					break;
				}
			}
			
			session.update(player);

			responseBuilder.setNumberOfReinforcements(numberOfReinforcements);
		} catch (Exception e) {
			Server.getServer().logger.error(e.getMessage(), e);
			tran.rollback();
			return null;
		} finally {
			tran.commit();
		}
		
		return responseBuilder.build();
	}
	private GeneratedMessage proccessAddUnitRequest(AddUnitRequest request, Session session) {
		AddUnitResponse.Builder responseBuilder = AddUnitResponse.newBuilder();
		
		Transaction tran = session.beginTransaction();
		
		try{
			offensive.Server.Hybernate.POJO.Player player = this.getPlayerForGame(request.getGameId(), session);
			
			if(player.getNumberOfReinforcements() > 0) { 
				offensive.Server.Hybernate.POJO.Territory territory = (offensive.Server.Hybernate.POJO.Territory) HibernateUtil.executeScalarHql(String.format("FROM Territory territory WHERE territory.field = %s", request.getTerritoryId()), session);
				
				territory.incrementNumberOfTroops();
				player.decreaseNumberOfUnits();
				
				session.update(territory);
				session.update(player);
				
				responseBuilder.setIsSuccessfull(true);
			} else {
				responseBuilder.setIsSuccessfull(false);
			}
		} catch (Exception e) {
			Server.getServer().logger.error(e.getMessage(), e);
			tran.rollback();
			return null;
		} finally {
			tran.commit();
		}
		
		return responseBuilder.build();
	}
	
	private GeneratedMessage proccessCommandRequest(long gameId, Command request, Session session) {
		MoveUnitsRequest.Builder responseBuilder = MoveUnitsRequest.newBuilder();

		offensive.Server.Hybernate.POJO.Command command = new offensive.Server.Hybernate.POJO.Command();

		Transaction tran = session.beginTransaction();

		try {
			CurrentGame game = (CurrentGame)session.get(CurrentGame.class, gameId);
			
			command.setGame(game);
			command.setPlayer(this.getPlayerForGame(gameId, session));
			command.setRound(game.getCurrentRound());
			command.setPhase(game.getPhase());
			command.setSource((offensive.Server.Hybernate.POJO.Field)session.get(Field.class, request.getSourceTerritory()));
			command.setSource((offensive.Server.Hybernate.POJO.Field)session.get(Field.class, request.getDestinationTerritory()));
			command.setTroopNumber(request.getNumberOfUnits());
			
			session.save(command);
		} catch (Exception e) {
			Server.getServer().logger.error(e.getMessage(), e);
			tran.rollback();
			return null;
		} finally {
			tran.commit();
		}
		
		return responseBuilder.build();
	}
	
	private GeneratedMessage proccessRollDiceRequest(RollDiceRequest reqest, Session session) {
		RollDiceResponse.Builder responseBuilder = RollDiceResponse.newBuilder();
		
		responseBuilder.setNumber(CommonRandom.next(6) + 1);
		
		return responseBuilder.build();
		
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
		Transaction tran = session.beginTransaction();
		
		User user = null;

		try {
			offensive.Server.Hybernate.POJO.User userPOJO = (offensive.Server.Hybernate.POJO.User)session.get(offensive.Server.Hybernate.POJO.User.class, userId);
			
			user = this.getUserFromPOJO(userPOJO, session);
			
			if(user == null) {
				throw new UserNotFoundException(String.format("User with id = %s is not found", userId));
			}
		} finally {
			tran.rollback();
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
		
		playerBuilder.setUser(this.getUser(player.getUser().getId(), session));
		playerBuilder.setColor(player.getColor().getId());
		playerBuilder.setIsPlayedMove(player.getIsPlayedMove());
		
		for(Card card: player.getCards()){
			playerBuilder.addCards(card.getId());
		}
		
		playerBuilder.setNumberOfReinforcments(player.getNumberOfReinforcements());
		
		return playerBuilder.build();
	}
	
	private Territory getTerritoryFromPOJO(offensive.Server.Hybernate.POJO.Territory territory) {
		Territory.Builder territoryBuilder = Territory.newBuilder();
		
		territoryBuilder.setId(territory.getField().getId());
		territoryBuilder.setTroopsOnIt(territory.getTroopsOnIt());
		territoryBuilder.setUserId(territory.getPlayer().getUser().getId());
		
		return territoryBuilder.build();
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
		commandBuilder.setSourceTerritory(command.getSource().getId());
		commandBuilder.setDestinationTerritory(command.getDestination().getId());
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
			gameContextBuilder.addTerritories(this.getTerritoryFromPOJO(territory));
		}
		
		for(offensive.Server.Hybernate.POJO.Alliance alliance: game.getAlliances()) {
			gameContextBuilder.addAlliances(this.getAllianceFromPOJO(alliance));
		}
		
		for(offensive.Server.Hybernate.POJO.Command command: game.getCommands()) {
			gameContextBuilder.addPendingComands(this.getCommandFromPOJO(command));
		}
		
		return gameContextBuilder.build();
	}
	
	private short tradeCards(int card1, int card2, int card3) {
		if(card1 != card2 && card1 != card3 && card2 != card3) {
			return 10;
		}
		
		if(card1 == card2 && card2 == card3) {
			switch (card1) {
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
}
