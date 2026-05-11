package com.Logic.BoardRegion;

import java.util.Iterator;
import java.util.Objects;

import com.Logic.Position;

import one.util.streamex.StreamEx;

// The union of two regions. Includes all the squares in the first component region and in the second component region.
public class Union implements BoardRegion {
    private BoardRegion firstRegion;
    private BoardRegion secondRegion;

    Union(BoardRegion firstRegion, BoardRegion secondRegion) {
        this.firstRegion = firstRegion;
        this.secondRegion = secondRegion;
    }

    @Override
    public boolean includes(Position pos) {
        Objects.requireNonNull(pos);
        return firstRegion.includes(pos) || secondRegion.includes(pos);
    }

    @Override
    public Iterator<Position> iterator() {
        return StreamEx.of(firstRegion.iterator()).append(StreamEx.of(secondRegion.difference(firstRegion).iterator()))
                .iterator();
    }
}
