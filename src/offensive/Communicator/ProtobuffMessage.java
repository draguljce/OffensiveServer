package offensive.Communicator;

import offensive.Server.Server;

import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.InvalidProtocolBufferException;

import communication.protos.CommunicationProtos.AddUnitRequest;
import communication.protos.CommunicationProtos.AttackRequest;
import communication.protos.CommunicationProtos.CreateGameRequest;
import communication.protos.CommunicationProtos.FilterFriendsRequest;
import communication.protos.CommunicationProtos.GetOpenGamesRequest;
import communication.protos.CommunicationProtos.GetUserDataRequest;
import communication.protos.CommunicationProtos.InvokeAllianceRequest;
import communication.protos.CommunicationProtos.JoinGameRequest;
import communication.protos.CommunicationProtos.MoveUnitsRequest;
import communication.protos.CommunicationProtos.RollDiceRequest;
import communication.protos.CommunicationProtos.TradeCardsRequest;

public class ProtobuffMessage extends Message{

	public GeneratedMessage data;
	
	public ProtobuffMessage(HandlerId handlerId, int ticketId, byte status, GeneratedMessage data) {
		super(handlerId, ticketId, status);

		this.data = data;
		this.dataLength = this.getDataLength();
		this.serializationType = SerializationType.protobuff;
	}

	public ProtobuffMessage(HandlerId handlerId, int ticketId) {
		super(handlerId, ticketId, (byte)0);
		
		this.serializationType = SerializationType.protobuff;
	}
	
	public ProtobuffMessage(HandlerId handlerId, int ticketId, GeneratedMessage message) {
		super(handlerId, ticketId, (byte)0);
		
		this.serializationType = SerializationType.protobuff;
		this.data = message;
		this.dataLength = this.getDataLength();
	}
	
	public ProtobuffMessage(HandlerId handlerId, int ticketId, byte[] data) {
		super(handlerId, ticketId, (byte)0);
		
		this.serializationType = SerializationType.protobuff;
		this.data = this.getDataObject(handlerId, data);
		this.dataLength = this.getDataLength();
	}

	private GeneratedMessage getDataObject(HandlerId handlerId, byte[] data) {
		GeneratedMessage generatedMessage = null;
		
		try {
			switch (handlerId) {
			case GetUserDataRequest:
				generatedMessage = GetUserDataRequest.parseFrom(data);
				break;

			case FilterFriendsRequest:
				generatedMessage = FilterFriendsRequest.parseFrom(data);
				break;
				
			case CreateGameRequest:
				generatedMessage = CreateGameRequest.parseFrom(data);
				break;
				
			case GetOpenGamesRequest:
				generatedMessage = GetOpenGamesRequest.parseFrom(data);
				break;
				
			case JoinGameRequest:
				generatedMessage = JoinGameRequest.parseFrom(data);
				break;
				
			case InvokeAllianceRequest:
				generatedMessage = InvokeAllianceRequest.parseFrom(data);
				break;
				
			case TradeCardsRequest:
				generatedMessage = TradeCardsRequest.parseFrom(data);
				break;
				
			case AddUnitRequest:
				generatedMessage = AddUnitRequest.parseFrom(data);
				break;
			
			case MoveUnitsRequest:
				generatedMessage = MoveUnitsRequest.parseFrom(data);
				break;
			
			case AttackRequest:
				generatedMessage = AttackRequest.parseFrom(data);
				break;
				
			case RollDiceRequest:
				generatedMessage = RollDiceRequest.parseFrom(data);
				break;

			default:
				break;
			}
		} catch (InvalidProtocolBufferException e) {
			Server.getServer().logger.error(e.getMessage(), e);
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
		
		return header + this.data;
	}
}
