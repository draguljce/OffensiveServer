package offensive.Server.WorkerThreads;

import java.net.Socket;
import java.util.concurrent.Callable;

import offensive.Server.Server;

public class HandlerThread implements Runnable {

	private Socket socket;
	
	public HandlerThread(Socket socket) {
		this.socket = socket;
	}

	@Override
	public void run() {
		
	}

}
