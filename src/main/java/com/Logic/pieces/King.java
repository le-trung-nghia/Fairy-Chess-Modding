package com.Logic.pieces;

import com.Logic.Color;
import com.Logic.GameState;
import com.Logic.Piece;
import com.Logic.Position;
import com.Logic.BoardRegion.BoardRegion;
import com.Logic.BoardRegion.Rectangle;
import com.Logic.Vector;

public class King extends Piece {
    public King(Color color, Position position) {
        super(color, position);
    }

    @Override
    protected String icon() {
        // returns the patb for the resource file containing the icon of the King piece
        return color == Color.BLACK ? "black_king.png" : "white_king.png"; // returns the color as well
    }

    @Override
    protected void onMoveCommand(GameState state, Position to) {
        // the destination has a piece
        if (state.getSquare(to) != null) {
            state.capture(to);
        }
        state.move(this.position, to);
        // pass control of the next turn to the other player
        state.passControl();
    }

    @Override
    protected String[][] getMovableSquares(GameState state) {
        // each element of the 2D array contains the image file that is displayed on
        // each movable square
        String[][] movableSquares = new String[8][8];

        // the King can move to and attack all surrounding squares
        BoardRegion movableRegion = new Rectangle(position.saturatingSub(new Vector(1, 1)),
                position.saturatingAdd(new Vector(1, 1))).difference(position);
        for (Position square : movableRegion) {
            if (state.hasEnemy(position, color)) {
                movableSquares[square.row()][square.col()] = "attack.png";
            } else {
                movableSquares[square.row()][square.col()] = "move.png";
            }
        }
        return movableSquares;
    }

    // this piece does not listen to any events, so event methods do not need to be
    // overridden
}