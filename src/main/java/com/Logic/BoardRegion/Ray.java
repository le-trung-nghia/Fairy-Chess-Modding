package com.Logic.BoardRegion;

import java.util.Iterator;
import java.util.Objects;

import com.Logic.Direction;
import com.Logic.Position;
import com.Logic.Vector;

import one.util.streamex.StreamEx;

// A ray on the board, starting from a point on the board and extending infinitely in one direction
public record Ray(Position origin, Direction direction) implements BoardRegion {
    public Ray {
        Objects.requireNonNull(origin);
        Objects.requireNonNull(direction);
    }

    @Override
    public Iterator<Position> iterator() {
        return StreamEx
                .iterate(origin.toVector(), current -> current.isInBounds(),
                        current -> current.add(direction.unitVector()))
                .map(Vector::toPosition)
                .iterator();
    }

    @Override
    public boolean includes(Position pos) {
        return origin.vectorTowards(pos).direction() == direction;
    }
}
