package poker.model.compare;

import org.junit.Test;
import poker.common.card.Card;
import poker.common.player.Player;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class TestPlayersCompare {

    @Test
    public void testConstructor() {
        assertThrows(IllegalStateException.class, () -> new PlayersCompare());
    }

    @Test
    public void testGetWinner() throws IOException {
        List<Player> players = new ArrayList<>();
        players.add(new Player(SocketChannel.open(), 1));
        players.add(new Player(SocketChannel.open(), 2));

        players.get(0).getPlayerDeck().addCardToDeck(new Card(10, 1));
        players.get(0).getPlayerDeck().addCardToDeck(new Card(11, 1));
        players.get(0).getPlayerDeck().addCardToDeck(new Card(12, 1));
        players.get(0).getPlayerDeck().addCardToDeck(new Card(13, 1));
        players.get(0).getPlayerDeck().addCardToDeck(new Card(14, 1));

        players.get(1).getPlayerDeck().addCardToDeck(new Card(3, 1));
        players.get(1).getPlayerDeck().addCardToDeck(new Card(5, 2));
        players.get(1).getPlayerDeck().addCardToDeck(new Card(11, 4));
        players.get(1).getPlayerDeck().addCardToDeck(new Card(5, 3));
        players.get(1).getPlayerDeck().addCardToDeck(new Card(3, 2));

        assertEquals(players.get(0), PlayersCompare.getWinner(players));

        players.clear();
        players.add(new Player(SocketChannel.open(), 1));
        players.add(new Player(SocketChannel.open(), 2));

        players.get(0).getPlayerDeck().addCardToDeck(new Card(1, 1));
        players.get(0).getPlayerDeck().addCardToDeck(new Card(3, 1));
        players.get(0).getPlayerDeck().addCardToDeck(new Card(7, 1));
        players.get(0).getPlayerDeck().addCardToDeck(new Card(10, 1));
        players.get(0).getPlayerDeck().addCardToDeck(new Card(13, 1));

        players.get(1).getPlayerDeck().addCardToDeck(new Card(1, 3));
        players.get(1).getPlayerDeck().addCardToDeck(new Card(3, 3));
        players.get(1).getPlayerDeck().addCardToDeck(new Card(7, 3));
        players.get(1).getPlayerDeck().addCardToDeck(new Card(10, 3));
        players.get(1).getPlayerDeck().addCardToDeck(new Card(13, 3));

        assertEquals(players.get(1), PlayersCompare.getWinner(players));

        players.clear();
        players.add(new Player(SocketChannel.open(), 1));
        players.add(new Player(SocketChannel.open(), 2));

        players.get(0).getPlayerDeck().addCardToDeck(new Card(13, 1));
        players.get(0).getPlayerDeck().addCardToDeck(new Card(10, 2));
        players.get(0).getPlayerDeck().addCardToDeck(new Card(7, 1));
        players.get(0).getPlayerDeck().addCardToDeck(new Card(3, 3));
        players.get(0).getPlayerDeck().addCardToDeck(new Card(1, 2));

        players.get(1).getPlayerDeck().addCardToDeck(new Card(1, 3));
        players.get(1).getPlayerDeck().addCardToDeck(new Card(3, 2));
        players.get(1).getPlayerDeck().addCardToDeck(new Card(7, 3));
        players.get(1).getPlayerDeck().addCardToDeck(new Card(10, 2));
        players.get(1).getPlayerDeck().addCardToDeck(new Card(13, 1));

        assertEquals(players.get(0), PlayersCompare.getWinner(players));

        players.get(0).getPlayerDeck().addCardToDeck(new Card(3, 1));
        players.get(0).getPlayerDeck().addCardToDeck(new Card(3, 2));
        players.get(0).getPlayerDeck().addCardToDeck(new Card(3, 3));
        players.get(0).getPlayerDeck().addCardToDeck(new Card(6, 1));
        players.get(0).getPlayerDeck().addCardToDeck(new Card(6, 2));

        players.get(1).getPlayerDeck().addCardToDeck(new Card(1, 3));
        players.get(1).getPlayerDeck().addCardToDeck(new Card(3, 2));
        players.get(1).getPlayerDeck().addCardToDeck(new Card(7, 3));
        players.get(1).getPlayerDeck().addCardToDeck(new Card(10, 2));
        players.get(1).getPlayerDeck().addCardToDeck(new Card(13, 1));

        assertEquals(players.get(0), PlayersCompare.getWinner(players));
    }
}
