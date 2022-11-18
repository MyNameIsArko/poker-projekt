package poker.server;

import poker.common.player.Player;
import poker.common.state.GameState;
import poker.model.game.Game;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static poker.model.compare.PlayersCompare.getWinner;

public class Server {

    private static final String PLAYER_STRING = "Player ";
    private static int playersAmount;
    private static int antAmount;

    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());

    private static ServerSocketChannel serverSocketChannel;
    private static Selector selector;

    private static boolean gameStarted = false;
    private static boolean gameCountdownStarted = false;


    public static void main(String[] args) throws IOException {
        if (isArgsWrong(args)) return;

        setupServer();

        Game game = new Game(antAmount);

        // Keep server running
        while (true) {
            try {
                if (game.getPlayers().size() == playersAmount && !gameStarted && !gameCountdownStarted) {
                    startGame(game);
                }
                if (manageServerAndHasGameEnded(game)) return;
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "There was unexpected error on server!");
                break;
            }
        }
        serverSocketChannel.close();
    }

    private static boolean manageServerAndHasGameEnded(Game game) throws IOException {
        // Select a set of keys whose corresponding channels are ready for I/O operations
        selector.select();

        Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();

        while (iterator.hasNext()) {
            SelectionKey key = iterator.next();

            if (!(gameStarted && isClientNotPlayer(game, iterator, key))) {
                int exitCode = handleKey(game, key);
                if (exitCode == 1) return true;
                else if (exitCode == 0) break;
            }

            iterator.remove();
        }
        return false;
    }

    private static int handleKey(Game game, SelectionKey key) throws IOException {
        // Check if key's channel is ready to accept new connection
        if (key.isAcceptable()) {
            handleKeyAccept(playersAmount, antAmount, selector, serverSocketChannel, game);
            // Check if key's channel is ready for reading
        } else if (key.isReadable()) {
            // READ client queue

            SocketChannel client = (SocketChannel) key.channel();

            ByteBuffer buffer = ByteBuffer.allocate(256);
            int read = client.read(buffer);
            if (read == -1) {
                // EOF. Remove player from map and close connection
                client.close();
            } else if (read > 0 && handleReadAndGameEnd(game, client, buffer)) return 1;
            if (gameStarted && handleNextMoveAndIfGameEnded(game)) return 0;
        }
        return -1;
    }

    private static boolean handleNextMoveAndIfGameEnded(Game game) throws IOException {
        game.nextPlayerAction();
        if (game.getCurrentState() == GameState.SHOWDOWN) {
            startShowdown(game);
            return true;
        }
        return false;
    }

    private static boolean handleReadAndGameEnd(Game game, SocketChannel client, ByteBuffer buffer) throws IOException {
        // Update position and limit in byteBuffer
        buffer.flip();
        String result = new String(buffer.array()).trim();
        LOGGER.log(Level.INFO, "Server - Message from: {0} - {1}", new Object[] {client.getRemoteAddress(), result});
        Player player = game.findPlayerFromClient(client);
        if (game.getCurrentState() == GameState.ANTE) {
            handleAnte(game, client, result);
        } else if (game.getCurrentState() == GameState.FIRST_BID || game.getCurrentState() == GameState.SECOND_BID) {
            return handleBidAndGameEnd(game, client, result, player);
        } else if (game.getCurrentState() == GameState.DISCARD_CARDS && !result.equals("0")) {
            handleDiscardState(game, result, player);
        }
        return false;
    }

    private static void handleAnte(Game game, SocketChannel client, String result) throws IOException {
        if (result.equals("NO")) {
            sendClientMessageAndClose("QUIT", client);
        } else {
            handleAnteState(playersAmount, game, client);
        }
    }

    private static boolean handleBidAndGameEnd(Game game, SocketChannel client, String result, Player player) throws IOException {
        switch (result) {
            case "FOLD":
                if (handlePlayerFoldAndWin(game, player)) return true;
                break;
            case "CHECK":
                sendOtherClientsMessage(game, client, PLAYER_STRING + player.getPlayerNum() + " has checked.");
                break;
            case "CALL":
                sendOtherClientsMessage(game, client, PLAYER_STRING + player.getPlayerNum() + " has called.");
                break;
            default:
                if (result.contains("RAISE")) {
                    raiseBid(game, client, result, player);
                }
                break;
        }
        return false;
    }

    private static boolean handlePlayerFoldAndWin(Game game, Player player) throws IOException {
        sendOtherClientsMessage(game, player.getClient(), PLAYER_STRING + player.getPlayerNum() + " has folded.");
        game.removePlayer(player);
        sendClientMessageAndClose("QUIT", player.getClient());
        if (game.getPlayers().size() < 2) {
            sendClientMessageAndClose("WIN", game.getPlayers().get(0).getClient());
            return true;
        }
        return false;
    }

    private static void sendClientMessageAndClose(String message, SocketChannel client) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());
        client.write(buffer);
        client.close();
    }

    private static void startGame(Game game) {
        LOGGER.log(Level.INFO, "Server - starting game");
        gameCountdownStarted = true;
        final int[] i = {3};
        Timer timer = new Timer();
        TimerTask countdownTask = new TimerTask() {
            @Override
            public void run() {
                for (Player player : game.getPlayers()) {
                    SocketChannel client = player.getClient();
                    ByteBuffer buffer = ByteBuffer.wrap(Integer.toString(i[0]).getBytes());
                    try {
                        client.write(buffer);
                    } catch (IOException ignored) {
                        LOGGER.log(Level.SEVERE, "Could not write to client!");
                    }
                }
                if (i[0] == 0) {
                    gameStarted = true;
                    timer.cancel();
                    try {
                        game.startGame();
                        game.givePlayersCards();
                        game.showInfoToPlayers();
                        game.nextPlayerAction();
                    } catch (IOException e) {
                        LOGGER.log(Level.SEVERE, "Cannot start the game!");
                    }
                }
                i[0] -= 1;
            }
        };
        timer.schedule(countdownTask, 0, 1000);
    }

    private static void setupServer() throws IOException {
        selector = Selector.open();

        serverSocketChannel = ServerSocketChannel.open();

        InetSocketAddress inetSocketAddress = new InetSocketAddress("localhost", 9999);

        serverSocketChannel.bind(inetSocketAddress);

        LOGGER.log(Level.INFO, "Starting server on port 9999");

        serverSocketChannel.configureBlocking(false);

        int ops = serverSocketChannel.validOps();

        serverSocketChannel.register(selector, ops, null);
    }

    private static boolean isArgsWrong(String[] args) {
        try {
            playersAmount = Integer.parseInt(args[0]);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Enter player amount!");
            return true;
        }
        try {
            antAmount = Integer.parseInt(args[1]);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Enter entrance cost!");
            return true;
        }
        return false;
    }

    private static void handleAnteState(int playersAmount, Game game, SocketChannel client) throws IOException {
        ByteBuffer buffer;
        if (game.getPlayers().size() < playersAmount) {
            Player newPlayer = new Player(client, game.getPlayers().size() + 1);
            game.addPlayer(newPlayer);
            buffer = ByteBuffer.wrap("Game will start soon...\n".getBytes());
            client.write(buffer);
        } else {
            buffer = ByteBuffer.wrap("MAX".getBytes());
            client.write(buffer);
        }
    }

    private static void handleKeyAccept(int playersAmount, int antAmount, Selector selector, ServerSocketChannel serverSocketChannel, Game game) throws IOException {
        SocketChannel client = serverSocketChannel.accept();
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        if (game.getPlayers().size() >= playersAmount) {
            ByteBuffer buffer = ByteBuffer.wrap("MAX".getBytes());
            client.write(buffer);
            client.close();
        } else if (gameStarted) {
            ByteBuffer buffer = ByteBuffer.wrap("QUIT".getBytes());
            client.write(buffer);
            client.close();
        } else {
            LOGGER.log(Level.INFO, "Server - Connection accepted: {0}", client.getRemoteAddress());
            ByteBuffer buffer = ByteBuffer.wrap(("Entrance cost equals " + antAmount + " tokens.\nACTION ANTE").getBytes());
            client.write(buffer);
        }
    }

    private static void sendOtherClientsMessage(Game game, SocketChannel client, String playerString) throws IOException {
        ByteBuffer buffer;
        for (Player otherPlayer : game.getPlayers()) {
            SocketChannel thisClient = otherPlayer.getClient();
            if (!thisClient.equals(client)) {
                buffer = ByteBuffer.wrap(playerString.getBytes());
                thisClient.write(buffer);
            }
        }
    }

    private static void raiseBid(Game game, SocketChannel client, String result, Player player) throws IOException {
        int amount = Integer.parseInt(result.replace("RAISE", "").trim());
        game.raiseBid(amount, player);
        sendOtherClientsMessage(game, client, PLAYER_STRING + player.getPlayerNum() + " has raised the bid to: " + amount + "!");
    }

    private static void handleDiscardState(Game game, String result, Player player) throws IOException {
        Set<String> cardsSet = new HashSet<>(Arrays.asList(result.split(" ")));
        String[] cardsToRemove = cardsSet.toArray(new String[0]);
        Arrays.sort(cardsToRemove);
        for (int i = cardsToRemove.length - 1; i >= 0; i--) {
            int index = Integer.parseInt(cardsToRemove[i]) - 1;
            player.getPlayerDeck().removeCardByIndex(index);
        }
        game.givePlayerCards(player);
        game.showInfoToPlayer(player);
    }

    private static void startShowdown(Game game) throws IOException {
        ByteBuffer buffer;
        Player bestPlayer = getWinner(game.getPlayers());
        for (Player player : game.getPlayers()) {
            if (!player.equals(bestPlayer)) {
                buffer = ByteBuffer.wrap("LOSE".getBytes());
            } else {
                buffer = ByteBuffer.wrap("WIN".getBytes());
            }
            player.getClient().write(buffer);
            player.getClient().close();
        }
        game.resetGame();
        gameStarted = false;
        gameCountdownStarted = false;
    }

    private static boolean isClientNotPlayer(Game game, Iterator<SelectionKey> iterator, SelectionKey key) {
        try {
            boolean found = false;
            for (Player player : game.getPlayers()) {
                if (player.getClient() == (SocketChannel) key.channel()) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                iterator.remove();
                return true;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Cannot get SocketChannel");
            return true;
        }
        return false;
    }
}
