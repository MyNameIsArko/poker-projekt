package poker.common.bid;

import org.junit.Test;

import static org.junit.Assert.*;

public class BidValuesTest {
    @Test
    public void testConstructor () {
        assertThrows(IllegalStateException.class, () -> { BidValues bidValues = new BidValues(); });
    }

    @Test
    public void testIsBidValue() {
        assertTrue(BidValues.isBidValue("raise", false));
        assertTrue(BidValues.isBidValue("check", true));
        assertFalse(BidValues.isBidValue("check", false));
        assertFalse(BidValues.isBidValue("call", true));
        assertTrue(BidValues.isBidValue("call", false));
        assertTrue(BidValues.isBidValue("fold", false));
        assertFalse(BidValues.isBidValue("foldeeee", false));
    }
}
