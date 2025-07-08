package com.game.models;

public class Map {

    private final Game.TurnType[][] map;

    Map(){
        this.map = new Game.TurnType[3][3];
    }

    void setCell(Game.TurnType type, byte x, byte y){
        this.map[x][y] = type;
    }

    Game.TurnType getCell(byte x, byte y){
        return this.map[x][y];
    }
}
