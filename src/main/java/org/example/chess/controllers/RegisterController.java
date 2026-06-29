package org.example.chess.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.example.chess.Main;
import org.example.chess.SoundManager;
import org.example.chess.database.DatabaseManager;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class RegisterController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;

    @FXML
    public void handleRegister() {
        SoundManager.button();
        String user = usernameField.getText().trim();
        String pass = passwordField.getText();
        if (user.isEmpty() || pass.isEmpty()) {
            SoundManager.error();
            statusLabel.setText("Enter username and password.");
            return;
        }
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement("INSERT INTO users (username, password) VALUES (?, ?)")) {
            ps.setString(1, user);
            ps.setString(2, pass);
            ps.executeUpdate();
            Main.setSessionUser(user);
            Main.showScene("pre_play.fxml");
        } catch (Exception e) {
            Main.setSessionUser(user);
            try {
                Main.showScene("pre_play.fxml");
            } catch (Exception ignored) {
                statusLabel.setText("Saved for local play. Database is offline.");
            }
        }
    }

    @FXML public void handleBackToLogin() throws Exception { SoundManager.button(); Main.showScene("login.fxml"); }
}
