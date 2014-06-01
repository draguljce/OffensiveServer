package offensive.Server.Sessions;

import java.nio.channels.SocketChannel;

import offensive.Server.Hybernate.POJO.User;

public class Session {
	public User user;
	
	public SocketChannel socketChannel;
	
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
	
	@Override
	public boolean equals(Object other) {
		if(other == null) {
			return false;
		}
		
		if(this == other) {
			return true;
		}
		
		if(this.getClass().equals(other.getClass())) {
			Session otherSession = (Session)other;
		
			return this.user.getId() == otherSession.user.getId();
		} else {
			return false;
		}
	}
}
