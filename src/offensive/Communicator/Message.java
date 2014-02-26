package offensive.Communicator;

import org.json.JSONObject;

import com.google.protobuf.GeneratedMessage;


public class Message {
	public HandlerId handlerId;
	public int ticketId;
	public int dataLength;
	public byte status;
	public byte serializationType;
	
	public byte[] data;
	
	public Message(HandlerId handlerId, int ticketId, byte[] data) {
		this(handlerId, ticketId, data, (byte)0x00, (byte)0x00);
	}
	
	public Message(HandlerId handlerId, int ticketId, byte[] data, byte status, byte serializationType) {
		this.handlerId = handlerId;
		this.ticketId = ticketId;
		this.data = data;
		this.status = status;
		this.serializationType = serializationType;
	}
	
	@Override
	public String toString() {
		return this.handlerId.toString() + "|" + this.ticketId + "|" + this.dataLength + "|" + this.status + "|" + this.serializationType + "\n" + new String(this.data);
	}
}