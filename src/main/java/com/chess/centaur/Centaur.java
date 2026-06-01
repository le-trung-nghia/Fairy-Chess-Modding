package com.chess.centaur;

import com.chess.logic.boardregion.Ray;
import com.chess.logic.state.BoardPiece;
import com.chess.logic.state.GameState;
import com.chess.logic.types.*;
import com.chess.registry.PiecePath;
import one.util.streamex.StreamEx;

public class Centaur extends Piece {
    private static final Direction[] Directions = {
        Direction.NORTHEAST, Direction.NORTHWEST,
        Direction.SOUTHEAST, Direction.SOUTHWEST
    };
    private static final Vector[] jumps = {
        new Vector(-2,-1), new Vector(-2,1),
        new Vector(-1,-2), new Vector(-1,2),
        new Vector(1,-2),  new Vector(1,2),
        new Vector(2,-1),  new Vector(2,1)
    };

    @Override public String identifier() { return "centaur"; }

    @Override
    public String icon(BoardPiece thisState) {
        return thisState.color() == Color.BLACK ? "black_centaur.png" : "white_centaur.png";
    }

    @Override public PiecePath[] promotionOptions(GameState state, BoardPiece thisState) { return null; }

    @Override
    public void onMoveCommand(GameState state, BoardPiece thisState, Position to) {
        if (state.hasEnemy(to, thisState.color())) state.capture(to);
        state.move(thisState.position(), to);
        state.passControl();
    }

    @Override
    public String[][] getMovableSquares(GameState state, BoardPiece thisState) {
        String[][] moves = new String[8][8];

        // Bishop rays
        for (Direction d : Directions) {
            for (Position pos : (Iterable<Position>) () -> StreamEx
                    .of(new Ray(thisState.position(), d).iterator()).skip(1).iterator()) {
                BoardPiece curr = state.getSquare(pos);
                if (curr == null) {
                    moves[pos.row()][pos.col()] = "move.png";
                } else {
                    if (curr.color() != thisState.color()) moves[pos.row()][pos.col()] = "attack.png";
                    break;
                }
            }
        }

        // Knight jumps
        for (Vector j : jumps) {
            Vector newPos = thisState.position().toVector().add(j);
            if (!newPos.isInBounds()) continue;
            Position pos = newPos.toPosition();
            BoardPiece curr = state.getSquare(pos);
            if (curr == null) moves[pos.row()][pos.col()] = "move.png";
            else if (curr.color() != thisState.color()) moves[pos.row()][pos.col()] = "attack.png";
        }

        return moves;
    }
}
