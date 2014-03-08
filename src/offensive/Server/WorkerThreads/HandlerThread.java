package offensive.Server.WorkerThreads;

import java.io.IOException;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

import offensive.Communicator.Communicator;
import offensive.Communicator.Message;
import offensive.Communicator.ProtobuffMessage;
import offensive.Server.Server;
import offensive.Server.Hybernate.POJO.CurrentGame;
import offensive.Server.Hybernate.POJO.FacebookUser;
import offensive.Server.Hybernate.POJO.InviteRecord;
import offensive.Server.Hybernate.POJO.Play;
import offensive.Server.Hybernate.POJO.PlayerGameRecord;
import offensive.Server.Utilities.Constants;
import offensive.Server.Utilities.HibernateUtil;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.communication.DataProtos.GameContext;
import com.communication.DataProtos.GameDescription;
import com.communication.DataProtos.LightGameContext;
import com.communication.DataProtos.User;
import com.communication.DataProtos.User.Builder;
import com.communication.DataProtos.UserStatistics;
import com.google.protobuf.GeneratedMessage;

public class HandlerThread implements Runnable {

	private Socket socket;
	
	public HandlerThread(Socket socket) {
		this.socket = socket;
	}

	@Override
	public void run() {
		try {
			Server.getServer().logger.info("Started proccessing request");
			
			Message receivedMessage;
			
			try {
				receivedMessage = Communicator.getCommunicator().acceptMessage(this.socket);
			} catch (Exception e) {
				Server.getServer().logger.error(e.getMessage(), e);
				return;
			}
			
			if(receivedMessage == null) {
				Server.getServer().logger.error("Failed to read client message.");
				return;
			}
			
			Message response;
			
			try {
				response = this.proccessRequest(receivedMessage);
			}
			catch (Exception e) {
				Server.getServer().logger.error(e.getMessage(), e);
				response = null;
			}
			
			try {
				Communicator.getCommunicator().sendMessage(response, this.socket);
			} catch (IOException e) {
				Server.getServer().logger.error(e.getMessage(), e);
			}
			
			Server.getServer().logger.info("Finished proccessing request.");
		} finally {
			try {
				this.socket.close();
			} catch (IOException e) {
				Server.getServer().logger.error(e.getMessage(), e);
			}
		}
	}
	
	private Message proccessRequest(Message request) {
		Message response = new ProtobuffMessage(request.handlerId, request.ticketId, new byte[0]);
		switch(request.handlerId) {
		case GetUserDataRequest:
			break;
		default:
			throw new IllegalArgumentException("Illegal handler ID!!!");
		}
		
		return response;
	}
	
	// Here are handlers definitions.
	/*******************************************************************************************************************************************************/
	private GeneratedMessage proccessGetUserDataRequest(Message request) {
		return null;
	}
	
	private User getUser(int userId) {
		Session session = Server.getServer().sessionFactory.openSession();
		Transaction tran = session.beginTransaction();
		
		Builder userBuilder = User.newBuilder();
		try {
			offensive.Server.Hybernate.POJO.User user = (offensive.Server.Hybernate.POJO.User)session.get(offensive.Server.Hybernate.POJO.User.class, userId);
			
			if(user != null) {
				userBuilder.setUserId(user.getId());
				
				if(user.getType().getName().equals(Constants.FacebookUserType)) {
					FacebookUser facebookUser = (FacebookUser)session.get(FacebookUser.class, user.getId());
					
					userBuilder.setFacebookId(facebookUser.getFacebookId());
				}
			}
			
		} finally {
			tran.rollback();
		}
		
		return userBuilder.hasUserId() ? userBuilder.build() : null;
	}
	
