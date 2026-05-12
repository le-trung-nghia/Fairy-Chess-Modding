package com.Logic.BoardRegion;

import java.util.Iterator;
import java.util.Objects;

import com.Logic.Position;

import one.util.streamex.StreamEx;

// The union of two regions. Includes all the squares in the first component region and in the second component region.
public record Union(BoardRegion firstRegion, BoardRegion secondRegion) implements BoardRegion {
    public Union {
        Objects.requireNonNull(firstRegion);
        Objects.requireNonNull(secondRegion);
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
