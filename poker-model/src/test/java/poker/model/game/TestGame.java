package poker.model.game;

import org.junit.Test;
import poker.common.card.Card;
import poker.common.player.Player;
import poker.common.state.GameState;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;

import static org.junit.Assert.*;

public class TestGame {
    @Test
    public void testConstructor() {
        Game game = new Game(10);
        assertNotNull(game.getPlayers());
        assertNotNull(game.getCurrentState());
    }

    @Test
    public void testStartGame() {
        Game game = new Game(5);
        game.startGame();
        assertEquals(GameState.FIRST_BID, game.getCurrentState());
    }

    @Test
    public void testAddRemovePlayer() throws IOException {
        Game game = new Game(5);
        Player player = new Player(SocketChannel.open(), 3);
        game.addPlayer(player);
        assertEquals(1, game.getPlayers().size());
        game.removePlayer(player);
        assertEquals(0, game.getPlayers().size());
    }

    @Test
    public void testGivePlayersCard() throws IOException {
        Game game = new Game(5);
        game.addPlayer(new Player(SocketChannel.open(), 2));
        game.givePlayersCards();
        assertEquals(5, game.getPlayers().get(0).getPlayerDeck().getCards().size());
    }

    @Test
    public void testResetGame() throws IOException {
        Game game = new Game(5);
        Player p1 = new Player(SocketChannel.open(), 2);
        game.addPlayer(p1);
        game.startGame();
        game.raiseBid(10, p1);
        game.resetGame();
        assertEquals(0, game.getPlayers().size());
        assertEquals(GameState.ANTE, game.getCurrentState());
    }

    @Test
    public void testGivePlayerCard() throws IOException {
        Game game = new Game(5);
        Player player = new Player(SocketChannel.open(), 2);
        game.addPlayer(player);
        game.givePlayerCards(player);
        assertEquals(5, game.getPlayers().get(0).getPlayerDeck().getCards().size());
    }

    @Test
    public void testFindPlayerFromClient() throws IOException {
        Game game = new Game(5);
        SocketChannel client = SocketChannel.open();
        Player player = new Player(client, 2);
        game.addPlayer(player);
        assertEquals(player, game.findPlayerFromClient(client));

        SocketChannel client2 = SocketChannel.open();
        assertNull(game.findPlayerFromClient(client2));
    }

    @Test
    public void testShowInfoToPlayers() throws IOException {
        Selector selector = Selector.open();

        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();

        InetSocketAddress inetSocketAddress = new InetSocketAddress("localhost", 9999);

        serverSocketChannel.bind(inetSocketAddress);

        serverSocketChannel.configureBlocking(false);

        int ops = serverSocketChannel.validOps();

        serverSocketChannel.register(selector, ops, null);

        Game game = new Game(5);
        InetSocketAddress inetSocketAddress2 = new InetSocketAddress("localhost", 9999);
        SocketChannel client = SocketChannel.open(inetSocketAddress2);
        Player player = new Player(client, 2);
        game.addPlayer(player);

        player.getPlayerDeck().addCardToDeck(new Card(1 ,2));
        player.getPlayerDeck().addCardToDeck(new Card(2 ,2));
        player.getPlayerDeck().addCardToDeck(new Card(3 ,2));

        try {
            game.showInfoToPlayers();
        } catch (Exception e) {
            fail("Cannot send message to player!");
        } finally {
            player.getClient().close();
            serverSocketChannel.close();
            selector.close();
        }
    }

    @Test
    public void testNextPlayerAction() throws IOException {
        Selector selector = Selector.open();
//
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
//
        InetSocketAddress inetSocketAddress = new InetSocketAddress("localhost", 9999);
//
        serverSocketChannel.bind(inetSocketAddress);
//
        serverSocketChannel.configureBlocking(false);
//
        int ops = serverSocketChannel.validOps();
//
        serverSocketChannel.register(selector, ops, null);

        Game game = new Game(5);
        InetSocketAddress inetSocketAddress2 = new InetSocketAddress("localhost", 9999);
        SocketChannel client = SocketChannel.open(inetSocketAddress2);
        Player player = new Player(client, 2);
        game.addPlayer(player);
        game.startGame();

        GameState previous = game.getCurrentState();
        game.nextPlayerAction();
        assertEquals(previous, game.getCurrentState());

        Player player2 = new Player(client, 2);
        game.addPlayer(player2);
        game.nextPlayerAction();
        game.nextPlayerAction();
        game.nextPlayerAction();
        GameState currentState = game.getCurrentState();
        assertNotEquals(previous, currentState);

        game.addPlayer(new Player(client, 3));
        game.removePlayer(player2);
        game.nextPlayerAction();

        assertEquals(currentState, game.getCurrentState());

        game.nextPlayerAction();
        game.raiseBid(10, player);

        game.nextPlayerAction();
        game.nextPlayerAction();

        assertNotEquals(currentState, game.getCurrentState());

        game.nextPlayerAction();
        game.nextPlayerAction();
        assertEquals(GameState.SHOWDOWN, game.getCurrentState());

        game.resetGame();
        game.addPlayer(player);
        game.addPlayer(player2);
        game.startGame();
        game.nextPlayerAction();
        game.raiseBid(10, player);
        game.nextPlayerAction();
        game.nextPlayerAction();
        assertEquals(GameState.DISCARD_CARDS, game.getCurrentState());
        game.nextPlayerAction();
        game.nextPlayerAction();
        assertEquals(GameState.SECOND_BID, game.getCurrentState());
        game.nextPlayerAction();
        game.raiseBid(20, player2);
        game.nextPlayerAction();
        assertEquals(GameState.SECOND_BID, game.getCurrentState());


        client.close();
        serverSocketChannel.close();
    }
}
