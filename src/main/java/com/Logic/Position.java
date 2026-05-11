package com.Logic;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import com.Logic.BoardRegion.BoardRegion;

// A square on the board
public record Position(int row, int col) implements BoardRegion {
    public Position {
        if (row < 0 || row > 7) {
            throw new IllegalArgumentException(
                    "Position must have row between 0 and 7 inclusive, got %d".formatted(row));
        }
        if (col < 0 || col > 7) {
            throw new IllegalArgumentException(
                    "Position must have column between 0 and 7 inclusive, got %d".formatted(row));
        }
    }

    public boolean equals(Position other) {
        Objects.requireNonNull(other);
        return (this.row == other.row && this.col == other.col);
    }

    // add a vector to this position
    public Position add(Vector displacement) {
        Objects.requireNonNull(displacement);
        return new Position(this.row + displacement.row(), this.col + displacement.col());
    }

    public Position saturatingAdd(Vector displacement) {
        Objects.requireNonNull(displacement);
        return new Position(Math.clamp(this.row + displacement.row(), 0, 7),
                Math.clamp(this.col + displacement.col(), 0, 7));
    }

    // subtract a vector from this position
    public Position sub(Vector displacement) {
        Objects.requireNonNull(displacement);
        return new Position(this.row - displacement.row(), this.col - displacement.col());
    }

    public Position saturatingSub(Vector displacement) {
        Objects.requireNonNull(displacement);
        return new Position(Math.clamp(this.row - displacement.row(), 0, 7),
                Math.clamp(this.col - displacement.col(), 0, 7));
    }

    // get the vector pointing from this position to another position
    public Vector vectorTowards(Position other) {
        Objects.requireNonNull(other);
        return new Vector(other.row - this.row, other.col - this.col);
    }

    // convert this position to a vector
    public Vector toVector() {
        return new Vector(row, col);
    }

    public int row() {
        return row;
    }

    public int col() {
        return col;
    }

    @Override
    public boolean includes(Position pos) {
        return this.equals(pos);
    }

    @Override
    public Iterator<Position> iterator() {
        return List.of(this).iterator();
    }
}
