package com.Logic;

import java.util.Objects;

// A direction on the board, including cardinal and diagonal directions.
public enum Direction {
    NORTH(-1, 0),
    SOUTH(1, 0),
    EAST(0, 1),
    WEST(0, -1),
    NORTHEAST(-1, 1),
    NORTHWEST(-1, -1),
    SOUTHEAST(1, 1),
    SOUTHWEST(1, -1);

    private Vector unitVector;
    private Direction nextClockwise;
    private Direction nextCounterClockwise;
    private Direction opposite;

    private Direction(int row, int col) {
        unitVector = new Vector(row, col);
    }

    static {
        NORTH.nextClockwise = NORTHEAST;
        SOUTH.nextClockwise = SOUTHWEST;
        WEST.nextClockwise = NORTHWEST;
        EAST.nextClockwise = SOUTHEAST;
        NORTHWEST.nextClockwise = NORTH;
        NORTHEAST.nextClockwise = EAST;
        SOUTHWEST.nextClockwise = WEST;
        SOUTHEAST.nextClockwise = SOUTH;

        NORTH.nextCounterClockwise = NORTHWEST;
        SOUTH.nextCounterClockwise = SOUTHEAST;
        WEST.nextCounterClockwise = SOUTHWEST;
        EAST.nextCounterClockwise = NORTHEAST;
        NORTHWEST.nextCounterClockwise = WEST;
        NORTHEAST.nextCounterClockwise = NORTH;
        SOUTHWEST.nextCounterClockwise = SOUTH;
        SOUTHEAST.nextCounterClockwise = EAST;

        NORTH.opposite = SOUTH;
        SOUTH.opposite = NORTH;
        WEST.opposite = EAST;
        EAST.opposite = WEST;
        NORTHWEST.opposite = SOUTHEAST;
        NORTHEAST.opposite = SOUTHWEST;
        SOUTHWEST.opposite = NORTHEAST;
        SOUTHEAST.opposite = NORTHWEST;
    }

    // get the unit vector pointing in this direction
    public Vector unitVector() {
        return this.unitVector;
    }

    public boolean isParallel(Direction other) {
        Objects.requireNonNull(other);
        return this == other || this == other.opposite();
    }

    public boolean isHorizontal() {
        return this == WEST || this == EAST;
    }

    public boolean isVertical() {
        return this == NORTH || this == SOUTH;
    }

    public boolean isMainDiagonal() {
        return this == NORTHWEST || this == SOUTHEAST;
    }

    public boolean isSubdiagonal() {
        return this == SOUTHWEST || this == NORTHEAST;
    }

    public boolean isCardinal() {
        return this.isHorizontal() || this.isVertical();
    }

    public boolean isDiagonal() {
        return !this.isCardinal();
    }

    // for a vertical cardinal direction, get the corresponding diagonal
    // direction that skews to the left of the board
    public Direction skewLeft() {
        return switch (this) {
            case NORTH -> NORTHWEST;
            case SOUTH -> SOUTHWEST;
            default -> throw new IllegalArgumentException();
        };
    }

    // for a vertical cardinal direction, get the corresponding diagonal
    // direction that skews to the right of the board
    public Direction skewRight() {
        return switch (this) {
            case NORTH -> NORTHEAST;
            case SOUTH -> SOUTHEAST;
            default -> throw new IllegalArgumentException();
        };
    }

    // for a horizontal cardinal direction, get the corresponding diagonal
    // direction that skews to the top of the board
    public Direction skewTop() {
        return switch (this) {
            case WEST -> NORTHWEST;
            case EAST -> NORTHEAST;
            default -> throw new IllegalArgumentException();
        };
    }

    // for a horizontal cardinal direction, get the corresponding diagonal
    // direction that skews to the bottom of the board
    public Direction skewBottom() {
        return switch (this) {
            case WEST -> SOUTHWEST;
            case EAST -> SOUTHEAST;
            default -> throw new IllegalArgumentException();
        };
    }

    // get the direction that is closest clockwise to this direction
    public Direction rotateClockwise() {
        return this.nextClockwise;
    }

    // get the direction tbat is closest counterclockwise to this direction
    public Direction rotateCounterclockwise() {
        return this.nextCounterClockwise;
    }

    // get the opposite direction of this direction
    public Direction opposite() {
        return this.opposite;
    }
}
