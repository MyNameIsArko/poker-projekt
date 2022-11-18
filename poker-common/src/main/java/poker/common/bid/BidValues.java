package poker.common.bid;

public class BidValues {

    BidValues() {
        throw new IllegalStateException("Utility class");
    }
    public static boolean isBidValue(String value, boolean canCheck) {
        if (value.contains("raise")) {
            return true;
        }
        if (canCheck) {
            if (value.equals("check")) {
                return true;
            }
        } else {
            if (value.equals("call")) {
                return true;
            }
        }
        return value.equals("fold");
    }
}
