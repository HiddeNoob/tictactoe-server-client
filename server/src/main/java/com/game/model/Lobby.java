package com.game.model;

import com.game.proto.LobbyState;
import com.game.proto.PlayerType;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Lobby {
    public final String id;
    private final Map<PlayerType, Player> players = new ConcurrentHashMap<>();
    private Game game = new Game();

    public Lobby(Player host) {
        this.id = UUID.randomUUID().toString().substring(0, 8);
        players.put(PlayerType.X, host);
    }

    public boolean addPlayer(Player client) {
        if (isFull()) return false;
        players.put(PlayerType.O, client);
        game.start();
        return true;
    }

    public void resetGame(){
        this.game = new Game();
    }

    public void removePlayer(String playerId) {
        players.values().removeIf(p -> p.id.equals(playerId));
    }

    public boolean isFull() { return players.size() == 2; }
    public boolean isEmpty() { return players.isEmpty(); }
    public Map<PlayerType, Player> getPlayers() { return players; }
    public Game getGame() { return game; }

    public PlayerType getPlayerType(String playerId) {
        for (Map.Entry<PlayerType, Player> entry : players.entrySet()) {
            if (entry.getValue().id.equals(playerId)) return entry.getKey();
        }
        return PlayerType.TYPE_UNSPECIFIED;
    }

    public LobbyState toProto() {
        LobbyState.Builder builder = LobbyState.newBuilder().setId(id).setGameState(game.toProto());
        players.forEach((type, player) -> builder.addPlayers(player.toProto(type)));
        return builder.build();
    }
}