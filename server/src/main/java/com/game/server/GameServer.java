package com.game.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class GameServer {
    private final int port;
    private final ConnectionHandler connectionHandler = new ConnectionHandler();

    public GameServer(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("XOX Game Server started on port: " + port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                connectionHandler.handleNewConnection(clientSocket);
            }
        }
    }

    public static void main(String[] args) {
        try {
            new GameServer(8080).start();
        } catch (IOException e) {
            System.err.println("Server failed to start: " + e.getMessage());
        }
    }
}