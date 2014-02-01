package offensive.Server;

import offensive.Server.WorkerThreads.CleanupThread;

public class Osrv {

	private static Server server;
	
	private static Thread serverThread;
	
	public static void main(String[] args){ 

		if(args.length > 0 && args[0].equals("-debug")) {
			try {
				Thread.sleep(Integer.parseInt(args[1]));
			} catch (NumberFormatException | InterruptedException e) {
				System.exit(-1);
			}
		}
		
		Osrv.server = new Server(args);
		Osrv.server.initialize();
		
		Osrv.serverThread = new Thread(Osrv.server, "Server");
		
		Osrv.serverThread.start();
		
		Runtime.getRuntime().addShutdownHook(new Thread(new CleanupThread(), "Cleanup"));
		
		try {
			Osrv.serverThread.join();
		} catch (InterruptedException e) {
			Server.logger.error(e.getMessage(), e);
			Server.logger.error("Server exited with code 1");
			System.exit(1);
		}
	}
	
	public static void shutdown() {
		Osrv.server.shutdown();
		try {
			// If server thread doesn't shutdown in 30 minutes just kill it...
			Osrv.serverThread.join(30 * 60 * 1000);
		} catch (InterruptedException e) {
			Server.logger.error(e.getMessage(), e);
		}
	}
}
