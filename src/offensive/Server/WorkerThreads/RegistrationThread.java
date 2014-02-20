package offensive.Server.WorkerThreads;

import java.io.IOException;
import java.net.Socket;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.communication.CommunicationProtos.RegisterRequest;
import com.communication.CommunicationProtos.RegisterResponse;

import offensive.Communicator.Communicator;
import offensive.Communicator.HandlerId;
import offensive.Communicator.Message;
import offensive.Communicator.PredefinedMessages;
import offensive.Server.GameServer;
import offensive.Server.Server;
import offensive.Server.Hybernate.POJO.OffensiveUser;
import offensive.Server.Hybernate.POJO.User;
import offensive.Server.Hybernate.POJO.UserType;
import offensive.Server.Utilities.Constants;
import offensive.Server.Utilities.HibernateUtil;

public class RegistrationThread implements Runnable {

	private Socket socket;
	
	public RegistrationThread(Socket socket) {
		this.socket = socket;
	}
	
	@Override
	public void run() {
		try {
			Server.getServer().logger.info("Started proccessing register request");
			
			Message receivedMessage = Communicator.getCommunicator().acceptMessage(this.socket);
			
			if(receivedMessage == null) {
				Server.getServer().logger.error("Failed to read client message.");
				return;
			}
			
			Message response = new Message(HandlerId.RegisterResponse, receivedMessage.ticketId, null);
			
			try {
				response.data = this.proccessRegisterRequest((RegisterRequest)receivedMessage.data);
			}
			catch (Exception e) {
				Server.getServer().logger.error(e.getMessage(), e);
				response = PredefinedMessages.getUnkownErrorMessage(receivedMessage.handlerId, receivedMessage.ticketId);
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
	
	private RegisterResponse proccessRegisterRequest(RegisterRequest registerRequest) {
		Server.getServer().logger.info("Received register request.");
		
		RegisterResponse response = null;
		
		// Open session.
		Session session = GameServer.sessionFactory.openSession();
		
		try {
		// We need to check if user already exists.
		Transaction tran = session.beginTransaction();
		
		List results = HibernateUtil.executeHql("FROM " + Constants.OffensiveUserClassName + " oUser WHERE oUser.userName = '" + registerRequest.getUsername() + "'", session);
		
		if(!results.isEmpty()) {
			// User already exists, registration is unsuccessful.
			tran.rollback();
			response = RegisterResponse.newBuilder().setIsSuccessfull(false).build();
		} else {
			User user = new User(new UserType(Constants.OffensiveUserType));
			
			Integer id = (Integer)session.save(user);
			
			OffensiveUser offensiveUser = new OffensiveUser(id, registerRequest.getUsername(), registerRequest.getPasswordHash());
			
			session.save(offensiveUser);
			tran.commit();
			
			response = RegisterResponse.newBuilder().setIsSuccessfull(true).build();
		}
		} finally {
			session.close();
		}
		
		return response;
	}
	
}
