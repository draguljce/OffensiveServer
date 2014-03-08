package offensive.Communicator;

import com.google.protobuf.GeneratedMessage;

public class ProtobuffMessage extends Message{

	public GeneratedMessage data;
	
	public ProtobuffMessage(HandlerId handlerId, int ticketId, byte status, GeneratedMessage data) {
		super(handlerId, ticketId, status);

		this.data = data;
		this.dataLength = this.getDataLength();
		this.serializationType = SerializationType.protobuff;
	}

	public ProtobuffMessage(HandlerId handlerId, int ticketId, byte[] data) {
		super(handlerId, ticketId, (byte)0);
		
		this.serializationType = SerializationType.protobuff;
		this.data = this.getDataObject(handlerId, data);
		this.dataLength = this.getDataLength();
	}

	private GeneratedMessage getDataObject(HandlerId handlerId, byte[] data2) {
		GeneratedMessage generatedMessage = null;
		
		switch (handlerId) {
		case RegisterRequest:
			
			break;

		default:
			break;
		}
		
		return generatedMessage;
	}

	@Override
	public byte[] getDataBytes() {
		return this.data.toByteArray();
	}
	
	@Override
	public String toString() {
		String header = super.toString();
		
		return header + "\n" + this.data;
	}
}
