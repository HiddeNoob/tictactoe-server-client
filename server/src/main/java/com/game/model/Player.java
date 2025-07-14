package com.game.model;

import com.game.proto.PlayerType;

public class Player {
    public final String id;
    public final String username;

    public Player(String id, String username) {
        this.id = id;
        this.username = username;
    }

    // protobuf ile serialize
    public com.game.proto.Player toProto(PlayerType type) {
        return com.game.proto.Player.newBuilder()
                .setId(this.id)
                .setUsername(this.username)
                .setType(type)
                .build();
    }
}