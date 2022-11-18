package poker.common.state;

public enum GameState {
    NULL,
    ANTE,
    FIRST_BID,
    DISCARD_CARDS,
    SECOND_BID,
    SHOWDOWN;

    private static final GameState[] states = values();

    public GameState next() {
        return states[(this.ordinal() + 1) % states.length];
    }
}
