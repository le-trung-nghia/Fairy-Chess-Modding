package com.Logic;

import com.Logic.pieces.Pawn;
import com.Logic.pieces.Rook;
import com.Logic.pieces.Knight;
import com.Logic.pieces.Bishop;
import com.Logic.pieces.Queen;
import com.Logic.pieces.King;
import java.util.ArrayList;

// The entire state of the game at one point in the match
public class GameState {
    // row 0-7, column 0-7
    private BoardPiece[][] board;
    private int turnNumber = 1;
    private Color turnPlayer = Color.WHITE;
    // captured pieces
    private ArrayList<BoardPiece> captured;

    // get the piece (or the lack of one) at a square on the board
    public BoardPiece getSquare(Position pos) {
        return board[pos.row()][pos.col()];
    }

    private void setSquare(Position pos, BoardPiece piece) {
        board[pos.row()][pos.col()] = piece;
    }

    private void swapSquares(Position src, Position dst) {
        BoardPiece dstPiece = getSquare(dst);
        setSquare(dst, getSquare(src));
        setSquare(src, dstPiece);
    }

    // get the turn number
    public int getTurnNumber() {
        return turnNumber;
    }

    // displace a piece on the board to another location
    public void displace(Position src, Position dst) {
        if (getSquare(src) == null) {
            throw new IllegalStateException(
                    "Cannot displace piece at (%d, %d) because there is no piece there."
                            .formatted(src.row(), src.col()));
        }
        if (getSquare(dst) != null) {
            throw new IllegalStateException(
                    "Piece at (%d, %d) cannot be displaced to (%d, %d) because it already contains another piece."
                            .formatted(src.row(), src.col(), dst.row(), dst.col()));
        }
        swapSquares(src, dst);
        getSquare(dst).displace(this, dst);
    }

    public void displace(Position src, Vector displacement) {
        displace(src, src.add(displacement));
    }

    // move a piece on the board to another location
    public void move(Position src, Position dst) {
        if (getSquare(src) == null) {
            throw new IllegalStateException(
                    "Cannot move piece at (%d, %d) because there is no piece there."
                            .formatted(src.row(), src.col()));
        }
        if (getSquare(dst) != null) {
            throw new IllegalStateException(
                    "Piece at (%d, %d) cannot be moved to (%d, %d) because it already contains another piece."
                            .formatted(src.row(), src.col(), dst.row(), dst.col()));
        }
        swapSquares(src, dst);
        getSquare(dst).move(dst);
    }

    public void move(Position src, Vector displacement) {
        move(src, src.add(displacement));
    }

    public void capture(Position pos) {
        BoardPiece piece = getSquare(pos);
        setSquare(pos, null);
        piece.capture(this);
    }

    public boolean hasEnemy(Position pos, Color color) {
        BoardPiece piece = getSquare(pos);
        return piece != null && piece.color() != color;
    }

    public void passControl() {
        turnPlayer = turnPlayer.oppositeColor();
    }


    // Add constructor to initialize game state
    public GameState() {
        this.board = new BoardPiece[8][8];
        this.captured = new ArrayList<>();

        // For loading the initial position
        // Black
        for (int col = 0; col < 8; col++) {
            board[1][col] = new BoardPiece(false, Color.BLACK, new Pawn(Color.BLACK, new Position(1, col)));
        }

        board[0][0] = new BoardPiece(false, Color.BLACK, new Rook(Color.BLACK, new Position(0, 0)));
        board[0][1] = new BoardPiece(false, Color.BLACK, new Knight(Color.BLACK, new Position(0, 1)));
        board[0][2] = new BoardPiece(false, Color.BLACK, new Bishop(Color.BLACK, new Position(0, 2)));
        board[0][3] = new BoardPiece(false, Color.BLACK, new Queen(Color.BLACK, new Position(0, 3)));
        board[0][4] = new BoardPiece(true, Color.BLACK, new King(Color.BLACK, new Position(0, 4)));
        board[0][5] = new BoardPiece(false, Color.BLACK, new Bishop(Color.BLACK, new Position(0, 5)));
        board[0][6] = new BoardPiece(false, Color.BLACK, new Knight(Color.BLACK, new Position(0, 6)));
        board[0][7] = new BoardPiece(false, Color.BLACK, new Rook(Color.BLACK, new Position(0, 7)));

        // White pieces
        for (int col = 0; col < 8; col++) {
            board[6][col] = new BoardPiece(false, Color.WHITE, new Pawn(Color.WHITE, new Position(6, col)));
        }

        board[7][0] = new BoardPiece(false, Color.WHITE, new Rook(Color.WHITE, new Position(7, 0)));
        board[7][1] = new BoardPiece(false, Color.WHITE, new Knight(Color.WHITE, new Position(7, 1)));
        board[7][2] = new BoardPiece(false, Color.WHITE, new Bishop(Color.WHITE, new Position(7, 2)));
        board[7][3] = new BoardPiece(false, Color.WHITE, new Queen(Color.WHITE, new Position(7, 3)));
        board[7][4] = new BoardPiece(true, Color.WHITE, new King(Color.WHITE, new Position(7, 4)));
        board[7][5] = new BoardPiece(false, Color.WHITE, new Bishop(Color.WHITE, new Position(7, 5)));
        board[7][6] = new BoardPiece(false, Color.WHITE, new Knight(Color.WHITE, new Position(7, 6)));
        board[7][7] = new BoardPiece(false, Color.WHITE, new Rook(Color.WHITE, new Position(7, 7)));
    }
}