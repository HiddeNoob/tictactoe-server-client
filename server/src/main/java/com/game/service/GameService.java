package com.game.service;

import com.game.model.Lobby;
import com.game.model.Player;
import com.game.proto.MakeMoveRequest;
import com.game.proto.PlayerType;
import com.game.proto.Winner;

// Oyun içi hamle mantığını yönetir.
public class GameService {
    private final LobbyService lobbyService;
    private final NotificationService notificationService;

    public GameService(LobbyService lobbyService, NotificationService notificationService) {
        this.lobbyService = lobbyService;
        this.notificationService = notificationService;
    }

    public void makeMove(Player player, MakeMoveRequest req) {
        Lobby lobby = lobbyService.findLobbyByPlayer(player.id);
        if (lobby == null || lobby.getGame() == null) return;

        PlayerType playerType = lobby.getPlayerType(player.id);
        if (lobby.getGame().makeMove(req.getPosition(), playerType)) {
            notificationService.notifyLobby(lobby);

            if(lobby.getGame().getWinner() != Winner.WINNER_UNSPECIFIED) {
                System.out.println("Game over in lobby " + lobby.id + ". Winner: " + lobby.getGame().getWinner());
            }
        }
    }
}