package offensive.Server.WorkerThreads;

import java.io.IOException;
import java.net.Socket;
import java.util.List;

import offensive.Communicator.Communicator;
import offensive.Communicator.Message;
import offensive.Communicator.JsonMessage;
import offensive.Server.Server;
import offensive.Server.Hybernate.POJO.OffensiveUser;
import offensive.Server.Hybernate.POJO.FacebookUser;
import offensive.Server.Hybernate.POJO.User;
import offensive.Server.Hybernate.POJO.UserType;
import offensive.Server.Utilities.Constants;
import offensive.Server.Utilities.HibernateUtil;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.json.JSONException;
import org.json.JSONObject;

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
			
			Message response;
			
			try {
				response = this.proccessRequest((JsonMessage)receivedMessage);
				Communicator.getCommunicator().sendMessage(response, this.socket);
			} catch (Exception e) {
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
	
	private Message proccessRequest(JsonMessage request) {
		JsonMessage response;
		
		try{
			response = new JsonMessage(request.handlerId, request.ticketId);
			
			switch (request.handlerId) {
			case RegisterRequest:
				response.setData(this.proccessRegisterRequest(request.getData()));
				break;
				
			case NoFacebookLoginRequest:
				response.setData(this.proccessNoFacebookLoginRequest(request.getData()));
				break;
	
			case FacebookLoginRequest:
				response.setData(this.proccessFacebookLoginRequest(request.getData()));
				break;
				
			default:
				break;
			}
		}catch(JSONException e) {
			Server.getServer().logger.error(e.getMessage(), e);
			response = null;
		}
		
		return response;
	}
	
	/**
	 * @throws JSONException ****************************************************************************************************/
	private JSONObject proccessRegisterRequest(JSONObject registerRequest) throws JSONException {
		Server.getServer().logger.info("Received register request.");
		
		JSONObject response = new JSONObject();
		
		String userName = registerRequest.getString("username");
		String passwordHash = registerRequest.getString("passwordHash");
		
		// Open session.
		Session session = Server.getServer().sessionFactory.openSession();
		
		try {
			// We need to check if user already exists.
			Transaction tran = session.beginTransaction();
			
			List results = HibernateUtil.executeHql("FROM " + Constants.OffensiveUserClassName + " oUser WHERE oUser.userName = '" + userName + "'", session);
			
			if(results == null || !results.isEmpty()) {
				// User already exists, registration is unsuccessful.
				tran.rollback();
				Server.getServer().logger.info("User " + userName + " already exists. Registration failed.");
				response.put("isSuccessfull", "false");
			} else {
				User user = new User(new UserType(Constants.OffensiveUserType));
				
				Integer id = (Integer)session.save(user);
				
				OffensiveUser offensiveUser = new OffensiveUser(id, userName, passwordHash);
				
				session.save(offensiveUser);
				tran.commit();
				
				Server.getServer().logger.info("User " + userName + "with password " + passwordHash + " is now registered.");
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
		
		return response;
	}	
	
	private JSONObject proccessNoFacebookLoginRequest(JSONObject noFacebookLoginRequest) throws JSONException {
		Server.getServer().logger.info("Received no-facebook login request.");
		
		JSONObject response = new JSONObject();
		
		String userName = noFacebookLoginRequest.getString("username");
		String passwordHash = noFacebookLoginRequest.getString("passwordHash");
		
		Session session = Server.getServer().sessionFactory.openSession();
		
		Transaction tran = session.beginTransaction();
		long userId;
		
		try {
			// Check if user exists.
			List results = HibernateUtil.executeHql("FROM OffensiveUser oUser WHERE oUser.userName='" + userName + "'", session);
			
			if(results.isEmpty()) {
				userId = -1;
				Server.getServer().logger.info("User does not exist.");
			} else {
				assert results.size() == 1;
				OffensiveUser user = (OffensiveUser)results.remove(0);
				
				if(passwordHash.equals(user.getPassword())) {
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
		
		return response;
	}
	
	private JSONObject proccessFacebookLoginRequest(JSONObject facebookLoginRequest) throws JSONException {
		Server.getServer().logger.info("Received no-facebook login request.");
		
		JSONObject response = new JSONObject();
		
		long facebookId = Long.parseLong(facebookLoginRequest.getString("facebookId"));
		
		Session session = Server.getServer().sessionFactory.openSession();
		
		Transaction tran = session.beginTransaction();
		long userId;
		
		try {
			// Check if user exists.
			List results = HibernateUtil.executeHql("FROM FacebookUser fUser WHERE fUser.facebookId ='" + facebookId + "'", session);
			
			if(results.isEmpty()) {
				Server.getServer().logger.info("Facebook user does not exist.");
				
				User user = new User(new UserType(Constants.FacebookUserType));
				
				Integer id = (Integer)session.save(user);
				
				FacebookUser facebookUser = new FacebookUser(id, facebookId);
				
				session.save(facebookUser);
				tran.commit();
				
				Server.getServer().logger.info("Registered new facebook user with userId: " + facebookId);
				userId = id;
			} else {
				assert results.size() == 1;
				FacebookUser user = (FacebookUser)results.remove(0);
				userId = user.getId();
			}
			
		} finally {
			session.close();
			
			Server.getServer().logger.info("Finished proccessing no-facebook login request.");
		}
		
		try {
			response.put("userId", userId);
		} catch (JSONException e) {
			Server.getServer().logger.error(e.getMessage(), e);
		}
		
		return response;
	}
}