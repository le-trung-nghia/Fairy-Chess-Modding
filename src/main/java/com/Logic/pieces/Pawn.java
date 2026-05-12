package com.Logic.pieces;

import com.Logic.BoardPiece;
import com.Logic.Color;
import com.Logic.Direction;
import com.Logic.GameState;
import com.Logic.Piece;
import com.Logic.Position;
import com.Logic.Vector;

public class Pawn extends Piece {
    private boolean hasMoved = false;
    
    public Pawn(Color color, Position position) {
        super(color, position);
    }

    @Override
    protected String icon() {
        return color == Color.BLACK ? "black_pawn.png" : "white_pawn.png";
    }

    @Override
    protected void onMoveCommand(GameState state, Position to) {
        String[][] movable = getMovableSquares(state);
        if (movable[to.row()][to.col()] != null) {
            if (state.hasEnemy(to, color)) {
                state.capture(to);
            }
            state.move(position, to);
            hasMoved = true;
            state.passControl();
        }
    }

    @Override
    protected String[][] getMovableSquares(GameState state) {
        String[][] moves = new String[8][8];
        Direction forward = color.forwardDirection();
        Vector fwd = forward.unitVector();

        //One step forward
        Position oneStep = position.add(fwd);
        if (state.getSquare(oneStep) == null) {
            moves[oneStep.row()][oneStep.col()] = ".png";

            //Two steps forward
            if(!hasMoved) {
                Position twoSteps = position.add(fwd.mul(2));
                if (state.getSquare(twoStep) == null) {
                    moves[twoSteps.row()][twoSteps.col()] = ".png";
                }
            }
        }

        //Captures
        for (Direction diag: new Direction[]{forward.skewLeft(), forward.skewRight()}) {
            Position capturePos = position,add(diag.unitVector());
            if (state.hasEnemy(capturePos, color) {
                moves[capturePos.row()][capturePos.col()] = ".png";
    }
}
