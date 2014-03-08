package offensive.Communicator;

import org.json.JSONException;
import org.json.JSONObject;

public class JsonMessage extends Message{

	private JSONObject data;
	
	public JsonMessage(HandlerId handlerId, int ticketId) {
		super(handlerId, ticketId, (byte)0);
		
		this.serializationType = SerializationType.JSON;
	}
	
	public JsonMessage(HandlerId handlerId, int ticketId, byte[] data) throws JSONException {
		super(handlerId, ticketId, (byte)0);
		
		this.data = new JSONObject(new String(data));
		this.dataLength = this.getDataLength();
		this.serializationType = SerializationType.JSON;
	}
	
	public JsonMessage(HandlerId handlerId, int ticketId, int dataLength, byte status, JSONObject data) {
		super(handlerId, ticketId, status);
		
		this.data = data;
		this.serializationType = SerializationType.JSON;
	}

	@Override
	public byte[] getDataBytes() {
		return this.data.toString().getBytes();
	}
	
	@Override
	public String toString() {
		String header = super.toString();
		
		return header + "\n" + this.data;
	}
	
	public JSONObject getData() {
		return this.data;
	}
	
	public void setData(JSONObject data) {
		this.data = data;
		this.dataLength = this.getDataLength();
	}
}
