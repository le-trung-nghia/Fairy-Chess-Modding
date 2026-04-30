package com.Logic;

public class Rook extends Piece {
    public Rook (Color color, int row, int col) {
        super(color, row, col);
    }

    @Override
    public String getImageFileName() {
        return "piece_img/" + (getColor() == Color.BLACK ? "black" : "white") + "_rook.png";
    }

    @Override
    public void move(GameState state, int row, int col) {
        // TODO: Implement pawn movement rules
    }

    @Override
    public String[][] getMovableSquares() {
        // TODO: Return possible squares this pawn can move to
        return new String[8][8];
    }

    @Override
    public void onDisplace(int prevRow, int prevCol) {
        // Called when pawn is moved adversarially
    }

    @Override
    public void onColorChange() {
        // Called when pawn's color is changed
    }

    @Override
    public void onCaptured() {
        // Called when pawn is captured
    }
}