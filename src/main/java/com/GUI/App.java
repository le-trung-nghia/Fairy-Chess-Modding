package com.GUI;

import com.Logic.Piece;
import com.Logic.GameState;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class App extends Application {
	
	private static final int SQUARE_SIZE = 95;
	private GameState logic;
	private Pane boardPane;
	private int selectedRow = -1;
	private int selectedCol = -1;
	
	public void renderBoard(Pane boardPane, GameState logic) {
	    // Board background - scale to board size
	    ImageView background = new ImageView(new Image(getClass().getResourceAsStream("/Chess_Board.png")));
	    int boardSize = SQUARE_SIZE * 8;
	    background.setFitWidth(boardSize);
	    background.setFitHeight(boardSize);
	    background.setPreserveRatio(false);
	    boardPane.getChildren().add(background);

	    // Add invisible squares for click detection
	    for (int row = 0; row < 8; row++) {
	        for (int col = 0; col < 8; col++) {
	            int finalRow = row;
	            int finalCol = col;

	            Rectangle square = new Rectangle(col * SQUARE_SIZE, row * SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE);
	            square.setFill(Color.TRANSPARENT);
	            square.setOnMouseClicked(event -> handleSquareClick(finalRow, finalCol));
	            boardPane.getChildren().add(square);
	        }
	    }

	    // Piece grid
	    for (int row = 0; row < 8; row++) {
	        for (int col = 0; col < 8; col++) {
	            Piece piece = logic.getSquare(row, col);

	            if (piece != null) {
	                addPieceToBoard(boardPane, piece, row, col);
	            }
	        }
	    }
	}
	
	private void addPieceToBoard(Pane boardPane, Piece piece, int row, int col) {
	    // Calculate coords
	    double x = col * SQUARE_SIZE;
	    double y = row * SQUARE_SIZE;

	    // Create the Image View
	    String path = "/" + piece.getImageFileName();
	    Image img = new Image(getClass().getResourceAsStream(path));
	    ImageView pieceView = new ImageView(img);

	    // Set position and properties
	    pieceView.setX(x);
	    pieceView.setY(y);
	    pieceView.setFitWidth(SQUARE_SIZE);
	    pieceView.setFitHeight(SQUARE_SIZE);
	    pieceView.setSmooth(true);
	    
	    // Highlight selected piece
	    if (row == selectedRow && col == selectedCol) {
	        pieceView.setOpacity(0.7);
	    }
	    
	    // Make it transparent so clicks pass to the squares below
	    pieceView.setMouseTransparent(true);
	    
	    // Store row and col for reference
	    pieceView.setUserData(new int[]{row, col});

	    // Add to the Pane
	    boardPane.getChildren().add(pieceView);
	}
	
	private void handleSquareClick(int row, int col) {
	    // If a piece is selected, try to move it
	    if (selectedRow != -1) {
	        movePiece(selectedRow, selectedCol, row, col);
	        clearSelection();
	    } else if (logic.getSquare(row, col) != null) {
	        // No piece selected, so select the piece at this square (if there is one)
	        selectedRow = row;
	        selectedCol = col;
	        redrawBoard();
	    }
	}
	
  // TODO: Change this after correct implementation
	private void movePiece(int fromRow, int fromCol, int toRow, int toCol) {
	    // Check if destination is occupied
	    if (logic.getSquare(toRow, toCol) != null) {
	        System.out.println("Cannot move piece to occupied square");
	        return;
	    }
	    
	    // Move the piece using GameState's displace method
	    Piece piece = logic.getSquare(fromRow, fromCol);
	    if (piece != null) {
	        logic.displace(fromRow, fromCol, toRow, toCol);
	        
	        // Re-render to show the new positions
	        redrawBoard();
	    }
	}
	
	private void clearSelection() {
	    selectedRow = -1;
	    selectedCol = -1;
	    // Re-render to remove highlighting
	    redrawBoard();
	}
	
	private void redrawBoard() {
	    boardPane.getChildren().clear();
	    renderBoard(boardPane, logic);
	}
	
    @Override
    public void start(Stage stage) {
        // App Icon
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/icon.png")));

        // Layout & Scene
        boardPane = new Pane();
        logic = new GameState();
        int boardSize = SQUARE_SIZE * 8;
        Scene scene = new Scene(boardPane, boardSize, boardSize);        
        renderBoard(boardPane, logic);

        stage.setTitle("Fairy Chess 2026");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }
    
    public static void main(String[] args) {
        launch();
    }
}