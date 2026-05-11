package com.Logic;

import java.util.Objects;

// A piece on the board along with some state
public class BoardPiece {
    private boolean isKing;
    private Color color;
    private Piece piece;

    BoardPiece(boolean isKing, Color color, Piece piece) {
        this.isKing = isKing;
        this.color = color;
        this.piece = piece;
    }

    void move(Position pos) {
        piece.move(Objects.requireNonNull(pos));
    }

    void displace(GameState state, Position pos) {
        piece.displace(Objects.requireNonNull(state), Objects.requireNonNull(pos));
    }

    void capture(GameState state) {
        piece.onCapture(Objects.requireNonNull(state));
    }

    void changeColor(GameState state, Color color) {
        piece.changeColor(Objects.requireNonNull(state), Objects.requireNonNull(color));
    }

    public boolean isKing() {
        return isKing;
    }

    public Color color() {
        return color;
    }

    // Exposed the method icon
    public String icon() {
        return piece.icon();
    }
}
