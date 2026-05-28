package com.chess.gui;

import com.chess.logic.state.BoardPiece;
import com.chess.logic.state.GameState;
import com.chess.logic.state.PieceState;
import com.chess.logic.types.Piece;
import com.chess.logic.types.Position;
import com.chess.registry.PiecePath;
import com.chess.registry.PieceRegistry;

import java.io.File;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

public class App extends Application {

    //  Constants 

    private static final int SQUARE_SIZE = 70;
    private static final int MARGIN      = 20;
    private static final int SIDE_PANE_W = 250;
    private static final int BOARD_PX    = SQUARE_SIZE * 8;          // 560
    private static final int WINDOW_W    = MARGIN + BOARD_PX + MARGIN + SIDE_PANE_W + MARGIN; // 870
    private static final int WINDOW_H    = BOARD_PX + MARGIN * 2;    // 600

    //  Inner types 

    /** One move in history; carries optional promotion metadata. */
    private static class MoveRecord {
        final Position from, to;
        String promotionPack  = null;  // null → no promotion this move
        String promotionPiece = null;
        MoveRecord(Position from, Position to) { this.from = from; this.to = to; }
    }

    /** A single piece placement used for the custom board layout. */
    private record BoardPlacement(
            String packName, String pieceName,
            com.chess.logic.types.Color color,
            int row, int col, boolean isKing) {}

    //  Game state ─

    private GameState logic;
    private Pane      boardPane;
    private Position  selectedPosition = null;
    private String[][]validMoves       = null;

    /** History; viewIndex 0 = initial board. */
    private final List<MoveRecord> moveHistory = new ArrayList<>();
    private int viewIndex = 0;

    //  Timer 

    private int      whiteTimeSeconds;
    private int      blackTimeSeconds;
    private Timeline gameTimer;

    //  Registry & custom layout ─

    private final PieceRegistry registry = new PieceRegistry();
    /** null → use buildDefaultLayout() */
    private List<BoardPlacement> customBoardLayout = null;

    //  Live UI handles (game scene) ─

    private Label whiteTimerLabel, blackTimerLabel, turnLabel;
    private VBox  historyListBox;
    private Stage primaryStage;

    //  Board-editor state ─

    private GameState          editorState;
    private final BoardPlacement[][] editorGrid = new BoardPlacement[8][8];
    private String             editorSelectedPack  = null;
    private String             editorSelectedPiece = null;
    private com.chess.logic.types.Color editorSelectedColor =
            com.chess.logic.types.Color.WHITE;
    private Pane  editorBoardPane;
    private Label editorStatusLabel;
    private VBox  editorPaletteBox;   // rebuilt on each selection change

