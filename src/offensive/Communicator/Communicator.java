package offensive.Communicator;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;

import offensive.Server.Server;
import offensive.Server.Utilities.Constants;

import com.communication.CommunicationProtos.RegisterRequest;
import com.communication.CommunicationProtos.RegisterResponse;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.InvalidProtocolBufferException;

public class Communicator {

	private static Communicator onlyInstance = new Communicator();
	
	private Communicator(){};
	
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
		
		HandlerId handlerId;
		int ticketId;
		int dataLength;
		GeneratedMessage data;
		
		// 13 bytes is header length
		byte[] messageBytes = new byte[13];
		
		// Read header.
		try {
			inputStream.read(messageBytes, 0, 13);
		} catch (IOException e) {
			Server.getServer().logger.error(e.getMessage(), e);
			return null;
		}
		
		ByteBuffer messageBuffer = ByteBuffer.wrap(messageBytes);
		
		// First 4 bytes are handler ID.
		handlerId = HandlerId.parse(messageBuffer.getInt(0));
		ticketId = messageBuffer.getInt(4);
		dataLength = messageBuffer.getInt(8);
		
		// Now read the rest of the message.
		messageBytes = new byte[dataLength];
		try {
			inputStream.read(messageBytes, 0, dataLength);
		} catch (IOException e) {
			Server.getServer().logger.error(e.getMessage(), e);
			return null;
		}
	
		try {
			data = this.deserialize(handlerId, messageBytes);
		} catch (InvalidProtocolBufferException e) {
			Server.getServer().logger.error(e.getMessage(), e);
			return null;
		}
		
		return new Message(handlerId, ticketId, data);
	}
	
	public void sendMessage(Message response, Socket socket) throws IOException {
		byte[] data = response.data != null ? response.data.toByteArray() : new byte[0];
		response.dataLength = data.length;
		
		// Add additional 13 bytes to data length for header.
		ByteBuffer byteBuffer = ByteBuffer.allocate(data.length + 13);
		
		byteBuffer.putInt(response.handlerId.ordinal());
		byteBuffer.putInt(response.ticketId);
		byteBuffer.putInt(response.dataLength);
		byteBuffer.put((byte) 0b0000);
		
		byteBuffer.put(data);
		
		OutputStream outputStream = socket.getOutputStream();
		
		outputStream.write(byteBuffer.array());
	}
	
	private GeneratedMessage deserialize(HandlerId handlerId, byte[] data) throws InvalidProtocolBufferException {
		switch (handlerId) {
		case RegisterRequest:
			return RegisterRequest.parseFrom(data);
			
		case RegisterResponse:
			return RegisterResponse.parseFrom(data);
		
		default:
			// We should never reach here.
			throw new IllegalArgumentException("HandlerId has illegal value!!!");
		}
	}
}
