package org.example.chess.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.ToggleButton;
import javafx.stage.Stage;
import org.example.chess.Main;
import org.example.chess.SoundManager;

public class PrePlayController {
    @FXML private Spinner<Integer> difficultySpinner;
    @FXML private ComboBox<String> colorComboBox;
    @FXML private ComboBox<Integer> timeComboBox;
    @FXML private ToggleButton soundToggleButton;

    @FXML
    public void initialize() {
        difficultySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 20, 5));
        colorComboBox.getItems().addAll("WHITE", "BLACK");
        colorComboBox.setValue("WHITE");
        timeComboBox.getItems().addAll(5, 10, 30, 60);
        timeComboBox.setValue(10);
        updateSoundToggleText();
    }

    @FXML
    public void handleStartMatch() {
        SoundManager.button();
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("chess_board.fxml"));
            Parent root = loader.load();
            ChessBoardController controller = loader.getController();
            controller.initGameData(difficultySpinner.getValue(), colorComboBox.getValue(), timeComboBox.getValue(), false);
            Stage stage = (Stage) colorComboBox.getScene().getWindow();
            stage.setScene(new Scene(root));
            Main.applyFullscreenSetting();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleStartLocalMatch() {
        SoundManager.button();
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("chess_board.fxml"));
            Parent root = loader.load();
            ChessBoardController controller = loader.getController();
            controller.initGameData(difficultySpinner.getValue(), colorComboBox.getValue(), timeComboBox.getValue(), true);
            Stage stage = (Stage) colorComboBox.getScene().getWindow();
            stage.setScene(new Scene(root));
            Main.applyFullscreenSetting();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleToggleFullScreen() {
        SoundManager.button();
        Main.toggleFullScreen();
    }

    @FXML
    public void handleToggleSound() {
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

    @FXML
    public void handleViewHistory() throws Exception { SoundManager.button(); Main.showScene("history.fxml"); }

    @FXML
    public void handleOpenSettings() throws Exception { SoundManager.button(); Main.showScene("settings.fxml"); }
}
