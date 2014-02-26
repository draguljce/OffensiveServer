package offensive.Server.WorkerThreads;

import java.io.IOException;
import java.net.Socket;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.json.JSONException;
import org.json.JSONObject;

import com.communication.CommunicationProtos.NoFacebookLoginRequest;
import com.communication.CommunicationProtos.NoFacebookLoginResponse;
import com.communication.CommunicationProtos.RegisterRequest;
import com.communication.CommunicationProtos.RegisterResponse;
import com.google.protobuf.InvalidProtocolBufferException;

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
			
			Message receivedMessage;
			
			try {
				receivedMessage = Communicator.getCommunicator().acceptMessage(this.socket);
			} catch (Exception e) {
				Server.getServer().logger.info(e.getMessage(), e);
				return;
			}
			
			if(receivedMessage == null) {
				Server.getServer().logger.error("Failed to read client message.");
				return;
			}
			
			Message response = this.proccessRequest(receivedMessage);
			
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
		Message response = new Message(request.handlerId, request.ticketId, null, (byte)0, (byte)1);
		
		try {
			switch (request.handlerId) {
			case RegisterRequest:
				response.data = this.proccessRegisterRequest(RegisterRequest.parseFrom(request.data));
				break;
				
			case NoFacebookLoginRequest:
				response.data = this.proccessNoFacebookLoginRequest(NoFacebookLoginRequest.parseFrom(request.data));
				break;
	
			case FacebookLoginRequest:
				response.data = this.proccessFacebookLoginRequest(NoFacebookLoginRequest.parseFrom(request.data));
				break;
				
			default:
				break;
			}
		} catch (InvalidProtocolBufferException e) {
			Server.getServer().logger.error(e.getMessage(), e);
			return PredefinedMessages.getUnkownErrorMessage(response.handlerId, response.ticketId);
		}
		
		return response;
	}
	
	/******************************************************************************************************/
	private byte[] proccessRegisterRequest(RegisterRequest registerRequest) {
		Server.getServer().logger.info("Received register request.");
		
		JSONObject response = new JSONObject();
		
		// Open session.
		Session session = Server.getServer().sessionFactory.openSession();
		
		try {
			// We need to check if user already exists.
			Transaction tran = session.beginTransaction();
			
			List results = HibernateUtil.executeHql("FROM " + Constants.OffensiveUserClassName + " oUser WHERE oUser.userName = '" + registerRequest.getUsername() + "'", session);
			
			if(results == null || !results.isEmpty()) {
				// User already exists, registration is unsuccessful.
				tran.rollback();
				Server.getServer().logger.info("User " + registerRequest.getUsername() + " already exists. Registration failed.");
				response.put("isSuccessfull", "false");
			} else {
				User user = new User(new UserType(Constants.OffensiveUserType));
				
				Integer id = (Integer)session.save(user);
				
				OffensiveUser offensiveUser = new OffensiveUser(id, registerRequest.getUsername(), registerRequest.getPasswordHash());
				
				session.save(offensiveUser);
				tran.commit();
				
				Server.getServer().logger.info("User " + registerRequest.getUsername() + "with password " + registerRequest.getPasswordHash() + " is now registered.");
				response.put("isSuccessfull", "true");
			}
		} catch (Exception e) {
			Server.getServer().logger.error(e.getMessage(), e);
			try {
				response.put("isSuccessfull", "false");
			} catch (JSONException e1) {
				Server.getServer().logger.error(e.getMessage(), e);
			}
		} finally {
			session.close();
		}
		
		return response.toString().getBytes();
	}	
	
	private byte[] proccessNoFacebookLoginRequest(NoFacebookLoginRequest noFacebookLoginRequest) {
		Server.getServer().logger.info("Received no-facebook login request.");
		
		JSONObject response = new JSONObject();
		
		Session session = Server.getServer().sessionFactory.openSession();
		
		Transaction tran = session.beginTransaction();
		long userId;
		
		try {
			// Check if user exists.
			List results = HibernateUtil.executeHql("FROM OffensiveUser oUser WHERE oUser.userName='" + noFacebookLoginRequest.getUsername() + "'", session);
			
			if(results.isEmpty()) {
				userId = -1;
				Server.getServer().logger.info("User does not exist.");
			} else {
				assert results.size() == 1;
				OffensiveUser user = (OffensiveUser)results.remove(0);
				
				if(noFacebookLoginRequest.getPasswordHash().equals(user.getPassword())) {
					userId = user.getId();
					Server.getServer().logger.info("User ID=" + user.getId() + " Name=" + user.getUserName() + " successfully logged-in.");
				} else {
					userId = -1;
					Server.getServer().logger.info("User provided wrong password.");
				}
			}
			
		} finally {
			tran.commit();
			session.close();
			
			Server.getServer().logger.info("Finished proccessing no-facebook login request.");
		}
		
		try {
			response.put("userId", userId);
		} catch (JSONException e) {
			Server.getServer().logger.error(e.getMessage(), e);
		}
		
		return response.toString().getBytes();
	}
	
	private byte[] proccessFacebookLoginRequest(NoFacebookLoginRequest noFacebookLoginRequest) {
		Server.getServer().logger.info("Received no-facebook login request.");
		
		JSONObject response = new JSONObject();
		
		Session session = Server.getServer().sessionFactory.openSession();
		
		Transaction tran = session.beginTransaction();
		long userId;
		
		try {
			// Check if user exists.
			List results = HibernateUtil.executeHql("FROM OffensiveUser oUser WHERE oUser.userName='" + noFacebookLoginRequest.getUsername() + "'", session);
			
			if(results.isEmpty()) {
				userId = -1;
				Server.getServer().logger.info("User does not exist.");
			} else {
				assert results.size() == 1;
				OffensiveUser user = (OffensiveUser)results.remove(0);
				
				if(noFacebookLoginRequest.getPasswordHash().equals(user.getPassword())) {
					userId = user.getId();
					Server.getServer().logger.info("User ID=" + user.getId() + " Name=" + user.getUserName() + " successfully logged-in.");
				} else {
					userId = -1;
					Server.getServer().logger.info("User provided wrong password.");
				}
			}
			
		} finally {
			tran.commit();
			session.close();
			
			Server.getServer().logger.info("Finished proccessing no-facebook login request.");
		}
		
		try {
			response.put("userId", userId);
		} catch (JSONException e) {
			Server.getServer().logger.error(e.getMessage(), e);
		}
		
		return response.toString().getBytes();
	}

}
