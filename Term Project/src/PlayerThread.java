import java.io.*;
import java.net.SocketException;
import java.util.concurrent.ArrayBlockingQueue;

public class PlayerThread implements Runnable {
	ArrayBlockingQueue<Message> processingQueue;
	private int id;
	private String username;
	private BufferedReader in;
	private PrintWriter out;
	private boolean ready;
	private boolean running = true;
	
	// pass in processing queue from lobby
	public PlayerThread(ArrayBlockingQueue<Message> processingQueue, BufferedReader in, PrintWriter out) {
		this.processingQueue = processingQueue;
		this.in = in;
		this.out = out;
		this.processingQueue = processingQueue;
		ready = false;
		running = true;

	}
	
	public void run() { //loop for new data
		try {
			System.out.println("PLAYER THREAD STARTING");
			String header;
			String input = "";
			
			while ((header = in.readLine()) != null) {	//intentionally hanging
			//while (in.ready()) {	
				//header = in.readLine();
				System.out.println("Header: " + header);
				System.out.println("substring: " + header.substring(0, 8));
				try {
					if (header.length() < 9 || !header.substring(0, 8).equals("Message:")) {
						this.sendMessage("Message:4\nType:History\n\nERROR; bad message header");
					}
					int length = Integer.parseInt(header.substring(8,9));				
				//System.out.println("message received from clientside: " + input);
					
					for (int i = 0; i < length-1; i++) {
						String curr = in.readLine();
						if (curr.equals("")) curr = " "; // currently not using the /n for body
						//System.out.println("curr: " + curr);
						input += curr;
					}
					//System.out.println("input: " + input);
					Message currMessage = new Message(input, id);
					input = "";
					//System.out.println("currMessage type: " + currMessage.getType());
					//System.out.println("currMessage body: " + currMessage.getBody());
					
					//if (currMessage != null) {
					processingQueue.add(currMessage); 
						//System.out.println("message added to queue: ");
					//}
				} catch (Exception e) {
					this.sendMessage("Message:4\nType:History\n\nERROR; bad message");
					e.printStackTrace();
				}			

				//out.println("message to player");
				
			}
			//System.out.println("exitedloop");

		} catch (SocketException e) {
			//this.id = -1;
			String disconnect = "Type:Command disconnect";
			processingQueue.add(new Message(disconnect, id));
			System.out.println("brute disconnect");
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
	}
	
	public boolean running() {
		return running;
	}
	public void disconnect() {
		running = false;
	}

	public boolean changeLobby(ArrayBlockingQueue<Message> processingQueue) {
		this.processingQueue = processingQueue;
		return true;
	}
	
	public void sendMessage(String s) {
		out.println(s);
	}
	
	public void loadMessage(BufferedReader in) {
		//System.out.println("playerThread in: " + in);
		this.in = in;
	}
	
	public Message poll() {
		return processingQueue.poll();
	}
	
	public boolean hasMessage() {
		if (processingQueue.peek() != null) return true;
		return false;
	}
	
	public void setID(int id) {
		this.id = id;
	}
	
	public int getID() {
		return id;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getUsername() {
		return username;
	}
	
	public void setReady(boolean ready) {
		this.ready = ready;
	}
	
	public boolean getReady() {
		return ready;
	}
	
}


