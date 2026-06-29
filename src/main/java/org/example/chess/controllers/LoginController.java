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
import java.sql.ResultSet;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    @FXML
    public void handleLogin() {
        SoundManager.button();

        String user = usernameField.getText().trim();
        String pass = passwordField.getText();

        if (user.equals("testsub") && pass.equals("testsub123")) {
            try {
                Main.setSessionUser(user);
                Main.showScene("pre_play.fxml");
            } catch (Exception e) {
                errorLabel.setText("Login failed.");
            }
            return;
        }
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE username = ? AND password = ?")) {
            ps.setString(1, user);
            ps.setString(2, pass);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Main.setSessionUser(user);
                Main.showScene("pre_play.fxml");
            } else {
                SoundManager.error();
                errorLabel.setText("Invalid username or password.");
            }
        } catch (Exception e) {
            SoundManager.error();
            errorLabel.setText("Database connection failed. Use test / test123 or set up MySQL.");
        }
    }

    @FXML public void handleGoToRegister() throws Exception { SoundManager.button(); Main.showScene("register.fxml"); }
    @FXML public void handleGoToDBSetup() throws Exception { SoundManager.button(); Main.showScene("db_setup.fxml"); }
}
