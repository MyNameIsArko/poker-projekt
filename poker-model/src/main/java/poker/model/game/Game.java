package poker.model.game;

import poker.common.card.Card;
import poker.common.deck.Deck;
import poker.common.player.Player;
import poker.common.state.GameState;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

public class Game {
    private final List<Player> players;
    private Deck deck;
    private int nextPlayerIndex = -1;

    private GameState currentState;

    private int bid;
    private boolean justRemovedPlayer = false;

    private boolean someoneRaisedBid = false;
    private Player whoRaisedBid = null;

    public void nextPlayerAction() throws IOException {
        if (players.size() < 2) {
            return;
        }
        if (!justRemovedPlayer) {
            nextPlayerIndex += 1;
        }
        if (someoneRaisedBid) {
            if (nextPlayerIndex >= players.size()) {
                nextPlayerIndex = 0;
            }
            if (players.get(nextPlayerIndex).equals(whoRaisedBid)) {
                whoRaisedBid = null;
                someoneRaisedBid = false;
                nextPlayerIndex = 0;
                currentState = currentState.next();
            }
        } else if (nextPlayerIndex >= players.size()) {
            nextPlayerIndex = 0;
            currentState = currentState.next();
        }
        if (currentState != GameState.FIRST_BID) {
            showInfoToPlayer(players.get(nextPlayerIndex));
        }
        ByteBuffer actionBuffer;
        switch (currentState) {
            case FIRST_BID -> actionBuffer = ByteBuffer.wrap(("ACTION FIRST_BID" + (someoneRaisedBid ? "" : " CHECK") + "\n").getBytes());
            case DISCARD_CARDS -> actionBuffer = ByteBuffer.wrap("ACTION DISCARD\n".getBytes());
            case SECOND_BID -> actionBuffer = ByteBuffer.wrap(("ACTION SECOND_BID" + (someoneRaisedBid ? "" : " CHECK") + "\n").getBytes());
            default -> actionBuffer = ByteBuffer.wrap("".getBytes());
        }
        players.get(nextPlayerIndex).getClient().write(actionBuffer);
        justRemovedPlayer = false;
    }

    public Game(int bid) {
        players = new ArrayList<>();
        deck = new Deck();
        deck.generateDeck();
        currentState = GameState.ANTE;
        this.bid = bid;
    }

    public void startGame() {
        currentState = currentState.next();
    }

    public void addPlayer(Player player) {
        players.add(player);
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void givePlayersCards() {
        deck.shuffle();
        for (int i = 0; i < 5; i++) {
            for (Player player : players) {
                Card cardToGive = deck.pullCard();
                player.getPlayerDeck().addCardToDeck(cardToGive);
            }
        }
    }

    public void givePlayerCards(Player player) {
        for (int i = player.getPlayerDeck().getCards().size(); i < 5; i++) {
            Card cardToGive = deck.pullCard();
            player.getPlayerDeck().addCardToDeck(cardToGive);
        }
    }

    public void raiseBid(int amount, Player who) {
        bid += amount;
        someoneRaisedBid = true;
        whoRaisedBid = who;
    }

    public void showInfoToPlayers() throws IOException {
        for (Player player : players) {
            showInfoToPlayer(player);
        }
    }

    public void showInfoToPlayer(Player player) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap((player.getPlayerDeck().getStringCards() + "\nCurrent bid: " + bid).getBytes());
        player.getClient().write(buffer);
    }

    public void resetGame() {
        players.clear();
        currentState = GameState.ANTE;
        nextPlayerIndex = -1;
        deck = new Deck();
        deck.generateDeck();
    }

    public GameState getCurrentState() {
        return currentState;
    }

    public void removePlayer(Player player) {
        players.remove(player);
        justRemovedPlayer = true;
    }

    public Player findPlayerFromClient(SocketChannel client) {
        for (Player player : players) {
            if (player.getClient().equals(client)) {
                return player;
            }
        }
        return null;
    }
}
