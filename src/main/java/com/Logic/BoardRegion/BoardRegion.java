package com.Logic.BoardRegion;

import java.util.Objects;

import com.Logic.Position;

// A set of squares on the board
// The set of squares may be iterated over using the Iterable interface
// Each square should only be iterated over once in implementors
public interface BoardRegion extends Iterable<Position> {
    // cheap method to check if a square is in this region
    boolean includes(Position pos);

    default BoardRegion difference(BoardRegion other) {
        return new Difference(this, Objects.requireNonNull(other));
    }

    default BoardRegion union(BoardRegion other) {
        return new Union(this, Objects.requireNonNull(other));
    }
}
