package com.GUI;

import com.Logic.BoardPiece;
import com.Logic.GameState;
import com.Logic.Position;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.util.Objects;

public class App extends Application {

	private static final int SQUARE_SIZE = 95;
	private GameState logic;
	private Pane boardPane;
	private Position selectedPosition = null;

	public void renderBoard(Pane boardPane, GameState logic) {
		// Board background - scale to board size
		ImageView background = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Chess_Board.png"))));
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
				Position pos = new Position(row, col);
				BoardPiece piece = logic.getSquare(pos);

				if (piece != null) {
					addPieceToBoard(boardPane, piece, row, col);
				}
			}
		}
	}

	private void addPieceToBoard(Pane boardPane, BoardPiece piece, int row, int col) {
		// Calculate coords
		double x = col * SQUARE_SIZE;
		double y = row * SQUARE_SIZE;

		// Create the Image View
		String path = "/piece_img/" + piece.icon();
		Image img = new Image(getClass().getResourceAsStream(path));
		ImageView pieceView = new ImageView(img);

		// Set position and properties
		pieceView.setX(x);
		pieceView.setY(y);
		pieceView.setFitWidth(SQUARE_SIZE);
		pieceView.setFitHeight(SQUARE_SIZE);
		pieceView.setSmooth(true);

		// Highlight selected piece
		if (selectedPosition != null && selectedPosition.row() == row && selectedPosition.col() == col) {
			pieceView.setOpacity(0.7);
		}

		// Make it transparent so clicks pass to the squares below
		pieceView.setMouseTransparent(true);

		// Add to the Pane
		boardPane.getChildren().add(pieceView);
	}

	private void handleSquareClick(int row, int col) {
		Position clickedPos = new Position(row, col);

		// If a piece is selected, try to move it
		if (selectedPosition != null) {
			movePiece(selectedPosition, clickedPos);
			clearSelection();
		} else if (logic.getSquare(clickedPos) != null) {
			// No piece selected, so select the piece at this square (if there is one)
			selectedPosition = clickedPos;
			redrawBoard();
		}
	}

	private void movePiece(Position fromPos, Position toPos) {
		// Check if destination is occupied
		if (logic.getSquare(toPos) != null) {
			System.out.println("Cannot move piece to occupied square");
			return;
		}

		// Move the piece using GameState's displace method
		BoardPiece piece = logic.getSquare(fromPos);
		if (piece != null) {
			try {
				logic.displace(fromPos, toPos);
				// Re-render to show the new positions
				redrawBoard();
			} catch (IllegalStateException e) {
				System.out.println("Move failed: " + e.getMessage());
			}
		}
	}

	private void clearSelection() {
		selectedPosition = null;
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