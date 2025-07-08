package com.game.models;

import com.game.connection.WebSocketConnection;

public class Player {
    public final String id;
    private Lobby currentLobby;
    private WebSocketConnection connection;
    Game.TurnType playerType;


    public Player(WebSocketConnection connection) {
        this.id = connection.getConnectionId();
    }

}