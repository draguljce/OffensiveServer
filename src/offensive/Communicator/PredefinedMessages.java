package offensive.Communicator;

public class PredefinedMessages {
	
	public static Message getUnkownErrorMessage(HandlerId handlerId, int ticketId) {
		return new Message(handlerId, ticketId, null);
	}
}
