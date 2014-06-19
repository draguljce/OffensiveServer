package offensive.Communicator;

import java.util.HashMap;
import java.util.Map;

public enum HandlerId {
	RegisterRequest,			//	0
	NoFacebookLoginRequest,		//	1
	FacebookLoginRequest, 		//	2
	GetUserDataRequest,			//	3
	FilterFriendsRequest,		//	4
	CreateGameRequest,			//	5
	GetOpenGamesRequest,		//	6
	JoinGameRequest,			//	7
	InvokeAllianceRequest,		//	8
	TradeCardsRequest,			//	9
	AddUnitRequest,				//	10
	MoveUnitsRequest,			//	11
	AttackRequest,				//	12
	CommandsSubmittedRequest,	//	13
	AllCommands,				//	14
	BorderClashes,				//	15
	AdvanceToNextBattle,		//	16
	RollDiceClicked,			//	17
	PlayerRolledDice,			//	18
	JoinGameNotification;		//	19
	
	private static Map<Integer, HandlerId> intToHandlerMap;
	
	private static Map<Integer, HandlerId> getMap() {
		if(HandlerId.intToHandlerMap == null) {
			HandlerId.intToHandlerMap = new HashMap<Integer, HandlerId>();
			
			for(HandlerId handlerId: HandlerId.values()) {
				HandlerId.intToHandlerMap.put(handlerId.ordinal(), handlerId);
			}
		}
		
		return HandlerId.intToHandlerMap;
	}
	
	public static HandlerId parse(int handlerId) {
		return HandlerId.getMap().get(handlerId);
	}
}