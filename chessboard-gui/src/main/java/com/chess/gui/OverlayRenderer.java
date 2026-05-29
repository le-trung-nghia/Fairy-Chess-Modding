package com.chess.gui;

import javafx.scene.layout.Pane;

/**
 * Implement this interface in a Piece subclass to provide custom overlay
 * rendering instead of the GUI's default dot / ring shapes.
 *
 * App.java checks instanceof at render time and calls these methods
 * instead of the built-in switch cases.
 */
public interface OverlayRenderer {
    void renderMoveOverlay(Pane pane, double x, double y, double squareSize);
    void renderAttackOverlay(Pane pane, double x, double y, double squareSize);
}
