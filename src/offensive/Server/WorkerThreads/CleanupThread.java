package offensive.Server.WorkerThreads;

import offensive.Server.Server;

public class CleanupThread implements Runnable {

	@Override
	public void run() {
		Server.getServer().shutdown();
	}

}
