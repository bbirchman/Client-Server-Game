import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
//connect 127.0.0.1 10000 
public class Lobby implements Runnable {
	ArrayBlockingQueue<Message> processingQueue;
	List<PlayerThread> playerList;
	List<Lobby> lobbyList;
	List<String> scoreBoardNames;
	List<Integer> scoreBoardNumbers;
	
	lobbyType type;
	lobbyState state;
	PokerGame pokerGame;
	private boolean gameRunningBuffer = false;
	//private boolean gameRunning = false;
	//private int playersReady;
	
	public Lobby(lobbyType type, List<Lobby> lobbyList, List<String> scoreBoardNames, List<Integer> scoreBoardNumbers) {
		this.type = type;
		this.state = lobbyState.STARTING;
		this.playerList = new ArrayList<PlayerThread>();
		this.processingQueue = new ArrayBlockingQueue<Message>(100);
		this.scoreBoardNames = scoreBoardNames;
		this.scoreBoardNumbers = scoreBoardNumbers;
		this.lobbyList = lobbyList;
		//this.playersReady = 0;
		//this.processingQueue = processingQueue;
	}

	
	public void run() { //Process messages
		
		while (true) {
			boolean updateHistory = false;
			boolean updateChat = false;				
			boolean newPlayerReady = false;
			int playersReady = 0;
			Message currMessage = null;
			
			if (this.type == lobbyType.GAME_LOBBY && this.state == lobbyState.RUNNING) {
				pokerGame = new PokerGame(playerList);
			}
			
			try {
				if ((currMessage = processingQueue.take()) != null) {
					System.out.println();
					
					ArrayList<String> currHeaders = currMessage.getHeaders(); //header 0 always contains ID
					PlayerThread currPlayer = null;
					int currID = Integer.parseInt(currHeaders.get(0));
					//String currUsername = "";
					//System.out.println(currMessage.getBody());
					
					List<PlayerThread> tempList = playerList; // Avoid threading conflicts
					for (PlayerThread p : tempList) { // Find player with ID from message
						if (p.getID() == -1) { // -1 ID means brute disconnect. Remove player from queue.
							playerList.remove(p);
							
						} else if (p.getID() == currID) {
							currPlayer = p;
						}
					}
					
					// if player is still connected
					if (currPlayer != null) {
						System.out.println("currType: " + currMessage.getType());
						System.out.println("currBody: " + currMessage.getBody());
						switch(currMessage.getType()) {
						
						case "Chat": 	  //update chat for every player
							System.out.println("CHAT_MESSAGE");

							updateChat = true;
							break;
						case "History":
							System.out.println("HISTORY MESSAGE IN QUEUE");
							updateHistory = true;
							break;
							
						case "Command":
							
							if (currMessage.getBody().equals("disconnect")) {
								updateHistory = true;
								currPlayer.disconnect();
								currMessage.setBody(currPlayer.getUsername() + " disconnected"); // for all other players					
								playerList.remove(currPlayer);
							}
							
							if (this.type == lobbyType.MAIN_LOBBY) { //if main lobby, look for main lobby commands only
								

								//System.out.println("current getBody(): " + currMessage.getBody());								
								if (currMessage.getBody().equals("creategame")) {
									
									Lobby newLobby = new Lobby(lobbyType.GAME_LOBBY, lobbyList, scoreBoardNames, scoreBoardNumbers);
									lobbyList.add(newLobby);
									Thread lobbyThread = new Thread(newLobby);
									lobbyThread.start(); //if lobby type is game lobby, the new lobby will instantiate a pokergame. 			
									currPlayer.changeLobby(newLobby.processingQueue);
									newLobby.addPlayer(currPlayer);
									playerList.remove(currPlayer);
									// send 
									System.out.println("CREATING GAME");
									currPlayer.sendMessage("Message:4\nType:History\n\ncreated new game");
									
								} else if (currMessage.getBody().equals("joingame")) {
									
									int lobbyID = Integer.parseInt(currHeaders.get(1));
									if (lobbyList.size()-1 < lobbyID || lobbyID == 0) {
										currMessage.setBody("Game doesnt exist");
										currPlayer.sendMessage(currMessage.sendResponse(4));

									} else if (lobbyList.get(lobbyID).state == lobbyState.RUNNING) {
										currMessage.setBody("Cannot join, game already in progress.");
										currPlayer.sendMessage(currMessage.sendResponse(4));
									} else {
										updateHistory = true;
										//System.out.println("joingame response:" + currMessage.sendResponse(4));
										currPlayer.changeLobby(lobbyList.get(lobbyID).processingQueue);
										lobbyList.get(lobbyID).addPlayer(currPlayer);
										playerList.remove(currPlayer);
										currMessage.setBody("joined game");
										currPlayer.sendMessage(currMessage.sendResponse(4));
										//currMessage.setBody(currPlayer.getUsername() + " has joined the game");
										
										//currPlayer.sendMessage();
									} 
									
								} else if (currMessage.getBody().equals("connect")) {
									currPlayer.setUsername(currHeaders.get(1));
									currPlayer.sendMessage(currMessage.sendResponse(4));
									
								} else if (currMessage.getBody().equals("listgames")) {
									int responseSize = 4;
									String gamelist = "current game lobbies: \n";
									System.out.println(lobbyList.size());
									
									for (int i = 1; i < lobbyList.size(); i++) {
										
										responseSize++;
										Lobby currLobby = lobbyList.get(i);
										gamelist += "game#" + i + " (" + currLobby.state + " with " + currLobby.playerList.size() + " players)  \n";
									}
									currMessage.setBody(gamelist);
									currPlayer.sendMessage(currMessage.sendResponse(responseSize));
								
								} else if (currMessage.getBody().equals("scoreboard")) {
									int responseSize = 4;
									String scoreboard = "high scores: \n";
									System.out.println(lobbyList.size());
									
									for (int i = 0; i < scoreBoardNames.size()-1; i++) {
										
										responseSize++;
										scoreboard += "" + scoreBoardNames.get(i) + ": " +  scoreBoardNumbers.get(i) + ", \n";
									}
									currMessage.setBody(scoreboard);
									currPlayer.sendMessage(currMessage.sendResponse(responseSize));
								} else {
								
									currPlayer.sendMessage("Message:4\nType:History\n\ninvalid command for main lobby"); // FORMAT MESSAGE FOR CLIENT
								}

							} else { // we are in a GAME_LOBBY; look for only game lobby commands
																										
								//before game starts, check and see if all players are ready
								if (currMessage.getBody().equals("exit")) {
									updateHistory = true;
									
									//if (this.state == lobbyState.RUNNING) pokerGame.fold(currID); // 				UNCOMMENT ONCE BUG FIXED IN POKERGAME.JAVA *******************																	
									currPlayer.changeLobby(lobbyList.get(0).processingQueue);
									lobbyList.get(0).addPlayer(currPlayer);
									playerList.remove(currPlayer);
									currMessage.setBody("exiting to main lobby");
									currPlayer.sendMessage(currMessage.sendResponse(4)); 
									currMessage.setBody(currPlayer.getUsername() + " left the game (folded)");
									
								}
								
								if (this.state == lobbyState.RUNNING) { // gameRunning is set true when ALL players have marked "ready"
									 if ( currMessage.getBody().equals("bet")) {
										updateHistory = true;
										int currBet = Integer.parseInt(currHeaders.get(1));
										pokerGame.bet(currID, currBet);
										currMessage.setBody(currPlayer.getUsername() + " has bet " + currBet + " chips");

									} else if (currMessage.getBody().equals("fold")) { 
										updateHistory = true;
										pokerGame.fold(currID, false); 
										currMessage.setBody(currPlayer.getUsername() + " has folded");
										
									} else if (currMessage.getBody().equals("disconnect")) {
										pokerGame.fold(currID, true);
									} else {
										//currPlayer.sendMessage("Message:4\nType:History\n\ninvalid game command"); // FORMAT MESSAGE FOR CLIENT **********************
									}
									
								} else { // IF GAME NOT RUNNING YET
									
									if (currMessage.getBody().equals("ready")) {
										currPlayer.setReady(true);
										newPlayerReady = true;
										
										currPlayer.sendMessage("Message:4\nType:History\n\nyou are ready");
									} else {
										currPlayer.sendMessage("Message:4\nType:History\n\ninvalid pre-game command"); // FORMAT MESSAGE FOR CLIENT **********************
									}
									for (PlayerThread p : playerList) {
										if (p.getReady()) {											
											playersReady++;
										}
									}
									System.out.println("players ready: " + playersReady);
									System.out.println("total players: " + playersReady);
								} 
							}								
							
							break;

						default: 		  //unknown type
							currPlayer.sendMessage("Message:4\nType:History\n\nunknown command");
						}

					} else { //player doesn't exist error (disconnected)
						if (this.type == lobbyType.GAME_LOBBY && this.state == lobbyState.RUNNING) pokerGame.fold(currID, true);
						//pokerGame.playerLeft(currID); 						
					}
					
					tempList = playerList; // Avoid threading conflicts				
					for (PlayerThread p : tempList) { //update messages for each player
						
						if (updateHistory) {					
							System.out.println("HISTORY_RESPONSE_BODY:" + currMessage.getResponseBody());
							p.sendMessage(currMessage.sendResponse(4));
							
						}
						if (updateChat) {
							if (p.getID() != currID) {
								String chatUsername = currPlayer.getUsername();
								String response = "Message:5\nType:Chat\nUser:" + chatUsername + "\n\n" + currMessage.getResponseBody();
								p.sendMessage(currMessage.sendResponse(response));
							}

							
							// needs to be currPlayer.sendChat() or something
						}
						// if game lobby, update gamestate
						if (this.type == lobbyType.GAME_LOBBY) {
							
							if (this.state == lobbyState.RUNNING) {
								//String gamestate = "game_state_update";
								String gamestate = pokerGame.getGamestate(p.getID());  // UNCOMMENT THIS WHEN POKER WORKS CORRECTLY ***************************
								
								currPlayer.sendMessage("Message:9\nType:Gamestate\n\n" + gamestate);
								
								if (pokerGame.getHistory(currID) != null) {
									
								}
								
								String winner;
								if ((winner = pokerGame.isGameOver()) != null) {    // use this once pokerGame.isGameOVer() returns STring or NULL instead of bool
									gameRunningBuffer = true;
									p.setReady(false);									
									currMessage.setBody("Game over, winner: " + winner);
									p.sendMessage(currMessage.sendResponse(4));
									int scoreboardid = scoreBoardNames.indexOf(winner);
									if(scoreboardid != -1) {
										scoreBoardNumbers.set(scoreboardid, scoreBoardNumbers.get(scoreboardid)+1);
									}else {
										scoreBoardNames.add(winner);
										scoreBoardNumbers.add(1);
									}
								}
							
							}
													
							if (this.state == lobbyState.STARTING) {
								if (newPlayerReady) {
									currMessage.setBody("ready players:");
									p.sendMessage(currMessage.sendResponse(4, "" + playersReady + "/" + playerList.size()));
								}
								if (playersReady >= playerList.size() && playerList.size() > 1) {
									currMessage.setBody("Everyone is ready. Game starting....");
									p.sendMessage(currMessage.sendResponse(4));
									gameRunningBuffer = true;			
								}				
							}
						}					
					}
					
					if (gameRunningBuffer) {
						if (this.state == lobbyState.STARTING) {
							this.state = lobbyState.RUNNING;
							gameRunningBuffer = false;
							System.out.println("GAME STARTING NOW");
							
						} else {
							this.state = lobbyState.STARTING;
							gameRunningBuffer = false;
							System.out.println("GAME RESTARTING NOW");
						}						
					}
									
					//System.out.println(currMessage.getBody());
				}
			} catch (NumberFormatException | InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println("EXCEPTION numberformat or interrupted");
			} catch (ConcurrentModificationException e) {
				System.out.println("ERROR: THREAD CONFLICT");
				//processingQueue.add(currMessage);
			}
			
			System.out.println("lobby " + this.type + " player count: " + playerList.size());
			if (this.type == lobbyType.GAME_LOBBY && playerList.size() == 0) {
				System.out.println("Closing game, no players remain");
				lobbyList.remove(this);
				break;
			}
		} //end of while loop
	}
	

	//if player exists, return false	
	//otherwise, add player and return true
	public boolean addPlayer(PlayerThread p) {
		if (this.type == lobbyType.GAME_LOBBY) {
			Message updateInfo = new Message("History", p.getUsername() + " has joined the game", p.getID());
			this.processingQueue.add(updateInfo);
		}

		playerList.add(p);
		return true;
	}
	//if player doesn't exist, return false
	//otherwise, remove player and return true
	public boolean removePlayer(int id) {
		return false;
	}
	
	//send input string to Message for parsing
	public void parseCommand(String s) {
	}
	
	//send input to lobby chat
	public boolean sendChat(String m) {
		return false;
	}
}