package offensive.Communicator;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import offensive.Server.Server;
import offensive.Server.Exceptions.FatalErrorException;

import org.json.JSONException;

public class Communicator {
	private final int headerLength = 14;

	protected static Communicator onlyInstance = new Communicator();
	
	public static Communicator getCommunicator() {
		return Communicator.onlyInstance;
	}
	
	public Message acceptMessage(SocketChannel socketChannel) throws IOException {
						
		int handlerId, ticketId, dataLength;
		byte status, serializationType;
		
		// 14 bytes is header length
		ByteBuffer[] messageBytes = new ByteBuffer[] { ByteBuffer.allocate(headerLength)};
		
		// Read header.
		try {
			long numberOfReadBytes = socketChannel.read(messageBytes, 0, 1);
			
			if(numberOfReadBytes == 0) {
				Server.getServer().logger.info("Client closed channel.");
				return null;
			}
			
			if(numberOfReadBytes != headerLength) {
				return null;
			}
		} catch (IOException e) {
			Server.getServer().logger.error(e.getMessage(), e);
			return null;
		}
		
		Server.getServer().logger.debug(messageBytes[0].toString());
		// First 4 bytes are handler ID.
		handlerId = messageBytes[0].getInt(0);
		ticketId = messageBytes[0].getInt(4);
		dataLength = messageBytes[0].getInt(8);
		status = messageBytes[0].get(12);
		serializationType = messageBytes[0].get(13);
		
		// Now read the rest of the message.
		Server.getServer().logger.debug("Received header:" + handlerId + "|" + ticketId + "|" + dataLength + "|" + status + "|" + serializationType);
		
		messageBytes[0].clear();
		messageBytes[0] = ByteBuffer.allocate(dataLength);
		try {
			int readBytes = 0;
			while(readBytes < dataLength) {
				readBytes += socketChannel.read(messageBytes, 0, 1);
				
				if(readBytes == 0) {
					return null;
				}
			}
		} catch (IOException e) {
			Server.getServer().logger.error(e.getMessage(), e);
			return null;
		}
		
		Message receivedMessage;
		try {
			receivedMessage = Message.build(handlerId, ticketId, status, serializationType, messageBytes[0].array());
		} catch (JSONException e) {
			Server.getServer().logger.error(e.getMessage(), e);
			return null;
		}
		
		Server.getServer().logger.debug("Received message from client: " + socketChannel.getRemoteAddress());
		Server.getServer().logger.debug(receivedMessage);
		
		return receivedMessage;
	}

	public void sendMessage(Message response, SocketChannel socketChannel) throws IOException, FatalErrorException {
		int bytesSent = socketChannel.write(response.serialize());
		Server.getServer().logger.debug(String.format("Server sent %s/%s bytes to client %s\n%s", bytesSent, response.dataLength + headerLength, socketChannel.getRemoteAddress(), response));
		if(bytesSent != response.dataLength + headerLength) {
			throw new FatalErrorException(String.format("Server sent only %s/%s bytes!!!", bytesSent, response.dataLength + headerLength));
		}
	}
}