	private UserStatistics getUserStatistics(User user) {
		if(user == null || !user.hasUserId()) {
			return null;
		}
		
		Session session = Server.getServer().sessionFactory.openSession();
		Transaction tran = session.beginTransaction();

		com.communication.DataProtos.UserStatistics.Builder userStatisticsBuilder = UserStatistics.newBuilder();
		boolean successfullyBuilt = false;
		try {
			List<PlayerGameRecord> playerGameRecords = HibernateUtil.executeHql(String.format("FROM PlayerGameRecord playedGames WHERE playedGames.player.id = %s", user.getUserId()), session);
			
			userStatisticsBuilder.setNumberOfGames(playerGameRecords.size());
			
			int gamesWon = 0;
			
			for(PlayerGameRecord playerGameRecord: playerGameRecords) {
				if(playerGameRecord.getPlayerStatistics().getRanking() == 1) {
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
	
	private List<GameDescription> getInvites(User user) {
		if(user == null || !user.hasUserId()) {
			return null;
		}
		
		Session session = Server.getServer().sessionFactory.openSession();
		Transaction tran = session.beginTransaction();

		List<GameDescription> invites = new LinkedList<GameDescription>();
		
		try {
			List<InviteRecord> playerGameRecords = HibernateUtil.executeHql(String.format("FROM InviteRecord invites WHERE invites.invitedUser.id = %s", user.getUserId()), session);
			
			for(InviteRecord inviteRecord: playerGameRecords) {
				invites.add(this.getGameDescriptionFromGame(inviteRecord.getInvite().getGame()));
			}
		} finally {
			tran.rollback();
		}
		
		return invites;
	}
	
	private List<GameContext> getJoinedGames(User user) {
		if(user == null || !user.hasUserId()) {
			return null;
		}
		
		Session session = Server.getServer().sessionFactory.openSession();
		Transaction tran = session.beginTransaction();

		List<GameContext> joinedGames = new LinkedList<GameContext>();
		 
		boolean successfullyBuilt = false;
		
		try {
			List<Play> playGameRecords = HibernateUtil.executeHql(String.format("FROM Play play WHERE play.player.user.id= %s", user.getUserId()), session);

			for(Play play: playGameRecords) {
				com.communication.DataProtos.GameContext.Builder gameContextBuilder = GameContext.newBuilder();
				
				gameContextBuilder.setGameDescription(this.getGameDescriptionFromGame(play.getGame()));
				joinedGames.add(this.getGameDescriptionFromGame(inviteRecord.getInvite().getGame()));
			}
		} finally {
			tran.rollback();
		}
		
		return invites;
	}
	
	private GameDescription getGameDescriptionFromGame(CurrentGame game) {
		if(game == null) {
			return null;
		}
		
		com.communication.DataProtos.GameDescription.Builder gameDescriptionBuilder = GameDescription.newBuilder();
		
		gameDescriptionBuilder.setGameId(game.getId());
		gameDescriptionBuilder.setGameName(game.getGameName());
		gameDescriptionBuilder.setNumberOfPlayers(game.getNumberOfPlayers());
		gameDescriptionBuilder.setNumberOfJoinedPlayers(game.getNumberOfJoinedPlayers());
		gameDescriptionBuilder.setObjective(game.getObjective().getId());
		
		return gameDescriptionBuilder.build();
	}
	
	private LightGameContext getLightGameContextFromGame(CurrentGame game) {
		if(game == null) {
			return null;
		}
		
		com.communication.DataProtos.LightGameContext.Builder lightGameContextBuilder = LightGameContext.newBuilder();
		
		lightGameContextBuilder.setGameDescription(this.getGameDescriptionFromGame(game));
		lightGameContextBuilder.setRound(game.getCurrentRound());
		lightGameContextBuilder.setPhase(game.getPhase().getId());
		
		Session session = Server.getServer().sessionFactory.openSession();
		Transaction tran = session.beginTransaction();

		List<Player> joinedGames = new LinkedList<GameContext>();
		 
		boolean successfullyBuilt = false;
		
		try {
			List<Play> playGameRecords = HibernateUtil.executeHql(String.format("FROM Play play WHERE play.game.id= %s", game.getId()), session);

			for(Play play: playGameRecords) {
				com.communication.DataProtos.GameContext.Builder gameContextBuilder = GameContext.newBuilder();
				
				gameContextBuilder.setGameDescription(this.getGameDescriptionFromGame(play.getGame()));
				joinedGames.add(this.getGameDescriptionFromGame(inviteRecord.getInvite().getGame()));
			}
		} finally {
			tran.rollback();
		}


		
		return gameDescriptionBuilder.build();
	}
}
