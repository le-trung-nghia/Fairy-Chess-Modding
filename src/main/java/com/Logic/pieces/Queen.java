package com.Logic.pieces;

import com.Logic.Color;
import com.Logic.GameState;
import com.Logic.Piece;
import com.Logic.Position;

public class Queen extends Piece {
    public Queen(Color color, Position position) {
        super(color, position);
    }

    @Override
    protected String icon() {
        return color == Color.BLACK ? "black_queen.png" : "white_queen.png";
    }

    @Override
    protected void onMoveCommand(GameState state, Position to) {
        // TODO: Implement bishop movement rules
    }

    @Override
    protected String[][] getMovableSquares(GameState state) {
        // TODO: Return possible squares this bishop can move to
        return new String[8][8];
    }
}