package offensive.Communicator;

import com.google.protobuf.GeneratedMessage;


public class Message {
	public HandlerId handlerId;
	public int ticketId;
	public int dataLength;
	
	public GeneratedMessage data;
	
	public Message(HandlerId handlerId2, int ticketId, GeneratedMessage data) {
		this.handlerId = handlerId2;
		this.ticketId = ticketId;
		this.data = data;
	}
}