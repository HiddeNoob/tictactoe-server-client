package com.game.main;

import com.game.proto.*;
import com.game.connection.*;
import com.game.models.*;
import com.google.protobuf.InvalidProtocolBufferException;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {

    private final int port;
    private final Map<String, Player> players = new ConcurrentHashMap<>();
    private final Map<String, Lobby> lobbies = new ConcurrentHashMap<>();

    public Server(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Sunucu " + port + " portunda başlatıldı!");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> handleClient(clientSocket)).start();
            }
        }
    }

    private void handleClient(Socket clientSocket) {
        WebSocketConnection conn = null;
        try {
            
            // tcp handshake
            conn = WebSocketConnection.establish(clientSocket);
            
            String connId = conn.getConnectionId();

            Player newPlayer = new Player(conn);
            players.put(connId, newPlayer);
            System.out.println("Yeni oyuncu sunucuya bağlandı: " + newPlayer.id);

            while (!conn.isClosed()) {
                byte[] message = conn.readBinaryMessage();
                if (message == null) {
                    break;
                }
                
                Player player = players.get(connId);
                if (player != null) {
                    PlayerInput input = PlayerInput.parseFrom(message);

                }
            }

        } catch (InvalidProtocolBufferException e) {
            System.err.println("Geçersiz protobuf mesajı: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Bir istemciyle bağlantı kesildi: " + e.getMessage());
        } finally {
            if (conn != null) {
                String connId = conn.getConnectionId();
                handleDisconnect(connId);
                try {
                    conn.close();
                } catch (IOException ioException) {
                    // ignore
                }
            }
        }
    }

    void handleDisconnect(String id){
        this.lobbies.remove(id);
        this.players.remove(id);
    }

    public static void main(String[] args) throws IOException {
        Server server = new Server(8080);
        server.start();
    }
}