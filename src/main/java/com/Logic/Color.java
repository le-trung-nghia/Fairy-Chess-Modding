package com.Logic;

public enum Color {
    BLACK,
    WHITE;

    // get the opposite color
    public Color oppositeColor() {
        return (this == BLACK) ? WHITE : BLACK;
    }

    // get the vector pointing towards the forward direction (i.e. for a pawn) for
    // the color
    public Vector forwardVector() {
        return (this == BLACK) ? Vector.TOWARDS_BLACK : Vector.TOWARDS_WHITE;
    }
}
