package offensive.Communicator;

import java.util.HashMap;
import java.util.Map;

public enum SerializationType {
	protobuff, JSON;
	
	private static Map<Byte, SerializationType> intToTypeMap;
	
	private static Map<Byte, SerializationType> getMap() {
		if(SerializationType.intToTypeMap == null) {
			SerializationType.intToTypeMap = new HashMap<Byte, SerializationType>();
			
			for(SerializationType serializationType: SerializationType.values()) {
				SerializationType.intToTypeMap.put((byte)serializationType.ordinal(), serializationType);
			}
		}
		
		return SerializationType.intToTypeMap;
	}
	
	public static SerializationType parse(byte serializationTypeByte) {
		return SerializationType.getMap().get(serializationTypeByte);
	}
}
