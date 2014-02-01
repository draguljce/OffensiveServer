package offensive.Server.WorkerThreads;

import offensive.Server.Osrv;

public class CleanupThread implements Runnable {

	@Override
	public void run() {
		Osrv.shutdown();
	}

}
