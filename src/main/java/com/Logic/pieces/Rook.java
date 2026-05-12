package com.Logic.pieces;

import com.Logic.Color;
import com.Logic.Direction;
import com.Logic.GameState;
import com.Logic.Piece;
import com.Logic.Position;
import com.Logic.Vector;

public class Rook extends Piece {
    private static final Direction[] Directions = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
    
    public Rook(Color color, Position position) {
        super(color, position);
    }

    @Override
    protected String icon() {
        return color == Color.BLACK ? "black_rook.png" : "white_rook.png";
    }

    @Override
    protected void onMoveCommand(GameState state, Position to) {
        String[][] movable = getMovableSquares(state);
        if (movable[to.row()][to.col()] != null) {
            if (state.hasEnemy(to, color)) {
                state.capture(to);
            }
            state.move(position, to);
            state.passControl();
    }

    @Override
    protected String[][] getMovableSquares(GameState state) {
        String[][] moves = new String[8][8];

        for (Direction d : Directions) {
            Vector step = d.unitVector();
            Vector newPos = position.toVector().add(step);

            while (newPos.isInBounds()) {
                Position pos = newPos.toPosition();
                BoardPiece curr = state.getSquare(pos);

                if (curr == null) {
                    moves[pos.row()][pos.col()] = "greensquare.png";
                }
                else {
                    if (curr.color() != color) {
                        moves[pos.row()][pos.col()] = "redsquare.png";
                    }
                    break;
                }

                curr = curr.add(step);
            }
        }

        return moves;
    }
}
