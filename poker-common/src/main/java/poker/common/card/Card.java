package poker.common.card;

public class Card {
    private final int rank;
    private final int suit;

    public Card(int rank, int suit) {
        this.rank = rank;
        this.suit = suit;
    }

    public int getRank() {
        return rank;
    }

    public int getSuit() {
        return suit;
    }

    @Override
    public String toString() {
        String suitString = switch (suit) {
            case 1 -> "Pik";
            case 2 -> "Kier";
            case 3 -> "Karo";
            default -> "Trefl";
        };
        String rankString = switch (rank) {
            case 11 -> "Jupek";
            case 12 -> "Dama";
            case 13 -> "Król";
            case 14 -> "As";
            default -> Integer.toString(rank);
        };
        return "Card: " + suitString + " " + rankString;
    }
}
