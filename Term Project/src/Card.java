import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Card {
	private static final String RANKS = "23456789TJQKA";
	
	public enum Category {
        HIGH_CARD,
        ONE_PAIR,
        TWO_PAIR,
        THREE_OF_A_KIND,
        STRAIGHT,
        FLUSH,
        FULL_HOUSE,
        FOUR_OF_A_KIND,
        STRAIGHT_FLUSH;
    }

    public enum Rank {
        TWO,
        THREE,
        FOUR,
        FIVE,
        SIX,
        SEVEN,
        EIGHT,
        NINE,
        TEN,
        JACK,
        QUEEN,
        KING,
        ACE;
    }

    public enum Suit {
        DIAMONDS,
        CLUBS,
        HEARTS,
        SPADES;
    }

    public final Rank rank;

    public final Suit suit;

    Card(Rank rank, Suit suit) {
        this.rank = rank;
        this.suit = suit;
    }
    
    public String toString() {
    	String cardDisplay = "";
    	switch(rank) {
		case ACE:
			cardDisplay += "A";
			break;
		case EIGHT:
			cardDisplay += "8";
			break;
		case FIVE:
			cardDisplay += "5";
			break;
		case FOUR:
			cardDisplay += "4";
			break;
		case JACK:
			cardDisplay += "J";
			break;
		case KING:
			cardDisplay += "K";
			break;
		case NINE:
			cardDisplay += "9";
			break;
		case QUEEN:
			cardDisplay += "Q";
			break;
		case SEVEN:
			cardDisplay += "7";
			break;
		case SIX:
			cardDisplay += "6";
			break;
		case TEN:
			cardDisplay += "10";
			break;
		case THREE:
			cardDisplay += "3";
			break;
		case TWO:
			cardDisplay += "2";
			break;
    	}
    	switch(suit) {
    	case CLUBS:
			cardDisplay += " CLUBS";
			break;
		case DIAMONDS:
			cardDisplay += " DIAMONDS";
			break;
		case HEARTS:
			cardDisplay += " HEARTS";
			break;
		case SPADES:
			cardDisplay += " SPADES";
			break;
    	
    	}
    	return cardDisplay;
    }
    
    public static List<Card> createDeck(){
    	List<Card> deck = Collections.synchronizedList(new ArrayList<Card>());
    	for(Rank rank: Rank.values()) {
    		for(Suit suit: Suit.values()) {
    			//One of each color
    			deck.add(new Card(rank, suit));
    			deck.add(new Card(rank, suit));
    		}
    	}
    	Random rnd = new Random();
    	Collections.shuffle(deck, rnd);
    	return deck;
    }
}
