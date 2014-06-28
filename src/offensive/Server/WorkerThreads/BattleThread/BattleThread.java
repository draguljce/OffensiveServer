package offensive.Server.WorkerThreads.BattleThread;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import offensive.Communicator.HandlerId;
import offensive.Communicator.ProtobuffMessage;
import offensive.Communicator.SendableMessage;
import offensive.Server.Server;
import offensive.Server.Hybernate.POJO.Command;
import offensive.Server.Hybernate.POJO.CurrentGame;
import offensive.Server.Hybernate.POJO.Phase;
import offensive.Server.Hybernate.POJO.Territory;
import offensive.Server.Hybernate.POJO.User;
import offensive.Server.Sessions.Session;
import offensive.Server.Sessions.Game.GameManager;
import offensive.Server.Utilities.Callbacks.ZeroParamsCallback;

import org.hibernate.Transaction;

import communication.protos.CommunicationProtos;
import communication.protos.CommunicationProtos.AdvancePhaseNotification;
import communication.protos.CommunicationProtos.AdvanceToNextBattle;
import communication.protos.CommunicationProtos.BorderClashes;
import communication.protos.CommunicationProtos.MultipleAttacks;
import communication.protos.CommunicationProtos.PlayerRolledDice;
import communication.protos.CommunicationProtos.RollDiceClicked;
import communication.protos.CommunicationProtos.SingleAttacks;
import communication.protos.CommunicationProtos.SpoilsOfWar;
import communication.protos.DataProtos;

public class BattleThread implements Runnable {
	private long gameId;
	private CurrentGame game;
	
	private Collection<Session> onlinePlayers;
	
	private ZeroParamsCallback signalFinishCallback;
	
	private Collection<ProtobuffMessage> messages = new LinkedList<>();
	
	public BattleThread(long gameId, ZeroParamsCallback callback) {
		this.gameId = gameId;
		
		this.signalFinishCallback = callback;
		onlinePlayers = GameManager.onlyInstance.getSessionsForGame(gameId);
	}
	
	@Override
	public void run() {
		Transaction tran = null;
		
		try {
			org.hibernate.Session session = Server.getServer().sessionFactory.openSession();
			tran = session.beginTransaction();
			
			this.game = (CurrentGame) session.get(CurrentGame.class, this.gameId);
			
			Set<Command> allCommands = this.game.getCommands();
			Collection<Army> armies = new LinkedList<Army>();
			
			CommunicationProtos.AllCommands.Builder allCommandsBuilder = CommunicationProtos.AllCommands.newBuilder();
			
			allCommandsBuilder.setGameId(this.gameId);
			allCommands.forEach(command -> {
					allCommandsBuilder.addCommands(command.toProtoCommand());
					armies.add(command.toArmy());
				}
			);
			
			SendableMessage allCommandsMessage = new SendableMessage(new ProtobuffMessage(HandlerId.AllCommands, 0, allCommandsBuilder.build()), this.onlinePlayers);
			allCommandsMessage.send();
			
			this.sleep();
			
			Collection<BattleContainer> borderClashes = this.getBorderClashes(armies);
			this.notifyAndExecute(borderClashes, BattleType.borderClash);
			armies.removeIf(army -> army.troopNumber == 0);
			
			this.sleep();
			
			Collection<BattleContainer> multipleAttacks = this.getMultipleAttacks(armies);
			this.notifyAndExecute(multipleAttacks, BattleType.multipleAttack);
			armies.removeIf(army -> army.troopNumber == 0);
			
			this.sleep();
			
			Collection<BattleContainer> singleAttacks = this.getSingleAttacks(armies);
			this.notifyAndExecute(singleAttacks, BattleType.singleAttack);
			armies.removeIf(army -> army.troopNumber == 0);
			
			this.sleep();
			
			Collection<BattleContainer> spoilsOfWar = this.getSpoilsOfWar(armies);
			this.notifyAndExecute(spoilsOfWar, BattleType.spoilsOfWar);
			armies.removeIf(army -> army.troopNumber == 0);
			
			this.sleep();
			
			for(Army army: armies) {
				Territory armyDestination = army.destinationTerritory;
				
				armyDestination.setPlayer(army.player);
				armyDestination.setTroopsOnIt((short)army.troopNumber);
			}
			
			this.advanceToNextPhase(this.game, session).send();
			tran.commit();
		} catch(Exception e) {
			Server.getServer().logger.error(e.getMessage(), e);
			tran.rollback();
		} finally {
			this.signalFinishCallback.call();
		}
	}
	
