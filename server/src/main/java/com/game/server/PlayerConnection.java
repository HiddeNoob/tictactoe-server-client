package com.game.server;

import com.game.connection.WebSocketConnection;
import com.game.model.Player;
import com.game.proto.ServerMessage;
import java.io.IOException;

// Bir oyuncuyu ve onun WebSocket bağlantısını bir arada tutan yardımcı sınıf.
public class PlayerConnection {
    public final Player player;
    private final WebSocketConnection connection;

    public PlayerConnection(Player player, WebSocketConnection connection) {
        this.player = player;
        this.connection = connection;
    }

    // Bu oyuncuya mesaj gönderir.
    public void sendMessage(ServerMessage message) {
        try {
            if (!connection.isClosed()) {
                connection.sendBinaryMessage(message.toByteArray());
            }
        } catch (IOException e) {
            System.err.println("Failed to send message to " + player.username + ": " + e.getMessage());
        }
    }

    public String getPlayerId() {
        return player.id;
    }
}