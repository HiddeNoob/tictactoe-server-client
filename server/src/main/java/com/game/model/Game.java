package com.game.model;

import com.game.proto.GameState;
import com.game.proto.PlayerType;
import com.game.proto.Winner;
import java.util.Arrays;
import java.util.stream.Stream;

// game logic
public class Game {
    private final PlayerType[] board = new PlayerType[9];
    private PlayerType currentTurn = PlayerType.X;
    private Winner winner = Winner.WINNER_UNSPECIFIED;
    private boolean isStarted = false;

    public Game() {
        Arrays.fill(board, PlayerType.TYPE_UNSPECIFIED);
    }

    public void start() { this.isStarted = true; }
    public boolean isStarted() { return isStarted; }
    public Winner getWinner() { return winner; }

    public synchronized boolean makeMove(int position, PlayerType playerType) {
        if (!isStarted || winner != Winner.WINNER_UNSPECIFIED || playerType != currentTurn || position < 0 || position > 8 || board[position] != PlayerType.TYPE_UNSPECIFIED) {
            return false;
        }
        board[position] = playerType;
        currentTurn = (playerType == PlayerType.X) ? PlayerType.O : PlayerType.X;
        checkWinner();
        return true;
    }

    private void checkWinner() {
        int[][] winConditions = {{0,1,2}, {3,4,5}, {6,7,8}, {0,3,6}, {1,4,7}, {2,5,8}, {0,4,8}, {2,4,6}};
        for (int[] c : winConditions) {
            if (board[c[0]] != PlayerType.TYPE_UNSPECIFIED && board[c[0]] == board[c[1]] && board[c[1]] == board[c[2]]) {
                winner = (board[c[0]] == PlayerType.X) ? Winner.PLAYER_X : Winner.PLAYER_O;
                return;
            }
        }
        if (Stream.of(board).allMatch(cell -> cell != PlayerType.TYPE_UNSPECIFIED)) {
            winner = Winner.DRAW;
        }
    }

    public GameState toProto() {
        GameState.Builder builder = GameState.newBuilder()
                .setCurrentTurn(currentTurn)
                .setWinner(winner);
        for (PlayerType cell : board) {
            builder.addBoard(cell);
        }
        return builder.build();
    }
}