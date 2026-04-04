package com.GUI;

import com.Logic.Piece;
import com.Logic.Pieces;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class App extends Application {
	public void renderBoard(Pane boardPane, Pieces logic) {
	    // Board
	    ImageView background = new ImageView(new Image(getClass().getResourceAsStream("/Chess_Board.png")));
	    boardPane.getChildren().add(background);

	    // Piece grid
	    for (int row = 0; row < 8; row++) {
	        for (int col = 0; col < 8; col++) {
	            Piece piece = logic.getPiece(row, col);

	            if (piece != null) {
	                // Calculate coords
	                double x = col * 120;
	                double y = row * 120;

	                // Create the Image View
	                String path = "/" + piece.getImageFileName();
	                Image img = new Image(getClass().getResourceAsStream(path));
	                ImageView pieceView = new ImageView(img);

	                // Set position and properties
	                pieceView.setX(x);
	                pieceView.setY(y);
	                pieceView.setFitWidth(120);
	                pieceView.setFitHeight(120);
	                pieceView.setSmooth(true);

	                // Add to the Pane
	                boardPane.getChildren().add(pieceView);
	            }
	        }
	    }
	}
    @Override
    public void start(Stage stage) {
        // App Icon
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/icon.png")));

        // Layout & Scene
        Pane root = new Pane();
        Pieces logic = new Pieces();
        Scene scene = new Scene(root, 960, 960);        
        renderBoard(root, logic);

        stage.setTitle("Maven JavaFX 25");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }
    
    public static void main(String[] args) {
        launch();
    }
}

