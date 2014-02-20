package offensive.Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import offensive.Server.Utilities.Constants;
import offensive.Server.Utilities.Environment;
import offensive.Server.WorkerThreads.HandlerThread;

public class GameServer extends Server implements Runnable {	
	
	private ExecutorService handlerExecutorService;
	
	private ExecutorService battleExecutorService; 
	
	public GameServer(Environment environment) {
		super(environment);
	}
	
	public void initialize() {
		this.logger.info("Server initialization started.");
		
		super.initialize();
		
		this.logger.info("Initializing handler threads.");
		this.handlerExecutorService = Executors.newFixedThreadPool(Integer.parseInt(this.environment.getVariable(Constants.HandlerThreadNumVarName)));
		this.logger.info("Initialization of handler threads completed. Number of handler threads is " + this.environment.getVariable(Constants.HandlerThreadNumVarName));
		
		this.logger.info("Initializing battle threads.");
		this.battleExecutorService = Executors.newFixedThreadPool(Integer.parseInt(this.environment.getVariable(Constants.BattleThreadNumVarName)));
		this.logger.info("Initialization of handler threads completed. Number of battle threads is " + this.environment.getVariable(Constants.BattleThreadNumVarName));
		
		this.logger.info("Opening server socket on port " + this.environment.getVariable(Constants.PortNumberVarName));
		try {
			serverSocket = new ServerSocket(Integer.parseInt(this.environment.getVariable(Constants.PortNumberVarName)));
		} catch (NumberFormatException | IOException e) {
			this.logger.fatal(e.getMessage(), e);
			System.exit(4);
		}
		this.logger.info("Server socket opened.");
		
		try {
			this.serverSocket.setSoTimeout(Integer.parseInt(this.environment.getVariable(Constants.ServerSocketTimeoutVarName)));
		} catch (NumberFormatException | SocketException e) {
			this.logger.fatal(e.getMessage(), e);
			System.exit(5);
		}
		
		this.logger.info("Server socket timeout is " + this.environment.getVariable(Constants.ServerSocketTimeoutVarName));
		
		this.logger.info("Server initialization completed.");
	}

	@Override
	public void run() {
		this.logger.info("Server is now listening on port " + this.environment.getVariable(Constants.PortNumberVarName));
		
		Socket socket = null;
		while(true) {
			try {	
				try {
					socket = serverSocket.accept();
				} catch (SocketException socketException) {
					if(this.shouldShutdown) {
						this.logger.info("Received shutdown request.");
						try {
							this.logger.info("Waiting for handler threads to finish...");
							this.handlerExecutorService.shutdown();
							this.handlerExecutorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
							
							this.logger.info("Waiting for battle threads to finish...");
							this.battleExecutorService.shutdown();
							this.battleExecutorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
						} catch (InterruptedException e) {
							this.logger.error(e.getMessage(), e);
						}
						
						this.logger.info("Shuting down...");
						return;
					}
					
					continue;
				}
				catch (SocketTimeoutException timeoutException) {
					// TODO: nothing to do here. For now...
					continue;
				}
				
				this.logger.info("Accepted connection from address " + socket.getRemoteSocketAddress());
				this.handlerExecutorService.submit(new HandlerThread(socket));
			} catch (NumberFormatException | IOException e) {
				this.logger.error(e.getMessage(), e);
				System.exit(3);
			}
		}
	}
}
