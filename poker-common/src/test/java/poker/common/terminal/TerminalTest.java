package poker.common.terminal;

import org.junit.Test;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;

public class TerminalTest {
    @Test
    public void testConstructor() {
        assertThrows(IllegalStateException.class, () -> { Terminal terminal = new Terminal(); });
    }

    @Test
    public void testClearConsole() {
        try {
            Terminal.clearConsole();
        } catch (Exception e) {
            fail();
        }
    }
}
