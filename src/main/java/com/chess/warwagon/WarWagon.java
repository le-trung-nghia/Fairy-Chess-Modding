package com.chess.warwagon;

import com.chess.logic.boardregion.Ray;
import com.chess.logic.boardregion.Rectangle;
import com.chess.logic.state.BoardPiece;
import com.chess.logic.state.GameState;
import com.chess.logic.types.*;
import com.chess.registry.PiecePath;
import one.util.streamex.StreamEx;

public class WarWagon extends Piece {
    private static final Direction[] Directions = {
        Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST
    };

    @Override public boolean isKing() { return false; }
    @Override public String identifier() { return "war_wagon"; }

    @Override
    public String icon(BoardPiece thisState) {
        return thisState.color() == Color.BLACK ? "black_war_wagon.png" : "white_war_wagon.png";
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

        // Rook rays
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

        // King steps (all 8 surrounding squares)
        for (Position pos : new Rectangle(
                thisState.position().saturatingSub(new Vector(1, 1)),
                thisState.position().saturatingAdd(new Vector(1, 1)))
                .difference(thisState.position())) {
            // don't overwrite a ray square already marked as attack
            if (moves[pos.row()][pos.col()] != null) continue;
            BoardPiece curr = state.getSquare(pos);
            if (curr == null) moves[pos.row()][pos.col()] = "move.png";
            else if (curr.color() != thisState.color()) moves[pos.row()][pos.col()] = "attack.png";
        }

        return moves;
    }
}
