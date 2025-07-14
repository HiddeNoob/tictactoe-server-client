package com.game.service;

import com.game.model.Lobby;
import com.game.model.Player;
import com.game.proto.ClientMessage;
import com.game.proto.JoinLobbyRequest;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

// Lobi oluşturma, katılma ve oyuncu yönetimi gibi iş mantığını yönetir.
public class LobbyService {
    private final Map<String, Lobby> lobbies = new ConcurrentHashMap<>();
    private final NotificationService notificationService;
    private final GameService gameService;

    public LobbyService(NotificationService notificationService) {
        this.notificationService = notificationService;
        this.gameService = new GameService(this, notificationService);
    }

    public void handleNewPlayer(Player player) {
        notificationService.notifyPlayerId(player); //oyuncuya kendi idsini gönder
        sendLobbyListToPlayer(player); //sadece yeni oyuncuya lobi listesini gönder
    }

    public void processRequest(Player player, ClientMessage msg) {
        switch (msg.getPayloadCase()) {
            case CREATE_LOBBY: createLobby(player); break;
            case JOIN_LOBBY: joinLobby(player, msg.getJoinLobby()); break;
            case MAKE_MOVE: gameService.makeMove(player, msg.getMakeMove()); break;
            default: break;
        }
    }

    public Lobby findLobbyByPlayer(String playerId) {
        return lobbies.values().stream()
                .filter(l -> l.getPlayerType(playerId) != com.game.proto.PlayerType.TYPE_UNSPECIFIED)
                .findFirst().orElse(null);
    }

    private void createLobby(Player host) {
        if (findLobbyByPlayer(host.id) != null) {
            notificationService.notifyError(host, "Zaten bir lobidesiniz.");
            return;
        }
        Lobby lobby = new Lobby(host);
        lobbies.put(lobby.id, lobby);
        broadcastLobbyList();
        notificationService.notifyLobby(lobby); // lobiye giren hosta oyun ekranını goster
    }

    private void joinLobby(Player client, JoinLobbyRequest req) {
        Lobby lobby = lobbies.get(req.getLobbyId());
        if (lobby != null && lobby.addPlayer(client)) {
            broadcastLobbyList();
            notificationService.notifyLobby(lobby); //lobiye katılan herkese güncel durumu gönder
        } else {
            notificationService.notifyError(client, "Lobi dolu veya bulunamadı.");
        }
    }

    public void handleDisconnect(Player player) {
        System.out.println("handleDisconnect called for player: " + player.username + " (ID: " + player.id + ")");
        Lobby lobby = findLobbyByPlayer(player.id);
        if (lobby != null) {
            System.out.println("Found lobby " + lobby.id + " for disconnecting player");
            lobby.removePlayer(player.id);
            if (lobby.isEmpty()) {
                System.out.println("Lobby is empty, removing lobby " + lobby.id);
                lobbies.remove(lobby.id);
            } else {
                lobby.resetGame();
            }
            broadcastLobbyList();
        } else {
            System.out.println("No lobby found for player " + player.username);
        }
    }

    private void broadcastLobbyList() {
        // Sadece dolu olmayan lobileri listele
        var openLobbies = lobbies.values().stream()
                .filter(l -> !l.isFull())
                .collect(Collectors.toList());
        
        // Oyunda olan oyuncuları hariç tut
        var playersInGame = lobbies.values().stream()// TODO daha verimli yapılabilir ?
                .filter(l -> l.isFull()) // Sadece dolu (aktif) lobilerdeki oyuncular
                .flatMap(l -> l.getPlayers().values().stream())
                .map(p -> p.id)
                .collect(Collectors.toList());
        
        notificationService.broadcastLobbyListExcept(openLobbies, playersInGame);
    }

    private void sendLobbyListToPlayer(Player player) {
        // Sadece dolu olmayan lobileri listele
        var openLobbies = lobbies.values().stream()
                .filter(l -> !l.isFull())
                .collect(Collectors.toList());
        notificationService.sendLobbyListToPlayer(player, openLobbies);
    }
}