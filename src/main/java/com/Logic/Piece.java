package com.Logic;

// A piece in the game
public abstract class Piece {
    public enum Color {
        BLACK,
        WHITE
    }

    private Color color;
    // current location is tracked automatically to avoid boilerplate
    private int row;
    private int col;

    public Piece(Color color, int row, int col) {
        this.color = color;
        this.row = row;
        this.col = col;
    }

    // Getters for color and position
    public Color getColor() {
        return color;
    }
    
    public int getRow() {
        return row;
    }
    
    public int getCol() {
        return col;
    }

    // called by GameState to update the position of this piece
    final public void updatePosition(int row, int col) {
        this.row = row;
        this.col = col;
    }
    
    public abstract String getImageFileName();
    // user commands this piece to "move" to a square to the board
    public abstract void move(GameState state, int row, int col);

    // get the squares this piece can move to
    // returns a 2D array of Strings that are filenames of images that should be overlaid on the movable squares
    // array can contain nulls to indicate that the corresponding square cannot be moved to
    public abstract String[][] getMovableSquares();

    // called when this piece is displaced (moved adversarially) by another piece
    // the previous location is passed to this function
    public abstract void onDisplace(int prevRow, int prevCol);

    // called when this piece's color is changed
    public abstract void onColorChange();

    // called when this piece is captured
    public abstract void onCaptured();
}