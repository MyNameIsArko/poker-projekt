package poker.common.deck;

import poker.common.card.Card;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public class Deck {

    private final List<Card> cards;

    public Deck() {
        cards = new ArrayList<>();
    }

    public List<Card> getCards() {
        return cards;
    }

    public void addCardToDeck(Card card) {
        cards.add(card);
    }

    private final SecureRandom rand = new SecureRandom();

    public Card pullCard() {
        Card card = cards.get(cards.size() - 1);
        cards.remove(cards.size() - 1);
        return card;
    }

    public void factory() {
        for (int i = 1; i < cards.size(); i++) {
            for (int j = 1; j < cards.size(); j++) {
                if (cards.get(j - 1).getSuit() > cards.get(j).getSuit() || (cards.get(j - 1).getSuit() == cards.get(j).getSuit() && cards.get(j - 1).getRank() > cards.get(j).getRank())) {
                    Card temp = cards.get(j - 1);
                    cards.set(j - 1, cards.get(j));
                    cards.set(j, temp);
                }
            }
        }
    }
    public void shuffle() {

        // Shuffle ten times
        for (int k = 0; k < 10; k++) {
            for (int i = 0; i < cards.size(); i++) {
                int j = rand.nextInt(cards.size());
                Card temp = cards.get(i);
                cards.set(i, cards.get(j));
                cards.set(j, temp);
            }}
    }

    public void generateDeck() {
        cards.clear();
        for (int i = 1; i < 5; i++) {
            for (int j = 1; j < 15; j++) {
                cards.add(new Card(j, i));
            }
        }
    }
    public String getStringCards() {
        StringBuilder cardsString = new StringBuilder();
        for (Card card : cards) {
            cardsString.append("| ").append(card).append(" ");
        }
        cardsString.append("|");
        return cardsString.toString();
    }

    public void removeCardByIndex(int index) {
        cards.remove(index);
    }
}
