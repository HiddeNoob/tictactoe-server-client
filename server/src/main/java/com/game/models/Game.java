package com.game.models;

import java.util.UUID;

public class Game {

    Map map;

    enum TurnType{
        X,
        O
    }


    String gameId;
    TurnType currentTurn = TurnType.X;

    Game(){
        this.gameId = UUID.randomUUID().toString();
        this.map = new Map();
    }

    public void makeMove(Player p,byte x,byte y) throws Exception{
        if(isInBounds(x,y)) throw new  Exception("Invalid move");
        if(p.playerType != currentTurn) throw new Exception("Not Your Turn");
        if(map.getCell(x,y) == null){
            map.setCell(p.playerType, x, y);
        }else{
            throw new Exception("This cell is already filled");
        }
    }

    private boolean isInBounds(byte x,byte y){
        return x >= 0 && x < 3 &&  y >= 0 && y < 3;
    }

}
