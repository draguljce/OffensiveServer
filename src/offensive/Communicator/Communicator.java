package offensive.Communicator;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;

import org.json.JSONException;

import offensive.Server.Server;
import offensive.Server.Utilities.Constants;

public class Communicator {

	private static Communicator onlyInstance = new Communicator();
	
	public static Communicator getCommunicator() {
		return Communicator.onlyInstance;
	}
	
	public Message acceptMessage(Socket socket) {
		InputStream inputStream;
		
		try {
			socket.setSoTimeout(Integer.parseInt(Server.getServer().environment.getVariable(Constants.SocketTimeoutVarName)));
		} catch (SocketException e) {
			Server.getServer().logger.error(e.getMessage(), e);
			return null;
		}
		
		try {
			inputStream = socket.getInputStream();
		} catch (IOException e) {
			Server.getServer().logger.error(e.getMessage(), e);
			return null;
		}
				
		int handlerId, ticketId, dataLength;
		byte status, serializationType;
		
		// 14 bytes is header length
		byte[] messageBytes = new byte[14];
		
		// Read header.
		try {
			inputStream.read(messageBytes, 0, 14);
		} catch (IOException e) {
			Server.getServer().logger.error(e.getMessage(), e);
			return null;
		}
		
		ByteBuffer messageBuffer = ByteBuffer.wrap(messageBytes);
		
		// First 4 bytes are handler ID.
		handlerId = messageBuffer.getInt(0);
		ticketId = messageBuffer.getInt(4);
		dataLength = messageBuffer.getInt(8);
		status = messageBuffer.get(12);
		serializationType = messageBuffer.get(13);
		
		// Now read the rest of the message.
		Server.getServer().logger.debug("Received header:" + handlerId + "|" + ticketId + "|" + dataLength + "|" + status + "|" + serializationType);
		messageBytes = new byte[dataLength];
		try {
			inputStream.read(messageBytes, 0, dataLength);
		} catch (IOException e) {
			Server.getServer().logger.error(e.getMessage(), e);
			return null;
		}
		
		Message receivedMessage;
		try {
			receivedMessage = Message.build(handlerId, ticketId, status, serializationType, messageBytes);
		} catch (JSONException e) {
			Server.getServer().logger.error(e.getMessage(), e);
			return null;
		}
		
		Server.getServer().logger.debug("Received message from client:");
		Server.getServer().logger.debug(receivedMessage);
		
		return receivedMessage;
	}

	public void sendMessage(Message response, Socket socket) throws IOException {
		OutputStream outputStream = socket.getOutputStream();
		
		Server.getServer().logger.debug("Sending response to client " + socket.getInetAddress() + ": " + response);
		outputStream.write(response.serialize());
	}
}
