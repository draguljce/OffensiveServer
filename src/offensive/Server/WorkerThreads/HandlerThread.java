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

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.google.protobuf.GeneratedMessage;
import communication.protos.CommunicationProtos.CreateGameRequest;
import communication.protos.CommunicationProtos.CreateGameResponse;
import communication.protos.CommunicationProtos.FilterFriendsRequest;
import communication.protos.CommunicationProtos.FilterFriendsResponse;
import communication.protos.CommunicationProtos.GetOpenGamesRequest;
import communication.protos.CommunicationProtos.GetOpenGamesResponse;
import communication.protos.CommunicationProtos.GetUserDataRequest;
import communication.protos.CommunicationProtos.GetUserDataResponse;
import communication.protos.CommunicationProtos.JoinGameRequest;
import communication.protos.CommunicationProtos.JoinGameResponse;
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
		
		offensive.Server.Hybernate.POJO.CurrentGame newGame = new CurrentGame(request.getGameName(), request.getNumberOfPlayers(), new Objective(request.getObjectiveCode()));
		
		List<Invite> invites = new LinkedList<Invite>();
		
		Transaction tran = session.beginTransaction();
		
		try{
			session.save(newGame);
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
			openedGames = (List<CurrentGame>)HibernateUtil.executeHql("FROM CurrentGame games WHERE games.isOpen = true", session);
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
				FacebookUser facebookUser = (FacebookUser)session.get(FacebookUser.class, user.getId());
				
				userBuilder.setFacebookId(facebookUser.getFacebookId());
			}
		} else {
			return null;
		}
		
		return userBuilder.build();
	}
	
	private User getUserFromPOJO(offensive.Server.Hybernate.POJO.FacebookUser user) {
		Builder userBuilder = User.newBuilder();
		
		if(user != null) {
			userBuilder.setUserId(user.getId());
			
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
}
