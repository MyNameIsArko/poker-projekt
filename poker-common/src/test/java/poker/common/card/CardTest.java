package poker.common.card;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CardTest {
    @Test
    public void testConstructor() {
        Card card = new Card(4, 2);
        assertEquals(4, card.getRank());
        assertEquals(2, card.getSuit());
    }
    @Test
    public void testToString() {
        Card card1 = new Card(4, 2);
        assertEquals("Card: Kier 4", card1.toString());
        Card card2 = new Card(11, 1);
        assertEquals("Card: Pik Jupek", card2.toString());
        Card card3 = new Card(14, 3);
        assertEquals("Card: Karo As", card3.toString());
        Card card4 = new Card(12, 4);
        assertEquals("Card: Trefl Dama", card4.toString());
        Card card5 = new Card(13, 1);
        assertEquals("Card: Pik Król", card5.toString());
    }
}
