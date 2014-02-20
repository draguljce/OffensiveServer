package offensive.Communicator;

import java.util.HashMap;
import java.util.Map;

public enum HandlerId {
	RegisterRequest,
	RegisterResponse,
	NoFacebookLoginRequest;
	
	private static Map<Integer, HandlerId> intToHandlerMap;
	
	private static Map<Integer, HandlerId> getMap() {
		if(HandlerId.intToHandlerMap == null) {
			HandlerId.intToHandlerMap = new HashMap<Integer, HandlerId>();
			
			for(HandlerId handlerId: HandlerId.values()) {
				HandlerId.intToHandlerMap.put(handlerId.ordinal(), handlerId);
			}
		}
		
		return HandlerId.intToHandlerMap;
	}
	
	public static HandlerId parse(int handlerId) {
		return HandlerId.getMap().get(handlerId);
	}
}