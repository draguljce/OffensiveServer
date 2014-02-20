package offensive.Server;

import offensive.Server.Utilities.Environment;
import offensive.Server.WorkerThreads.CleanupThread;

public class Osrv {

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
		
		Osrv.server = new GameServer(environment);
		Osrv.server.initialize();
		
		Osrv.serverThread = new Thread(Osrv.server, "Server");
		
		Osrv.serverThread.start();
		
		Runtime.getRuntime().addShutdownHook(new Thread(new CleanupThread(), "Cleanup"));
		
		try {
			Osrv.serverThread.join();
		} catch (InterruptedException e) {
			Osrv.server.logger.error(e.getMessage(), e);
			Osrv.server.logger.error("Server exited with code 1");
			System.exit(1);
		}
	}
	
	public static void shutdown() {
		Osrv.server.shutdown();
		try {
			// If server thread don't shutdown in 30 minutes just exit...
			Osrv.serverThread.join(30 * 60 * 1000);
		} catch (InterruptedException e) {
			Osrv.server.logger.error(e.getMessage(), e);
		}
	}
}
