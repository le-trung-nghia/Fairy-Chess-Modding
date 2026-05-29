module com.chess.gui {
    requires com.chess.logic;
    requires javafx.graphics;
    requires javafx.controls;

    exports com.chess.gui;
    opens com.chess.gui to javafx.graphics;
}
