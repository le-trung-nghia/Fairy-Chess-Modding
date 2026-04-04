package com.Logic;

public class Pieces {
    // row 0-7, column 0-7
    private Piece[][] board;

    public Pieces() {
        // Initialize an empty 8x8 grid
        this.board = new Piece[8][8];
        loadStartingPosition();
    }

    private void loadStartingPosition() {
        // Placing black pieces
        String b = "Black";
        board[0][0] = new Piece("Rook",   b, 0, 0);
        board[0][1] = new Piece("Knight", b, 0, 1);
        board[0][2] = new Piece("Bishop", b, 0, 2);
        board[0][3] = new Piece("Queen",  b, 0, 3);
        board[0][4] = new Piece("King",   b, 0, 4);
        board[0][5] = new Piece("Bishop", b, 0, 5);
        board[0][6] = new Piece("Knight", b, 0, 6);
        board[0][7] = new Piece("Rook",   b, 0, 7);

        for (int col = 0; col < 8; col++) {
            board[1][col] = new Piece("Pawn", b, 1, col);
        }

        // Placing white pieces
        String w = "White";
        for (int col = 0; col < 8; col++) {
            board[6][col] = new Piece("Pawn", w, 6, col);
        }

        board[7][0] = new Piece("Rook",   w, 7, 0);
        board[7][1] = new Piece("Knight", w, 7, 1);
        board[7][2] = new Piece("Bishop", w, 7, 2);
        board[7][3] = new Piece("Queen",  w, 7, 3);
        board[7][4] = new Piece("King",   w, 7, 4);
        board[7][5] = new Piece("Bishop", w, 7, 5);
        board[7][6] = new Piece("Knight", w, 7, 6);
        board[7][7] = new Piece("Rook",   w, 7, 7);
    }
    
    public Piece getPiece(int row, int col) {
        if (row >= 0 && row < 8 && col >= 0 && col < 8) {
            return board[row][col];
        }
        return null;
    }
}
