package offensive.Server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import offensive.Server.Utilities.Constants;
import offensive.Server.Utilities.Environment;
import offensive.Server.WorkerThreads.RegistrationThread;

public class RegistrationServer extends Server implements Runnable {	
	
	private ExecutorService registrationExecutorService;
	
	public RegistrationServer(Environment environment) {
		super(environment);
	}
	
	public void initialize(Thread serverThread) {
		this.logger.info("Server initialization started.");
		
		super.initialize(serverThread);
		
		this.logger.info("Initializing registration threads.");
		this.registrationExecutorService = Executors.newFixedThreadPool(Integer.parseInt(this.environment.getVariable(Constants.RegistrationThreadNumVarName)));
		this.logger.info("Initialization of registration threads completed. Number of handler threads is " + this.environment.getVariable(Constants.RegistrationThreadNumVarName));
		
		this.logger.info("Opening server socket on port " + this.environment.getVariable(Constants.RegistrationPortNumberVarName));
		try {
			this.serverSocketChannel = ServerSocketChannel.open();
		} catch (NumberFormatException | IOException e) {
			this.logger.fatal(e.getMessage(), e);
			System.exit(4);
		}
		this.logger.info("Server socket opened.");
		this.logger.info("Server socket timeout is " + this.environment.getVariable(Constants.ServerSocketTimeoutVarName));
		this.logger.info("Server initialization completed.");
	}

	
	@Override
	public void run() {
		this.logger.info("Server is now listening on port " + this.environment.getVariable(Constants.RegistrationPortNumberVarName));
		
		try {
			this.serverSocketChannel.bind(new InetSocketAddress(Integer.parseInt(this.environment.getVariable(Constants.RegistrationPortNumberVarName))));
		} catch (NumberFormatException | IOException e1) {
			this.logger.error(e1.getMessage(), e1);
			return;
		}
		
		SocketChannel socketChannel = null;
		while(true) {
			try {	
				try {
					socketChannel = this.serverSocketChannel.accept();
				} catch (SocketException | AsynchronousCloseException socketException) {
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
				
				this.logger.info("Accepted connection from address " + socketChannel.getRemoteAddress());
				this.registrationExecutorService.submit(new RegistrationThread(socketChannel));
			} catch (NumberFormatException | IOException e) {
				this.logger.error(e.getMessage(), e);
				System.exit(3);
			}
		}
	}
}
