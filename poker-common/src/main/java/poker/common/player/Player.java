package poker.common.player;

import poker.common.deck.Deck;

import java.nio.channels.SocketChannel;

public class Player {

    private final Deck playerDeck;

    private final SocketChannel client;
    private final int playerNum;

    public Player(SocketChannel client, int num) {
        this.playerDeck = new Deck();
        this.client = client;
        playerNum = num;
    }

    public Deck getPlayerDeck() {
        return playerDeck;
    }

    public SocketChannel getClient() {
        return client;
    }

    public int getPlayerNum() {
        return playerNum;
    }
}
