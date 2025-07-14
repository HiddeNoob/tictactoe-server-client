package com.game.server;

import com.game.connection.WebSocketConnection;
import com.game.model.Player;
import com.game.proto.ClientMessage;
import com.game.service.LobbyService;
import com.game.service.NotificationService;
import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// Oyuncu bağlantılarını ve yaşam döngülerini yönetir.
public class ConnectionHandler {
    private static final Map<String, PlayerConnection> connections = new ConcurrentHashMap<>();
    private final LobbyService lobbyService;

    public ConnectionHandler() {
        NotificationService notificationService = new NotificationService(connections);
        this.lobbyService = new LobbyService(notificationService);
    }

    // her bir bağlantı için yeni bir thred oluştur
    public void handleNewConnection(Socket socket) {
        new Thread(() -> {
            PlayerConnection playerConn = null;
            try {
                WebSocketConnection ws = WebSocketConnection.establish(socket);

                byte[] initialData = ws.readBinaryMessage();
                ClientMessage initialMsg = ClientMessage.parseFrom(initialData);

                Player player = new Player(ws.getConnectionId(), initialMsg.getInitialConnection().getUsername());
                playerConn = new PlayerConnection(player, ws);
                connections.put(player.id, playerConn);

                System.out.println("Player connected: " + player.username + " (ID: " + player.id + ") [" + connections.size() + " online]");
                lobbyService.handleNewPlayer(player);

                while (!ws.isClosed()) {
                    byte[] data = ws.readBinaryMessage();
                    if (data == null) break;

                    ClientMessage clientMsg = ClientMessage.parseFrom(data);
                    lobbyService.processRequest(player, clientMsg);
                }
            } catch (IOException e) {
                // Bağlantı kopması normal bir durum.
            } catch (Exception e) {
                System.err.println("Unexpected error in connection thread: " + e.getMessage());
            } finally {
                if (playerConn != null) {
                    System.out.println("Player disconnecting: " + playerConn.player.username + " (ID: " + playerConn.player.id + ")");
                    connections.remove(playerConn.getPlayerId());
                    lobbyService.handleDisconnect(playerConn.player);
                    System.out.println("Player disconnected: " + playerConn.player.username + " [" + connections.size() + " online]");
                }
            }
        }).start();
    }
}