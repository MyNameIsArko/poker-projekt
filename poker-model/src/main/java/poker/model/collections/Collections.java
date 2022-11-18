package poker.model.collections;

import poker.common.card.Card;
import poker.common.deck.Deck;

import java.util.HashMap;
import java.util.List;

public class Collections {
    public interface IsCollection {
        boolean check(Deck deck);
    }

    private static final IsCollection[] collections = new IsCollection[] {
            Collections::isRoyalFlush,
            Collections::isFlush,
            Collections::isFourOfKind,
            Collections::isFull,
            Collections::isColor,
            Collections::isStraight,
            Collections::isThreeOfKind,
            Collections::isTwoPair,
            Collections::isPair,
    };

    public static IsCollection[] getCollections() {
        return collections;
    }

    private static HashMap<Integer, Integer> getValueCounter(Deck deck) {
        HashMap<Integer, Integer> valueCounter = new HashMap<>();
        List<Card> cards = deck.getCards();
        for (Card card : cards) {
            if (valueCounter.containsKey(card.getRank())) {
                valueCounter.put(card.getRank(), valueCounter.get(card.getRank()) + 1);
            } else {
                valueCounter.put(card.getRank(), 1);
            }
        }
        return valueCounter;
    }

    private static boolean isRoyalFlush(Deck deck) {
        if (!isColor(deck)) {
            return false;
        }
        List<Card> cards = deck.getCards();
        // ! Size will always be 5
        return cards.size() == 5 && cards.get(0).getRank() == 10 && cards.get(1).getRank() == 11 &&
                cards.get(2).getRank() == 12 && cards.get(3).getRank() == 13 && cards.get(4).getRank() == 14;
    }

    private static boolean isFlush(Deck deck) {
        if (!isColor(deck)) {
            return false;
        }
        return isStraight(deck);
    }

    private static boolean isFourOfKind(Deck deck) {
        HashMap<Integer, Integer> valueCounter = getValueCounter(deck);
        for (int counter : valueCounter.values()) {
            if (counter == 4) {
                return true;
            }
        }
        return false;
    }

    private static boolean isFull(Deck deck) {
        List<Card> cards = deck.getCards();
        return (cards.get(0).getRank() == cards.get(1).getRank() && cards.get(1).getRank() == cards.get(2).getRank() &&
                cards.get(3).getRank() == cards.get(4).getRank()) || (cards.get(0).getRank() == cards.get(1).getRank() &&
                cards.get(2).getRank() == cards.get(3).getRank() &&  cards.get(3).getRank() == cards.get(4).getRank());
    }

    private static boolean isColor(Deck deck) {
        int suit = deck.getCards().get(0).getSuit();
        for (int i = 1; i < deck.getCards().size(); i++) {
            if (deck.getCards().get(i).getSuit() != suit) {
                return false;
            }
        }
        return true;
    }

    private static boolean isStraight(Deck deck) {
        List<Card> cards = deck.getCards();
        int firstRank = cards.get(0).getRank();
        for (int i = 1; i < cards.size(); i++) {
            if (cards.get(1).getRank() != firstRank + i) {
                return false;
            }
        }
        return true;
    }

    private static boolean isThreeOfKind(Deck deck) {
        HashMap<Integer, Integer> valueCounter = getValueCounter(deck);
        for (int counter : valueCounter.values()) {
            if (counter >= 3) {
                return true;
            }
        }
        return false;
    }

    private static boolean isTwoPair(Deck deck) {
        HashMap<Integer, Integer> valueCounter = getValueCounter(deck);
        int twoAmounts = 0;
        for (int counter : valueCounter.values()) {
            if (counter >= 2) {
                twoAmounts += 1;
            }
        }
        return twoAmounts >= 2;
    }

    private static boolean isPair(Deck deck) {
        HashMap<Integer, Integer> valueCounter = getValueCounter(deck);
        for (int counter : valueCounter.values()) {
            if (counter >= 2) {
                return true;
            }
        }
        return false;
    }
}
