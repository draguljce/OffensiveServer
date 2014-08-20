package offensive.Communicator;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

import offensive.Server.Server;
import offensive.Server.Exceptions.FatalErrorException;
import offensive.Server.Sessions.Session;

public class SendableMessage {
	public Message message;
	public Collection<Session> recipients;
	
	public SendableMessage(Message message, Session recipient) {
		this.message = message;
		this.recipients = new LinkedList<Session>();
		
		this.recipients.add(recipient);
	}
	
	public SendableMessage(Message message, Collection<Session> recipients) {
		this.message = message;
		this.recipients = recipients;
	}
	
	public void send() throws FatalErrorException {
		for(Session recipient: this.recipients) {
			try {
				Communicator.getCommunicator().sendMessage(this.message, recipient.socketChannel);
			} catch (IOException e) {
				Server.getServer().logger.error(e.getMessage(), e);
			}
		}
	}
}
