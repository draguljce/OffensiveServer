package offensive.Server;

import offensive.Server.Utilities.Environment;
import offensive.Server.WorkerThreads.CleanupThread;

public class OsrvRegistration {

	private static RegistrationServer registrationServer;
	
	private static Thread registrationServerThread;
	
	public static void main(String[] args){ 

		if(args.length > 0 && args[0].equals("-debug")) {
			try {
				Thread.sleep(Integer.parseInt(args[1]));
			} catch (NumberFormatException | InterruptedException e) {
				System.exit(-1);
			}
		}
		
		Environment environment = new Environment(args);
		
		OsrvRegistration.registrationServer = new RegistrationServer(environment);
		OsrvRegistration.registrationServerThread = new Thread(OsrvRegistration.registrationServer, "RegistrationServer");
		OsrvRegistration.registrationServer.initialize(OsrvRegistration.registrationServerThread);
		
		OsrvRegistration.registrationServerThread.start();
		
		Runtime.getRuntime().addShutdownHook(new Thread(new CleanupThread(), "Cleanup"));
		
		try {
			OsrvRegistration.registrationServerThread.join();
		} catch (InterruptedException e) {
			OsrvRegistration.registrationServer.logger.error(e.getMessage(), e);
			OsrvRegistration.registrationServer.logger.error("Server exited with code 1");
			System.exit(1);
		}
	}
}
