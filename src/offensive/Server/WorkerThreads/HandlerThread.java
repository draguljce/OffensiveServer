package offensive.Server.WorkerThreads;

import java.io.IOException;
import java.net.Socket;

import offensive.Communicator.Communicator;
import offensive.Communicator.Message;
import offensive.Communicator.PredefinedMessages;
import offensive.Server.Server;

public class HandlerThread implements Runnable {

	private Socket socket;
	
	public HandlerThread(Socket socket) {
		this.socket = socket;
	}

	@Override
	public void run() {
		try {
			Server.getServer().logger.info("Started proccessing request");
			
			Message receivedMessage = Communicator.getCommunicator().acceptMessage(this.socket);
			
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
	
	private Message proccessRequest(Message request) {
		Message response = new Message(null, request.ticketId, null);
		switch(request.handlerId) {
		case NoFacebookLoginRequest:
			break;
		default:
			throw new IllegalArgumentException("Illegal handler ID!!!");
		}
		
		return response;
	}
	
	// Here are handlers definitions.
	/*******************************************************************************************************************************************************/
}
