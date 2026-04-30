package com.Logic;

// Implementation of a piece
public abstract class Piece {
    // current location and color is tracked automatically to avoid boilerplate
    protected Color color;
    protected Position position;

    // Change constructor to public so it's visible
    public Piece(Color color, Position position) {
        this.color = color;
        this.position = position;
    }

    // called by GameState when this piece is moved
    final void move(Position to) {
        this.position = to;
    }

    // called by GameState when this piece is displaced
    final void displace(GameState state, Position to) {
        this.position = to;
        onDisplace(state);
    }

    // called by GameState to change the color of this piece
    final void changeColor(GameState state, Color color) {
        Color previousColor = this.color;
        this.color = color;
        if (color == previousColor.oppositeColor()) {
            onColorChange(state);
        }
    }

    // return the path to the icon for the piece
    protected abstract String icon();

    // user commands this piece to "move" to a square to the board
    protected abstract void onMoveCommand(GameState state, Position to);

    // get the squares this piece can move to
    // returns a 2D array of Strings that are filenames of images that should be
    // overlaid on the movable squares
    // array can contain nulls to indicate that the corresponding square cannot be
    // moved to
    protected abstract String[][] getMovableSquares(GameState state);

    // called when this piece is displaced by another piece
    protected void onDisplace(GameState state) {
    }

    // called when this piece's color is changed
    protected void onColorChange(GameState state) {
    }

    // called when this piece is captured
    protected void onCapture(GameState state) {
    }
}