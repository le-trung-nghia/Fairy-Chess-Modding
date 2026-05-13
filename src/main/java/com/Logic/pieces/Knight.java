package com.Logic.pieces;

import com.Logic.BoardPiece;
import com.Logic.Color;
import com.Logic.Direction;
import com.Logic.GameState;
import com.Logic.Piece;
import com.Logic.Position;
import com.Logic.Vector;

public class Knight extends Piece {
    private static final Vector[] jumps = {
        new Vector(-2, -1), new Vector(-2,  1),
        new Vector(-1, -2), new Vector(-1,  2),
        new Vector( 1, -2), new Vector( 1,  2),
        new Vector( 2, -1), new Vector( 2,  1)
    }; 
    
    public Knight(Color color, Position position) {
        super(color, position);
    }

    @Override
    protected String icon() {
        return color == Color.BLACK ? "black_knight.png" : "white_knight.png";
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
    }

    @Override
    protected String[][] getMovableSquares(GameState state) {
        String[][] moves = new String[8][8];

        for (Vector j : jumps) {
            Vector newPos = position.toVector().add(j);
            
            if (newPos.isInBounds()) {
                Position pos = target.toPosition();
                BoardPiece curr = state.getSquare(pos);
                if (curr == null) {
                    moves[pos.row()][pos.col()] = ".png";
                } else if (curr.color() != color) {
                    moves[pos.row()][pos.col()] = ".png";
                }
            }
        }
 
        return moves;
    }
}
