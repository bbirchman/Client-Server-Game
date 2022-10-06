import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PokerGame {
	List<PokerPlayer> players;
	List<PokerPlayer> spectators;
	int currentPlayer;
	int currentStarting;
	int pot;
	int toCall;
	String gameWinner;
	List<Card> community;
	List<Card> deck;
	private static int smallBlind = 100;
	private static int bigBlind = 200;

	public PokerGame(List<PlayerThread> playerList) {
		players = Collections.synchronizedList(new ArrayList<PokerPlayer>());
		spectators = Collections.synchronizedList(new ArrayList<PokerPlayer>());
		currentPlayer = 0;
		currentStarting = 0;
		pot = 0;
		toCall = 0;
		gameWinner = null;
		community = Collections.synchronizedList(new ArrayList<Card>());
		synchronized (playerList) {
			for (PlayerThread player : playerList) {
				players.add(new PokerPlayer(player));
			}
		}
		// Inital game setup
		resetGame();
	}

	public String getGamestate(int id) {
		String gamestate = "";
		synchronized (players) {
			for (PokerPlayer player : players) {
				gamestate += player.getPlayer().getUsername() + ": Chips: " + player.getChips() + " Bet: "
						+ player.getBet();
				if (!player.isInHand()) {
					gamestate += " [Folded]";
				}
				gamestate += "\n";
			}
			for (PokerPlayer player : spectators) {
				gamestate += player.getPlayer().getUsername() + " [SPECTATOR]\n";
			}
			gamestate += "\n";
			gamestate += "Hand: " + getPlayer(id).getHand() + "\n";
			gamestate += "Community: " + community + "\n";
			gamestate += "Pot " + pot + "\n";
			gamestate += "It is currently " + players.get(currentPlayer).getPlayer().getUsername()
					+ "'s turn to act.\n";
			gamestate += "They have to call at least " + (toCall - players.get(currentPlayer).getBet()) + " chips.";

		}
		return gamestate;
	}

	public String getHistory(int id) {
		synchronized (players) {
			String history = getPlayer(id).getHistory();
			getPlayer(id).setHistory(null);
			return history;
		}
	}

	public void fold(int id, boolean removed) {
		synchronized (players) {
			if (!isSpectator(id)) {
				PokerPlayer foldedPlayer = getPlayer(id);
				getPlayer(id).setInHand(false);
				boolean outOfOrder = false;
				if (getPlayer(id) != players.get(currentPlayer)) {
					outOfOrder = true;
				}
				if (removed) {
					players.remove(getPlayer(id));
				}
				// Check for no more players
				int playerCount = 0;
				PokerPlayer winner = null;
				for (PokerPlayer player : players) {
					if (player.isInHand()) {
						winner = player;
						playerCount++;
					}
				}
				if (playerCount < 2) {
					// Round over
					winner.setChips(winner.getChips() + pot);
					resetGame();
				} else {
					// Set history to player folded
					for (PokerPlayer player : players) {
						if (!removed) {
							player.setHistory(getPlayer(id).getPlayer().getUsername() + " folded");
						} else {
							player.setHistory(foldedPlayer.getPlayer().getUsername() + " left the game");
						}
					}
					for (PokerPlayer player : spectators) {
						if (!removed) {
							player.setHistory(getPlayer(id).getPlayer().getUsername() + " folded");
						} else {
							player.setHistory(foldedPlayer.getPlayer().getUsername() + " left the game");
						}
					}
					// Check for showdown
					nextPhaseCheck();
				}
			}
		}
	}

	public void bet(int id, int chips) {
		synchronized (players) {
			if (!isSpectator(id)) {
				PokerPlayer player = getPlayer(id);
				if (player.getPlayer().getID() == players.get(currentPlayer).getPlayer().getID()) {
					if (player.isInHand()) {
						if (player.getChips() >= ((toCall + chips) - player.getBet())) {
							player.setChips(player.getChips() - ((toCall - player.getBet()) + chips));
							pot += (toCall - player.getBet() + chips);
							toCall += chips;
							player.setBet(toCall);
							for (PokerPlayer playerMessage : players) {
								playerMessage.setHistory(player.getPlayer().getUsername() + " bet " + chips + " chips");
							}
							for (PokerPlayer playerMessage : spectators) {
								playerMessage.setHistory(player.getPlayer().getUsername() + " bet " + chips + " chips");
							}
							nextPhaseCheck();
						} else {
							player.setHistory("You do not have enough chips");
						}
					} else {
						player.setHistory("You are not currently in a hand");
					}
				} else {
					player.setHistory("It is not your turn to act");
				}
			}
		}
	}

	public String isGameOver() {
		return gameWinner;
	}

	// Check for all but one player folded or all checked with five cards shown
	// Figure out who won and award them the pot
	private void nextPhaseCheck() {
		// Check for all in
		boolean allInPass = true;
		// Check for only one active player not all in
		/*for (PokerPlayer player : players) {
			if ((player.getChips() > 0 && player.isInHand()) && player.getBet() < toCall) {
				for(PokerPlayer allinCheck : players) {
					if(!allinCheck.equals(player)) {
						if(!(allinCheck.getChips() == 0 || !allinCheck.isInHand())) {
							allInPass = false;
							break;
						}
					}
				}
			}
		}
		if (allInPass) {// only one active player not all in
			for (int i = community.size(); i < 5; i++) {
				community.add(deck.remove(0));
			}
		}*/
		// Check for community size
		switch (community.size()) {
		case 0: // Preflop
			if (callCheck()) {
				for (int i = 0; i < 3; i++) {
					community.add(deck.remove(0));
				}
			} 
			nextPlayer();
			break;
		case 3: // Flop
			if (callCheck()) {
				community.add(deck.remove(0));
			}
			nextPlayer();
			break;
		case 4: // Turn
			if (callCheck()) {
				community.add(deck.remove(0));
			}
			nextPlayer();
			break;
		case 5: // River
			if (callCheck()) {
				List<PokerPlayer> handCheck = Collections.synchronizedList(new ArrayList<PokerPlayer>());
				// Order players by hand strength
				for (PokerPlayer player : players) {
					if (player.isInHand()) {
						handCheck.add(player);
					}
				}
				//High card wins
				PokerPlayer winner = players.get(0);
				int max = 0;
				for(PokerPlayer player : handCheck) {
					for(Card card : player.getHand()) {
						Card.Rank[] ranks = Card.Rank.values();
						for(int i=0; i<ranks.length; i++) {
							if(card.rank == ranks[i]) {
								if(i>max) {
									winner = player;
								}
							}
						}
					}
				}
				winner.setChips(winner.getChips() + pot);
				for (int i = 0; i < players.size(); i++) {
					PokerPlayer player = players.get(i);
					if (player.getChips() <= 0) {
						spectators.add(players.get(i));
						players.remove(i);
						i--;
					}
					player.setHistory(winner.getPlayer().getUsername() + " won " + pot + " chips");
				}
				if(players.size()<=1) {
					gameWinner = winner.getPlayer().getUsername();
				}
				resetGame();
				// Find highest hand
				// player.
			} else {
				nextPlayer();
			}
			break;
		}

	}

	// Check for all players called
	private boolean callCheck() {
		synchronized (players) {
			for (PokerPlayer player : players) {
				if (player.isInHand() && player.getChips() > 0 && player.getBet() != toCall) {
					return false;
				}
			}
			return true;
		}
	}

	// Reset everything back to the start of a round
	private void resetGame() {
		pot = 0;
		deck = Card.createDeck();
		synchronized (players) {
			for (PokerPlayer player : players) {
				player.setBet(0);
				player.setInHand(true);
				List<Card> hand = Collections.synchronizedList(new ArrayList<Card>());
				hand.add(deck.remove(0));
				hand.add(deck.remove(0));
				player.setHand(hand);
			}
			community.clear();
			PokerPlayer blind = players.get(currentStarting);
			blind.setBet(Math.min(blind.getChips(), smallBlind));
			blind.setChips(blind.getChips() - Math.min(blind.getChips(), smallBlind));
			blind = players.get((currentStarting + 1) % players.size());
			blind.setBet(Math.min(blind.getChips(), bigBlind));
			blind.setChips(blind.getChips() - Math.min(blind.getChips(), bigBlind));
			currentPlayer = currentStarting;
			currentStarting = (currentStarting + 1) % players.size();
			pot += bigBlind + smallBlind;
			toCall = bigBlind;
		}
	}

	// Find the next active player
	private void nextPlayer() {
		currentPlayer = (currentPlayer + 1) % players.size();
		synchronized (players) {
			while (!(players.get(currentPlayer).isInHand() && players.get(currentPlayer).getChips() != 0)) {
				if (currentPlayer >= players.size()) {
					currentPlayer = 0;
				} else {
					currentPlayer++;
				}
			}
		}
	}

	private PokerPlayer getPlayer(int id) {
		for (PokerPlayer player : players) {
			if (player.getPlayer().getID() == id) {
				return player;
			}
		}
		for (PokerPlayer player : spectators) {
			if (player.getPlayer().getID() == id) {
				return player;
			}
		}
		return null;
	}

	private boolean isSpectator(int id) {
		for (PokerPlayer player : spectators) {
			if (player.getPlayer().getID() == id) {
				return true;
			}
		}
		return false;
	}
}
