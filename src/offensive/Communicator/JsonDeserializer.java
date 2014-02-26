package offensive.Communicator;

import offensive.Server.Server;

import org.json.JSONException;
import org.json.JSONObject;

import com.communication.CommunicationProtos.FacebookLoginRequest;
import com.communication.CommunicationProtos.NoFacebookLoginRequest;
import com.communication.CommunicationProtos.RegisterRequest;
import com.google.protobuf.GeneratedMessage;

public class JsonDeserializer implements Deserializer {

	@Override
	public GeneratedMessage deserialize(HandlerId handlerId, byte[] data) {
		switch (handlerId) {
		case RegisterRequest:
			
			return this.proccessRegisterRequest(data);
				
		case NoFacebookLoginRequest:
			return this.proccessNoFacebookLoginRequest(data);
		
		case FacebookLoginRequest:
			return this.proccessFacebookLoginRequest(data);
			
		default:
			// We should never reach here.
			throw new IllegalArgumentException("HandlerId has illegal value!!!");
		}
	}
	
	/*****************************************************************************************************/
	private GeneratedMessage proccessRegisterRequest(byte[] data) {
		String userName, pswHash;
		
		JSONObject jsonObject;

		try {
			jsonObject = new JSONObject(new String(data));
			userName = jsonObject.getString("username");
			pswHash = jsonObject.getString("passwordHash");
		} catch (JSONException e) {
			Server.getServer().logger.error(e.getMessage(), e);
			return null;
		}
		
		Server.getServer().logger.debug(new String(data));
		return RegisterRequest.newBuilder().setUsername(userName).setPasswordHash(pswHash).build();
	}
	
	private GeneratedMessage proccessNoFacebookLoginRequest(byte[] data) {
		String userName, pswHash;
		
		JSONObject jsonObject;

		try {
			jsonObject = new JSONObject(new String(data));
			userName = jsonObject.getString("username");
			pswHash = jsonObject.getString("passwordHash");
		} catch (JSONException e) {
			Server.getServer().logger.error(e.getMessage(), e);
			return null;
		}
		
		Server.getServer().logger.debug(new String(data));
		return NoFacebookLoginRequest.newBuilder().setUsername(userName).setPasswordHash(pswHash).build();

	} 
	
	private GeneratedMessage proccessFacebookLoginRequest(byte[] data) {
		Long facebookId;
		
		JSONObject jsonObject;

		try {
			jsonObject = new JSONObject(new String(data));
			facebookId = Long.parseLong(jsonObject.getString("facebookId"));
		} catch (JSONException e) {
			Server.getServer().logger.error(e.getMessage(), e);
			return null;
		}
		
		Server.getServer().logger.debug(new String(data));
		return FacebookLoginRequest.newBuilder().setFacebookId(facebookId).build();

	}
}
