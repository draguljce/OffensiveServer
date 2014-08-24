package offensive.Server.WorkerThreads.BattleThread;

import java.util.ArrayList;
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
import offensive.Server.Exceptions.FatalErrorException;
import offensive.Server.Hybernate.POJO.Command;
import offensive.Server.Hybernate.POJO.CurrentGame;
import offensive.Server.Hybernate.POJO.Phase;
import offensive.Server.Hybernate.POJO.Territory;
import offensive.Server.Sessions.Session;
import offensive.Server.Sessions.Game.GameManager;
import offensive.Server.Utilities.Constants;
import offensive.Server.Utilities.Callbacks.ZeroParamsCallback;

import org.hibernate.Transaction;

import communication.protos.CommunicationProtos;
import communication.protos.CommunicationProtos.AdvancePhaseNotification;
import communication.protos.CommunicationProtos.AdvanceToNextBattle;
import communication.protos.CommunicationProtos.BorderClashes;
import communication.protos.CommunicationProtos.MultipleAttacks;
import communication.protos.CommunicationProtos.PlayerRolledDice;
import communication.protos.CommunicationProtos.RollDiceClicked;
import communication.protos.CommunicationProtos.RollDiceClickedResponse;
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
		org.hibernate.Session session = Server.getServer().sessionFactory.openSession();
		
		try {
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
			
			this.sleep(1000);
			
			Collection<BattleContainer> borderClashes = this.getBorderClashes(armies);
			this.notifyAndExecute(borderClashes, BattleType.borderClash);
			armies.removeIf(army -> army.troopNumber == 0);
			
			this.sleep(1000);
			
			Collection<BattleContainer> multipleAttacks = this.getMultipleAttacks(armies);
			this.notifyAndExecute(multipleAttacks, BattleType.multipleAttack);
			armies.removeIf(army -> army.troopNumber == 0);
			
			this.sleep(1000);
			
			Collection<BattleContainer> singleAttacks = this.getSingleAttacks(armies);
			this.notifyAndExecute(singleAttacks, BattleType.singleAttack);
			armies.removeIf(army -> army.troopNumber == 0);
			
			this.sleep(1000);
			
			Collection<BattleContainer> spoilsOfWar = this.getSpoilsOfWar(armies);
			this.notifyAndExecute(spoilsOfWar, BattleType.spoilsOfWar);
			armies.removeIf(army -> army.troopNumber == 0);
			
			this.sleep(1000);
			
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
			session.close();
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
		HashSet<Integer> destinationTerritories = new HashSet<>();
		HashMap<Integer, LinkedList<Army>> destinationTerritoryToArmies = new HashMap<Integer, LinkedList<Army>>();
		
		for(Army army: armies) {
			if(destinationTerritories.contains(army.destinationTerritory.getField().getId())) {
				destinationTerritoryToArmies.get(army.destinationTerritory.getField().getId()).add(army);
			} else {
				destinationTerritories.add(army.destinationTerritory.getField().getId());
				LinkedList<Army> armyCol = new LinkedList<Army>();
				armyCol.add(army);
				destinationTerritoryToArmies.put(army.destinationTerritory.getField().getId(), armyCol);
			}
		}
		
		LinkedList<LinkedList<Army>> armyCol = new LinkedList<LinkedList<Army>>();
		
		for(LinkedList<Army> allArmies :destinationTerritoryToArmies.values()) {
			if(allArmies.size() > 1) {
				armyCol.add(allArmies);
			}
		}
		
		return armyCol;
	}
	
	private void sleep(long milis) {
		try {
			Thread.sleep(milis);
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
	
	private void execute(BattleContainer battleContainer) throws FatalErrorException {
		AdvanceToNextBattle.Builder advanceToNextBattleBuilder = AdvanceToNextBattle.newBuilder();
		
		advanceToNextBattleBuilder.setGameId(this.game.getId());
		advanceToNextBattleBuilder.setBattleInfo(battleContainer.toProtoBattleInfo());
		
		SendableMessage advanceToNextBattleMessage = new SendableMessage(new ProtobuffMessage(HandlerId.AdvanceToNextBattle, 0, advanceToNextBattleBuilder.build()), this.onlinePlayers);
		advanceToNextBattleMessage.send();
		
		this.sleep(1000);
		
		while(battleContainer.oneSide.size() != 0 && battleContainer.otherSide.size() != 0) {
			this.allUsersRoll(battleContainer);
			
			battleContainer.nextRound();
			
			this.sleep(2000);
		}
		
		this.sleep(1000);
	}
	
	private void allUsersRoll(BattleContainer commandContainer) throws FatalErrorException {
		ArrayList<Army> armiesThatNeedToRoll = new ArrayList<>();
		HashSet<Army> offlineArmiesThatNeedToRoll = new HashSet<>();
		
		this.populateOnlineAndOfflineUsers(armiesThatNeedToRoll, offlineArmiesThatNeedToRoll, commandContainer);
		
		long startTime = System.currentTimeMillis();
		long endTime = startTime + Constants.UserWaitTime;
		long waitTime = 2000;
		
		synchronized (this.messages) {
			while (armiesThatNeedToRoll.size() != 0 && waitTime > 0) {
				try {
					while(this.messages.size() == 0 && waitTime > 0) {
						this.messages.wait(waitTime);
						waitTime = endTime - System.currentTimeMillis();
					}
				} catch (InterruptedException e) {
					Server.getServer().logger.error(e.getMessage(), e);
					break;
				}
				
				if(System.currentTimeMillis() - startTime > 2000) {
					for(Army army :offlineArmiesThatNeedToRoll) {
						this.sendRollDiceMessage(army.sourceTerritory.getField().getId(), null);
					}
				}
				
				for(ProtobuffMessage message: this.messages) {
					Server.getServer().logger.info("Battle thread received new message.");
					
					if(RollDiceClicked.class.equals(message.data.getClass())) {
						RollDiceClicked rollDiceClicked = (RollDiceClicked)message.data;

						if(rollDiceClicked.getGameId() == this.game.getId()) {
							if (armiesThatNeedToRoll.removeIf(army -> 	army.player.getUser().getId() == message.sender.user.getId() &&
																		army.sourceTerritory.getField().getId() == rollDiceClicked.getTerritoryId())) {
								
								RollDiceClickedResponse.Builder rollDiceClickedResponseBuilder = RollDiceClickedResponse.newBuilder();

								SendableMessage rolleDiceClickedResponseMessage = new SendableMessage(new ProtobuffMessage(HandlerId.RollDiceClicked, message.ticketId, rollDiceClickedResponseBuilder.build()), message.sender);
								rolleDiceClickedResponseMessage.send();

								Server.getServer().logger.info("Broadcasting rolle dice message");
								this.sendRollDiceMessage(rollDiceClicked.getTerritoryId(), message.sender);
								
								armiesThatNeedToRoll.remove(message.sender.user);
							}
						}
					}
				}
				
				this.messages.clear();
			}
		}
		
		Server.getServer().logger.info("User timeout exceeded broadcasting roll dice messages.");

		// Enough waiting. Just go ahead and send RollDiceMessage.
		for(Army army : armiesThatNeedToRoll) {
			this.sendRollDiceMessage(army.sourceTerritory.getField().getId(), null);
		}
	}
	
	public void populateOnlineAndOfflineUsers(Collection<Army> onlinePlayers, Collection<Army> offlinePlayer, BattleContainer commandContainer) {
		for(Army army: commandContainer.oneSide) {
			boolean isOnline = false;
			for(Session session: this.onlinePlayers) {
				if(session.user.getId() == army.player.getUser().getId()) {
					onlinePlayers.add(army);
					isOnline = true;
					break;
				}
			}
			
			if(!isOnline) {
				offlinePlayer.add(army);
			}
		}
		
		for(Army army: commandContainer.otherSide) {
			boolean isOnline = false;
			for(Session session: this.onlinePlayers) {
				if(session.user.getId() == army.player.getUser().getId()) {
					onlinePlayers.add(army);
					isOnline = true;
					break;
				}
			}
			
			if(!isOnline) {
				offlinePlayer.add(army);
			}
		}
	}
	
	private void sendRollDiceMessage(int territoryId, Session sender) throws FatalErrorException {
		PlayerRolledDice.Builder playerRolledDiceBuilder = PlayerRolledDice.newBuilder();
		playerRolledDiceBuilder.setGameId(this.game.getId());
		playerRolledDiceBuilder.setTerritoryId(territoryId);
		
		Collection<Session> recepients = new ArrayList<>(this.onlinePlayers);
		
		if(sender != null) {
			recepients.removeIf(recepient -> recepient.user.getId() == sender.user.getId());
		}
		
		SendableMessage playerRolledDiceMessage = new SendableMessage(new ProtobuffMessage(HandlerId.PlayerRolledDice, 0, playerRolledDiceBuilder.build()), recepients);
		playerRolledDiceMessage.send();
	}
	
	private void notifyAndExecute(Collection<BattleContainer> battleContainers, BattleType battleType) throws FatalErrorException {
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
			
			SendableMessage multipleAttacksMessage = new SendableMessage(new ProtobuffMessage(HandlerId.MultipleAttacks, 0,multipleAttacksBuilder.build()), this.onlinePlayers);
			multipleAttacksMessage.send();
			
			break;

		case singleAttack:
			SingleAttacks.Builder singleAttacksBuilder = SingleAttacks.newBuilder();
			singleAttacksBuilder.setGameId(this.gameId);
			
			SendableMessage singleAttacksMessage = new SendableMessage(new ProtobuffMessage(HandlerId.SingleAttacks, 0,singleAttacksBuilder.build()), this.onlinePlayers);
			singleAttacksMessage.send();
			
			break;
			
		case spoilsOfWar:
			SpoilsOfWar.Builder spoilsOfWarBuilder = SpoilsOfWar.newBuilder();
			spoilsOfWarBuilder.setGameId(this.gameId);
			
			SendableMessage spoilsOfWarMessage = new SendableMessage(new ProtobuffMessage(HandlerId.SpoilsOfWar, 0,spoilsOfWarBuilder.build()), this.onlinePlayers);
			spoilsOfWarMessage.send();
			
			break;
			
		default:
			break;
		}

		for(BattleContainer battleContainer: battleContainers) {
			this.execute(battleContainer);
		}
	}
	
	private SendableMessage advanceToNextPhase(CurrentGame game, org.hibernate.Session session) {
		AdvancePhaseNotification.Builder advancePhaseNotificationBuilder = AdvancePhaseNotification.newBuilder();
		
		advancePhaseNotificationBuilder.setGameId(game.getId());
		
		Phase nextPhase = (Phase)session.get(Phase.class, game.getPhase().nextPhaseId());
		game.setPhase(nextPhase);
		
		for(offensive.Server.Hybernate.POJO.Player player: game.getPlayers()) {
			player.setIsPlayedMove(false);
		}

		session.update(game);
		
		return new SendableMessage(new ProtobuffMessage(HandlerId.AdvancePhaseNotification, 0, advancePhaseNotificationBuilder.build()), GameManager.onlyInstance.getSessionsForGame(game.getId()));
	}
}