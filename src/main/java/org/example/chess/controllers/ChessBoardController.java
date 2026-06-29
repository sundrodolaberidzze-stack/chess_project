package org.example.chess.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.example.chess.Main;
import org.example.chess.SoundManager;
import org.example.chess.SettingsState;
import org.example.chess.database.DatabaseManager;
import org.example.chess.engine.StockfishAI;
import org.example.chess.models.Board;
import org.example.chess.models.Move;
import org.example.chess.models.Piece;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ChessBoardController {
    @FXML private GridPane chessGrid;
    @FXML private CheckBox toggleMovesCheckBox;
    @FXML private Label timerLabel;
    @FXML private Label statusLabel;
    @FXML private Button playAgainButton;
    @FXML private Button mainMenuButton;
    @FXML private ToggleButton soundToggleButton;
    @FXML private Label blackLabel;
    @FXML private Label whiteLabel;

    private Board board;
    private int aiDifficulty;
    private Piece.Color playerColor;
    private int secondsRemaining;
    private Timer gameTimer;
    private StockfishAI aiEngine;
    private boolean gameOver;
    private boolean aiThinking;
    private boolean localMode;
    private boolean lowTimePlayed;
    private int selectedX = -1;
    private int selectedY = -1;
    private Move queuedPremove;
    private int premoveSelectedX = -1;
    private int premoveSelectedY = -1;
    private final StackPane[][] squarePanes = new StackPane[8][8];
    private final List<Circle> hintDots = new ArrayList<>();

    public void initGameData(int difficulty, String color, int minutes) {
        initGameData(difficulty, color, minutes, false);
    }

    public void initGameData(int difficulty, String color, int minutes, boolean localMode) {
        board = new Board();
        aiDifficulty = difficulty;
        playerColor = Piece.Color.valueOf(color);
        this.localMode = localMode;
        secondsRemaining = minutes * 60;
        if (!localMode) {
            aiEngine = new StockfishAI();
            aiEngine.startEngine();
        }
        buildGraphicalBoard();
        refreshBoardUI();
        startClock();
        updatePlayerLabels();
        updateSoundToggleText();
        updateStatus();
        Main.setCloseHandler(this::saveResignationOnClose);
        SoundManager.notifySound();
        if (!localMode && playerColor == Piece.Color.BLACK) makeAIMove();
    }

    private void buildGraphicalBoard() {
        chessGrid.getChildren().clear();
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                StackPane pane = new StackPane();
                pane.setPrefSize(75, 75);
                int bx = boardX(x);
                int by = boardY(y);
                final int fx = x;
                final int fy = y;
                pane.setOnMouseClicked(e -> handleSquareClick(fx, fy));
                pane.setOnDragOver(e -> {
                    if (e.getDragboard().hasString()) e.acceptTransferModes(TransferMode.MOVE);
                    e.consume();
                });
                pane.setOnDragDropped(e -> {
                    Dragboard db = e.getDragboard();
                    boolean success = false;
                    if (db.hasString()) {
                        String[] parts = db.getString().split(",");
                        success = tryPlayerMove(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), fx, fy);
                    }
                    e.setDropCompleted(success);
                    e.consume();
                });
                squarePanes[x][y] = pane;
                chessGrid.add(pane, bx, by);
            }
        }
    }

    private void updateSquareUI(int x, int y) {
        StackPane pane = squarePanes[x][y];
        pane.getChildren().clear();
        String baseColor = (x + y) % 2 == 0 ? SettingsState.lightSquareColor() : SettingsState.darkSquareColor();
        int[] whiteKing = board.getKingPosition(Piece.Color.WHITE);
        int[] blackKing = board.getKingPosition(Piece.Color.BLACK);
        if (whiteKing != null && whiteKing[0] == x && whiteKing[1] == y && board.isInCheck(Piece.Color.WHITE)) baseColor = "#d62828";
        if (blackKing != null && blackKing[0] == x && blackKing[1] == y && board.isInCheck(Piece.Color.BLACK)) baseColor = "#d62828";
        if (x == selectedX && y == selectedY) baseColor = "#f6f669";
        if (x == premoveSelectedX && y == premoveSelectedY) baseColor = "#ffb347";
        pane.setStyle("-fx-background-color: " + baseColor + ";");
        Piece p = board.getPiece(x, y);
        if (p == null) return;
        String color = p.getColor() == Piece.Color.WHITE ? "w" : "b";
        String type = p.getType().name().toLowerCase();
        String path = "/ChessPieces/" + color + "_" + type + "_1x_ns.png";
        Image img = new Image(getClass().getResourceAsStream(path));
        ImageView view = new ImageView(img);
        view.setFitWidth(65);
        view.setFitHeight(65);
        view.setPreserveRatio(true);
        setupDragAndDrop(view, x, y);
        pane.getChildren().add(view);
    }

    private void setupDragAndDrop(ImageView view, int srcX, int srcY) {
        view.setOnDragDetected(event -> {
            if (gameOver || (!localMode && aiThinking && !SettingsState.isPremovesEnabled()) || (!localMode && !aiThinking && board.getTurn() != playerColor)) return;
            Piece p = board.getPiece(srcX, srcY);
            if (p != null && ((localMode && p.getColor() == board.getTurn()) || (!localMode && p.getColor() == playerColor))) {
                Dragboard db = view.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                content.putString(srcX + "," + srcY);
                db.setContent(content);
                selectedX = srcX;
                selectedY = srcY;
                refreshBoardUI();
                if (toggleMovesCheckBox.isSelected()) showMoveHints(srcX, srcY);
                event.consume();
            }
        });
        view.setOnDragDone(event -> {
            clearHints();
            selectedX = -1;
            selectedY = -1;
            refreshBoardUI();
            event.consume();
        });
    }

    private void handleSquareClick(int x, int y) {
        if (gameOver) return;
        if (!localMode && aiThinking) {
            handlePremoveClick(x, y);
            return;
        }
        if (!localMode && board.getTurn() != playerColor) return;
        Piece p = board.getPiece(x, y);
        if (selectedX == -1) {
            if (p != null && ((localMode && p.getColor() == board.getTurn()) || (!localMode && p.getColor() == playerColor))) selectSquare(x, y);
            else SoundManager.error();
            return;
        }
        if (selectedX == x && selectedY == y) {
            selectedX = -1;
            selectedY = -1;
            clearHints();
            refreshBoardUI();
            return;
        }
        if (p != null && ((localMode && p.getColor() == board.getTurn()) || (!localMode && p.getColor() == playerColor))) {
            selectSquare(x, y);
            return;
        }
        if (!tryPlayerMove(selectedX, selectedY, x, y)) SoundManager.error();
    }


    private void handlePremoveClick(int x, int y) {
        if (!SettingsState.isPremovesEnabled()) return;
        Piece p = board.getPiece(x, y);
        if (premoveSelectedX == -1) {
            if (p != null && p.getColor() == playerColor) {
                premoveSelectedX = x;
                premoveSelectedY = y;
                selectedX = x;
                selectedY = y;
                SoundManager.select();
                refreshBoardUI();
            } else {
                SoundManager.error();
            }
            return;
        }
        if (premoveSelectedX == x && premoveSelectedY == y) {
            clearQueuedPremove();
            refreshBoardUI();
            return;
        }
        queuePremove(premoveSelectedX, premoveSelectedY, x, y);
    }

    private boolean queuePremove(int fx, int fy, int tx, int ty) {
        if (!SettingsState.isPremovesEnabled()) return false;
        Piece p = board.getPiece(fx, fy);
        if (p == null || p.getColor() != playerColor) {
            SoundManager.error();
            return false;
        }
        queuedPremove = choosePromotionIfNeeded(fx, fy, tx, ty);
        premoveSelectedX = fx;
        premoveSelectedY = fy;
        selectedX = -1;
        selectedY = -1;
        clearHints();
        statusLabel.setText("Premove ready");
        refreshBoardUI();
        SoundManager.button();
        return true;
    }

    private boolean playQueuedPremove() {
        if (queuedPremove == null || gameOver || localMode || board.getTurn() != playerColor) return false;
        Move move = queuedPremove;
        clearQueuedPremove();
        boolean capture = isCaptureMove(move);
        if (!board.makeMove(move)) {
            SoundManager.error();
            refreshBoardUI();
            updateStatus();
            return false;
        }
        refreshBoardUI();
        playMoveSound(capture);
        if (checkGameEndState()) return true;
        makeAIMove();
        return true;
    }

    private void clearQueuedPremove() {
        queuedPremove = null;
        premoveSelectedX = -1;
        premoveSelectedY = -1;
        selectedX = -1;
        selectedY = -1;
        clearHints();
    }

    private void selectSquare(int x, int y) {
        selectedX = x;
        selectedY = y;
        clearHints();
        refreshBoardUI();
        SoundManager.select();
        if (toggleMovesCheckBox.isSelected()) showMoveHints(x, y);
    }

    private boolean tryPlayerMove(int fx, int fy, int tx, int ty) {
        if (gameOver) return false;
        if (!localMode && aiThinking) return queuePremove(fx, fy, tx, ty);
        if (!localMode && board.getTurn() != playerColor) return false;
        Move move = choosePromotionIfNeeded(fx, fy, tx, ty);
        boolean capture = isCaptureMove(move);
        if (move == null || !board.makeMove(move)) {
            SoundManager.error();
            return false;
        }
        selectedX = -1;
        selectedY = -1;
        clearHints();
        refreshBoardUI();
        playMoveSound(capture);
        if (checkGameEndState()) return true;
        if (!localMode) makeAIMove();
        return true;
    }

    private boolean isCaptureMove(Move move) {
        if (move == null) return false;
        Piece moving = board.getPiece(move.getFromX(), move.getFromY());
        Piece target = board.getPiece(move.getToX(), move.getToY());
        if (target != null) return true;
        return moving != null && moving.getType() == Piece.Type.PAWN && move.getFromX() != move.getToX();
    }

    private void playMoveSound(boolean capture) {
        if (board.isInCheck(board.getTurn())) SoundManager.check();
        else if (capture) SoundManager.capture();
        else SoundManager.move();
    }

    private Move choosePromotionIfNeeded(int fx, int fy, int tx, int ty) {
        Piece p = board.getPiece(fx, fy);
        if (p != null && p.getType() == Piece.Type.PAWN && (ty == 0 || ty == 7)) {
            List<String> choices = Arrays.asList("QUEEN", "ROOK", "BISHOP", "KNIGHT");
            ChoiceDialog<String> dialog = new ChoiceDialog<>("QUEEN", choices);
            dialog.setTitle("Promotion");
            dialog.setHeaderText("Choose promotion piece");
            dialog.setContentText("Piece:");
            String result = dialog.showAndWait().orElse("QUEEN");
            return new Move(fx, fy, tx, ty, Piece.Type.valueOf(result));
        }
        return new Move(fx, fy, tx, ty);
    }

    private void showMoveHints(int fx, int fy) {
        clearHints();
        for (Move m : board.getValidMovesForPiece(fx, fy)) {
            Circle circle = new Circle(8, Color.web("#000000", 0.25));
            squarePanes[m.getToX()][m.getToY()].getChildren().add(circle);
            hintDots.add(circle);
        }
    }

    private void clearHints() {
        for (Circle dot : new ArrayList<>(hintDots)) {
            if (dot.getParent() instanceof StackPane pane) pane.getChildren().remove(dot);
        }
        hintDots.clear();
    }

    private void refreshBoardUI() {
        for (int y = 0; y < 8; y++) for (int x = 0; x < 8; x++) updateSquareUI(x, y);
        if (toggleMovesCheckBox != null && toggleMovesCheckBox.isSelected() && selectedX != -1) showMoveHints(selectedX, selectedY);
    }

    private void makeAIMove() {
        if (gameOver || localMode) return;
        aiThinking = true;
        statusLabel.setText("Stockfish calculating...");
        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(java.util.concurrent.ThreadLocalRandom.current().nextInt(300, 951));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            String bestMoveUci = aiEngine.getBestMove(board.generateFen(), aiDifficulty);
            Move move = bestMoveUci == null ? aiEngine.fallbackMove(board) : Move.fromUCI(bestMoveUci);
            Platform.runLater(() -> {
                boolean capture = isCaptureMove(move);
                if (move != null && board.makeMove(move)) playMoveSound(capture);
                aiThinking = false;
                refreshBoardUI();
                if (!checkGameEndState()) {
                    if (!playQueuedPremove()) updateStatus();
                }
            });
        });
        thread.setDaemon(true);
        thread.start();
    }

    private boolean checkGameEndState() {
        Piece.Color side = board.getTurn();
        if (board.isCheckmate(side)) {
            SoundManager.checkmate();
            endMatch(localMode ? ((side == Piece.Color.WHITE ? "Black" : "White") + " Win") : (side == playerColor ? "AI Win" : "Player Win"));
            return true;
        }
        if (board.isStalemate(side)) {
            SoundManager.draw();
            endMatch("Draw by Stalemate");
            return true;
        }
        if (board.isDrawByFiftyMoveRule()) {
            SoundManager.draw();
            endMatch("Draw by Fifty Move Rule");
            return true;
        }
        updateStatus();
        return false;
    }

    private void updateStatus() {
        if (gameOver) return;
        if (localMode) {
            if (board.isInCheck(board.getTurn())) statusLabel.setText(board.getTurn().name() + " king is in check");
            else statusLabel.setText(board.getTurn().name() + " to move");
        } else if (board.isInCheck(board.getTurn())) statusLabel.setText((board.getTurn() == playerColor ? "Your king" : "AI king") + " is in check");
        else statusLabel.setText(board.getTurn() == playerColor ? "Your Turn" : "AI Turn");
    }

    private void endMatch(String result) {
        if (gameOver) return;
        gameOver = true;
        if (gameTimer != null) gameTimer.cancel();
        if (aiEngine != null) aiEngine.stopEngine();
        Main.setCloseHandler(null);
        statusLabel.setText("Match Over: " + result);
        playAgainButton.setVisible(true);
        if (mainMenuButton != null) mainMenuButton.setVisible(true);
        if (result.toLowerCase().contains("draw")) SoundManager.draw();
        else if (result.toLowerCase().contains("player win")) SoundManager.victory();
        else SoundManager.defeat();
        saveHistory(result);
    }

    private void saveResignationOnClose() {
        if (!gameOver && board != null) endMatch("Resignation Loss");
    }

    @FXML public void handleResign() {
        SoundManager.button();
        endMatch("Resignation Loss");
    }

    private void saveHistory(String result) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement("INSERT INTO history (username, game_name, result, time_played, color) VALUES (?, ?, ?, ?, ?)")) {
            ps.setString(1, Main.getSessionUser() == null ? "Guest" : Main.getSessionUser());
            ps.setString(2, localMode ? "Local Friend Match" : "vs Stockfish Level " + aiDifficulty);
            ps.setString(3, result);
            ps.setString(4, timestamp);
            ps.setString(5, playerColor.name());
            ps.executeUpdate();
        } catch (Exception ignored) {}
    }

    private void startClock() {
        updateTimerLabel();
        gameTimer = new Timer(true);
        gameTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    if (gameOver) return;
                    secondsRemaining--;
                    updateTimerLabel();
                    if (!lowTimePlayed && secondsRemaining == 30) {
                        lowTimePlayed = true;
                        SoundManager.lowTime();
                    }
                    if (secondsRemaining <= 0) endMatch("Timeout Loss");
                });
            }
        }, 1000, 1000);
    }

    private void updateTimerLabel() {
        int mins = Math.max(0, secondsRemaining) / 60;
        int secs = Math.max(0, secondsRemaining) % 60;
        timerLabel.setText(String.format("Time Remaining: %02d:%02d", mins, secs));
    }

    private int boardX(int x) { return playerColor == Piece.Color.WHITE ? x : 7 - x; }
    private int boardY(int y) { return playerColor == Piece.Color.WHITE ? y : 7 - y; }

    @FXML public void handleToggleHints() {
        SoundManager.button();
        clearHints();
        refreshBoardUI();
    }

    private void updatePlayerLabels() {
        if (blackLabel != null) blackLabel.setText(localMode ? "♟ Black" : "♟ Black " + (playerColor == Piece.Color.BLACK ? "(You)" : "(AI)"));
        if (whiteLabel != null) whiteLabel.setText(localMode ? "♙ White" : "♙ White " + (playerColor == Piece.Color.WHITE ? "(You)" : "(AI)"));
    }

    @FXML public void handleToggleFullScreen() {
        SoundManager.button();
        Main.toggleFullScreen();
    }

    @FXML public void handleToggleSound() {
        SoundManager.toggle();
        if (SoundManager.isEnabled()) SoundManager.button();
        updateSoundToggleText();
    }

    private void updateSoundToggleText() {
        if (soundToggleButton != null) {
            soundToggleButton.setSelected(SoundManager.isEnabled());
            soundToggleButton.setText(SoundManager.isEnabled() ? "Sound: ON" : "Sound: OFF");
        }
    }

    @FXML public void handlePlayAgain() {
        SoundManager.button();
        try { Main.showScene("pre_play.fxml"); } catch (Exception ignored) {}
    }

    @FXML public void handleBackToMainMenu() {
        SoundManager.button();
        try { Main.showScene("pre_play.fxml"); } catch (Exception ignored) {}
    }
}
