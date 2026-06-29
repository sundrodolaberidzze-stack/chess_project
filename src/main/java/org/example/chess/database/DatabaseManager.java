package org.example.chess.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class DatabaseManager {
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/";
    private static final String DB_NAME = "chess_project_db";

    public static Connection getConnection() throws SQLException {
        Properties props = DatabaseConfig.loadConfig();
        String user = props.getProperty("db.user", "");
        String password = props.getProperty("db.password", "");
        if (user.isBlank()) {
            throw new SQLException("Database setup is not configured.");
        }
        return DriverManager.getConnection(URL + DB_NAME, user, password);
    }

    public static boolean testAndSetupConnection(String user, String password) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            Connection conn = DriverManager.getConnection(
                    URL,
                    user,
                    password
            );

            Statement stmt = conn.createStatement();
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME);
            stmt.close();
            conn.close();

            Connection dbConn = DriverManager.getConnection(
                    URL + DB_NAME,
                    user,
                    password
            );

            Statement dbStmt = dbConn.createStatement();

            dbStmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS users (" +
                            "id INT AUTO_INCREMENT PRIMARY KEY, " +
                            "username VARCHAR(50) UNIQUE NOT NULL, " +
                            "password VARCHAR(50) NOT NULL)"
            );

            dbStmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS history (" +
                            "id INT AUTO_INCREMENT PRIMARY KEY, " +
                            "username VARCHAR(50), " +
                            "game_name VARCHAR(100), " +
                            "result VARCHAR(50), " +
                            "time_played VARCHAR(50), " +
                            "color VARCHAR(20))"
            );

            dbStmt.close();
            dbConn.close();

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}