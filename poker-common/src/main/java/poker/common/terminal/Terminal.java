package poker.common.terminal;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Terminal {
    private static final Logger logger = Logger.getLogger(Terminal.class.getName());
    Terminal() {
        throw new IllegalStateException("Class utility");
    }
    public static void clearConsole() {
        logger.log(Level.INFO, "\033\143");
    }
}
