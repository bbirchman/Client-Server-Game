import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Server implements Runnable {
	//Cross thread variables
	AtomicInteger idPool;
	List<String> info;
	static List<Lobby> lobbyList;
	static List<String> scoreBoardNames;
	static List<Integer> scoreBoardNumbers;
	//static Lobby mainLobby;
	
	//Local variables
	Socket clientSocket;
	
	//thread safe int
	int clientID;
	
	public Server(Socket clientSocket, AtomicInteger id) {
		this.clientID = -1; //Unregistered
		this.clientSocket = clientSocket;
		//this.processingQueue = processingQueue;
		idPool = id;
	}
	
	public static void main(String[] args) {
		
		int portNumber = Integer.parseInt(args[0]);
		lobbyList = Collections.synchronizedList(new ArrayList<Lobby>());
		scoreBoardNames = Collections.synchronizedList(new ArrayList<String>());
		scoreBoardNumbers = Collections.synchronizedList(new ArrayList<Integer>());
		
		ServerSocket serverSocket;
		AtomicInteger id = new AtomicInteger();
		try {
			
			serverSocket = new ServerSocket(portNumber);
			while(true) {
				System.out.println("Waiting for connections...");
			    Socket clientSocket = serverSocket.accept();
			    Server newServer = new Server(clientSocket, id);
			    Thread serverThread = new Thread(newServer);
			    
			    serverThread.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public void run() { // runs for each client		
		try {
			Lobby mainLobby;
			PrintWriter out =
			        new PrintWriter(clientSocket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(
			        new InputStreamReader(clientSocket.getInputStream()));
			out.println("Connected");
			
			if (lobbyList.size() == 0) { //if first player to connect, create main lobby
				mainLobby = new Lobby(lobbyType.MAIN_LOBBY, lobbyList, scoreBoardNames, scoreBoardNumbers);
				lobbyList.add(mainLobby);
				Thread lobbyThread = new Thread(mainLobby);
				lobbyThread.start(); //create main lobby
			}
			//this should pass processingQueue from lobby, but queue is not updating when playerThread changes it
			PlayerThread newPlayer = new PlayerThread(lobbyList.get(0).processingQueue, in, out);
			Thread playerThread = new Thread(newPlayer);
			playerThread.start();
			//System.out.println("player thread started");
			
			//add player to main lobby
			lobbyList.get(0).addPlayer(newPlayer);
			clientID = idPool.getAndIncrement();
			newPlayer.setID(clientID);
			
			while (newPlayer.running()) {				
			}
			
			playerThread.join();
			System.out.println("player disconnected");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	}

}
