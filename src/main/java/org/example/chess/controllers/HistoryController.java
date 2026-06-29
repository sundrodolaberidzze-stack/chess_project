package org.example.chess.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Label;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.chess.Main;
import org.example.chess.SoundManager;
import org.example.chess.database.DatabaseManager;
import org.example.chess.models.GameHistory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class HistoryController {
    @FXML private TableView<GameHistory> historyTable;
    @FXML private TableColumn<GameHistory, String> gameColumn;
    @FXML private TableColumn<GameHistory, String> resultColumn;
    @FXML private TableColumn<GameHistory, String> timeColumn;
    @FXML private TableColumn<GameHistory, String> colorColumn;
    @FXML private Label recordLabel;
    @FXML private Label winPercentageLabel;

    @FXML
    public void initialize() {
        gameColumn.setCellValueFactory(new PropertyValueFactory<>("gameName"));
        resultColumn.setCellValueFactory(new PropertyValueFactory<>("result"));
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("timePlayed"));
        colorColumn.setCellValueFactory(new PropertyValueFactory<>("color"));
        loadHistoryData();
    }

    private void loadHistoryData() {
        ObservableList<GameHistory> data = FXCollections.observableArrayList();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM history WHERE username = ?")) {
            ps.setString(1, Main.getSessionUser());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                data.add(new GameHistory(
                        rs.getString("game_name"),
                        rs.getString("result"),
                        rs.getString("time_played"),
                        rs.getString("color")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        historyTable.setItems(data);
        updateRecord(data);
    }

    private void updateRecord(ObservableList<GameHistory> data) {
        int wins = 0;
        int losses = 0;
        int draws = 0;
        for (GameHistory game : data) {
            String result = game.getResult() == null ? "" : game.getResult().toLowerCase();
            if (result.contains("draw") || result.contains("stalemate") || result.contains("fifty")) draws++;
            else if (result.contains("player win")) wins++;
            else losses++;
        }
        int total = wins + losses + draws;
        double percent = total == 0 ? 0.0 : (wins * 100.0 / total);
        if (recordLabel != null) recordLabel.setText("W/L/D: " + wins + "/" + losses + "/" + draws);
        if (winPercentageLabel != null) winPercentageLabel.setText(String.format("Win Percentage: %.1f%%", percent));
    }

    @FXML
    public void handleBack() throws Exception { SoundManager.button(); Main.showScene("pre_play.fxml"); }
}
