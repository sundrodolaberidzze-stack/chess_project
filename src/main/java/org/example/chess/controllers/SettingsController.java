package org.example.chess.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import org.example.chess.Main;
import org.example.chess.SettingsState;
import org.example.chess.SoundManager;

public class SettingsController {
    @FXML private CheckBox premovesCheckBox;
    @FXML private CheckBox fullscreenCheckBox;
    @FXML private ComboBox<String> boardColorComboBox;

    @FXML
    public void initialize() {
        premovesCheckBox.setSelected(SettingsState.isPremovesEnabled());
        fullscreenCheckBox.setSelected(SettingsState.isFullscreenEverywhere());
        boardColorComboBox.getItems().setAll("Classic", "Blue", "Brown", "Gray", "Purple");
        boardColorComboBox.setValue(SettingsState.getBoardTheme());
    }

    @FXML
    public void handlePremovesToggle() {
        SoundManager.button();
        SettingsState.setPremovesEnabled(premovesCheckBox.isSelected());
    }

    @FXML
    public void handleFullscreenToggle() {
        SoundManager.button();
        SettingsState.setFullscreenEverywhere(fullscreenCheckBox.isSelected());
        Main.applyFullscreenSetting();
    }

    @FXML
    public void handleBoardColorChange() {
        SoundManager.button();
        SettingsState.setBoardTheme(boardColorComboBox.getValue());
    }

    @FXML
    public void handleBack() {
        SoundManager.button();
        try {
            Main.showScene("pre_play.fxml");
        } catch (Exception ignored) {
        }
    }
}
