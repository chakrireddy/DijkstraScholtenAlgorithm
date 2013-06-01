package ds;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class DijkstraScholtenAlgorithm {
	Logger logger = Logger.getLogger("DijkstraScholtenAlgorithm.class");
	FileHandler fileHandler = null;
	File logfile = new File(new File("Log").getAbsolutePath());
	private static int clock = 0;
	Node myNodeInfo = null;
	List<Node> nodeList = new ArrayList<Node>();
	//List<Client> clientList = new ArrayList<Client>();
	Vector<Client> clientList = new Vector<Client>();
	Map<Integer, Node> nodeMap = new HashMap<Integer, Node>();
	Server server = null;
	int numberofNodes = 0;
	boolean initializer = false;
	List<Task> taskList = new ArrayList<Task>();
	int acks = 0;

	public static void main(String[] args) {
		DijkstraScholtenAlgorithm dsAlgo = new DijkstraScholtenAlgorithm();
		dsAlgo.init();
		dsAlgo.run();
	}

	public void init() {		
		Formatter format = new SimpleFormatter();
		try {
			fileHandler = new FileHandler(logfile.getAbsolutePath());
			fileHandler.setFormatter(format);
			logger.addHandler(fileHandler);
			//logger.setLevel(Level.SEVERE);
		} catch (SecurityException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		// Load node information
		logger.log(Level.INFO, "Loading the configuration file config.txt");
		File configFile = new File("config.txt");
		BufferedReader buffFileReader = null;
		try {
			buffFileReader = new BufferedReader(new FileReader(configFile));
			String info = buffFileReader.readLine();
			String[] myinfo = info.split(",");
			myNodeInfo = new Node("localhost", Integer.parseInt(myinfo[1]),
					Integer.parseInt(myinfo[0]));
			String line = null;
			while ((line = buffFileReader.readLine()) != null) {
				String[] nodeInfo = line.split(",");
				Node nd = new Node(nodeInfo[1], Integer.parseInt(nodeInfo[2]),
						Integer.parseInt(nodeInfo[0]));
				nodeList.add(nd);
				nodeMap.put(nd.id, nd);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// Start the server
		logger.log(Level.INFO, "Start the server");
		server = new Server(myNodeInfo.port, this);
		server.start();
		logger.log(Level.INFO, "Server started");
	}

	public void run() {
		Test test = new Test(this);
		test.start();
		/*
		logger.log(Level.INFO, "Read sample file");
		File sampleFile = new File("sample.txt");
		logger.log(Level.INFO, "sample file path: "+sampleFile.getAbsolutePath());
		BufferedReader buffFileReader = null;
		try {
			buffFileReader = new BufferedReader(new FileReader(sampleFile));
			numberofNodes = Integer.parseInt(buffFileReader.readLine());
			String line = null;
			while ((line = buffFileReader.readLine()) != null) {
				String[] task = line.split(" ");
					if (Integer.parseInt(task[0]) == myNodeInfo.id) {
						String tsk = task[2];
						Task ts = null;
						if(tsk.equals("INIT")){
							ts = new Task(Integer.parseInt(task[1]), task[2], 0);
						}else if (tsk.equals("SEND")) {
							ts = new Task(Integer.parseInt(task[1]), task[2], Integer.parseInt(task[3]));
						}else if (tsk.equals("TICK")) {
							ts = new Task(Integer.parseInt(task[1]), task[2], Integer.parseInt(task[3]));
						}else if (tsk.equals("IDLE")) {
							ts = new Task(Integer.parseInt(task[1]), task[2], 0);
						}
						if(tsk != null){
							taskList.add(ts);
						}						
					}
			}
			
			for (Task t : taskList) {
				System.out.println(t.action+" timeto Trigger: "+t.timeToTrigger);
			}
			for (Task t : taskList) {
				System.out.println("clock: "+DijkstraScholtenAlgorithm.clock+" time: "+t.timeToTrigger);
				boolean iterator = true;
				while (iterator) {
					
					if ((t.timeToTrigger == 0)&&(DijkstraScholtenAlgorithm.getClock() == t.timeToTrigger)&&(t.action.equals("INIT"))){
						logger.log(Level.INFO, "node "+myNodeInfo.id+" initiated the termination protocol at clock: "+DijkstraScholtenAlgorithm.getClock());
						iterator = false;
					}
					
					if (DijkstraScholtenAlgorithm.getClock() == (t.timeToTrigger - 1)){
					if (t.action.equals("INIT")) {
						this.initializer = true;
						DijkstraScholtenAlgorithm
						.setClock(DijkstraScholtenAlgorithm
								.getClock() + 1);
						logger.log(Level.INFO, "node "+myNodeInfo.id+" initiated the termination protocol at clock: "+DijkstraScholtenAlgorithm.getClock());
					} else if (t.action.equals("SEND")) {
						// increment the clock and send message to
						// specific node
						DijkstraScholtenAlgorithm
								.setClock(DijkstraScholtenAlgorithm
										.getClock() + 1);
						Node node = nodeMap.get(t.param);
						Client client = new Client(node, this);
						client.connect();
						clientList.add(client);
						acks++;
					} else if (t.action.equals("TICK")) {
						logger.log(Level.INFO,"ticking ");
						// increment the clock and sleep
						try {
							
							DijkstraScholtenAlgorithm
									.setClock(DijkstraScholtenAlgorithm
											.getClock() + 1);
							logger.log(Level.INFO, "node "+myNodeInfo.id+" sleeping for "+t.param+" at clock: "+DijkstraScholtenAlgorithm.getClock());
							Thread.sleep(t.param * 1000);
						} catch (NumberFormatException e) {
							e.printStackTrace();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					} else if (t.action.equals("IDLE")) {
						// increment the clock, send acknowledgment and close
						DijkstraScholtenAlgorithm
						.setClock(DijkstraScholtenAlgorithm
								.getClock() + 1);
						logger.log(Level.INFO," node "+myNodeInfo.id+" is IDLE at clock: "+DijkstraScholtenAlgorithm.getClock());
						logger.log(Level.INFO, "check for pending acknowledgments");
						if(server.parent != null){
							logger.log(Level.INFO, "parent is: "+server.parent.id+" clientsize: "+clientList.size());
						}
						while(clientList.size()>0){
							
						}
						while(acks>0){
							
						}
						if(server.parentSocket != null){
							DijkstraScholtenAlgorithm
							.setClock(DijkstraScholtenAlgorithm
									.getClock() + 1);
							logger.log(Level.INFO, " no pending acknowledgements. Sending ack and closing the connection with node "+server.parent.id+" at clock: "+DijkstraScholtenAlgorithm.getClock());
							ObjectOutputStream oos = new ObjectOutputStream(
									server.parentSocket.getOutputStream());
							Ack ack = new Ack();
							ack.timestamp = DijkstraScholtenAlgorithm.getClock();
							oos.writeObject(ack);
							oos.flush();
							oos.close();
							server.sockListener.stop();
							server.parentSocket.close();
							server.parentSocket = null;
							server.parent = null;
						}else if(initializer){
							logger.log(Level.INFO, "Task terminated");
						}
						
					}
					iterator = false;	
					}
					
				}
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}*/
	}

	public static int getClock() {
		return clock;
	}

	public synchronized static void setClock(int clock) {
		DijkstraScholtenAlgorithm.clock = clock;
	}
	
	private class Task {
		int timeToTrigger = 0;
		String action = null;
		int param = 0;
		public Task(int time, String action, int param) {
			this.timeToTrigger = time;
			this.action = action;
			this.param = param;
		}
	}
	
	class Test extends Thread {
		DijkstraScholtenAlgorithm ds = null;
		public Test(DijkstraScholtenAlgorithm ds) {
			this.ds = ds;
		}
		
		@Override
		public void run() {

			logger.log(Level.INFO, "Read sample file");
			File sampleFile = new File("sample.txt");
			logger.log(Level.INFO, "sample file path: "+sampleFile.getAbsolutePath());
			BufferedReader buffFileReader = null;
			try {
				buffFileReader = new BufferedReader(new FileReader(sampleFile));
				numberofNodes = Integer.parseInt(buffFileReader.readLine());
				String line = null;
				while ((line = buffFileReader.readLine()) != null) {
					String[] task = line.split(" ");
						if (Integer.parseInt(task[0]) == myNodeInfo.id) {
							String tsk = task[2];
							Task ts = null;
							if(tsk.equals("INIT")){
								ts = new Task(Integer.parseInt(task[1]), task[2], 0);
							}else if (tsk.equals("SEND")) {
								ts = new Task(Integer.parseInt(task[1]), task[2], Integer.parseInt(task[3]));
							}else if (tsk.equals("TICK")) {
								ts = new Task(Integer.parseInt(task[1]), task[2], Integer.parseInt(task[3]));
							}else if (tsk.equals("IDLE")) {
								ts = new Task(Integer.parseInt(task[1]), task[2], 0);
							}
							if(tsk != null){
								taskList.add(ts);
							}						
						}
				}				
				
				for (Task t : taskList) {
					//System.out.println("clock: "+DijkstraScholtenAlgorithm.clock+" time: "+t.timeToTrigger);
					boolean iterator = true;
					while (iterator) {
						
						if ((t.timeToTrigger == 0)&&(DijkstraScholtenAlgorithm.getClock() == t.timeToTrigger)&&(t.action.equals("INIT"))){
							logger.log(Level.INFO, "node "+myNodeInfo.id+" initiated the termination protocol at clock: "+DijkstraScholtenAlgorithm.getClock());
							iterator = false;
							initializer = true;
						}
						
						if (DijkstraScholtenAlgorithm.getClock() == (t.timeToTrigger - 1)){
						/*if (t.action.equals("INIT")) {
							this.initializer = true;
							DijkstraScholtenAlgorithm
							.setClock(DijkstraScholtenAlgorithm
									.getClock() + 1);
							logger.log(Level.INFO, "node "+myNodeInfo.id+" initiated the termination protocol at clock: "+DijkstraScholtenAlgorithm.getClock());
						} else*/ if (t.action.equals("SEND")) {
							// increment the clock and send message to
							// specific node
							DijkstraScholtenAlgorithm
									.setClock(DijkstraScholtenAlgorithm
											.getClock() + 1);
							Node node = nodeMap.get(t.param);
							Client client = new Client(node, ds);
							client.connect();
							clientList.add(client);
							acks++;
						} else if (t.action.equals("TICK")) {
							logger.log(Level.INFO,"ticking ");
							// increment the clock and sleep
							try {
								
								DijkstraScholtenAlgorithm
										.setClock(DijkstraScholtenAlgorithm
												.getClock() + 1);
								logger.log(Level.INFO, "node "+myNodeInfo.id+" sleeping for "+t.param+" at clock: "+DijkstraScholtenAlgorithm.getClock());
								Thread.sleep(t.param * 1000);
							} catch (NumberFormatException e) {
								e.printStackTrace();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						} else if (t.action.equals("IDLE")) {
							// increment the clock, send acknowledgment and close
							DijkstraScholtenAlgorithm
							.setClock(DijkstraScholtenAlgorithm
									.getClock() + 1);
							logger.log(Level.INFO," node "+myNodeInfo.id+" is IDLE at clock: "+DijkstraScholtenAlgorithm.getClock());
							logger.log(Level.INFO, "check for pending acknowledgments");
							if(server.parent != null){
								logger.log(Level.INFO, "parent is: "+server.parent.id+" clientsize: "+clientList.size());
							}
							for (Client cc : ds.clientList) {
								System.out.println("client ack needed: "+cc.node.id);
							}
							while(clientList.size()!=0){
								
							}
							/*while(acks>0){
								
							}*/
							System.out.println("acknowledgements received");
							if(server.parentSocket != null){
								DijkstraScholtenAlgorithm
								.setClock(DijkstraScholtenAlgorithm
										.getClock() + 1);
								logger.log(Level.INFO, " no pending acknowledgements. Sending ack and closing the connection with node "+server.parent.id+" at clock: "+DijkstraScholtenAlgorithm.getClock());
								ObjectOutputStream oos = new ObjectOutputStream(
										server.parentSocket.getOutputStream());
								Ack ack = new Ack();
								ack.timestamp = DijkstraScholtenAlgorithm.getClock();
								oos.writeObject(ack);
								oos.flush();
								oos.close();
								server.sockListener.stop();
								server.parentSocket.close();
								server.parentSocket = null;
								server.parent = null;
							}else if(initializer){
								logger.log(Level.INFO, "Task terminated");
							}
							
						}
						iterator = false;	
						}
						
					}
				}

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
