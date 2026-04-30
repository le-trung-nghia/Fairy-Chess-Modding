package com.Logic;

import java.util.Iterator;
import java.util.NoSuchElementException;

// A rectangular region on the board
public record Rectangle(Position topLeft, Position bottomRight) implements Iterable<Position> {
    // an iterator that iterates over the squares of a rectangular region
    // from left to right, from top to bottom
    public static class RectangleIterator implements Iterator<Position> {
        private Position current;
        private Position bottomRight;

        private RectangleIterator(Rectangle rect) {
            current = rect.topLeft;
            bottomRight = rect.bottomRight;
        }

        @Override
        public boolean hasNext() {
            return current != null;
        }

        @Override
        public Position next() {
            if (current == null) {
                throw new NoSuchElementException();
            }
            Position outputPosition = current;
            if (current.col() < bottomRight.col()) {
                current = current.add(new Vector(0, 1));
            } else if (current.row() < bottomRight.row()) {
                current = current.add(new Vector(1, 0));
            } else {
                current = null;
            }
            return outputPosition;
        }
    }

    public RectangleIterator iterator() {
        return new RectangleIterator(this);
    }

    public Position topLeft() {
        return topLeft;
    }

    public Position bottomRight() {
        return bottomRight;
    }
}