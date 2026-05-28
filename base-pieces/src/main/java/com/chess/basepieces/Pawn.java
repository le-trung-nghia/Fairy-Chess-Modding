package com.chess.basepieces;

import com.chess.logic.state.BoardPiece;
import com.chess.logic.state.GameState;
import com.chess.logic.types.Color;
import com.chess.logic.types.Direction;
import com.chess.logic.types.Piece;
import com.chess.logic.types.Position;
import com.chess.logic.types.Vector;

public class Pawn extends Piece {
    private boolean hasMoved = false;

    @Override
    public String icon(BoardPiece thisState) {
        return thisState.color() == Color.BLACK ? "black_pawn.png" : "white_pawn.png";
    }

    @Override
    public String identifier() {
        return "pawn";
    }

    @Override
    public void onMoveCommand(GameState state, BoardPiece thisState, Position to) {
        if (state.hasEnemy(to, thisState.color())) {
            state.capture(to);
        }
        state.move(thisState.position(), to);
        hasMoved = true;
        state.passControl();
    }

    @Override
    public String[][] getMovableSquares(GameState state, BoardPiece thisState) {
        String[][] moves = new String[8][8];
        Direction forward = thisState.color().forwardDirection();
        Vector    origin  = thisState.position().toVector();
        Vector    fwd     = forward.unitVector();

        // One step forward
        Vector v1 = origin.add(fwd);
        if (v1.isInBounds()) {
            Position oneStep = v1.toPosition();
            if (state.getSquare(oneStep) == null) {
                moves[oneStep.row()][oneStep.col()] = "move.png";

                // Two steps forward (only from starting position)
                if (!hasMoved) {
                    Vector v2 = origin.add(fwd.mul(2));
                    if (v2.isInBounds()) {
                        Position twoSteps = v2.toPosition();
                        if (state.getSquare(twoSteps) == null) {
                            moves[twoSteps.row()][twoSteps.col()] = "move.png";
                        }
                    }
                }
            }
        }

        // Diagonal captures — must bounds-check before constructing Position
        for (Direction diag : new Direction[] { forward.skewLeft(), forward.skewRight() }) {
            Vector vc = origin.add(diag.unitVector());
            if (!vc.isInBounds()) continue;          // off the board (col a or col h)
            Position capturePos = vc.toPosition();
            if (state.hasEnemy(capturePos, thisState.color())) {
                moves[capturePos.row()][capturePos.col()] = "attack.png";
            }
        }

        return moves;
    }
}
