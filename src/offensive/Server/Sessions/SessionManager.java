package offensive.Server.Sessions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SessionManager {
	private Map<Integer, List<Session>> gameToSessionsMap;
	
	public SessionManager() {
		this.gameToSessionsMap = new HashMap<Integer, List<Session>>();
	}
	
	public void openSession() {
		
	}
}
