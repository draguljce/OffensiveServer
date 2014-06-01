package offensive.Server.Sessions;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import offensive.Communicator.Communicator;
import offensive.Communicator.ProtobuffMessage;
import offensive.Server.Server;
import offensive.Server.Hybernate.POJO.CurrentGame;
import offensive.Server.Sessions.Game.GameManager;
import offensive.Server.Utilities.Constants;
import offensive.Server.Utilities.Environment;
import offensive.Server.Utilities.Callbacks.ZeroParamsCallback;
import offensive.Server.WorkerThreads.HandlerThread;
import offensive.Server.WorkerThreads.BattleThread.BattleThread;

public class SessionManager implements Runnable{
	private Selector selector;
	
	private Environment environment;
	
	private ExecutorService handlerExecutorService;
	
	private ExecutorService battleExecutorService;
	
	private boolean shouldShutdown;
	private Thread sessionThread;
	
	private static SessionManager onlyInstance = new SessionManager();
	
	private List<SocketChannel> newChannels = new LinkedList<SocketChannel>();
	
	private HashMap<Long, BattleThread> battleThreadMap = new HashMap<>();
	
	public void initialize(Environment environment, Thread sessionThread) {
		this.environment = environment;
		
		Server.getServer().logger.info("Initializing handler threads.");
		this.handlerExecutorService = Executors.newFixedThreadPool(Integer.parseInt(this.environment.getVariable(Constants.HandlerThreadNumVarName)));
		Server.getServer().logger.info("Initialization of handler threads completed. Number of handler threads is " + this.environment.getVariable(Constants.HandlerThreadNumVarName));
		
		Server.getServer().logger.info("Initializing battle threads.");
		this.battleExecutorService = Executors.newFixedThreadPool(Integer.parseInt(this.environment.getVariable(Constants.BattleThreadNumVarName)));
		Server.getServer().logger.info("Initialization of handler threads completed. Number of battle threads is " + this.environment.getVariable(Constants.BattleThreadNumVarName));
		
		this.sessionThread = sessionThread;
	}
	
	public void CreateSession(SocketChannel socketChannel) {
			try {
				socketChannel.configureBlocking(false);
				
				synchronized(this.newChannels) {
					this.newChannels.add(socketChannel);
				}
				
				this.selector.wakeup();
			} catch (IOException e) {
				Server.getServer().logger.error(e.getMessage(), e);
				return;
			}
		
	}
	
	private void registerNewChannels() {
		synchronized(this.newChannels) {
			for(SocketChannel channel : this.newChannels) {
				this.register(channel);
			}
			
			this.newChannels.clear();
		}
	}
	
	private Session register(SocketChannel socketChannel) {
		Session newSession = null;

		try {
			SelectionKey key = socketChannel.register(this.selector, SelectionKey.OP_READ);
			
			newSession = new Session(socketChannel); 
			key.attach(newSession);
			
			try {
				Server.getServer().logger.info("Created new session for client: " + socketChannel.getRemoteAddress());
			} catch (IOException e) {
				Server.getServer().logger.error(e.getMessage(), e);
			}
		} catch (ClosedChannelException e) {
			Server.getServer().logger.error(e.getMessage(), e);
		}
		
		return newSession;
	}
	
	public static SessionManager getOnlyInstance() {
		return SessionManager.onlyInstance;
	}

	@Override
	public void run() {
		try {
			this.selector = Selector.open();
		} catch (IOException e) {
			Server.getServer().logger.error(e.getMessage(), e);
		}
		
		while(true) {
			try {
				this.selector.select();
				
				this.registerNewChannels();
				
				if(this.shouldShutdown) {
					Server.getServer().logger.info("Received shutdown request.");
					try {
						Server.getServer().logger.info("Waiting for handler threads to finish...");
						this.handlerExecutorService.shutdown();
						this.handlerExecutorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
						
						Server.getServer().logger.info("Waiting for battle threads to finish...");
						this.battleExecutorService.shutdown();
						this.battleExecutorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
					} catch (InterruptedException e) {
						Server.getServer().logger.error(e.getMessage(), e);
					}
					
					Server.getServer().logger.info("Shuting down...");
					return;
				}
				
				Server.getServer().logger.info("Received client request(s)");
			} catch (IOException e1) {
				Server.getServer().logger.error(e1.getMessage(), e1);
			}
			
			for(SelectionKey key :this.selector.selectedKeys()) {
				Session session = (Session)key.attachment();
				ProtobuffMessage request;
				
				if(session.socketChannel.isOpen()) {
					try {
						request = (ProtobuffMessage)Communicator.getCommunicator().acceptMessage(session.socketChannel);
						
						if(request == null) {
							Server.getServer().logger.error("Failed to read client message. Closing channel.");
							
							this.removeKey(key);
							continue;
						}
						
						request.sender = session;
						
						if(request.IsBattleThreadMessage) {
							this.battleThreadMap.get(request.gameId).addMessage(request);
						} else {
							this.handlerExecutorService.submit(new HandlerThread(key, request));
						}
					} catch (Exception e) {
						Server.getServer().logger.error(e.getMessage(), e);
						continue;
					}
				} else {
					Server.getServer().logger.info("Socket closed. Removing selection key.");
					this.removeKey(key);
					
					continue;
				}
			}
			
			this.selector.selectedKeys().clear();
		}
	}
	
	public void shutdown() {
		this.shouldShutdown = true;
		
		try {
			this.selector.close();
		} catch (IOException e) {
			Server.getServer().logger.error(e.getMessage(), e);
		} 
		finally {
			// Wait for up to 30 minutes for shutdown to complete.
			try {
				this.sessionThread.join(30 * 60 * 1000);
			} catch (InterruptedException e) {
				Server.getServer().logger.error(e.getMessage(), e);
			}
		}
	}
	
	public void removeKey(SelectionKey key) {
		key.cancel();
		
		GameManager.onlyInstance.removeGames((Session)key.attachment());
		
		try {
			key.channel().close();
		} catch (IOException e) {
			Server.getServer().logger.error(e.getMessage(), e);
		}
	}
	
	public void startBattle(CurrentGame game) {
		ZeroParamsCallback callback = new ZeroParamsCallback()  {
			private HashMap<Long, BattleThread> map;
			private long gameId;
			
			ZeroParamsCallback initialise(HashMap<Long, BattleThread> map, long gameId) {
				this.map = map;
				this.gameId = gameId;
				
				return this;
			}
			
			@Override
			public void call() {
				this.map.remove(this.gameId);
			}
		}.initialise(this.battleThreadMap, game.getId());
		
		BattleThread battleThread = new BattleThread(game, callback);
		Thread newBattleThread = new Thread(battleThread);
		
		this.battleThreadMap.put(game.getId(), battleThread);
		
		this.battleExecutorService.submit(newBattleThread);
	}
}
