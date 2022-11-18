package poker.common.deck;

import org.junit.Test;
import poker.common.card.Card;

import static org.junit.Assert.*;

public class DeckTest {
    @Test
    public void testConstructor() {
        Deck deck = new Deck();
        assertNotNull(deck.getCards());
    }

    @Test
    public void testGenerateDeck() {
        Deck deck = new Deck();
        deck.generateDeck();
        assertEquals(56, deck.getCards().size());
    }

    @Test
    public void testAddCardToDeck() {
        Deck deck = new Deck();
        assertEquals(0, deck.getCards().size());
        deck.addCardToDeck(new Card(2, 4));
        assertEquals(1, deck.getCards().size());
    }

    @Test
    public void testPullCard() {
        Deck deck = new Deck();
        deck.addCardToDeck(new Card(2, 4));
        deck.addCardToDeck(new Card(3, 1));
        Card popCard = deck.pullCard();
        assertEquals(3, popCard.getRank());
        assertEquals(1, popCard.getSuit());
    }

    @Test
    public void testFactory() {
        Deck deck = new Deck();
        deck.generateDeck();
        Card tmp = deck.getCards().get(5);
        deck.getCards().set(5, deck.getCards().get(12));
        deck.getCards().set(12, tmp);
        tmp = deck.getCards().get(4);
        deck.getCards().set(4, deck.getCards().get(46));
        deck.getCards().set(46, tmp);
        deck.factory();
        boolean isSorted = true;
        for (int i = deck.getCards().size() - 1; i > 0; i--) {
            if (deck.getCards().get(i).getSuit() < deck.getCards().get(i - 1).getSuit()) {
                isSorted = false;
                break;
            }
        }
        assertTrue("Deck is not sorted!",isSorted);
    }

    @Test
    public void testShuffle() {
        Deck deck = new Deck();
        deck.generateDeck();
        deck.shuffle();
        int shuffledCards = 0;
        for (int i = 1; i < deck.getCards().size(); i++) {
            if (deck.getCards().get(i - 1).getSuit() > deck.getCards().get(i).getSuit() ||
                    (deck.getCards().get(i - 1).getSuit() == deck.getCards().get(i).getSuit()) &&
                            deck.getCards().get(i - 1).getRank() > deck.getCards().get(i).getRank()) {
                shuffledCards += 1;
            }
        }
        assertTrue("Not enough shuffled cards!", shuffledCards > 10);
    }

    @Test
    public void testRemoveCardByIndex() {
        Deck deck = new Deck();
        Card card1 = new Card(2, 2);
        Card card2 = new Card(9, 3);
        deck.addCardToDeck(card1);
        deck.addCardToDeck(card2);
        deck.removeCardByIndex(0);
        assertEquals(card2, deck.getCards().get(0));
    }

    @Test
    public void testGetStringCards() {
        Deck deck = new Deck();
        Card card1 = new Card(2, 2);
        Card card2 = new Card(9, 3);
        deck.addCardToDeck(card1);
        deck.addCardToDeck(card2);
        String cardString = "| " + card1 + " | " + card2 + " |";
        assertEquals(cardString, deck.getStringCards());
    }
}
