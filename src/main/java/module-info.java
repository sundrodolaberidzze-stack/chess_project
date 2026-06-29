module org.example.chess {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires java.sql;

    opens org.example.chess to javafx.fxml;
    opens org.example.chess.controllers to javafx.fxml;
    opens org.example.chess.models to javafx.base;

    exports org.example.chess;
    exports org.example.chess.controllers;
    exports org.example.chess.models;
}