package com.chess.logic.state;

import java.io.InputStream;
import java.util.Objects;

import com.chess.logic.types.Color;
import com.chess.logic.types.Piece;
import com.chess.logic.types.Position;

// A piece on the board along with some state
public class BoardPiece {
    private PieceState state;
    private Piece piece;

    BoardPiece(PieceState state, Piece piece) {
        this.piece = Objects.requireNonNull(piece);
        this.state = Objects.requireNonNull(state);
    }

    void move(Position pos) {
        this.state.setPosition(pos);
    }

    void commandMove(GameState state, Position pos) {
        this.piece.onMoveCommand(state, this, pos);
    }

    void displace(GameState state, Position pos) {
        this.state.setPosition(pos);
        piece.onDisplace(state, this);
    }

    void capture(GameState state) {
        piece.onCapture(state, this);
    }

    void changeColor(GameState state, Color color) {
        this.state.setColor(color);
        piece.onColorChange(state, this);
    }

    void changeKingStatus(GameState state, boolean isKing) {
        this.state.setKingStatus(isKing);
        piece.onKingStatusChange(state, this);
    }

    public boolean isKing() {
        return this.state.isKing();
    }

    public Color color() {
        return this.state.color();
    }

    public Position position() {
        return this.state.position();
    }

    // Expose the underlying piece (e.g. for instanceof checks in the GUI)
    public Piece piece() { return piece; }

    // Expose the method icon
    public String icon() {
        return piece.icon(this);
    }

    // Load the icon using the piece's own classloader so plugin JARs find their own
    // resources
    public final InputStream iconStream() {
        return piece.getClass().getClassLoader().getResourceAsStream(icon());
    }

    // Load any named resource from the piece's own JAR — null if not found
    public InputStream resourceStream(String name) {
        return piece.getClass().getClassLoader().getResourceAsStream(name);
    }


    // Delegate valid-move computation to the underlying piece
    public String[][] getMovableSquares(GameState state) {
        return piece.getMovableSquares(state, this);
    }
}
