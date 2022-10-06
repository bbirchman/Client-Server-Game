import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;

public class Client {
	public static int COMMAND_BUFFER = 100;
	
	public static void main(String[] args) {
		PrintWriter out = null;
		BufferedReader in = null;
		boolean forcedisconnect = true;
		
		//Create processing queues
		// used on the UI thread (ClientUIThread.java) to queue up server messages
		ArrayBlockingQueue<String> commands = new ArrayBlockingQueue<String>(COMMAND_BUFFER);
		// used by the Connection thread (Client.java)
		ArrayBlockingQueue<String> messages = new ArrayBlockingQueue<String>(COMMAND_BUFFER);
		//Create thread for display
		ClientUIThread newUI = new ClientUIThread(commands, messages);
		//Create thread for processing
		Thread UIThread = new Thread(newUI);
		UIThread.start();
		
		boolean connected = false;
		Socket clientSocket = null;
		
		// while the user hasn't closed the window
		while(UIThread.isAlive()) {
			// queue message to UI to display connection command
			commands.add("Type:Gamestate\n\nCOMMANDS:\r\n-connect {IP} {Port Number} {Username}");
			
			// loop until the user is connected
			while(!connected && UIThread.isAlive()) {
				// If there is a message from the UI thread
				if(!messages.isEmpty()) {
					// get message
					String message = messages.remove();
					// check formatting
					if(message.substring(0, 7).equals("Command")) {
						// try to connect to server
						String[] connectStr = message.split("\\s");
						if(connectStr.length == 5 && connectStr[1].equals("connect")) {
							try {
								String hostName = connectStr[2];
								int portNumber = Integer.parseInt(connectStr[3]);
								clientSocket = new Socket(hostName, portNumber);
								out = new PrintWriter(clientSocket.getOutputStream(), true);
							    in = new BufferedReader(
							            new InputStreamReader(clientSocket.getInputStream()));
							    out.println("Message:4\nType:Command\n\n" + connectStr[1] 
							    		+ " " + connectStr[2] + " " + connectStr[3] + " " + 
							    		connectStr[4]);
							    connected = true;
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								//System.out.println("error");
							}
						}
						else if (connectStr.length == 2 && connectStr[1].contentEquals("commands")) {
							// if the user wants to know commands again show them
							commands.add("Type:Gamestate\n\nCOMMANDS:\r\n-connect {IP} {Port Number} {Username}");
						}
						else {
							// if anything but commands or connect ask them to connect to server
							commands.add("Type:Gamestate\n\nPlease connect to server\r\n"
									+ "Type commands in Action window to see how to connect");
						}
					}
					else {
						// if anything but commands or connect ask them to connect to server
						commands.add("Type:Chat\nUser:Server\n\nPlease connect to server to chat\r\n"
								+ "Type commands in Action window to see how to connect");
					}
				}
			}
			// if connected, show the user the new commands
			commands.add("Type:Gamestate\n\nCOMMANDS:\r\n-connect {IP} {Port Number} {Username}"
					+ "\r\n-disconnect"
					+ "\r\n-creategame"
					+ "\r\n-joingame {ID}"
					+ "\r\n-Username {Username}"
					+ "\r\n-scoreboard"
					+ "\r\n-listgames"
					+ "\r\n-ready"
					+ "\r\n-unready"
					+ "\r\n-exit"
					+ "\r\n-bet {chips}"
					+ "\r\n-call"
					+ "\r\n-fold");
			// loop till disconnected
			while(connected && UIThread.isAlive()) {
				// if UI thread queued a message
				if(!messages.isEmpty()) {
					String message = messages.remove();
					// check formatting
					if(message.substring(0, 7).equals("Command")) {
						String[] connectStr = message.split("\\s");
						//System.out.println("connectStr[0]:" + connectStr[0]);
						//System.out.println("connectStr[1]:" + connectStr[1]);
						// if the user wants the commands list queue the for UI thread
						if (connectStr.length == 2 && connectStr[1].contentEquals("commands")) {
							commands.add("Type:Gamestate\n\nCOMMANDS:\r\n-connect {IP} {Port Number} {Username}"
									+ "\r\n-disconnect"
									+ "\r\n-creategame"
									+ "\r\n-joingame {ID}"
									+ "\r\n-Username {Username}"
									+ "\r\n-scoreboard"
									+ "\r\n-listgames"
									+ "\r\n-ready"
									+ "\r\n-unready"
									+ "\r\n-exit"
									+ "\r\n-bet {chips}"
									+ "\r\n-call"
									+ "\r\n-fold");
						}
						else {
							// assume message is correctly formatted
							String command = message.substring(8);
							System.out.println("Sent this message:\nMessage:4\nType:Command\n\n" + command);
							out.println("Message:4\nType:Command\n\n" + command); // BEN ADDED THIS LINE TO SEND TO SERVER
							if((connectStr.length == 2 && connectStr[1].equals("disconnect")) || 
									(connectStr.length == 5 && connectStr[1].equals("connect"))) {
								// disconnect somehow
								connected = false;
								forcedisconnect = false;
							}
						}
					}
					else {
						System.out.println("Sent this message:\"\n" + message + "\" end");
						out.println(message);
					}
				}
				//connect 127.0.0.1 10000 noah
				else {
					
					String serverMessage;
					
					try {
						if (in.ready()) { // BEN ADDED THIS - in.readline hangs, if(in.ready()) will enter if in has a new message
							serverMessage= in.readLine();
							
							System.out.println("Recieved this message:\"\n" + serverMessage);
							//
							if (serverMessage != null && serverMessage.length() > 8 && 
									serverMessage.substring(0, 8).contentEquals("Message:")) {
								int length = Integer.parseInt(serverMessage.substring(8, serverMessage.length())) - 1;
								String whole = "";
								while (length > 0) {								
									whole += in.readLine() + "\n";
									
									//System.out.println("length:" + length);
									length--;
								}
								
								System.out.println(whole + "\" end");
								// add message to UI thread
								commands.add(whole);
							}
						}

					} catch (IOException e) {
						System.out.println("IO EXCEPTION");
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					
				}
				//System.out.println("FROZEN bottom");
				
			}

			commands.add("Type:History\n\nDisconnected from server");

		}
		if (forcedisconnect) {
			out.println("Message:4\nType:Command\n\ndisconnect");
			System.out.println("Forced disconnect");
		}
	}
	
	
	
	public static void userInputLoop(PrintWriter out, BufferedReader in) throws IOException {	
		boolean exit = false;
		Scanner userInput = new Scanner(System.in);
		while(!exit) {
			String serverOutput = in.readLine();
			System.out.println("hanging?");
			System.out.println(serverOutput);
			System.out.println("Please input command:");
			String inputLine = userInput.nextLine();
			ArrayList<String> tokens = new ArrayList<String>(Arrays.asList(inputLine.split(" ")));
			out.println(inputLine);
			
		}
		System.out.println("EXITED LOOP");
	}
}