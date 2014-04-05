package offensive.Server.Sessions;

import java.nio.channels.SocketChannel;
import java.util.HashSet;

import offensive.Server.Hybernate.POJO.User;
import offensive.Server.Sessions.Game.Game;

public class Session {
	public User user;
	
	public SocketChannel socketChannel;
	
	public HashSet<Game> myGames;
	
	Session(SocketChannel socketChannel){
		this.socketChannel = socketChannel;
	};
	
	@Override
	public int hashCode() {		
		if(this.user == null) {
			return 0;
		}
		else {
			return (int)this.user.getId();
		}
	}
}
