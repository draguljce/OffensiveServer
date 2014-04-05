package offensive.Communicator;

import java.nio.ByteBuffer;

import org.json.JSONException;

public abstract class Message {
	public HandlerId handlerId;
	public int ticketId;
	public int dataLength;
	public byte status;
	public SerializationType serializationType;
	
	Message(HandlerId handlerId, int ticketId, byte status) {
		this.handlerId = handlerId;
		this.ticketId = ticketId;
		this.status = status;
	}
	
	@Override
	public String toString() {
		return "\n" + this.handlerId.toString() + "|" + this.ticketId + "|" + this.dataLength + "|" + this.status + "|" + this.serializationType + "\n";
	}
	
	public ByteBuffer serialize() {
		byte[] byteData = this.getDataBytes();
		this.dataLength = byteData.length;
		
		// Add additional 14 bytes to data length for header.
		ByteBuffer byteBuffer = ByteBuffer.allocate(byteData.length + 14);
		
		byteBuffer.putInt(this.handlerId.ordinal());
		byteBuffer.putInt(this.ticketId);
		byteBuffer.putInt(this.dataLength);
		byteBuffer.put(this.status);
		byteBuffer.put((byte)this.serializationType.ordinal());
		
		byteBuffer.put(byteData);
		
		byteBuffer.rewind();
		return byteBuffer;
	}
	
	public static Message build(int handlerIdInt, int ticketId, byte status, byte serializationTypeByte, byte[] data) throws JSONException {
		HandlerId handlerId = HandlerId.parse(handlerIdInt);
		SerializationType serializationType = SerializationType.parse(serializationTypeByte);
		
		if(serializationType.equals(SerializationType.JSON)) {
			return new JsonMessage(handlerId, ticketId, data);
		} else {
			return new ProtobuffMessage(handlerId, ticketId, data);
		}
	}
	
	protected int getDataLength() {
		byte[] dataBytes = this.getDataBytes();
		
		if(dataBytes != null) {
			return dataBytes.length;
		} else {
			return 0;
		}
	}
	public abstract byte[] getDataBytes();
}