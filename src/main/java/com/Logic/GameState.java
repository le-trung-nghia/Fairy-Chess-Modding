package com.Logic;

import java.util.ArrayList;

// The entire state of the game at one point in the match
public class GameState {
    // row 0-7, column 0-7
    private Piece[][] board;
    // captured pieces
    private ArrayList<Piece> captured;

    public GameState() {
        this.board = new Piece[8][8];
        this.captured = new ArrayList<>();
        loadStartingPosition();
    }

    // get the piece (or the lack of one) at a square on the board
    public Piece getSquare(int row, int col) {
        if (row >= 0 && row < 8 && col >= 0 && col < 8) {
            return board[row][col];
        }
        return null;
    }

    // displace a piece on the board to another location
    // TODO: implement actual logic
    public void displace(int rowSrc, int colSrc, int rowDst, int colDst) {
        if (rowSrc >= 0 && rowSrc < 8 && colSrc >= 0 && colSrc < 8 &&
            rowDst >= 0 && rowDst < 8 && colDst >= 0 && colDst < 8) {
            
            Piece piece = board[rowSrc][colSrc];
            if (piece != null) {
                // Move piece to destination
                board[rowDst][colDst] = piece;
                board[rowSrc][colSrc] = null;
                // Update the piece's internal position tracking
                piece.updatePosition(rowDst, colDst);
            }
        }
    }

    // move a piece on the board to another location
    // TODO: implement this
    public void move(int rowSrc, int colSrc, int rowDst, int colDst) {
        displace(rowSrc, colSrc, rowDst, colDst);
    }

    // capture a piece on the board
    // TODO: implement this
    public void capture(int row, int col) {
    }

    // change the color of a piece on the board
    // TODO: implement this
    public void changeColor(int row, int col) {
    }

    private void loadStartingPosition() {
        // Placing black pieces
        board[0][0] = new Rook(Piece.Color.BLACK, 0, 0);
        board[0][1] = new Knight(Piece.Color.BLACK, 0, 1);
        board[0][2] = new Bishop(Piece.Color.BLACK, 0, 2);
        board[0][3] = new Queen(Piece.Color.BLACK, 0, 3);
        board[0][4] = new King(Piece.Color.BLACK, 0, 4);
        board[0][5] = new Bishop(Piece.Color.BLACK, 0, 5);
        board[0][6] = new Knight(Piece.Color.BLACK, 0, 6);
        board[0][7] = new Rook(Piece.Color.BLACK, 0, 7);

        for (int col = 0; col < 8; col++) {
            board[1][col] = new Pawn(Piece.Color.BLACK, 1, col);
        }

        // Placing white pieces
        for (int col = 0; col < 8; col++) {
            board[6][col] = new Pawn(Piece.Color.WHITE, 6, col);
        }

        board[7][0] = new Rook(Piece.Color.WHITE, 7, 0);
        board[7][1] = new Knight(Piece.Color.WHITE, 7, 1);
        board[7][2] = new Bishop(Piece.Color.WHITE, 7, 2);
        board[7][3] = new Queen(Piece.Color.WHITE, 7, 3);
        board[7][4] = new King(Piece.Color.WHITE, 7, 4);
        board[7][5] = new Bishop(Piece.Color.WHITE, 7, 5);
        board[7][6] = new Knight(Piece.Color.WHITE, 7, 6);
        board[7][7] = new Rook(Piece.Color.WHITE, 7, 7);
    }
}
