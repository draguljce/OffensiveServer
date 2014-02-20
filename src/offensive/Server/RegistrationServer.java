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
import offensive.Server.WorkerThreads.RegistrationThread;

public class RegistrationServer extends Server implements Runnable {	
	
	private ExecutorService registrationExecutorService;
	
	public RegistrationServer(Environment environment) {
		super(environment);
	}
	
	public void initialize() {
		this.logger.info("Server initialization started.");
		
		super.initialize();
		
		this.logger.info("Initializing registration threads.");
		this.registrationExecutorService = Executors.newFixedThreadPool(Integer.parseInt(this.environment.getVariable(Constants.RegistrationThreadNumVarName)));
		this.logger.info("Initialization of registration threads completed. Number of handler threads is " + this.environment.getVariable(Constants.RegistrationThreadNumVarName));
		
		this.logger.info("Opening server socket on port " + this.environment.getVariable(Constants.RegistrationPortNumberVarName));
		try {
			this.serverSocket = new ServerSocket(Integer.parseInt(this.environment.getVariable(Constants.RegistrationPortNumberVarName)));
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
		this.logger.info("Server is now listening on port " + this.environment.getVariable(Constants.RegistrationPortNumberVarName));
		
		Socket socket = null;
		while(true) {
			try {	
				try {
					socket = this.serverSocket.accept();
				} catch (SocketException socketException) {
					if(this.shouldShutdown) {
						this.logger.info("Received shutdown request.");
						try {
							this.logger.info("Waiting for handler threads to finish...");
							this.registrationExecutorService.shutdown();
							this.registrationExecutorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
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
				this.registrationExecutorService.submit(new RegistrationThread(socket));
			} catch (NumberFormatException | IOException e) {
				this.logger.error(e.getMessage(), e);
				System.exit(3);
			}
		}
	}
}
