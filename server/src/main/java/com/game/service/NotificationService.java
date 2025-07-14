package com.game.service;

import com.game.model.Lobby;
import com.game.model.Player;
import com.game.proto.ErrorNotification;
import com.game.proto.GameStateUpdate;
import com.game.proto.LobbyListUpdate;
import com.game.proto.PlayerIdResponse;
import com.game.proto.ServerMessage;
import com.game.server.PlayerConnection;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

// Oyunculara bildirim göndermekten sorumlu servis.
public class NotificationService {
    private final Map<String, PlayerConnection> connections;

    public NotificationService(Map<String, PlayerConnection> connections) {
        this.connections = connections;
    }

    // Tek bir oyuncuya hata mesajı gönderir.
    public void notifyError(Player player, String errorMessage) {
        ErrorNotification error = ErrorNotification.newBuilder().setMessage(errorMessage).build();
        ServerMessage msg = ServerMessage.newBuilder().setError(error).build();
        sendMessage(player.id, msg);
    }

    // Oyuncuya kendi ID'sini gönderir.
    public void notifyPlayerId(Player player) {
        System.out.println("Sending player ID to " + player.username + ": " + player.id);
        PlayerIdResponse playerIdResponse = PlayerIdResponse.newBuilder().setPlayerId(player.id).build();
        ServerMessage msg = ServerMessage.newBuilder().setPlayerId(playerIdResponse).build();
        sendMessage(player.id, msg);
    }

    // Belirli bir lobiye oyun durumu güncellemesi gönderir.
    public void notifyLobby(Lobby lobby) {
        GameStateUpdate update = GameStateUpdate.newBuilder().setLobby(lobby.toProto()).build();
        ServerMessage msg = ServerMessage.newBuilder().setGameState(update).build();
        lobby.getPlayers().values().forEach(p -> sendMessage(p.id, msg));
    }

    // Tüm online oyunculara lobi listesini yayınlar.
    public void broadcastLobbyList(Collection<Lobby> lobbyList) {
        LobbyListUpdate update = LobbyListUpdate.newBuilder()
                .addAllLobbies(lobbyList.stream().map(Lobby::toProto).collect(Collectors.toList()))
                .build();
        ServerMessage msg = ServerMessage.newBuilder().setLobbyList(update).build();
        connections.values().forEach(conn -> conn.sendMessage(msg));
    }

    // Belirli oyuncular hariç tüm oyunculara lobi listesini yayınlar.
    public void broadcastLobbyListExcept(Collection<Lobby> lobbyList, Collection<String> excludePlayerIds) {
        System.out.println("Broadcasting lobby list except players: " + excludePlayerIds);
        System.out.println("Total connected players: " + connections.size());
        
        LobbyListUpdate update = LobbyListUpdate.newBuilder()
                .addAllLobbies(lobbyList.stream().map(Lobby::toProto).collect(Collectors.toList()))
                .build();
        ServerMessage msg = ServerMessage.newBuilder().setLobbyList(update).build();
        
        connections.values().forEach(conn -> {
            if (!excludePlayerIds.contains(conn.player.id)) {
                System.out.println("Sending lobby list to: " + conn.player.username + " (ID: " + conn.player.id + ")");
                conn.sendMessage(msg);
            } else {
                System.out.println("Skipping player in game: " + conn.player.username + " (ID: " + conn.player.id + ")");
            }
        });
    }

    // Tek bir oyuncuya lobi listesini gönderir.
    public void sendLobbyListToPlayer(Player player, Collection<Lobby> lobbyList) {
        LobbyListUpdate update = LobbyListUpdate.newBuilder()
                .addAllLobbies(lobbyList.stream().map(Lobby::toProto).collect(Collectors.toList()))
                .build();
        ServerMessage msg = ServerMessage.newBuilder().setLobbyList(update).build();
        sendMessage(player.id, msg);
    }

    private void sendMessage(String playerId, ServerMessage message) {
        PlayerConnection conn = connections.get(playerId);
        if (conn != null) {
            conn.sendMessage(message);
        }
    }
}