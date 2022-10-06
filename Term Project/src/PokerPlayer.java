import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PokerPlayer {
	

	private PlayerThread player;
	private List<Card> hand;
	private int chips;
	private int bet;
	private boolean inHand;
	private String history;
	public static final int startingChips = 5000;
	
	public PokerPlayer(PlayerThread player) {
		chips = startingChips;
		bet = 0;
		this.player = player;
		inHand = true;
		hand = Collections.synchronizedList(new ArrayList<Card>());
	}
	
	public PlayerThread getPlayer() {
		return player;
	}

	public List<Card> getHand() {
		return hand;
	}

	public int getChips() {
		return chips;
	}

	public int getBet() {
		return bet;
	}

	public boolean isInHand() {
		return inHand;
	}

	public String getHistory() {
		return history;
	}
	
	public void setChips(int chips) {
		this.chips = chips;
	}

	public void setInHand(boolean inHand) {
		this.inHand = inHand;
	}

	public void setHistory(String history) {
		this.history = history;
	}
	
	public void setBet(int bet) {
		this.bet = bet;
	}
	
	public void setHand(List<Card> hand) {
		this.hand = hand;
	}
}
