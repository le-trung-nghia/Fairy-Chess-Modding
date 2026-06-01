package com.chess.viking;

import com.chess.logic.state.BoardPiece;
import com.chess.logic.state.GameState;
import com.chess.logic.types.Color;
import com.chess.logic.types.Direction;
import com.chess.logic.types.Piece;
import com.chess.logic.types.Position;
import com.chess.logic.types.Vector;
import com.chess.registry.PiecePath;

// Piece Viking
// Movement rules: Move up to 2 times horizontally or vertically (not all moves have to be in the same direction).
public class Viking extends Piece {

    private static final Direction[] CARDINAL =
            { Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST };

    // Identity
    @Override public boolean isKing() { return false; }

    @Override
    public String identifier() {
        return "viking";
    }

    @Override
    public String icon(BoardPiece thisState) {
        return thisState.color() == Color.BLACK ? "black_viking.png" : "white_viking.png";
    }

    @Override
    public PiecePath[] promotionOptions(GameState state, BoardPiece thisState) { return null; }

    // Movement
    // Executes a move.  Captures an enemy at the destination first (if present),
    // then moves the Viking there, then passes control to the other player.
    @Override
    public void onMoveCommand(GameState state, BoardPiece thisState, Position to) {
        if (state.hasEnemy(to, thisState.color())) {
            state.capture(to);
        }
        state.move(thisState.position(), to);
        state.passControl();
    }

    // Returns all squares the Viking can legally move to.
    @Override
    public String[][] getMovableSquares(GameState state, BoardPiece thisState) {
        String[][] moves  = new String[8][8];
        Vector     origin = thisState.position().toVector();

        for (Direction d1 : CARDINAL) {
            Vector v1 = origin.add(d1.unitVector());
            if (!v1.isInBounds()) continue;                    // off the board
            Position step1 = v1.toPosition();
            BoardPiece at1 = state.getSquare(step1);
            if (at1 == null) {
                // Empty — Viking can stop here …
                moves[step1.row()][step1.col()] = "move.png";
                // … or take a second step in any cardinal direction
                for (Direction d2 : CARDINAL) {
                    Vector v2 = v1.add(d2.unitVector());
                    if (!v2.isInBounds()) continue;            // off the board
                    if (v2.equals(origin)) continue;           // don't return to start
                    Position step2 = v2.toPosition();
                    BoardPiece at2 = state.getSquare(step2);
                    if (at2 == null) {
                        moves[step2.row()][step2.col()] = "move.png";
                    } else if (at2.color() != thisState.color()) {
                        moves[step2.row()][step2.col()] = "attack.png";
                    }
                    // Own piece at step2 → blocked, skip
                }
            } else if (at1.color() != thisState.color()) {
                // Enemy at step 1 — can capture, but path ends here
                moves[step1.row()][step1.col()] = "attack.png";
            }
            // Own piece at step1 → fully blocked in this direction
        }
        return moves;
    }
}
