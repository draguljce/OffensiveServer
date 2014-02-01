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

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;

public class Server implements Runnable {
	public static Logger logger;
	
	private Environment environment;
	
	private ExecutorService handlerExecutorService;
	
	private ExecutorService battleExecutorService;
	
	private ServerSocket serverSocket;
	
	private boolean shouldShutdown = false;
	
	private SessionFactory sessionFactory;
	
	private ServiceRegistry serviceRegistry;
	
	public Server(String[] args) {
		this.environment = new Environment(args);
		
		Server.logger = Logger.getLogger(this.getClass());
	}
	
	void initialize() {
		Server.logger.info("Server initialization started.");
		
		Server.logger.info("Building session factory...");
		
		//Configuration configuration = new Configuration();
		//configuration.configure();
		//this.serviceRegistry = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties()).build();
		//this.sessionFactory = new Configuration().buildSessionFactory(this.serviceRegistry);
		
		Server.logger.info("Building session factory completed.");
		
		Server.logger.debug("Initializing handler threads.");
		this.handlerExecutorService = Executors.newFixedThreadPool(Integer.parseInt(this.environment.getVariable(Constants.HandlerThreadNumVarName)));
		Server.logger.debug("Initialization of handler threads completed. Number of handler threads is " + this.environment.getVariable(Constants.HandlerThreadNumVarName));
		
		Server.logger.debug("Initializing battle threads.");
		this.battleExecutorService = Executors.newFixedThreadPool(Integer.parseInt(this.environment.getVariable(Constants.BattleThreadNumVarName)));
		Server.logger.debug("Initialization of handler threads completed. Number of battle threads is " + this.environment.getVariable(Constants.BattleThreadNumVarName));
		
		Server.logger.info("Opening server socket on port " + this.environment.getVariable(Constants.PortNumberVarName));
		try {
			serverSocket = new ServerSocket(Integer.parseInt(this.environment.getVariable(Constants.PortNumberVarName)));
		} catch (NumberFormatException | IOException e) {
			Server.logger.fatal(e.getMessage(), e);
			System.exit(4);
		}
		Server.logger.info("Server socket opened.");
		
		try {
			this.serverSocket.setSoTimeout(Integer.parseInt(this.environment.getVariable(Constants.ServerSocketTimeoutVarName)));
		} catch (NumberFormatException | SocketException e) {
			Server.logger.fatal(e.getMessage(), e);
			System.exit(5);
		}
		
		Server.logger.info("Server socket timeout is " + this.environment.getVariable(Constants.ServerSocketTimeoutVarName));
		
		Server.logger.info("Server initialization completed.");
	}

	@Override
	public void run() {
		Server.logger.info("Server is now listening on port " + this.environment.getVariable(Constants.PortNumberVarName));
		
		Socket socket = null;
		while(true) {
			try {	
				try {
					socket = serverSocket.accept();
				} catch (SocketException socketException) {
					if(this.shouldShutdown) {
						Server.logger.info("Received shutdown request.");
						try {
							Server.logger.info("Waiting for handler threads to finish...");
							this.handlerExecutorService.shutdown();
							this.handlerExecutorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
							
							Server.logger.info("Waiting for battle threads to finish...");
							this.battleExecutorService.shutdown();
							this.battleExecutorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
						} catch (InterruptedException e) {
							Server.logger.error(e.getMessage(), e);
						}
						
						Server.logger.info("Shuting down...");
						return;
					}
					
					continue;
				}
				catch (SocketTimeoutException timeoutException) {
					// TODO: nothing to do here. For now...
					continue;
				}
				
				Server.logger.info("Accepted connection from address " + socket.getRemoteSocketAddress());
				this.handlerExecutorService.submit(new HandlerThread(socket));
			} catch (NumberFormatException | IOException e) {
				Server.logger.error(e.getMessage(), e);
				System.exit(3);
			}
		}
	}
	
	public void shutdown() {
		this.shouldShutdown = true;
		
		try {
			this.serverSocket.close();
		} catch (IOException e) {
			Server.logger.error(e.getMessage(), e);
		}
	}
}