	private Collection<BattleContainer> getBorderClashes(Collection<Army> armies) {
		HashMap<Long, Army> armyMap = new HashMap<>();
		List<BattleContainer> borderClashes = new LinkedList<BattleContainer>();
		
		for(Army army: armies) {
			int sourceFieldId = army.sourceTerritory.getField().getId();
			int targetFieldId = army.destinationTerritory.getField().getId();
			
			int smallerId = sourceFieldId < targetFieldId ? sourceFieldId : targetFieldId;
			int biggerId = sourceFieldId > targetFieldId ? sourceFieldId : targetFieldId;
			
			long armyId = ((long)smallerId << 32) | (biggerId & 0xFFFFFFFFL);
			
			Army matchedArmy = armyMap.remove(armyId);
			
			if(matchedArmy != null) {
				BattleContainer battleContainer = new BattleContainer();
				
				battleContainer.add(army);
				battleContainer.add(matchedArmy);
				
				battleContainer.oneSide.add(army);
				battleContainer.otherSide.add(matchedArmy);
				
				borderClashes.add(battleContainer);
			} else {
				armyMap.put(armyId, army);
			}
		}
		
		return borderClashes;
	}
	
	private Collection<BattleContainer> getMultipleAttacks(Collection<Army> armies) {
		Collection<BattleContainer> multipleAttacks = new LinkedList<BattleContainer>();
		
		for(LinkedList<Army> multipleAttack: this.armiesWithSameDestination(armies)) {
			BattleContainer battleContainer = new BattleContainer();
			
			battleContainer.armies.addAll(multipleAttack);
			battleContainer.oneSide.addAll(multipleAttack);
			battleContainer.armies.add(multipleAttack.element().destinationTerritory.getArmy());
			battleContainer.otherSide.add(multipleAttack.element().destinationTerritory.getArmy());
			
			multipleAttacks.add(battleContainer);
		}
		
		return multipleAttacks;
	}
	
	private Collection<BattleContainer> getSingleAttacks(Collection<Army> armies) {
		Collection<BattleContainer> singleAttacks = new LinkedList<BattleContainer>();
		HashMap<Territory, Army> destinationTerritories = new HashMap<>();
		
		for(Army army: armies) {
			if(destinationTerritories.containsKey((army.destinationTerritory))) {
				destinationTerritories.remove(army.destinationTerritory);
			} else {
				destinationTerritories.put(army.destinationTerritory, army);
			}
		}
		
		for(Army army: destinationTerritories.values()) {
			BattleContainer battleContainer = new BattleContainer();
			
			battleContainer.armies.add(army);
			battleContainer.oneSide.add(army);
			battleContainer.armies.add(army.destinationTerritory.getArmy());
			battleContainer.otherSide.add(army.destinationTerritory.getArmy());
			
			singleAttacks.add(battleContainer);
		}
		
		return singleAttacks;
	}
	
	private Collection<BattleContainer> getSpoilsOfWar(Collection<Army> armies) {
		Collection<BattleContainer> spoilsOfWar = new LinkedList<BattleContainer>();
		
		for(LinkedList<Army> multipleAttack: this.armiesWithSameDestination(armies)) {
			BattleContainer battleContainer = new BattleContainer();
			
			battleContainer.armies.addAll(multipleAttack);
			
			spoilsOfWar.add(battleContainer);
		}
		
		return spoilsOfWar;
	}
	
