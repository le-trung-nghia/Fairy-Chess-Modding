package com.chess.cardinal;

import com.chess.logic.boardregion.Ray;
import com.chess.logic.boardregion.Rectangle;
import com.chess.logic.state.BoardPiece;
import com.chess.logic.state.GameState;
import com.chess.logic.types.*;
import com.chess.registry.PiecePath;
import one.util.streamex.StreamEx;

public class Cardinal extends Piece {
    private static final Direction[] Directions = {
        Direction.NORTHEAST, Direction.NORTHWEST,
        Direction.SOUTHEAST, Direction.SOUTHWEST
    };

    @Override public boolean isKing() { return false; }
    @Override public String identifier() { return "cardinal"; }

    @Override
    public String icon(BoardPiece thisState) {
        return thisState.color() == Color.BLACK ? "black_cardinal.png" : "white_cardinal.png";
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

        // King steps
        for (Position pos : new Rectangle(
                thisState.position().saturatingSub(new Vector(1, 1)),
                thisState.position().saturatingAdd(new Vector(1, 1)))
                .difference(thisState.position())) {
            if (moves[pos.row()][pos.col()] != null) continue;
            BoardPiece curr = state.getSquare(pos);
            if (curr == null) moves[pos.row()][pos.col()] = "move.png";
            else if (curr.color() != thisState.color()) moves[pos.row()][pos.col()] = "attack.png";
        }

        return moves;
    }
}
