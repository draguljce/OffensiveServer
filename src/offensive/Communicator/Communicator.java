package offensive.Communicator;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.HashMap;

import org.infinispan.commons.hash.Hash;

import offensive.Server.Server;
import offensive.Server.Utilities.Constants;

import com.communication.CommunicationProtos.NoFacebookLoginRequest;
import com.communication.CommunicationProtos.NoFacebookLoginResponse;
import com.communication.CommunicationProtos.RegisterRequest;
import com.communication.CommunicationProtos.RegisterResponse;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.InvalidProtocolBufferException;

public class Communicator {

	private static Communicator onlyInstance = new Communicator();
	
	private Deserializer[] deserializers;
	
	private Communicator() {
		this.deserializers = new Deserializer[] { new ProtoDeserializer(), new JsonDeserializer() };
	};
	
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
		int ticketId, dataLength, status;
		Byte serializationType;
		GeneratedMessage data;
		
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
		handlerId = HandlerId.parse(messageBuffer.getInt(0));
		ticketId = messageBuffer.getInt(4);
		dataLength = messageBuffer.getInt(8);
		status = messageBuffer.get(12);
		serializationType = messageBuffer.get(13);
		
		// Now read the rest of the message.
		messageBytes = new byte[dataLength];
		try {
			inputStream.read(messageBytes, 0, dataLength);
		} catch (IOException e) {
			Server.getServer().logger.error(e.getMessage(), e);
			return null;
		}
		
		// Deserialization part.
		Deserializer deserializer = this.deserializers[serializationType.intValue()];
		
		data = deserializer.deserialize(handlerId, messageBytes);
		
		Message receivedMessage = new Message(handlerId, ticketId, data.toByteArray(), (byte)0, serializationType);
		Server.getServer().logger.debug("Received message from client:");
		Server.getServer().logger.debug(receivedMessage);
		
		return receivedMessage;
	}

	public void sendMessage(Message response, Socket socket) throws IOException {
		response.dataLength = response.data.length;
		
		// Add additional 14 bytes to data length for header.
		ByteBuffer byteBuffer = ByteBuffer.allocate(response.data.length + 14);
		
		byteBuffer.putInt(response.handlerId.ordinal());
		byteBuffer.putInt(response.ticketId);
		byteBuffer.putInt(response.dataLength);
		byteBuffer.put((byte) 0b0000);
		byteBuffer.put(response.serializationType);
		
		byteBuffer.put(response.data);
		
		OutputStream outputStream = socket.getOutputStream();
		
		Server.getServer().logger.debug("Sending response to client " + socket.getInetAddress() + ": " + response);
		outputStream.write(byteBuffer.array());
	}
}