	private Collection<LinkedList<Army>> armiesWithSameDestination(Collection<Army> armies) {
		HashSet<Territory> destinationTerritories = new HashSet<>();
		HashMap<Territory, LinkedList<Army>> destinationTerritoryToArmies = new HashMap<Territory, LinkedList<Army>>();
		
		for(Army army: armies) {
			if(destinationTerritories.contains(army.destinationTerritory)) {
				destinationTerritoryToArmies.get(army.destinationTerritory).add(army);
			} else {
				destinationTerritories.add(army.destinationTerritory);
				destinationTerritoryToArmies.put(army.destinationTerritory, new LinkedList<Army>());
			}
		}
		
		return destinationTerritoryToArmies.values();
	}
	
	private void sleep() {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			Server.getServer().logger.error(e.getMessage(), e);
		}
	}

	public void addMessage(ProtobuffMessage message) {
		synchronized (this.messages) {
			this.messages.add(message);
			this.messages.notify();
		}
	}
	
	private void execute(BattleContainer battleContainer) {
		AdvanceToNextBattle.Builder advanceToNextBattleBuilder = AdvanceToNextBattle.newBuilder();
		
		advanceToNextBattleBuilder.setGameId(this.game.getId());
		advanceToNextBattleBuilder.setBattleInfo(battleContainer.toProtoBattleInfo());
		
		SendableMessage advanceToNextBattleMessage = new SendableMessage(new ProtobuffMessage(HandlerId.AdvanceToNextBattle, 0, advanceToNextBattleBuilder.build()), this.onlinePlayers);
		advanceToNextBattleMessage.send();
		
		while(battleContainer.oneSide.size() != 0 && battleContainer.otherSide.size() != 0) {
			this.allUsersRoll(battleContainer);
			
			battleContainer.nextRound();
		}
	}
	
	private void allUsersRoll(BattleContainer commandContainer) {
		HashSet<User> usersThatNeedToRoll = new HashSet<>();
		HashSet<User> offlineUsersThatNeedToRoll = new HashSet<>();
		
		this.populateOnlineAndOfflineUsers(usersThatNeedToRoll, offlineUsersThatNeedToRoll, commandContainer);
		
		this.sleep();
		
		offlineUsersThatNeedToRoll.forEach(user -> this.sendRollDiceMessage(user));
		
		synchronized (this.messages) {
			while (usersThatNeedToRoll.size() != 0) {
				try {
					while(this.messages.size() == 0) {
						this.messages.wait();
					}
				} catch (InterruptedException e) {
					Server.getServer().logger.error(e.getMessage(), e);
					break;
				}
				
				for(ProtobuffMessage message: this.messages) {
					if(RollDiceClicked.class.equals(message.data.getClass())) {
						RollDiceClicked rollDiceClicked = (RollDiceClicked)message.data;
						
						if(rollDiceClicked.getGameId() == this.game.getId() && usersThatNeedToRoll.contains(message.sender.user)) {
							this.sendRollDiceMessage(message.sender.user);
							
							usersThatNeedToRoll.remove(message.sender.user);
						}
					}
				}
				
				this.messages.clear();
			}
		}
	}
	
	public void populateOnlineAndOfflineUsers(Collection<User> onlineUsers, Collection<User> offlineUser, BattleContainer commandContainer) {
		for(Army army: commandContainer.oneSide) {
			boolean isOnline = false;
			for(Session session: this.onlinePlayers) {
				if(session.user.equals(army.player.getUser())) {
					onlineUsers.add(session.user);
					isOnline = true;
					break;
				}
			}
			
			if(!isOnline) {
				offlineUser.add(army.player.getUser());
			}
		}
		
		for(Army army: commandContainer.otherSide) {
			boolean isOnline = false;
			for(Session session: this.onlinePlayers) {
				if(session.user.equals(army.player.getUser())) {
					onlineUsers.add(session.user);
					isOnline = true;
					break;
				}
			}
			
			if(!isOnline) {
				offlineUser.add(army.player.getUser());
			}
		}
	}
	
	private void sendRollDiceMessage(User user) {
		PlayerRolledDice.Builder playerRolledDiceBuilder = PlayerRolledDice.newBuilder();
		playerRolledDiceBuilder.setGameId(this.game.getId());
		playerRolledDiceBuilder.setUser(user.toProtoUser());
		
		SendableMessage playerRolledDiceMessage = new SendableMessage(new ProtobuffMessage(HandlerId.PlayerRolledDice, 0, playerRolledDiceBuilder.build()), this.onlinePlayers);
		playerRolledDiceMessage.send();
	}
	
	private void notifyAndExecute(Collection<BattleContainer> battleContainers, BattleType battleType) {
		Collection<DataProtos.BattleInfo> protoBattleInfos = new LinkedList<DataProtos.BattleInfo>();
		
		battleContainers.forEach(battleContainer -> protoBattleInfos.addAll(protoBattleInfos));
		
		switch (battleType) {
		case borderClash:
			BorderClashes.Builder borderClashesBuilder = BorderClashes.newBuilder();
			borderClashesBuilder.setGameId(this.gameId);
			
			SendableMessage borderClashesMessage = new SendableMessage(new ProtobuffMessage(HandlerId.BorderClashes, 0,borderClashesBuilder.build()), this.onlinePlayers);
			borderClashesMessage.send();
			
			break;
			
		case multipleAttack:
			MultipleAttacks.Builder multipleAttacksBuilder = MultipleAttacks.newBuilder();
			multipleAttacksBuilder.setGameId(this.gameId);
			
			SendableMessage multipleAttacksMessage = new SendableMessage(new ProtobuffMessage(HandlerId.BorderClashes, 0,multipleAttacksBuilder.build()), this.onlinePlayers);
			multipleAttacksMessage.send();
			
			break;

		case singleAttack:
			SingleAttacks.Builder singleAttacksBuilder = SingleAttacks.newBuilder();
			singleAttacksBuilder.setGameId(this.gameId);
			
			SendableMessage singleAttacksMessage = new SendableMessage(new ProtobuffMessage(HandlerId.BorderClashes, 0,singleAttacksBuilder.build()), this.onlinePlayers);
			singleAttacksMessage.send();
			
			break;
			
		case spoilsOfWar:
			SpoilsOfWar.Builder spoilsOfWarBuilder = SpoilsOfWar.newBuilder();
			spoilsOfWarBuilder.setGameId(this.gameId);
			
			SendableMessage spoilsOfWarMessage = new SendableMessage(new ProtobuffMessage(HandlerId.BorderClashes, 0,spoilsOfWarBuilder.build()), this.onlinePlayers);
			spoilsOfWarMessage.send();
			
		default:
			break;
		}

		for(BattleContainer battleContainer: battleContainers) {
			this.execute(battleContainer);
		}
	}
	
	private SendableMessage advanceToNextPhase(CurrentGame game, org.hibernate.Session session) {
		Phase nextPhase = (Phase)session.get(Phase.class, game.getPhase().nextPhaseId());
		game.setPhase(nextPhase);
		
		if(nextPhase.getId() == 1) {
			game.nextRound();
		}
		
		for(offensive.Server.Hybernate.POJO.Player player: game.getPlayers()) {
			player.setIsPlayedMove(false);
		}

		AdvancePhaseNotification.Builder advancePhaseNotificationBuilder = AdvancePhaseNotification.newBuilder();
		
		return new SendableMessage(new ProtobuffMessage(advancePhaseNotificationBuilder.build()), GameManager.onlyInstance.getSessionsForGame(game.getId()));
	}
}