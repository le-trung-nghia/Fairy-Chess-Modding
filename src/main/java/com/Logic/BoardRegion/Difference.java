package com.Logic.BoardRegion;

import java.util.Iterator;
import java.util.Objects;

import com.Logic.Position;

import one.util.streamex.StreamEx;

// The difference of two regions. Includes all the squares in the first component region but not in the second component region.
public class Difference implements BoardRegion {
    private BoardRegion firstRegion;
    private BoardRegion secondRegion;

    Difference(BoardRegion firstRegion, BoardRegion secondRegion) {
        this.firstRegion = firstRegion;
        this.secondRegion = secondRegion;
    }

    @Override
    public boolean includes(Position pos) {
        Objects.requireNonNull(pos);
        return firstRegion.includes(pos) & !secondRegion.includes(pos);
    }

    @Override
    public Iterator<Position> iterator() {
        return StreamEx.of(firstRegion.iterator()).remove(secondRegion::includes).iterator();
    }
}
