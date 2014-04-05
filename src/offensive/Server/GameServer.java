package offensive.Server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import offensive.Server.Sessions.SessionManager;
import offensive.Server.Utilities.Constants;
import offensive.Server.Utilities.Environment;

public class GameServer extends Server implements Runnable {
	
	private SessionManager sessionManager;
	private Thread sessionManagerThread;
	
	public GameServer(Environment environment) {
		super(environment);
	}
	
	public void initialize(Thread serverThread) {
		this.logger.info("Server initialization started.");
		
		super.initialize(serverThread);
		
		this.logger.info("Opening server socket on port " + this.environment.getVariable(Constants.PortNumberVarName));
		try {
			serverSocketChannel = ServerSocketChannel.open();
		} catch (NumberFormatException | IOException e) {
			this.logger.fatal(e.getMessage(), e);
			System.exit(4);
		}
		this.logger.info("Server socket opened.");
		this.logger.info("Server socket timeout is " + this.environment.getVariable(Constants.ServerSocketTimeoutVarName));
		
		this.logger.info("Session manager initialization started.");
		this.sessionManager = SessionManager.getOnlyInstance();
		this.sessionManagerThread = new Thread(this.sessionManager, "Session manager");
		this.sessionManager.initialize(environment, this.sessionManagerThread);
		
		this.logger.info("Starting session manager.");
		this.sessionManagerThread.start();
		this.logger.info("Session manager started.");
		
		this.logger.info("Session manager initialization completed.");
		
		this.logger.info("Server initialization completed.");
	}

	@Override
	public void run() {
		this.logger.info("Server is now listening on port " + this.environment.getVariable(Constants.PortNumberVarName));
		
		try {
			this.serverSocketChannel.bind(new InetSocketAddress(Integer.parseInt(this.environment.getVariable(Constants.PortNumberVarName))));
		} catch (NumberFormatException | IOException e1) {
			this.logger.error(e1.getMessage(), e1);
			return;
		}
		SocketChannel socketChannel = null;
		while(true) {
			try {	
				try {
					socketChannel = serverSocketChannel.accept();
				} catch (SocketException | AsynchronousCloseException socketException) {
					if(this.shouldShutdown) {
						this.logger.info("Shutting down session manager.");
						this.sessionManager.shutdown();
						this.logger.info("Session manager shutdown.");
						return;
					}
					
					continue;
				}
				catch (SocketTimeoutException timeoutException) {
					// TODO: nothing to do here. For now...
					continue;
				}
				
				this.logger.info("Accepted connection from address " + socketChannel.getRemoteAddress());
				SessionManager.getOnlyInstance().CreateSession(socketChannel);
			} catch (NumberFormatException | IOException e) {
				this.logger.error(e.getMessage(), e);
				System.exit(3);
			}
		}
	}
}
