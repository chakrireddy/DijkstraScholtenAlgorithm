package ds;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server extends Thread {

	int port = 0;
	ServerSocket serverSocket = null;
	Node parent = null;
	Socket parentSocket = null;
	DijkstraScholtenAlgorithm ds = null;
	Map<Integer, Socket> listenersMap = new HashMap<Integer, Socket>();
	Logger logger = Logger.getLogger("Server.class");
	FileHandler handler = null;
	Listener sockListener = null;

	public Server(int port, DijkstraScholtenAlgorithm ds) {
		logger.addHandler(ds.fileHandler);
		//logger.setLevel(Level.SEVERE);
		this.port = port;
		this.ds = ds;
	}

	@Override
	public void run() {
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException ioe) {
			ioe.printStackTrace();
			logger.log(Level.SEVERE, ioe.getMessage());
			System.exit(1);
		}

		while (true) {
			try {
				Socket socket = serverSocket.accept();
				if (this.parentSocket != null) {
					/*DijkstraScholtenAlgorithm
							.setClock(DijkstraScholtenAlgorithm.getClock() + 1);*/
					ObjectInputStream ois = new ObjectInputStream(
							socket.getInputStream());
					Object obj = null;
					try {
						obj = ois.readObject();
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
						logger.log(Level.SEVERE, e.getMessage());
					}
					if (obj instanceof Message) {
						Message msg = (Message) obj;
						//TODO set the clock by comparing the local time and message clock
						if((msg.timestamp+1)>(DijkstraScholtenAlgorithm.getClock()+1)){
							//reset the clock
							DijkstraScholtenAlgorithm.setClock(msg.timestamp+1);
						}else {
						DijkstraScholtenAlgorithm
								.setClock(DijkstraScholtenAlgorithm.getClock() + 1);
						}
						logger.log(Level.INFO, "Received message from node "
								+ msg.senderNode.id + " at clock: "
								+ DijkstraScholtenAlgorithm.getClock());
						// send acknowledgment msg
						DijkstraScholtenAlgorithm
								.setClock(DijkstraScholtenAlgorithm.getClock() + 1);
						logger.log(Level.INFO,
								"Sending acknowledgment to node "
										+ msg.senderNode.id + " at clock: "
										+ DijkstraScholtenAlgorithm.getClock());
						ObjectOutputStream oos = new ObjectOutputStream(
								socket.getOutputStream());
						Ack ack = new Ack();
						ack.timestamp = DijkstraScholtenAlgorithm.getClock();
						oos.writeObject(ack);
						oos.flush();
					}

				}
				Listener listener = new Listener(socket, this);
				this.sockListener = listener;
				listener.start();				
			} catch (IOException e) {
				e.printStackTrace();
				logger.log(Level.SEVERE, e.getMessage());
			}
		}
	}
}

class Listener extends Thread {
	Server server = null;
	Socket socket = null;
	ObjectInputStream ois = null;

	public Listener(Socket socket, Server server) {
		this.socket = socket;
		this.server = server;
		try {
			ois = new ObjectInputStream(socket.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
			server.logger.log(Level.SEVERE, e.getMessage());
			//TODO remove system exit code
			//System.exit(1);
		}
	}

	@Override
	public void run() {
		while (true) {
			try {
				Object obj = ois.readObject();
				System.out.println("object received");
				if (obj instanceof Message) {
					Message msg = (Message) obj;
					server.parent = msg.senderNode;
					server.parentSocket = socket;
					server.listenersMap.put(msg.senderNode.id, socket);
					if((msg.timestamp+1)>(DijkstraScholtenAlgorithm.getClock()+1)){
						//reset the clock
						DijkstraScholtenAlgorithm.setClock(msg.timestamp+1);
					}else {
					DijkstraScholtenAlgorithm
							.setClock(DijkstraScholtenAlgorithm.getClock() + 1);
					}
					server.logger.log(Level.INFO, "Received message from node "
							+ msg.senderNode.id + " at clock: "
							+ DijkstraScholtenAlgorithm.getClock());
				}
			} catch (IOException e) {
				e.printStackTrace();
				server.logger.log(Level.SEVERE, e.getMessage());
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				server.logger.log(Level.SEVERE, e.getMessage());
			}
		}
	}
}