    //  JavaFX entry ─

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        stage.getIcons().add(new Image(Objects.requireNonNull(
                getClass().getResourceAsStream("/icon.png"))));
        stage.setTitle("Fairy Chess 2026");
        stage.setResizable(false);
        loadPacks();
        showMenuScene();
        stage.show();
    }

    //  Pack loading ─

    /**
     * Scans the packs directory and registers every JAR found there.
     *
     * Resolution order:
     *  1. JVM property "packs.dir" (explicit override)
     *  2. Auto-detect:
     *     - Dev mode (classes/): walk up three levels to project root → root/target/packs/
     *     - Fat-JAR mode: packs/ directory next to the JAR
     */
    private void loadPacks() {
        File packsDir;
        String prop = System.getProperty("packs.dir");
        if (prop != null) {
            packsDir = new File(prop);
        } else {
            try {
                String location = App.class.getProtectionDomain()
                        .getCodeSource().getLocation().toURI().getPath();
                File loc = new File(location);
                if (loc.isDirectory()) {
                    // Dev: <module>/target/classes/ → up ×3 → root/target/packs/
                    File root = loc.getParentFile().getParentFile().getParentFile();
                    packsDir = new File(new File(root, "target"), "packs");
                } else {
                    packsDir = new File(loc.getParentFile(), "packs");
                }
            } catch (URISyntaxException e) {
                System.err.println("[packs] could not determine packs directory: " + e.getMessage());
                return;
            }
        }

        if (!packsDir.isDirectory()) {
            System.out.println("[packs] directory not found: " + packsDir.getAbsolutePath());
            return;
        }
        File[] jars = packsDir.listFiles(f -> f.getName().endsWith(".jar"));
        if (jars == null || jars.length == 0) {
            System.out.println("[packs] no JARs found in " + packsDir.getAbsolutePath());
            return;
        }
        for (File jar : jars) {
            try {
                registry.registerPiecePack(jar);
                System.out.println("[packs] loaded: " + jar.getName());
            } catch (RuntimeException e) {
                System.err.println("[packs] failed: " + jar.getName() + ": " + e.getMessage());
            }
        }
    }

    //  Scene navigation ─

    private void showMenuScene() {
        Text logo = new Text("FAIRY CHESS");
        logo.setFont(Font.font("Arial", FontWeight.BOLD, 62));
        logo.setFill(Color.WHITE);

        Button playBtn     = mainButton("PLAY",     220);
        Button settingsBtn = mainButton("SETTINGS", 220);
        playBtn.setOnAction(e -> showTimeSelectScene());
        settingsBtn.setOnAction(e -> showSettingsScene());

        VBox box = new VBox(28, logo, playBtn, settingsBtn);
        box.setAlignment(Pos.CENTER);
        StackPane root = new StackPane(box);
        root.setStyle("-fx-background-color: #2b2b2b;");
        primaryStage.setScene(new Scene(root, WINDOW_W, WINDOW_H));
    }

    private void showTimeSelectScene() {
        Text title = new Text("Select Time Control");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 34));
        title.setFill(Color.WHITE);

        Button b30  = mainButton("30 minutes", 220);
        Button b10  = mainButton("10 minutes", 220);
        Button b3   = mainButton(" 3 minutes", 220);
        Button back = grayButton("← Back",     220);

        b30 .setOnAction(e -> startGame(30 * 60));
        b10 .setOnAction(e -> startGame(10 * 60));
        b3  .setOnAction(e -> startGame( 3 * 60));
        back.setOnAction(e -> showMenuScene());

        VBox box = new VBox(16, title, b30, b10, b3, back);
        box.setAlignment(Pos.CENTER);
        StackPane root = new StackPane(box);
        root.setStyle("-fx-background-color: #2b2b2b;");
        primaryStage.setScene(new Scene(root, WINDOW_W, WINDOW_H));
    }

    //  Settings scene ─

    private void showSettingsScene() {
        Text title = new Text("Settings");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 44));
        title.setFill(Color.WHITE);

        Button customizeBtn = mainButton("Customize Board", 240);
        Button backBtn      = grayButton("← Back",          240);
        customizeBtn.setOnAction(e -> showBoardEditorScene());
        backBtn     .setOnAction(e -> showMenuScene());

        VBox box = new VBox(16, title, customizeBtn, backBtn);
        box.setAlignment(Pos.CENTER);
        StackPane root = new StackPane(box);
        root.setStyle("-fx-background-color: #2b2b2b;");
        primaryStage.setScene(new Scene(root, WINDOW_W, WINDOW_H));
    }

    //  Board editor (Settings) ─

    private void showBoardEditorScene() {
        initEditor();

        Pane main = new Pane();
        main.setStyle("-fx-background-color: #505050;");

        // Board
        editorBoardPane = new Pane();
        Rectangle bg = new Rectangle(MARGIN, MARGIN, BOARD_PX, BOARD_PX);
        bg.setFill(Color.web("#808080"));
        main.getChildren().addAll(bg, editorBoardPane);
        editorBoardPane.setLayoutX(MARGIN);
        editorBoardPane.setLayoutY(MARGIN);

        // Sidebar
        VBox sidebar = buildEditorSidebar();
        sidebar.setLayoutX(MARGIN + BOARD_PX + MARGIN);
        sidebar.setLayoutY(MARGIN);
        sidebar.setPrefWidth(SIDE_PANE_W);
        sidebar.setPrefHeight(BOARD_PX);
        main.getChildren().add(sidebar);

        renderEditorBoard();
        primaryStage.setScene(new Scene(main, WINDOW_W, WINDOW_H));
    }

    private void initEditor() {
        editorState = new GameState();
        for (int r = 0; r < 8; r++) for (int c = 0; c < 8; c++) editorGrid[r][c] = null;
        editorSelectedPack  = null;
        editorSelectedPiece = null;
        editorSelectedColor = com.chess.logic.types.Color.WHITE;

        List<BoardPlacement> layout =
                (customBoardLayout != null) ? customBoardLayout : buildDefaultLayout();
        for (BoardPlacement bp : layout) {
            try {
                Piece p = registry.instantiatePiece(new PiecePath(bp.packName(), bp.pieceName()));
                editorState.place(p, new PieceState(bp.isKing(), bp.color(), new Position(bp.row(), bp.col())));
                editorGrid[bp.row()][bp.col()] = bp;
            } catch (Exception e) {
                System.out.println("[editor] skip " + bp.pieceName() + ": " + e.getMessage());
            }
        }
    }

    private VBox buildEditorSidebar() {
        Text title = new Text("Board Editor");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        title.setFill(Color.WHITE);

        // Status / selection indicator
        editorStatusLabel = new Label("Click a piece to select, then click board");
        editorStatusLabel.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 11; -fx-wrap-text: true;");
        editorStatusLabel.setMaxWidth(SIDE_PANE_W - 20);

        // Deselect button (enters erase mode)
        Button deselectBtn = sideButton("✕ Deselect (erase mode)");
        deselectBtn.setMaxWidth(Double.MAX_VALUE);
        deselectBtn.setOnAction(e -> {
            editorSelectedPack  = null;
            editorSelectedPiece = null;
            editorStatusLabel.setText("Click board squares to erase pieces");
            rebuildEditorPalette();
        });

        // Palette
        editorPaletteBox = new VBox(4);
        buildEditorPalette(editorPaletteBox);

        ScrollPane scroll = new ScrollPane(editorPaletteBox);
        scroll.setStyle("-fx-background: #2b2b2b; -fx-background-color: #2b2b2b;" +
                "-fx-border-color: transparent;");
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        // Bottom controls
        Button resetBtn  = sideButton("↺ Reset to Classic");
        Button applyBtn  = mainButton("Apply & Back", SIDE_PANE_W - 20);
        Button cancelBtn = grayButton("Cancel",        SIDE_PANE_W - 20);
        resetBtn .setMaxWidth(Double.MAX_VALUE);
        applyBtn .setMaxWidth(Double.MAX_VALUE);
        cancelBtn.setMaxWidth(Double.MAX_VALUE);

        resetBtn.setOnAction(e -> {
            initEditorFromLayout(buildDefaultLayout());
            renderEditorBoard();
        });
        applyBtn.setOnAction(e -> {
            saveEditorLayout();
            showMenuScene();
        });
        cancelBtn.setOnAction(e -> showMenuScene());

        VBox sidebar = new VBox(6,
                title, editorStatusLabel, deselectBtn,
                scroll,
                resetBtn, applyBtn, cancelBtn);
        sidebar.setStyle("-fx-background-color: #3a3a3a;" +
                "-fx-border-color: #222; -fx-border-width: 1; -fx-padding: 10;");
        return sidebar;
    }

    private void initEditorFromLayout(List<BoardPlacement> layout) {
        // Clear editor state
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                if (editorGrid[r][c] != null) {
                    Position pos = new Position(r, c);
                    if (editorState.getSquare(pos) != null)
                        try { editorState.capture(pos); } catch (Exception ignored) {}
                    editorGrid[r][c] = null;
                }
            }
        }
        for (BoardPlacement bp : layout) {
            try {
                Piece p = registry.instantiatePiece(new PiecePath(bp.packName(), bp.pieceName()));
                editorState.place(p, new PieceState(bp.isKing(), bp.color(), new Position(bp.row(), bp.col())));
                editorGrid[bp.row()][bp.col()] = bp;
            } catch (Exception e) {
                System.out.println("[editor] skip " + bp.pieceName() + ": " + e.getMessage());
            }
        }
    }

    private void buildEditorPalette(VBox container) {
        container.getChildren().clear();
        Map<String, List<String>> packs = registry.listRegisteredPieces();

        for (var entry : packs.entrySet()) {
            String packName = entry.getKey();

            // Pack header
            Label header = new Label(packName.toUpperCase());
            header.setStyle("-fx-text-fill: #888888; -fx-font-size: 10; -fx-font-weight: bold;" +
                    "-fx-padding: 6 0 2 2;");
            container.getChildren().add(header);

            // One row per piece: [white icon] [piece name] [black icon]
            for (String pieceName : entry.getValue()) {
                StackPane whiteCell = makePaletteCell(packName, pieceName,
                        com.chess.logic.types.Color.WHITE);
                StackPane blackCell = makePaletteCell(packName, pieceName,
                        com.chess.logic.types.Color.BLACK);

                Label nameLabel = new Label(pieceName);
                nameLabel.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 11;");
                nameLabel.setMinWidth(70);
                nameLabel.setAlignment(Pos.CENTER);

                HBox row = new HBox(4, whiteCell, nameLabel, blackCell);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setStyle("-fx-padding: 2 4;");
                container.getChildren().add(row);
            }
        }
    }

    private StackPane makePaletteCell(String packName, String pieceName,
            com.chess.logic.types.Color color) {
        StackPane cell = new StackPane();
        cell.setPrefSize(52, 52);

        Image img = loadPieceImage(packName, pieceName, color);
        if (img != null) {
            ImageView iv = new ImageView(img);
            iv.setFitWidth(46);
            iv.setFitHeight(46);
            iv.setSmooth(true);
            cell.getChildren().add(iv);
        } else {
            Text t = new Text("?");
            t.setFill(Color.LIGHTGRAY);
            cell.getChildren().add(t);
        }

        applyPaletteCellStyle(cell, packName, pieceName, color);

        cell.setOnMouseClicked(e -> {
            boolean alreadySelected = packName.equals(editorSelectedPack)
                    && pieceName.equals(editorSelectedPiece)
                    && color == editorSelectedColor;
            if (alreadySelected) {
                editorSelectedPack  = null;
                editorSelectedPiece = null;
                editorStatusLabel.setText("Click board squares to erase pieces");
            } else {
                editorSelectedPack  = packName;
                editorSelectedPiece = pieceName;
                editorSelectedColor = color;
                String colorName = (color == com.chess.logic.types.Color.WHITE) ? "White" : "Black";
                editorStatusLabel.setText("Placing: " + colorName + " " + pieceName
                        + " (" + packName + ")");
            }
            rebuildEditorPalette();
        });
        return cell;
    }

    private void applyPaletteCellStyle(StackPane cell, String packName,
            String pieceName, com.chess.logic.types.Color color) {
        boolean sel = packName.equals(editorSelectedPack)
                && pieceName.equals(editorSelectedPiece)
                && color == editorSelectedColor;
        cell.setStyle(sel
                ? "-fx-background-color: #3a3a3a; -fx-border-color: #f0c040; " +
                  "-fx-border-width: 2; -fx-cursor: hand;"
                : "-fx-background-color: #2b2b2b; -fx-border-color: #444; " +
                  "-fx-border-width: 1; -fx-cursor: hand;");
    }

    /** Rebuilds the palette VBox to refresh selection highlights. */
    private void rebuildEditorPalette() {
        buildEditorPalette(editorPaletteBox);
    }

    private void renderEditorBoard() {
        if (editorBoardPane == null) return;
        editorBoardPane.getChildren().clear();

        ImageView bg = new ImageView(new Image(Objects.requireNonNull(
                getClass().getResourceAsStream("/Chess_Board.png"))));
        bg.setFitWidth(BOARD_PX);
        bg.setFitHeight(BOARD_PX);
        bg.setPreserveRatio(false);
        editorBoardPane.getChildren().add(bg);

        // Click squares
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                int r = row, c = col;
                Rectangle sq = new Rectangle(
                        col * SQUARE_SIZE, row * SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE);
                sq.setFill(Color.TRANSPARENT);
                sq.setOnMouseClicked(ev -> editorClickSquare(r, c));
                editorBoardPane.getChildren().add(sq);
            }
        }

        // Pieces
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                BoardPiece bp = editorState.getSquare(new Position(row, col));
                if (bp != null) addPieceToPane(editorBoardPane, bp, row, col, false);
            }
        }
    }

    private void editorClickSquare(int row, int col) {
        Position pos = new Position(row, col);
        BoardPiece existing = editorState.getSquare(pos);

        if (editorSelectedPack != null) {
            // Remove existing piece first
            if (existing != null)
                try { editorState.capture(pos); } catch (Exception ignored) {}
            // Place the selected piece
            try {
                boolean isKing = editorSelectedPiece.equals("king");
                Piece p = registry.instantiatePiece(
                        new PiecePath(editorSelectedPack, editorSelectedPiece));
                editorState.place(p, new PieceState(isKing, editorSelectedColor, pos));
                editorGrid[row][col] = new BoardPlacement(editorSelectedPack,
                        editorSelectedPiece, editorSelectedColor, row, col, isKing);
            } catch (Exception e) {
                System.out.println("[editor] place failed: " + e.getMessage());
            }
        } else {
            // Erase mode: remove piece if present
            if (existing != null) {
                try { editorState.capture(pos); } catch (Exception ignored) {}
                editorGrid[row][col] = null;
            }
        }
        renderEditorBoard();
    }

    private void saveEditorLayout() {
        customBoardLayout = new ArrayList<>();
        for (int r = 0; r < 8; r++)
            for (int c = 0; c < 8; c++)
                if (editorGrid[r][c] != null)
                    customBoardLayout.add(editorGrid[r][c]);
    }

    //  Game scene 

    private void startGame(int timeSeconds) {
        logic = new GameState();
        setupBoard(logic);
        moveHistory.clear();
        viewIndex = 0;
        selectedPosition = null;
        validMoves       = null;
        whiteTimeSeconds = timeSeconds;
        blackTimeSeconds = timeSeconds;
        if (gameTimer != null) gameTimer.stop();

        Pane main = new Pane();
        main.setStyle("-fx-background-color: #505050;");

        boardPane = new Pane();
        Rectangle bgRect = new Rectangle(MARGIN, MARGIN, BOARD_PX, BOARD_PX);
        bgRect.setFill(Color.web("#808080"));
        main.getChildren().addAll(bgRect, boardPane);
        boardPane.setLayoutX(MARGIN);
        boardPane.setLayoutY(MARGIN);

        VBox sidebar = buildGameSidebar();
        sidebar.setLayoutX(MARGIN + BOARD_PX + MARGIN);
        sidebar.setLayoutY(MARGIN);
        sidebar.setPrefWidth(SIDE_PANE_W);
        sidebar.setPrefHeight(BOARD_PX);
        main.getChildren().add(sidebar);

        renderBoard(boardPane, logic);
        primaryStage.setScene(new Scene(main, WINDOW_W, WINDOW_H));
        startGameTimer();
    }

    private VBox buildGameSidebar() {
        turnLabel = new Label("⬜  White to move");
        turnLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14; -fx-font-weight: bold;");

        blackTimerLabel = new Label("⬛  " + formatTime(blackTimeSeconds));
        whiteTimerLabel = new Label("⬜  " + formatTime(whiteTimeSeconds));
        blackTimerLabel.setMaxWidth(Double.MAX_VALUE);
        whiteTimerLabel.setMaxWidth(Double.MAX_VALUE);
        applyTimerStyle(whiteTimerLabel, true);
        applyTimerStyle(blackTimerLabel, false);

        Label historyHeader = new Label("Move History");
        historyHeader.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 12; " +
                "-fx-font-weight: bold; -fx-padding: 6 0 2 0;");

        historyListBox = new VBox(2);
        historyListBox.setStyle("-fx-padding: 2 0;");

        ScrollPane scroll = new ScrollPane(historyListBox);
        scroll.setStyle("-fx-background: #2b2b2b; -fx-background-color: #2b2b2b;" +
                "-fx-border-color: transparent;");
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        Button backBtn = sideButton("◀ Back");
        Button fwdBtn  = sideButton("Forward ▶");
        backBtn.setOnAction(e -> navigateHistory(-1));
        fwdBtn .setOnAction(e -> navigateHistory(+1));
        HBox nav = new HBox(6, backBtn, fwdBtn);
        nav.setAlignment(Pos.CENTER);

        VBox sidebar = new VBox(8,
                turnLabel, blackTimerLabel, whiteTimerLabel,
                historyHeader, scroll, nav);
        sidebar.setStyle("-fx-background-color: #3a3a3a;" +
                "-fx-border-color: #222; -fx-border-width: 1; -fx-padding: 10;");
        return sidebar;
    }

    //  Timer ─

    private void startGameTimer() {
        gameTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> tickTimer()));
        gameTimer.setCycleCount(Timeline.INDEFINITE);
        gameTimer.play();
    }

    private void tickTimer() {
        boolean whiteTurn = logic.turnPlayer() == com.chess.logic.types.Color.WHITE;
        if (whiteTurn) {
            if (--whiteTimeSeconds <= 0) { whiteTimeSeconds = 0; gameTimer.stop(); onTimeout(false); return; }
        } else {
            if (--blackTimeSeconds <= 0) { blackTimeSeconds = 0; gameTimer.stop(); onTimeout(true);  return; }
        }
        refreshTimerUI();
    }

    private void onTimeout(boolean whiteWins) {
        refreshTimerUI();
        turnLabel.setText(whiteWins ? "⬜  White wins on time!" : "⬛  Black wins on time!");
        turnLabel.setStyle("-fx-text-fill: #f0c040; -fx-font-size: 13; -fx-font-weight: bold;");
    }

    private void refreshTimerUI() {
        boolean whiteTurn = logic.turnPlayer() == com.chess.logic.types.Color.WHITE;
        applyTimerStyle(whiteTimerLabel, whiteTurn);
        applyTimerStyle(blackTimerLabel, !whiteTurn);
        whiteTimerLabel.setText("⬜  " + formatTime(whiteTimeSeconds));
        blackTimerLabel.setText("⬛  " + formatTime(blackTimeSeconds));
        if (gameTimer != null && gameTimer.getStatus() == Timeline.Status.RUNNING) {
            turnLabel.setText(whiteTurn ? "⬜  White to move" : "⬛  Black to move");
            turnLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14; -fx-font-weight: bold;");
        }
    }

    private void applyTimerStyle(Label lbl, boolean active) {
        String base = "-fx-font-size: 18; -fx-font-weight: bold;" +
                "-fx-padding: 6 10; -fx-background-radius: 0;";
        lbl.setStyle(active
                ? "-fx-text-fill: white;   -fx-background-color: #505050;" + base
                : "-fx-text-fill: #777777; -fx-background-color: #2b2b2b;" + base);
    }

    private String formatTime(int secs) {
        return String.format("%02d:%02d", secs / 60, secs % 60);
    }

    //  Move history 

    private void navigateHistory(int delta) {
        int next = viewIndex + delta;
        if (next < 0 || next > moveHistory.size()) return;
        viewIndex = next;
        selectedPosition = null;
        validMoves       = null;
        updateHistoryList();
        redrawBoard();
    }

    private void updateHistoryList() {
        historyListBox.getChildren().clear();
        for (int i = 0; i < moveHistory.size(); i++) {
            boolean isWhite = (i % 2 == 0);
            String prefix = isWhite ? (i / 2 + 1) + ".  " : "      ";
            String text   = prefix + formatMove(moveHistory.get(i));
            boolean current = (i == viewIndex - 1);

            Label lbl = new Label(text);
            lbl.setMaxWidth(Double.MAX_VALUE);
            lbl.setStyle(current
                    ? "-fx-text-fill: #1a1a1a; -fx-font-size: 13;" +
                      "-fx-background-color: #f0c040; -fx-padding: 3 6; -fx-background-radius: 0;"
                    : "-fx-text-fill: #cccccc; -fx-font-size: 13; -fx-padding: 3 6;");

            final int jumpTo = i + 1;
            lbl.setOnMouseClicked(e -> {
                viewIndex = jumpTo;
                selectedPosition = null;
                validMoves       = null;
                updateHistoryList();
                redrawBoard();
            });
            historyListBox.getChildren().add(lbl);
        }
    }

    private String formatMove(MoveRecord r) {
        String base = toAlgebraic(r.from) + " → " + toAlgebraic(r.to);
        return (r.promotionPiece != null) ? base + "=" + r.promotionPiece : base;
    }

    private String toAlgebraic(Position p) {
        return "" + (char) ('a' + p.col()) + (8 - p.row());
    }

    //  Board rendering ─

    private void renderBoard(Pane pane, GameState state) {
        ImageView bg = new ImageView(new Image(Objects.requireNonNull(
                getClass().getResourceAsStream("/Chess_Board.png"))));
        bg.setFitWidth(BOARD_PX);
        bg.setFitHeight(BOARD_PX);
        bg.setPreserveRatio(false);
        pane.getChildren().add(bg);

        boolean inReplay = viewIndex < moveHistory.size();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                int r = row, c = col;
                Rectangle sq = new Rectangle(
                        col * SQUARE_SIZE, row * SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE);
                sq.setFill(Color.TRANSPARENT);
                if (!inReplay) sq.setOnMouseClicked(ev -> handleSquareClick(r, c));
                pane.getChildren().add(sq);
            }
        }

        if (!inReplay && selectedPosition != null && validMoves != null)
            renderMoveHighlights(pane, validMoves);

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                BoardPiece piece = state.getSquare(new Position(row, col));
                if (piece != null) {
                    boolean dimmed = selectedPosition != null
                            && selectedPosition.row() == row
                            && selectedPosition.col() == col;
                    addPieceToPane(pane, piece, row, col, dimmed);
                }
            }
        }
    }

    private void renderMoveHighlights(Pane pane, String[][] moves) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if (moves[row][col] == null) continue;
                double x = col * SQUARE_SIZE, y = row * SQUARE_SIZE;

                if (moves[row][col].equals("attack.png")) {
                    Rectangle ring = new Rectangle(x, y, SQUARE_SIZE, SQUARE_SIZE);
                    ring.setFill(Color.TRANSPARENT);
                    ring.setStroke(Color.color(0.85, 0.1, 0.1, 0.8));
                    ring.setStrokeWidth(5);
                    ring.setMouseTransparent(true);
                    pane.getChildren().add(ring);
                } else {
                    Circle dot = new Circle(
                            x + SQUARE_SIZE / 2.0, y + SQUARE_SIZE / 2.0, SQUARE_SIZE / 4.5);
                    dot.setFill(Color.color(0.1, 0.1, 0.1, 0.35));
                    dot.setMouseTransparent(true);
                    pane.getChildren().add(dot);
                }
            }
        }
    }

    private void addPieceToPane(Pane pane, BoardPiece piece, int row, int col, boolean dim) {
        InputStream stream = piece.iconStream();
        if (stream == null) {
            System.out.println("Missing icon: " + piece.icon());
            return;
        }
        ImageView iv = new ImageView(new Image(stream));
        iv.setX(col * SQUARE_SIZE);
        iv.setY(row * SQUARE_SIZE);
        iv.setFitWidth(SQUARE_SIZE);
        iv.setFitHeight(SQUARE_SIZE);
        iv.setSmooth(true);
        if (dim) iv.setOpacity(0.7);
        iv.setMouseTransparent(true);
        pane.getChildren().add(iv);
    }

    // Click handling

    private void handleSquareClick(int row, int col) {
        Position      clicked = new Position(row, col);
        BoardPiece clickedPiece = logic.getSquare(clicked);

        if (selectedPosition != null) {
            if (validMoves != null && validMoves[row][col] != null) {
                // Record move color before commandMove passes control
                com.chess.logic.types.Color movingColor = logic.turnPlayer();

                try {
                    logic.commandMove(selectedPosition, clicked);
                    MoveRecord rec = new MoveRecord(selectedPosition, clicked);
                    moveHistory.add(rec);
                    viewIndex = moveHistory.size();
                    updateHistoryList();
                    refreshTimerUI();

                    // Check for pawn promotion
                    BoardPiece moved = logic.getSquare(clicked);
                    if (moved != null && moved.icon().contains("pawn")) {
                        int backRank = (movingColor == com.chess.logic.types.Color.WHITE) ? 0 : 7;
                        if (clicked.row() == backRank) {
                            selectedPosition = null;
                            validMoves       = null;
                            redrawBoard();
                            showPromotionOverlay(clicked, movingColor, rec);
                            return;
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Move failed: " + e.getMessage());
                }
                selectedPosition = null;
                validMoves       = null;

            } else if (clickedPiece != null && clickedPiece.color() == logic.turnPlayer()) {
                selectedPosition = clicked;
                validMoves       = computeValidMoves(clicked, clickedPiece);
            } else {
                selectedPosition = null;
                validMoves       = null;
            }
        } else {
            if (clickedPiece != null && clickedPiece.color() == logic.turnPlayer()) {
                selectedPosition = clicked;
                validMoves       = computeValidMoves(clicked, clickedPiece);
            }
        }

        redrawBoard();
    }

    private String[][] computeValidMoves(Position pos, BoardPiece piece) {
        try {
            return piece.getMovableSquares(logic);
        } catch (Exception e) {
            System.out.println("Could not compute moves for " + pos + ": " + e.getMessage());
            return null;
        }
    }

    private void redrawBoard() {
        if (boardPane == null) return;
        boardPane.getChildren().clear();
        GameState display = (viewIndex < moveHistory.size())
                ? buildStateAtIndex(viewIndex) : logic;
        renderBoard(boardPane, display);
    }

    private GameState buildStateAtIndex(int idx) {
        GameState state = new GameState();
        setupBoard(state);
        for (int i = 0; i < idx; i++) {
            MoveRecord r = moveHistory.get(i);
            try { state.commandMove(r.from, r.to); } catch (Exception ignored) { break; }
            if (r.promotionPiece != null) {
                try {
                    com.chess.logic.types.Color c = (i % 2 == 0)
                            ? com.chess.logic.types.Color.WHITE
                            : com.chess.logic.types.Color.BLACK;
                    state.capture(r.to);
                    Piece promoted = registry.instantiatePiece(
                            new PiecePath(r.promotionPack, r.promotionPiece));
                    state.place(promoted, new PieceState(false, c, r.to));
                } catch (Exception ignored) {}
            }
        }
        return state;
    }

    //  Pawn promotion 

    private void showPromotionOverlay(Position pos,
            com.chess.logic.types.Color color, MoveRecord rec) {
        if (gameTimer != null) gameTimer.stop();

        String[] choices = {"queen", "rook", "bishop", "knight"};

        Rectangle dim = new Rectangle(0, 0, BOARD_PX, BOARD_PX);
        dim.setFill(Color.color(0, 0, 0, 0.72));

        Text title = new Text("Promote pawn");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        title.setFill(Color.WHITE);

        HBox choiceRow = new HBox(10);
        choiceRow.setAlignment(Pos.CENTER);

        for (String choice : choices) {
            Image img = loadPieceImage("base", choice, color);
            StackPane cell = new StackPane();
            cell.setPrefSize(90, 90);
            if (img != null) {
                ImageView iv = new ImageView(img);
                iv.setFitWidth(80);
                iv.setFitHeight(80);
                iv.setSmooth(true);
                cell.getChildren().add(iv);
            } else {
                cell.getChildren().add(new Text(choice));
            }
            cell.setStyle("-fx-background-color: #3a3a3a; -fx-border-color: #555;" +
                    "-fx-border-width: 2; -fx-cursor: hand;");
            cell.setOnMouseEntered(e ->
                    cell.setStyle("-fx-background-color: #555555; -fx-border-color: #f0c040;" +
                            "-fx-border-width: 2; -fx-cursor: hand;"));
            cell.setOnMouseExited(e ->
                    cell.setStyle("-fx-background-color: #3a3a3a; -fx-border-color: #555;" +
                            "-fx-border-width: 2; -fx-cursor: hand;"));
            final String chosen = choice;
            cell.setOnMouseClicked(e -> applyPromotion(pos, color, "base", chosen, rec));
            choiceRow.getChildren().add(cell);
        }

        VBox content = new VBox(16, title, choiceRow);
        content.setAlignment(Pos.CENTER);
        content.setStyle("-fx-background-color: #2b2b2b; -fx-padding: 28;" +
                "-fx-border-color: #666; -fx-border-width: 2;");
        content.setMaxWidth(440);
        content.setMaxHeight(200);

        StackPane overlay = new StackPane(dim, content);
        overlay.setPrefSize(BOARD_PX, BOARD_PX);
        overlay.setId("promotionOverlay");
        boardPane.getChildren().add(overlay);
    }

    private void applyPromotion(Position pos, com.chess.logic.types.Color color,
            String packName, String pieceName, MoveRecord rec) {
        boardPane.getChildren().removeIf(n -> "promotionOverlay".equals(n.getId()));
        try {
            logic.capture(pos);
            Piece promoted = registry.instantiatePiece(new PiecePath(packName, pieceName));
            logic.place(promoted, new PieceState(false, color, pos));
            rec.promotionPack  = packName;
            rec.promotionPiece = pieceName;
            updateHistoryList();
        } catch (Exception e) {
            System.out.println("Promotion failed: " + e.getMessage());
        }
        if (gameTimer != null) gameTimer.play();
        redrawBoard();
        refreshTimerUI();
    }

    //  Board setup ─

    private void setupBoard(GameState state) {
        List<BoardPlacement> layout =
                (customBoardLayout != null) ? customBoardLayout : buildDefaultLayout();
        for (BoardPlacement bp : layout) {
            try {
                Piece p = registry.instantiatePiece(new PiecePath(bp.packName(), bp.pieceName()));
                state.place(p, new PieceState(bp.isKing(), bp.color(),
                        new Position(bp.row(), bp.col())));
            } catch (Exception e) {
                System.out.println("[setup] skip " + bp.packName() + ":" +
                        bp.pieceName() + " — " + e.getMessage());
            }
        }
    }

    private List<BoardPlacement> buildDefaultLayout() {
        List<BoardPlacement> layout = new ArrayList<>();
        String[] back = {"rook","knight","bishop","queen","king","bishop","knight","rook"};
        for (int col = 0; col < 8; col++) {
            boolean king = back[col].equals("king");
            layout.add(new BoardPlacement("base", back[col],
                    com.chess.logic.types.Color.BLACK, 0, col, king));
            layout.add(new BoardPlacement("base", "pawn",
                    com.chess.logic.types.Color.BLACK, 1, col, false));
            layout.add(new BoardPlacement("base", "pawn",
                    com.chess.logic.types.Color.WHITE, 6, col, false));
            layout.add(new BoardPlacement("base", back[col],
                    com.chess.logic.types.Color.WHITE, 7, col, king));
        }
//        // FIXME demo Vikings — remove or customise via the board editor
//        layout.add(new BoardPlacement("viking", "viking",
//                com.chess.logic.types.Color.BLACK, 2, 3, false));
//        layout.add(new BoardPlacement("viking", "viking",
//                com.chess.logic.types.Color.WHITE, 5, 3, false));
        return layout;
    }

    /** Loads a piece image via a temporary GameState (so the piece's own classloader is used). */
    private Image loadPieceImage(String packName, String pieceName,
            com.chess.logic.types.Color color) {
        try {
            GameState dummy = new GameState();
            Piece p = registry.instantiatePiece(new PiecePath(packName, pieceName));
            dummy.place(p, new PieceState(false, color, new Position(4, 4)));
            BoardPiece bp = dummy.getSquare(new Position(4, 4));
            InputStream stream = bp.iconStream();
            if (stream != null) return new Image(stream);
        } catch (Exception e) {
            System.out.println("loadPieceImage: " + pieceName + ": " + e.getMessage());
        }
        return null;
    }

    //  UI helpers 

    private String mainButtonCss(double width) {
        return "-fx-background-color: #4a90d9; -fx-text-fill: white;" +
                "-fx-font-size: 15; -fx-font-weight: bold;" +
                "-fx-min-width: " + width + ";" +
                "-fx-padding: 12 24; -fx-background-radius: 0; -fx-cursor: hand;";
    }

    private Button mainButton(String text, double width) {
        Button b = new Button(text);
        b.setStyle(mainButtonCss(width));
        return b;
    }

    private Button grayButton(String text, double width) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color: #555555; -fx-text-fill: white;" +
                "-fx-font-size: 15; -fx-font-weight: bold;" +
                "-fx-min-width: " + width + ";" +
                "-fx-padding: 12 24; -fx-background-radius: 0; -fx-cursor: hand;");
        return b;
    }

    private Button sideButton(String text) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color: #4a4a4a; -fx-text-fill: #cccccc;" +
                "-fx-font-size: 12; -fx-padding: 5 10;" +
                "-fx-background-radius: 0; -fx-cursor: hand;");
        return b;
    }

    public static void main(String[] args) { launch(); }
}
