package poker.common.player;

import org.junit.Test;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import static org.junit.Assert.*;

public class PlayerTest {
    @Test
    public void testConstructor() throws IOException {
        SocketChannel client = SocketChannel.open();
        int num = 2;
        Player player = new Player(client, num);
        assertEquals(num, player.getPlayerNum());
        assertEquals(client, player.getClient());
        assertNotNull(player.getPlayerDeck());
    }
}
