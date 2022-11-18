package poker.common.state;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class GameStateTest {
    @Test
    public void testNext() {
        GameState currentState = GameState.NULL;
        assertEquals(GameState.ANTE, currentState.next());
        currentState = currentState.next();
        assertEquals(GameState.FIRST_BID, currentState.next());
        currentState = currentState.next();
        assertEquals(GameState.DISCARD_CARDS, currentState.next());
        currentState = currentState.next();
        assertEquals(GameState.SECOND_BID, currentState.next());
        currentState = currentState.next();
        assertEquals(GameState.SHOWDOWN, currentState.next());
    }
}
