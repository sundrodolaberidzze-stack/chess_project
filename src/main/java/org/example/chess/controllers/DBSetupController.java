package org.example.chess.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.example.chess.Main;
import org.example.chess.SoundManager;
import org.example.chess.database.DatabaseConfig;
import org.example.chess.database.DatabaseManager;

public class DBSetupController {
    @FXML private TextField userField;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;

    @FXML
    public void handleSave() {
        SoundManager.button();
        String user = userField.getText();
        String pass = passwordField.getText();
        if (DatabaseManager.testAndSetupConnection(user, pass)) {
            DatabaseConfig.saveConfig(user, pass);
            try {
                Main.showScene("login.fxml");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            SoundManager.error();
            statusLabel.setText("Connection configuration rejected by Host Database.");
        }
    }
}
