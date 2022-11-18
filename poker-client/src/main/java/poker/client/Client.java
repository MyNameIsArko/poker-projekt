package poker.client;

import poker.common.state.GameState;
import poker.common.terminal.Terminal;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static poker.common.bid.BidValues.isBidValue;

public class Client {

    private static String messagePart = "";
    private static final String CHECK = "CHECK";
    private static final String FIRST_BID = "FIRST_BID";
    private static final Logger LOGGER = Logger.getLogger(Client.class.getName());

    private static boolean canCheck = false;
    private static boolean clientTurn = false;

    private static int currentBid = 0;

    private static GameState currentState = GameState.NULL;

    public static void main(String[] args) throws IOException {
        InetSocketAddress inetSocketAddress = new InetSocketAddress("localhost", 9999);

        try (SocketChannel client = SocketChannel.open(inetSocketAddress)) {
            Scanner clientInput = new Scanner(System.in);

            // Keep client connected until it doesn't send quit.
            while (true) {
                if (clientTurn) {
                    clientTurn = false;
                    handleClientTurn(client, clientInput);
                } else {
                    if (handleClientRead(client) == -1) return;
                }
            }
        }
    }

    private static int handleClientRead(SocketChannel client) throws IOException {
        String result = getResultFromRead(client);
        if (result == null) return -1;
        if (result.contains("bid to")) {
            getBidValue(result);
        } else if (result.contains("ACTION")) {
            handleAction(result);
        } else {
            String message;
            message = getMessageFromServer(result);
            if (!message.equals("")) {
                LOGGER.log(Level.INFO, message);
                return -1;
            }
        }
        result = cleanResult(result);
        if (result.equals("")) {
            return 0;
        }
        if (result.contains("|")) {
            Terminal.clearConsole();
        }
        LOGGER.log(Level.INFO, result);
        return 1;
    }
    private static String getResultFromRead(SocketChannel client) throws IOException {
        ByteBuffer readBuffer = ByteBuffer.allocate(256);
        int read = client.read(readBuffer);
        if (read == -1) {
            LOGGER.log(Level.INFO, "Connection to server was broken!");
            return null;
        }
        readBuffer.flip();
        return new String(readBuffer.array()).trim();
    }

    private static String getMessageFromServer(String result) {
        String message;
        switch (result) {
            case "CLOSE" -> message = "Game has started already!";
            case "MAX" -> message = "Game has max players!";
            case "QUIT" -> message = "You do not participate in this game.";
            case "WIN" -> message = "You win!";
            case "LOSE" -> message = "You lose!";
            default -> message = "";
        }
        return message;
    }

    private static void handleAction(String result) {
        if (result.contains("ANTE")) {
            getAnteBid(result);
            currentState = GameState.ANTE;
        } else if (result.contains(FIRST_BID) || result.contains("SECOND_BID")) {
            currentState = result.contains(FIRST_BID) ? GameState.FIRST_BID : GameState.SECOND_BID;
            if (result.contains(CHECK)) {
                canCheck = true;
            }
        } else if (result.contains("DISCARD")) {
            currentState = GameState.DISCARD_CARDS;
        } else if (result.contains("SHOWDOWN")) {
            currentState = GameState.SHOWDOWN;
        }
        clientTurn = true;
    }

    private static void getBidValue(String result) {
        String[] resultArray = result.split(" ");
        String stringValue = resultArray[resultArray.length - 1].replace("!", "");
        currentBid = Integer.parseInt(stringValue);
    }

    private static void getAnteBid(String result) {
        String[] resultArray = result.split(" ");
        currentBid = Integer.parseInt(resultArray[3]);
    }

    private static void handleClientTurn(SocketChannel client, Scanner clientInput) throws IOException {
        messagePart = (canCheck ? "check/" : "call/");
        logFromState(currentState);
        String query;
        do {
            query = clientInput.nextLine();
        } while (query.equals("") || ((currentState == GameState.ANTE && isNotAnteAnswer(query)) ||
                ((currentState == GameState.FIRST_BID || currentState == GameState.SECOND_BID) && isNotBidAnswer(query, canCheck, currentBid)) ||
                (currentState == GameState.DISCARD_CARDS && isNotDiscardAnswer(query))));
        canCheck = false;

        byte[] message = query.toUpperCase().getBytes();
        ByteBuffer buffer = ByteBuffer.wrap(message);
        client.write(buffer);

        buffer.clear();
    }

    private static void logFromState(GameState currentState) {
        switch (currentState) {
            case ANTE -> LOGGER.log(Level.INFO, "Do you want to enter this game?\nAnswer yes/no");
            case FIRST_BID ->
                    LOGGER.log(Level.INFO, "First bid. What you want to do?\nfold/{0}raise [amount]", messagePart);
            case DISCARD_CARDS -> LOGGER.log(Level.INFO, "Select cards you discard (1-5). Write them space separated.\nIf you do not want to remove any card write 0");
            case SECOND_BID -> LOGGER.log(Level.INFO, "Second bid. What you want to do?\nfold/{0}raise [amount]", messagePart);
            case SHOWDOWN -> LOGGER.log(Level.INFO, "It is showdown!");
            default -> LOGGER.log(Level.INFO, "Unknown case!");
        }
    }

    private static boolean isNotBidAnswer(String query, boolean canCheck, int currentBid) {
        String answer = query.toLowerCase();
        if (!isBidValue(answer, canCheck)) {
            LOGGER.log(Level.WARNING, "Answer using fold/{0}raise [amount]!", messagePart);
            return true;
        } else if (answer.contains("raise")) {
            try {
                if (Integer.parseInt(query.replace("raise", "").trim()) <= 0) {
                    LOGGER.log(Level.WARNING, "You cannot raise with negative or zero amount!");
                    return true;
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "That is not a valid number!");
                return true;
            }
        }
        return false;
    }

    private static boolean isNotDiscardAnswer(String query) {
        String[] numbers = query.split(" ");
        boolean allGood;
        Set<Integer> cards = new HashSet<>();
        if (numbers.length > 1) {
            allGood = getCardsSet(numbers, cards);
        } else {
            allGood = checkSingleNumber(numbers);
        }
        if (cards.size() > 4) {
            LOGGER.log(Level.WARNING, "Nie możesz odrzucić wszystkich kart!");
            return true;
        }
        if (!allGood) {
            LOGGER.log(Level.WARNING, "Podaj liczby z przedziału 1-5 lub 0!");
            return true;
        }
        return false;
    }

    private static boolean checkSingleNumber(String[] numbers) {
        try {
            int n = Integer.parseInt(numbers[0]);
            if (n < 0 || n > 5) {
                return false;
            }
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    private static boolean getCardsSet(String[] numbers, Set<Integer> cards) {
        for (String num : numbers) {
            try {
                int n = Integer.parseInt(num);
                if (n < 1 || n > 5) {
                    return false;
                }
                cards.add(n);
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }

    private static boolean isNotAnteAnswer(String query) {
        String answer = query.toLowerCase();
        if (!answer.equals("yes") && !answer.equals("no")) {
            LOGGER.log(Level.WARNING, "Answer yes or no!");
            return true;
        }
        return false;
    }

    private static String cleanResult(String result) {
        return result.replace("ACTION", "")
                .replace("DISCARD", "")
                .replace("ANTE", "")
                .replace(FIRST_BID, "")
                .replace("SECOND_BID", "")
                .replace("SHOWDOWN", "")
                .replace(CHECK, "").trim();
    }
}