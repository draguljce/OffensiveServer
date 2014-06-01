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
import offensive.Server.Hybernate.POJO.User;
import offensive.Server.Sessions.Session;
import offensive.Server.Sessions.Game.GameManager;
import offensive.Server.Utilities.Callbacks.ZeroParamsCallback;
import communication.protos.CommunicationProtos;
import communication.protos.CommunicationProtos.AdvanceToNextBattle;
import communication.protos.CommunicationProtos.BorderClashes;
import communication.protos.CommunicationProtos.PlayerRolledDice;
import communication.protos.CommunicationProtos.RollDiceClicked;

public class BattleThread implements Runnable {
	private CurrentGame game;
	
	private Collection<Session> onlinePlayers;
	
	private ZeroParamsCallback signalFinishCallback;
	
	private Collection<ProtobuffMessage> messages = new LinkedList<>();
	
	public BattleThread(CurrentGame game, ZeroParamsCallback callback) {
		this.game = game;
		
		this.signalFinishCallback = callback;
		onlinePlayers = GameManager.onlyInstance.getSessionsForGame(game.getId());
	}
	
	@Override
	public void run() {
		Set<Command> allCommands = this.game.getCommands();
		CommunicationProtos.AllCommands.Builder allCommandsBuilder = CommunicationProtos.AllCommands.newBuilder();
		
		for(Command command: allCommands) {
			allCommandsBuilder.addCommands(command.toProtoCommand());
		}
		
		SendableMessage allCommandsMessage = new SendableMessage(new ProtobuffMessage(HandlerId.AllCommands, 0, allCommandsBuilder.build()), this.onlinePlayers);
		allCommandsMessage.send();
		
		this.sleep();
		
		List<CommandContainer> borderClashes = this.removeBorderClashes(allCommands);
		BorderClashes.Builder borderClashesBuilder = BorderClashes.newBuilder();
		
		for(CommandContainer commandContainer: borderClashes) {
			borderClashesBuilder.addBattleInfo(commandContainer.toProtoBattleInfo());
		}
		
		SendableMessage borderClashesMessage = new SendableMessage(new ProtobuffMessage(HandlerId.BorderClashes, 0,borderClashesBuilder.build()), this.onlinePlayers);
		borderClashesMessage.send();
		
		for(CommandContainer commandContainer: borderClashes) {
			this.execute(commandContainer);
		}
		
		this.sleep();
		
		this.signalFinishCallback.call();
	}
	
	private List<CommandContainer> removeBorderClashes(Collection<Command> commands) {
		HashMap<Long, Command> commandsMap = new HashMap<>();
		List<CommandContainer> borderClashes = new LinkedList<CommandContainer>();
		
		for(Command command: commands) {
			int sourceFieldId = command.getSource().getId();
			int targetFieldId = command.getDestination().getId();
			
			int smallerId = sourceFieldId < targetFieldId ? sourceFieldId : targetFieldId;
			int biggerId = sourceFieldId > targetFieldId ? sourceFieldId : targetFieldId;
			
			long commandId = ((long)smallerId << 32) | (biggerId & 0xFFFFFFFFL);
			
			Command matchedCommand = commandsMap.remove(commandId);
			
			if(matchedCommand != null) {
				CommandContainer commandContainer = new CommandContainer(true);
				
				commandContainer.add(command);
				commandContainer.add(matchedCommand);
				
				commandContainer.oneSide.add(new Army(command));
				commandContainer.otherSide.add(new Army(matchedCommand));
				
				commands.remove(command);
				commands.remove(matchedCommand);
				
				borderClashes.add(commandContainer);
			} else {
				commandsMap.put(commandId, command);
			}
		}
		
		return borderClashes;
	}
	
	private void sleep() {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			Server.getServer().logger.error(e.getMessage(), e);
		}
	}

	public void addMessage(ProtobuffMessage message) {
		this.messages.add(message);
		this.messages.notify();
	}
	
	private void execute(CommandContainer commandContainer) {
		AdvanceToNextBattle.Builder advanceToNextBattleBuilder = AdvanceToNextBattle.newBuilder();
		
		advanceToNextBattleBuilder.setGameId(this.game.getId());
		
		HashSet<User> usersThatNeedToRoll = new HashSet<>();
		HashSet<User> offlineUsersThatNeedToRoll = new HashSet<>();
		
		for(Army army: commandContainer.oneSide) {
			boolean isOnline = false;
			for(Session session: this.onlinePlayers) {
				if(session.user.equals(army.player.getUser())) {
					usersThatNeedToRoll.add(session.user);
					isOnline = true;
					break;
				}
			}
			
			if(!isOnline) {
				offlineUsersThatNeedToRoll.add(army.player.getUser());
			}
		}
		
		for(Army army: commandContainer.otherSide) {
			boolean isOnline = false;
			for(Session session: this.onlinePlayers) {
				if(session.user.equals(army.player.getUser())) {
					usersThatNeedToRoll.add(session.user);
					isOnline = true;
					break;
				}
			}
			
			if(!isOnline) {
				offlineUsersThatNeedToRoll.add(army.player.getUser());
			}
		}
		
		advanceToNextBattleBuilder.setBattleInfo(commandContainer.toProtoBattleInfo());
		
		SendableMessage advanceToNextBattleMessage = new SendableMessage(new ProtobuffMessage(HandlerId.AdvanceToNextBattle, 0, advanceToNextBattleBuilder.build()), this.onlinePlayers);
		advanceToNextBattleMessage.send();
		
		this.sleep();
				
		synchronized (this.messages) {
			
			while (usersThatNeedToRoll.size() != 0) {
				for(ProtobuffMessage message: this.messages) {
					if(RollDiceClicked.class.equals(message.data.getClass())) {
						RollDiceClicked rollDiceClicked = (RollDiceClicked)message.data;
						
						if(rollDiceClicked.getGameId() == this.game.getId() && usersThatNeedToRoll.contains(message.sender.user)) {
							PlayerRolledDice.Builder playerRolledDiceBuilder = PlayerRolledDice.newBuilder();
							playerRolledDiceBuilder.setGameId(this.game.getId());
							playerRolledDiceBuilder.setUser(message.sender.user.toProtoUser());
							
							SendableMessage playerRolledDiceMessage = new SendableMessage(new ProtobuffMessage(HandlerId.PlayerRolledDice, 0, playerRolledDiceBuilder.build()), this.onlinePlayers);
							usersThatNeedToRoll.remove(message.sender.user);
						}
					}
				}
				
				try {
					this.messages.wait();
				} catch (InterruptedException e) {
					Server.getServer().logger.error(e.getMessage(), e);
					break;
				}
			}
		}		
	}
}