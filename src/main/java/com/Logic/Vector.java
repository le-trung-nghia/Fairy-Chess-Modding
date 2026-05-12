package com.Logic;

import java.util.Objects;

public record Vector(int row, int col) {
    final static Vector TOWARDS_BLACK = new Vector(1, 0);
    final static Vector TOWARDS_WHITE = new Vector(-1, 0);

    public boolean equals(Vector other) {
        Objects.requireNonNull(other);
        return (this.row == other.row && this.col == other.col);
    }

    // add another vector to this vector
    public Vector add(Vector other) {
        Objects.requireNonNull(other);
        return new Vector(this.row + other.row, this.col + other.col);
    }

    // subtract another vector from this vector
    public Vector sub(Vector other) {
        Objects.requireNonNull(other);
        return new Vector(this.row - other.row, this.col - other.col);
    }

    // multiply this vector by a scalar
    public Vector mul(int multiplier) {
        return new Vector(row * multiplier, col * multiplier);
    }

    // negate this vector
    public Vector negate() {
        return this.mul(-1);
    }

    // return true if this is a zero vector
    public boolean isZero() {
        return row == 0 && col == 0;
    }

    // return true if this vector represents a board position
    public boolean isInBounds() {
        return 0 <= row && row <= 7 && 0 <= col && col <= 7;
    }

    // convert this vector to a position
    // this method will error if the vector is out of bounds
    public Position toPosition() {
        return new Position(row, col);
    }

    // return true if this vector is a positive multiple of a direction unit vector
    public boolean isDirectional() {
        return !this.isZero() && Math.abs(row) == Math.abs(col);
    }

    // rotate this vector clockwise by 90 degrees
    public Vector rotateClockwise() {
        return new Vector(col, -row);
    }

    // rotate this vector counterclockwise by 90 degrees
    public Vector rotateCounterclockwise() {
        return new Vector(-col, row);
    }

    // if this vector is a positive multiple of a direction unit vector, return that
    // direction
    // else, throws IllegalArgumentException
    public Direction direction() {
        if (!this.isDirectional()) {
            throw new IllegalArgumentException();
        }
        if (row == 0) {
            return col > 0 ? Direction.EAST : Direction.WEST;
        }
        if (col == 0) {
            return row > 0 ? Direction.SOUTH : Direction.NORTH;
        }
        if (row > 0 && col > 0) {
            return Direction.SOUTHEAST;
        } else if (row < 0 && col < 0) {
            return Direction.NORTHWEST;
        } else if (row < 0 && col > 0) {
            return Direction.NORTHEAST;
        } else {
            // row > 0 && col < 0
            return Direction.SOUTHWEST;
        }
    }
}
