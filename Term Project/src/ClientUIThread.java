import java.util.concurrent.ArrayBlockingQueue;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class ClientUIThread implements Runnable {
	
	ArrayBlockingQueue<String> processingQueue;
	ArrayBlockingQueue<String> messageQueue;

	// get the queues
	public ClientUIThread(ArrayBlockingQueue<String> processingQueue, ArrayBlockingQueue<String> messageQueue) {
		this.processingQueue = processingQueue;
		this.messageQueue = messageQueue;
	}
	
	public void run() {
		// create Window
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new GridLayout(1, false));
		GameComposite world = new GameComposite(shell, SWT.NONE);
		world.setMessageQueue(messageQueue);
		shell.pack();
		shell.open();
		while(!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
			// translate top message if queue is not empty
			if(!processingQueue.isEmpty()) {
				translateMessage(processingQueue.remove(), world);
			}
		}
		display.dispose();
	}
	
	
	public void translateMessage(String message, GameComposite curWindow) {
		int i;
		
		// expects "Type:" to be first
		if (message.length() > 5 && message.substring(0, 5).equals("Type:")) {
			
			// find next "\n" char
			for (i = 5; i < message.length() && message.charAt(i) != '\n'; i++);
			
			// get the message type
			String type = message.substring(5, i);
			//System.out.println("Type: " + type + ".");
			
			if (type.equals("History")) {
				// Get history message
				String history = message.substring(i+2, message.length());
				// update history window
				curWindow.addHistory(history);
			}
			else if (type.equals("Gamestate")) {
				// get gamestate message
				String gameState = message.substring(i+2, message.length());				
				// update game window
				curWindow.setGameText(gameState);
			}
			else if (type.equals("Chat")) {
				i+=1;
				// find user
				if(message.length() >= i+5 && message.substring(i, i+5).equals("User:")) {
					int j;
					
					// find next \n char
					for (j = i+5; j < message.length() && message.charAt(j) != '\n'; j++);
					
					// get username
					String user = message.substring(i+5, j);
					
					// get message
					String chatMessage = message.substring(j+2, message.length());
					
					//update window
					curWindow.addChatMessage("[" + user + "]: " + chatMessage);

				}
			}
			
		}
		else {
			// incorrect message format
		}
	}

}