import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Combo;

import java.awt.EventQueue;
import java.util.concurrent.ArrayBlockingQueue;

//import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.wb.swt.SWTResourceManager;

public class GameComposite extends Composite {
	private Text NewMessage;
	private Text NewAction;
	private Text GameTextWindow;
	private Text ChatText;
	private Text HistoryText;
	ArrayBlockingQueue<String> messageQueue;
	
	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new GridLayout(1, false));
		GameComposite world = new GameComposite(shell, SWT.NONE);
		shell.pack();
		shell.open();
		while(!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}
	

	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public GameComposite(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout(3, false));
		
		Composite HistoryWindow = new Composite(this, SWT.NONE);
		HistoryWindow.setLayout(new GridLayout(1, false));
		GridData gd_HistoryWindow = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
		gd_HistoryWindow.heightHint = 500;
		gd_HistoryWindow.widthHint = 200;
		HistoryWindow.setLayoutData(gd_HistoryWindow);
		
		Label lblHistory = new Label(HistoryWindow, SWT.NONE);
		lblHistory.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblHistory.setText("History");
		
		Composite composite_2 = new Composite(HistoryWindow, SWT.NONE);
		composite_2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		composite_2.setLayout(new GridLayout(1, false));
		
		HistoryText = new Text(composite_2, SWT.BORDER | SWT.READ_ONLY | SWT.WRAP | SWT.V_SCROLL);
		HistoryText.setBackground(SWTResourceManager.getColor(SWT.COLOR_LIST_BACKGROUND));
		HistoryText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		Composite GameWindow = new Composite(this, SWT.NONE);
		GridData gd_GameWindow = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gd_GameWindow.widthHint = 277;
		GameWindow.setLayoutData(gd_GameWindow);
		GameWindow.setLayout(new GridLayout(1, false));
		
		Label lblGame = new Label(GameWindow, SWT.NONE);
		lblGame.setText("Game");
		
		Composite composite = new Composite(GameWindow, SWT.NONE);
		GridData gd_composite = new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1);
		gd_composite.widthHint = 242;
		composite.setLayoutData(gd_composite);
		composite.setLayout(new GridLayout(1, false));
		
		GameTextWindow = new Text(composite, SWT.BORDER | SWT.READ_ONLY | SWT.WRAP | SWT.V_SCROLL);
		GameTextWindow.setBackground(SWTResourceManager.getColor(SWT.COLOR_LIST_BACKGROUND));
		GameTextWindow.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		Composite ActionSender = new Composite(GameWindow, SWT.NONE);
		ActionSender.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false, 1, 1));
		ActionSender.setBounds(0, 0, 377, 40);
		ActionSender.setLayout(new GridLayout(3, false));
		
		Label lblAction = new Label(ActionSender, SWT.NONE);
		lblAction.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblAction.setText("Action");
		
		NewAction = new Text(ActionSender, SWT.BORDER);
		NewAction.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		NewAction.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == 13 && !NewAction.getText().equals("")) {
					//setGameText(GameTextWindow.getText() + "\r\n" + NewAction.getText());
					sendAction(NewAction.getText());
					NewAction.setText("");
				}
			}
		});
		
		Button ActionSendBtn = new Button(ActionSender, SWT.NONE);
		ActionSendBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				if (!NewAction.getText().equals("")) {
					//setGameText(GameTextWindow.getText() + "\r\n" + NewAction.getText());
					sendAction(NewAction.getText());
					NewAction.setText("");
				}
			}
		});
		ActionSendBtn.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		ActionSendBtn.setText("Send");
		
		Composite ChatWindow = new Composite(this, SWT.NONE);
		GridData gd_ChatWindow = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
		gd_ChatWindow.widthHint = 278;
		ChatWindow.setLayoutData(gd_ChatWindow);
		ChatWindow.setLayout(new GridLayout(1, false));
		
		Label ChatLabel = new Label(ChatWindow, SWT.NONE);
		ChatLabel.setText("Chat");
		
		Composite composite_1 = new Composite(ChatWindow, SWT.NONE);
		composite_1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1));
		composite_1.setLayout(new GridLayout(1, false));
		
		ChatText = new Text(composite_1, SWT.BORDER | SWT.READ_ONLY | SWT.WRAP | SWT.V_SCROLL);
		ChatText.setBackground(SWTResourceManager.getColor(SWT.COLOR_LIST_BACKGROUND));
		ChatText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		Composite MessageSender = new Composite(ChatWindow, SWT.NONE);
		MessageSender.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false, 1, 1));
		MessageSender.setSize(456, 40);
		MessageSender.setLayout(new GridLayout(3, false));
		
		Label MessageSendButton = new Label(MessageSender, SWT.NONE);
		MessageSendButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		MessageSendButton.setBounds(0, 0, 70, 20);
		MessageSendButton.setText("Message");
		
		NewMessage = new Text(MessageSender, SWT.BORDER);
		NewMessage.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == 13 && !NewMessage.getText().equals("")) {
					addChatMessage("[ME]: " + NewMessage.getText());
					if(messageQueue != null) {
						messageQueue.add("Message:4\nType:Chat\n\n" + NewMessage.getText());
					}
					NewMessage.setText("");
				}
			}
		});
		NewMessage.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Button ChatSendBtn = new Button(MessageSender, SWT.NONE);
		ChatSendBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				if (!NewMessage.getText().equals("")) {
					addChatMessage("[ME]: " + NewMessage.getText());
					if(messageQueue != null) {
						messageQueue.add("Message:4\nType:Chat\n\n" + NewMessage.getText());
					}
					
					NewMessage.setText("");
					
				}
			}
		});
		ChatSendBtn.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		ChatSendBtn.setText("Send");

	}
	
	
	public void addChatMessage(String message) {
		if (ChatText.getText().length() < 1)
			ChatText.setText(message + "\r\n");
		else {
			int start = ChatText.getText().length();
			ChatText.setText(ChatText.getText() + message + "\r\n");
			ChatText.setSelection(start);
		}
	}
	
	public void sendAction(String action) {
		if(messageQueue != null) {
			messageQueue.add("Command " + action);
		}
	}
	
	public void setGameText(String text) {
		GameTextWindow.setText(text);
	}
	
	public void addHistory(String historyText) {
		if (HistoryText.getText().length() < 1)
			HistoryText.setText(historyText + "\r\n");
		else {
			int start = HistoryText.getText().length();
			HistoryText.setText(HistoryText.getText() + historyText + "\r\n");
			HistoryText.setSelection(start);
		}
	}

	public void setMessageQueue(ArrayBlockingQueue<String> messageQueue) {
		this.messageQueue = messageQueue;
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
	
	
}
