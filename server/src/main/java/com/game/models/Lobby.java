package com.game.models;

public class Lobby {
   private String id;
   Player host;
   Player enemy;
   Game game;

   private Lobby(Player host,Player enemy,String id){
        this.host = host;
        this.enemy = enemy;
        this.setId(id);
   }

   public Game startGame(Player host) throws Exception{
       if(host != this.host){
           throw new Exception("Unauthorized player");
       }
       this.game = new Game();
       return this.game;
   }

   private void setId(String id){
        this.id = id;
   }

   public String getId() {
       return id;
   }

   public Lobby createLobby(Player p1){
       return new Lobby(p1, null,p1.id);
   }


}
