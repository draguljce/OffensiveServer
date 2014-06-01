package offensive.Server;

import java.io.IOException;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;

import offensive.Server.Utilities.CommonRandom;
import offensive.Server.Utilities.Environment;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.mapping.Table;
import org.hibernate.service.ServiceRegistry;

public class Server {
	public Logger logger;
	
	public Environment environment;
	
	public SessionFactory sessionFactory;
	
	public CommonRandom rand = new CommonRandom();
	
	protected ServerSocketChannel serverSocketChannel;
	
	protected boolean shouldShutdown;
	
	private static Server server;
	
	private Thread serverThread;
	
	protected Server(Environment environment) {
		this.logger = Logger.getLogger(this.getClass());
		
		this.environment = environment;
		
		Server.server = this;
	}
	
	protected static void setServer(Server server) {
		Server.server = server;
	}
	
	public static Server getServer() {
		return Server.server; 
	}
	
	protected void initialize(Thread serverThread) {
		this.serverThread = serverThread;
		this.logger.info("Building session factory...");
		
		Configuration configuration = new Configuration();
		configuration.configure();
		ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties()).build();
		this.sessionFactory = configuration.buildSessionFactory(serviceRegistry);
		
		this.logger.debug("Classes mappings:");
		for(String key : this.sessionFactory.getAllClassMetadata().keySet()) {
			this.logger.debug(key);
		}
		
		this.logger.debug("Table mappings:");
		for(Iterator<Table> iterator = configuration.getTableMappings(); iterator.hasNext();) {
			Table map = iterator.next();
			this.logger.debug(map);
		}
		
		this.logger.info("Building session factory completed.");
	}
	
	public void shutdown() {
		this.shouldShutdown = true;
		
		try {
			this.serverSocketChannel.close();
		} catch (IOException e) {
			this.logger.error(e.getMessage(), e);
		}
		
		// Wait for up to 30 minutes for shutdown to complete.
		try {
			this.serverThread.join(30 * 60 * 1000);
		} catch (InterruptedException e) {
			this.logger.error(e.getMessage(), e);
		}
	}
}
