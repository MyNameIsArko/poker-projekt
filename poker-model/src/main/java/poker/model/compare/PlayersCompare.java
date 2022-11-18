package poker.model.compare;

import poker.common.card.Card;
import poker.common.deck.Deck;
import poker.common.player.Player;
import poker.model.collections.Collections;

import java.util.ArrayList;
import java.util.List;

import static poker.model.collections.Collections.getCollections;

public class PlayersCompare {

    PlayersCompare() {
        throw new IllegalStateException("Utility class");
    }
    public static Player getWinner(List<Player> players) {
        for (Player player : players) {
            player.getPlayerDeck().factory();
        }
        List<Player> playersWon = new ArrayList<>();
        // Check collections from the highest valued to lowest for every player and get players with best collections
        getPlayersWon(players, playersWon);
        if (playersWon.isEmpty()) {
            // Highest card
            return getPlayerWithHighestCard(players);
        } else {
            if (playersWon.size() > 1) {
                // Highest colors
                return getPlayerWithHighestColor(playersWon);
            } else {
                // We have a winner!
                return playersWon.get(0);
            }
        }
    }

    private static Player getPlayerWithHighestColor(List<Player> players) {
        Player playerWithBestColor = players.get(0);
        for (Player player : players) {
            if (getHighestSuit(playerWithBestColor.getPlayerDeck()) < getHighestSuit(player.getPlayerDeck())) {
                playerWithBestColor = player;
            }
        }
        return playerWithBestColor;
    }

    private static Player getPlayerWithHighestCard(List<Player> players) {
        Player playerWithBestCard = players.get(0);
        for (Player player : players) {
            if (getHighestRank(playerWithBestCard.getPlayerDeck()) < getHighestRank(player.getPlayerDeck())) {
                playerWithBestCard = player;
            }
        }
        return playerWithBestCard;
    }

    private static void getPlayersWon(List<Player> players, List<Player> playersWon) {
        for (Collections.IsCollection collection : getCollections()) {
            for (Player player : players) {
                if (collection.check(player.getPlayerDeck())) {
                    playersWon.add(player);
                }
            }
            if (!playersWon.isEmpty()) {
                break;
            }
        }
    }

    private static int getHighestSuit(Deck deck) {
        List<Card> cards = deck.getCards();
        int maxSuit = cards.get(0).getSuit();
        for (Card card : cards) {
            if (maxSuit < card.getSuit()) {
                maxSuit = card.getSuit();
            }
        }
        return maxSuit;
    }

    private static int getHighestRank(Deck deck) {
        List<Card> cards = deck.getCards();
        int maxRank = cards.get(0).getRank();
        for (Card card : cards) {
            if (maxRank < card.getRank()) {
                maxRank = card.getRank();
            }
        }
        return maxRank;
    }
}
