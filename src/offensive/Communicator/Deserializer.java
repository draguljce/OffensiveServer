package offensive.Communicator;

import com.google.protobuf.GeneratedMessage;

public interface Deserializer {
	public GeneratedMessage deserialize(HandlerId handlerId, byte[] data);
}
