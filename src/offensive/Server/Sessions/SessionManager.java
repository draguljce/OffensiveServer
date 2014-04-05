package offensive.Server.Sessions;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import offensive.Communicator.Communicator;
import offensive.Communicator.ProtobuffMessage;
import offensive.Server.Server;
import offensive.Server.Utilities.Constants;
import offensive.Server.Utilities.Environment;
import offensive.Server.WorkerThreads.HandlerThread;

public class SessionManager implements Runnable{
	private Selector selector;
	private HashSet<Session> allSessions;
	
	private Environment environment;
	
	private ExecutorService handlerExecutorService;
	
	private ExecutorService battleExecutorService;
	
	private boolean shouldShutdown;
	private Thread sessionThread;
	
	private static SessionManager onlyInstance = new SessionManager();
	
	private List<SocketChannel> newChannels = new LinkedList<SocketChannel>();
	
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
	
	private SessionManager() {
		this.allSessions = new HashSet<Session>();
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
	
	private void register(SocketChannel socketChannel) {
		try {
			SelectionKey key = socketChannel.register(this.selector, SelectionKey.OP_READ);
			
			key.attach(new Session(socketChannel));
			this.allSessions.add(new Session(socketChannel));
			
			try {
				Server.getServer().logger.info("Created new session for client: " + socketChannel.getRemoteAddress());
			} catch (IOException e) {
				Server.getServer().logger.error(e.getMessage(), e);
			}
		} catch (ClosedChannelException e) {
			Server.getServer().logger.error(e.getMessage(), e);
		}
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
					} catch (Exception e) {
						Server.getServer().logger.error(e.getMessage(), e);
						continue;
					}
				} else {
					Server.getServer().logger.info("Socket closed. Removing selection key.");
					this.removeKey(key);
					
					continue;
				}
				
				this.handlerExecutorService.submit(new HandlerThread(session, request));
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
	
	private void removeKey(SelectionKey key) {
		key.cancel();
		try {
			key.channel().close();
		} catch (IOException e) {
			Server.getServer().logger.error(e.getMessage(), e);
		}
	}
}
