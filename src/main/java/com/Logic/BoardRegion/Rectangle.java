package com.Logic.BoardRegion;

import java.util.Iterator;
import java.util.Objects;

import com.Logic.Vector;

import one.util.streamex.StreamEx;

import com.Logic.Position;

// A rectangular region on the board
public record Rectangle(Position topLeft, Position bottomRight) implements BoardRegion {
    public Rectangle {
        Objects.requireNonNull(topLeft);
        Objects.requireNonNull(bottomRight);
    }

    @Override
    public Iterator<Position> iterator() {
        return StreamEx.iterate(topLeft, Objects::nonNull, current -> {
            if (current.col() < bottomRight.col()) {
                return current.add(new Vector(0, 1));
            } else if (current.row() < bottomRight.row()) {
                return current.add(new Vector(1, 0));
            } else {
                return null;
            }
        }).iterator();
    }

    public Position topLeft() {
        return topLeft;
    }

    public Position bottomRight() {
        return bottomRight;
    }

    @Override
    public boolean includes(Position pos) {
        Objects.requireNonNull(pos);
        int row = pos.row();
        int col = pos.col();
        return topLeft.col() <= col && col <= bottomRight.col() && topLeft.row() <= row && row <= bottomRight.row();
    }
}