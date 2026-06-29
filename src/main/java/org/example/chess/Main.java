package org.example.chess;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;
import org.example.chess.database.DatabaseConfig;
import org.example.chess.database.DatabaseManager;

import java.util.Properties;

public class Main extends Application {
    private static Stage primaryStage;
    private static String sessionUser;
    private static Runnable closeHandler;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        primaryStage.setTitle("Chess");
        primaryStage.setFullScreenExitHint("");
        primaryStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        primaryStage.setOnCloseRequest(event -> {
            if (closeHandler != null) closeHandler.run();
        });

        showScene("login.fxml");

        primaryStage.show();
    }

    public static void showScene(String fxml) throws Exception {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource(fxml));
        Parent root = loader.load();
        primaryStage.setScene(new Scene(root));
        applyFullscreenSetting();
    }

    public static void setSessionUser(String user) {
        sessionUser = user;
    }

    public static String getSessionUser() {
        return sessionUser;
    }

    public static void setCloseHandler(Runnable handler) {
        closeHandler = handler;
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void toggleFullScreen() {
        if (primaryStage == null) return;
        boolean fullscreen = !primaryStage.isFullScreen();
        SettingsState.setFullscreenEverywhere(fullscreen);
        primaryStage.setFullScreenExitHint("");
        primaryStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        primaryStage.setFullScreen(fullscreen);
    }

    public static void applyFullscreenSetting() {
        if (primaryStage == null) return;
        primaryStage.setFullScreenExitHint("");
        primaryStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        primaryStage.setFullScreen(SettingsState.isFullscreenEverywhere());
    }

    public static boolean isFullScreen() {
        return primaryStage != null && primaryStage.isFullScreen();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
