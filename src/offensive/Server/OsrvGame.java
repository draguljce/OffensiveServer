package offensive.Server;

import offensive.Server.Utilities.Environment;
import offensive.Server.WorkerThreads.CleanupThread;

public class OsrvGame {

	private static GameServer server;
	
	private static Thread serverThread;
	
	public static void main(String[] args){ 

		if(args.length > 0 && args[0].equals("-debug")) {
			try {
				Thread.sleep(Integer.parseInt(args[1]));
			} catch (NumberFormatException | InterruptedException e) {
				System.exit(-1);
			}
		}
		
		Environment environment = new Environment(args);
		
		Runtime.getRuntime().addShutdownHook(new Thread(new CleanupThread(), "Cleanup"));
		
		OsrvGame.server = new GameServer(environment);
		OsrvGame.serverThread = new Thread(OsrvGame.server, "Server");
		OsrvGame.server.initialize(OsrvGame.serverThread);
		
		OsrvGame.serverThread.start();
		
		try {
			OsrvGame.serverThread.join();
		} catch (InterruptedException e) {
			OsrvGame.server.logger.error(e.getMessage(), e);
			OsrvGame.server.logger.error("Server exited with code 1");
			System.exit(1);
		}
	}
	
	public static void shutdown() {
		OsrvGame.server.shutdown();
		try {
			// If server thread don't shutdown in 5 minutes just exit...
			OsrvGame.serverThread.join(5 * 60 * 1000);
		} catch (InterruptedException e) {
			OsrvGame.server.logger.error(e.getMessage(), e);
		}
	}
}
