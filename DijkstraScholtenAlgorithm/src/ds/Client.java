package ds;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client{
	Socket socket = null;
	Node node = null;
	DijkstraScholtenAlgorithm ds = null;
	Logger logger = Logger.getLogger("Client.java");
	FileHandler handler = null;
	public Client(Node node, DijkstraScholtenAlgorithm ds) {
		handler = ds.fileHandler;
		logger.addHandler(handler);
		//logger.setLevel(Level.SEVERE);
		this.node = node;
		this.ds = ds;
	}
	
	public void connect() {
		while (socket == null) {
			try {
				socket = new Socket(node.host, node.port);
				
			} catch (UnknownHostException e) {
				// e.printStackTrace();
			} catch (IOException e) {
				// e.printStackTrace();
			}
		}
		ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(
					socket.getOutputStream());
			logger.log(Level.INFO, "Sending message to node "+node.id+" at clock: "+DijkstraScholtenAlgorithm.getClock());
			oos.writeObject(new Message(DijkstraScholtenAlgorithm
					.getClock(), ds.myNodeInfo));
			oos.flush();
			ClientListener cl = new ClientListener(socket,this);
			cl.start();
			logger.log(Level.INFO, "message sent");
			
		} catch (IOException e) {
			e.printStackTrace();
			logger.log(Level.INFO, e.getMessage());
		}		
		return;
	}
}

class ClientListener extends Thread {
	Socket socket = null;
	ObjectInputStream ois = null;
	Client client = null;

	public ClientListener(Socket socket, Client client) {
		this.socket = socket;
		this.client = client;
	}

	@Override
	public void run() {
		try {
			ois = new ObjectInputStream(socket.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
			client.logger.log(Level.INFO, e.getMessage());
		}
		while (true) {
			try {
				Object obj = ois.readObject();
				if (obj instanceof Ack) {
					Ack ack = (Ack)obj;
					if((ack.timestamp+1)>(DijkstraScholtenAlgorithm.getClock()+1)){
						//reset the clock
						DijkstraScholtenAlgorithm.setClock(ack.timestamp+1);
					}else {
					DijkstraScholtenAlgorithm
							.setClock(DijkstraScholtenAlgorithm.getClock() + 1);
					}
					client.logger.log(Level.INFO, "Received acknowledge from node "+client.node.id+" at clock: "+DijkstraScholtenAlgorithm.getClock());
					//socket.close();
					//client.logger.log(Level.INFO, "Closing connection with node "+client.node.id+" at clock: "+DijkstraScholtenAlgorithm.getClock());
/*					int removeIndex = 0;
					boolean found = false;
					for (int i = 0; i < client.ds.clientList.size(); i++) {
						if(client.ds.clientList.get(i).node.id == client.node.id){
							removeIndex = i;
							found = true;
						}
					}
					if(found){
						client.ds.clientList.remove(removeIndex);
						
					}*/
					client.ds.clientList.remove(client);
					//System.out.println("size after removing: "+client.ds.clientList.size());
					client.ds.acks--;
					break;
				}
			} catch (IOException e) {
				e.printStackTrace();
				client.logger.log(Level.INFO, e.getMessage());
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				client.logger.log(Level.INFO, e.getMessage());
			}
		}
	}
}
